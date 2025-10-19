package com.sha.client_service.util.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ClientEventType {

    CREATED("CREATED"),
    UPDATED("UPDATED"),
    DELETED("DELETED");

    @JsonValue
    private final String type;

    ClientEventType(String type) {
        this.type = type;
    }
}
