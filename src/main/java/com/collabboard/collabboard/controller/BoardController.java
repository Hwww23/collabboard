package com.collabboard.collabboard.controller;

import com.collabboard.collabboard.dto.*;
import com.collabboard.collabboard.security.CurrentUser;
import com.collabboard.collabboard.service.BoardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardDto createBoard(@RequestBody CreateBoardRequest req) {
        return boardService.createBoard(req.getName(), CurrentUser.getId());
    }

    @GetMapping
    public List<BoardDto> getMyBoards() {
        return boardService.getBoardsForUser(CurrentUser.getId());
    }

    @GetMapping("/{boardId}")
    public BoardDto getBoard(@PathVariable Long boardId) {
        return boardService.getBoard(boardId, CurrentUser.getId());
    }

    @PostMapping("/join/{inviteCode}")
    public BoardDto joinBoard(@PathVariable String inviteCode) {
        return boardService.joinBoard(inviteCode, CurrentUser.getId());
    }

    @PostMapping("/{boardId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto createTask(@PathVariable Long boardId,
                              @RequestBody CreateTaskRequest req) {
        return boardService.createTask(boardId, req, CurrentUser.getId());
    }

    @PatchMapping("/{boardId}/tasks/move")
    public TaskDto moveTask(@PathVariable Long boardId,
                            @RequestBody MoveTaskRequest req) {
        return boardService.moveTask(boardId, req, CurrentUser.getId());
    }
}