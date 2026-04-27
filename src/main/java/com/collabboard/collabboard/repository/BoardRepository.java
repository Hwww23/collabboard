package com.collabboard.collabboard.repository;

import com.collabboard.collabboard.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    Optional<Board> findByInviteCode(String inviteCode);

    // Find all boards where the user is either owner or member
    @Query("""
        SELECT DISTINCT b FROM Board b
        LEFT JOIN b.members m
        WHERE b.owner.id = :userId OR m.user.id = :userId
    """)
    List<Board> findAllByUserId(@Param("userId") Long userId);
}