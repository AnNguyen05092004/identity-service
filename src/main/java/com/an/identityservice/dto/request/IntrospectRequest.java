package com.an.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// class này được dùng để nhận request từ client khi client muốn kiểm tra token còn hợp lệ hay không, nó chỉ chứa một
// trường token là chuỗi. Khi client gửi request đến endpoint introspect, server sẽ sử dụng trường token này để kiểm tra
// xem token đó có hợp lệ hay không và trả về kết quả cho client.
public class IntrospectRequest {
    String token;
}
