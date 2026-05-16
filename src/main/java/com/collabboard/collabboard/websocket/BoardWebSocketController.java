package com.collabboard.collabboard.websocket;

import com.collabboard.collabboard.dto.MoveTaskRequest;
import com.collabboard.collabboard.dto.CreateTaskRequest;
import com.collabboard.collabboard.dto.TaskDto;
import com.collabboard.collabboard.security.JwtUtil;
import com.collabboard.collabboard.service.BoardService;
import com.collabboard.collabboard.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BoardWebSocketController {

    private final BoardService boardService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public BoardWebSocketController(BoardService boardService,
                                    SimpMessagingTemplate messagingTemplate,
                                    JwtUtil jwtUtil,
                                    UserRepository userRepo) {
        this.boardService      = boardService;
        this.messagingTemplate = messagingTemplate;
        this.jwtUtil           = jwtUtil;
        this.userRepo          = userRepo;
    }

    /**
     * Handles task move events from any connected client.
     *
     * Client sends to:   /app/board/{boardId}/move
     * Server broadcasts: /topic/board/{boardId}
     *
     * The token header is how we authenticate WebSocket messages —
     * WebSockets don't have HTTP headers on every message so we
     * pass the JWT manually.
     */
    @MessageMapping("/board/{boardId}/move")
    public void handleTaskMove(
            @DestinationVariable Long boardId,
            @Header("token") String token,
            MoveTaskRequest req) {

        // Validate token
        if (!jwtUtil.isTokenValid(token)) {
            return;  // silently reject invalid tokens
        }

        Long userId = jwtUtil.extractUserId(token);
        String userEmail = jwtUtil.extractEmail(token);

        // Get display name
        String displayName = userRepo.findById(userId)
                .map(u -> u.getDisplayName())
                .orElse(userEmail);

        // Process the move
        TaskDto updatedTask = boardService.moveTask(boardId, req, userId);

        // Broadcast to all subscribers on this board's topic
        BoardEvent event = new BoardEvent(
                BoardEvent.Type.TASK_MOVED,
                boardId,
                updatedTask,
                displayName
        );

        messagingTemplate.convertAndSend("/topic/board/" + boardId, event);
    }

    @MessageMapping("/board/{boardId}/task/create")
    public void handleTaskCreate(
            @DestinationVariable Long boardId,
            @Header("token") String token,
            CreateTaskRequest req) {

        if (!jwtUtil.isTokenValid(token)) return;

        Long userId = jwtUtil.extractUserId(token);
        String displayName = userRepo.findById(userId)
                .map(u -> u.getDisplayName())
                .orElse("Unknown");

        TaskDto task = boardService.createTask(boardId, req, userId);

        BoardEvent event = new BoardEvent(
                BoardEvent.Type.TASK_CREATED,
                boardId,
                task,
                displayName
        );

        messagingTemplate.convertAndSend("/topic/board/" + boardId, event);
    }
}