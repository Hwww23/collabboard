package com.collabboard.collabboard.dto;

import lombok.Data;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Integer position;
    private Long columnId;
    private String assigneeName;
    private Long assigneeId;
}