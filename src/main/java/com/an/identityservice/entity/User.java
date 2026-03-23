package com.an.identityservice.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    // Đánh unique để giải quyết concurrent, không phân biệt hoa thường
    @Column(name = "username", unique = true, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    String password;
//    String firstName;  vì giờ đã có profile nên service này ko cần lưu thông tin này nữa
//    String lastName;   UserCreattionRequest thì giữ nguyên vì còn map qua profilerequest
//    LocalDate dob;

    @ManyToMany
    Set<Role> roles;
}
