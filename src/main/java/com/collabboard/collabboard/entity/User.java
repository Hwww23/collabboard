package com.collabboard.collabboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private String displayName;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // A user can be a member of many boards
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BoardMember> boardMemberships;
}