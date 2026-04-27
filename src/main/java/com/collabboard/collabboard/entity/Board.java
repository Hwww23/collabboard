package com.collabboard.collabboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "boards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // The user who created this board
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Unique code for inviting others — generated automatically
    @Column(unique = true, nullable = false)
    private String inviteCode = UUID.randomUUID().toString().substring(0, 8);

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<BoardMember> members;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    @OrderBy("position")
    private List<BoardColumn> columns;
}