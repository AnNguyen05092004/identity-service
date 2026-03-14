package com.an.identityservice.exception;

import java.util.Map;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.an.identityservice.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
// Khi có lỗi xảy ra trong controller, nó sẽ được chuyển đến các phương thức trong class này để xử lý
public class GlobalExceptionHandler {

    //    @ExceptionHandler(value = RuntimeException.class)
    //    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException ex) {
    //        ApiResponse apiResponse = new ApiResponse();
    //        apiResponse.setCode(1001);
    //        apiResponse.setMessage(ex.getMessage());
    //        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    //
    //    }
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String enumkey = ex.getFieldError().getDefaultMessage();

        ErrorCode errorCode;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumkey);

            // Mục tiêu:lấy các tham số của annotation validation.
            // Lấy thông tin chi tiết về lỗi ràng buộc từ ngoại lệ và trích xuất các thuộc tính của ràng buộc (metadata)
            var constraintViolation =
                    ex.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            // Lấy các thuộc tính của ràng buộc, ví dụ như giá trị tối thiểu (min), trả về một Map<String, Object> chứa
            // các thuộc tính và giá trị tương ứng của chúng. vd: {min=5}
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString());
        } catch (IllegalArgumentException e) {
            // Nếu enumkey không hợp lệ, sử dụng mã lỗi mặc định
            errorCode = ErrorCode.INVALID_KEY;
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                attributes != null ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = attributes.get(MIN_ATTRIBUTE).toString();
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

    // Exception tự định nghĩa
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // Đây là một phương thức xử lý ngoại lệ chung cho tất cả các loại ngoại lệ không được xử lý cụ thể khác. Khi một
    // ngoại lệ không được xử lý bởi các phương thức trước đó, nó sẽ được chuyển đến phương thức này để xử lý.
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingException(Exception ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UN_CATEGORIZED_ERROR.getCode());
        apiResponse.setMessage(ErrorCode.UN_CATEGORIZED_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
}
