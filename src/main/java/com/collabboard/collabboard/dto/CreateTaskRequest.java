package com.collabboard.collabboard.dto;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private Long columnId;
}