package com.collabboard.collabboard.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardEvent {

    public enum Type {
        TASK_MOVED,
        TASK_CREATED,
        TASK_DELETED
    }

    private Type type;
    private Long boardId;
    private Object payload;  // TaskDto or whatever is relevant
    private String triggeredBy;  // display name of who caused the event
}