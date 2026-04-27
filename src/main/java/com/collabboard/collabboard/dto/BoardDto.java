package com.collabboard.collabboard.dto;

import lombok.Data;
import java.util.List;

@Data
public class BoardDto {
    private Long id;
    private String name;
    private String inviteCode;
    private String ownerName;
    private List<ColumnDto> columns;
}