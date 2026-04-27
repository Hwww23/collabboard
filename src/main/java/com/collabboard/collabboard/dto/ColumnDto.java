package com.collabboard.collabboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class ColumnDto {
    private Long id;
    private String name;
    private Integer position;
    private List<TaskDto> tasks;
}