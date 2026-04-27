package com.collabboard.collabboard.service;

import com.collabboard.collabboard.dto.*;
import com.collabboard.collabboard.entity.*;
import com.collabboard.collabboard.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepo;
    private final BoardMemberRepository memberRepo;
    private final BoardColumnRepository columnRepo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public BoardService(BoardRepository boardRepo,
                        BoardMemberRepository memberRepo,
                        BoardColumnRepository columnRepo,
                        TaskRepository taskRepo,
                        UserRepository userRepo) {
        this.boardRepo  = boardRepo;
        this.memberRepo = memberRepo;
        this.columnRepo = columnRepo;
        this.taskRepo   = taskRepo;
        this.userRepo   = userRepo;
    }

    // ── Create board ─────────────────────────────────────
    @Transactional
    public BoardDto createBoard(String name, Long ownerId) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = new Board();
        board.setName(name);
        board.setOwner(owner);
        boardRepo.save(board);

        // Create default columns
        String[] defaultColumns = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < defaultColumns.length; i++) {
            BoardColumn col = new BoardColumn();
            col.setName(defaultColumns[i]);
            col.setPosition(i);
            col.setBoard(board);
            columnRepo.save(col);
        }

        return toDto(board);
    }

    // ── Get all boards for user ───────────────────────────
    public List<BoardDto> getBoardsForUser(Long userId) {
        return boardRepo.findAllByUserId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ── Get single board with columns and tasks ───────────
    public BoardDto getBoard(Long boardId, Long userId) {
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!isMember(boardId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this board");
        }

        return toDtoFull(board);
    }

    // ── Join board via invite code ────────────────────────
    @Transactional
    public BoardDto joinBoard(String inviteCode, Long userId) {
        Board board = boardRepo.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite code"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!isMember(board.getId(), userId)) {
            BoardMember member = new BoardMember();
            member.setBoard(board);
            member.setUser(user);
            memberRepo.save(member);
        }

        return toDtoFull(board);
    }

    // ── Create task ───────────────────────────────────────
    @Transactional
    public TaskDto createTask(Long boardId, CreateTaskRequest req, Long userId) {
        if (!isMember(boardId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this board");
        }

        BoardColumn column = columnRepo.findById(req.getColumnId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found"));

        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        int position = taskRepo.findByColumnIdOrderByPosition(column.getId()).size();

        Task task = new Task();
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setColumn(column);
        task.setBoard(board);
        task.setPosition(position);
        taskRepo.save(task);

        return toTaskDto(task);
    }

    // ── Move task ─────────────────────────────────────────
    @Transactional
    public TaskDto moveTask(Long boardId, MoveTaskRequest req, Long userId) {
        if (!isMember(boardId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of this board");
        }

        Task task = taskRepo.findById(req.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        BoardColumn targetColumn = columnRepo.findById(req.getTargetColumnId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Column not found"));

        task.setColumn(targetColumn);
        task.setPosition(req.getNewPosition());
        taskRepo.save(task);

        return toTaskDto(task);
    }

    // ── Helpers ───────────────────────────────────────────
    public boolean isMember(Long boardId, Long userId) {
        Board board = boardRepo.findById(boardId).orElse(null);
        if (board == null) return false;
        if (board.getOwner().getId().equals(userId)) return true;
        return memberRepo.existsByBoardIdAndUserId(boardId, userId);
    }

    private BoardDto toDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setInviteCode(board.getInviteCode());
        dto.setOwnerName(board.getOwner().getDisplayName());
        return dto;
    }

    private BoardDto toDtoFull(Board board) {
        BoardDto dto = toDto(board);
        List<ColumnDto> columns = columnRepo
                .findByBoardIdOrderByPosition(board.getId())
                .stream()
                .map(col -> {
                    ColumnDto cdto = new ColumnDto();
                    cdto.setId(col.getId());
                    cdto.setName(col.getName());
                    cdto.setPosition(col.getPosition());
                    cdto.setTasks(
                        taskRepo.findByColumnIdOrderByPosition(col.getId())
                                .stream()
                                .map(this::toTaskDto)
                                .toList()
                    );
                    return cdto;
                })
                .toList();
        dto.setColumns(columns);
        return dto;
    }

    private TaskDto toTaskDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPosition(task.getPosition());
        dto.setColumnId(task.getColumn().getId());
        if (task.getAssignee() != null) {
            dto.setAssigneeName(task.getAssignee().getDisplayName());
            dto.setAssigneeId(task.getAssignee().getId());
        }
        return dto;
    }
}