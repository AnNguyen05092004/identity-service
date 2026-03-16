package com.an.identityservice.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.an.identityservice.dto.request.AuthenticationRequest;
import com.an.identityservice.dto.request.IntrospectRequest;
import com.an.identityservice.dto.request.LogoutRequest;
import com.an.identityservice.dto.request.RefreshRequest;
import com.an.identityservice.dto.response.AuthenticationResponse;
import com.an.identityservice.dto.response.IntrospectResponse;
import com.an.identityservice.entity.InvalidatedToken;
import com.an.identityservice.entity.User;
import com.an.identityservice.exception.AppException;
import com.an.identityservice.exception.ErrorCode;
import com.an.identityservice.repository.InvalidatedTokenRepository;
import com.an.identityservice.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected String VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected String REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("SignKey: {}", SIGNER_KEY);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String token = generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (Exception e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        try {
            var signToken = verifyToken(
                    request.getToken(),
                    true); // Kiểm tra theo thời gian refreshable_duration để cho phép logout ngay cả khi token đã hết
            // hạn nhưng vẫn còn trong khoảng thời gian cho phép refresh
            String jit = signToken.getJWTClaimsSet().getJWTID();
            // Lưu jit vào blacklist để từ chối các token có jit này trong tương lai
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            // Nếu token không hợp lệ, chúng ta vẫn trả về thành công để tránh rò rỉ thông tin về token
            log.warn("Invalid token during logout: {}", e.getMessage());
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        // Kiểm tra hiệu lực của token
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // logout
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        // Tạo token mới
        String username = signedJWT.getJWTClaimsSet().getSubject();
        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        String token = generateToken(user);
        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        // isRefresh = true thì sẽ cộng thêm thời gian refreshable_duration vào thời gian hết hạn của token để cho phép
        // refresh token ngay cả khi token đã hết hạn nhưng vẫn còn trong khoảng thời gian cho phép refresh
        Date expirationTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getExpirationTime().getTime()
                        + Long.parseLong(REFRESHABLE_DURATION) * 1000)
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        if (!(verified && expirationTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra xem token đã bị thu hồi chưa
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            ;
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    // Tạo token JWT sau khi xác thực thành công
    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("an-identity-service")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(Long.parseLong(VALID_DURATION), ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString()) // Tạo id cho token để quản lý token khi đăng xuất
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            // log.error("Error signing JWT: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }
}
