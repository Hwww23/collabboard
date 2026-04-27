package com.collabboard.collabboard.repository;

import com.collabboard.collabboard.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);
}