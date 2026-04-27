package com.collabboard.collabboard.dto;

import lombok.Data;

@Data
public class MoveTaskRequest {
    private Long taskId;
    private Long targetColumnId;
    private Integer newPosition;
}