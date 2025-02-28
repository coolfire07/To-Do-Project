package org.example.todobek.entities;

import lombok.Getter;

@Getter

public enum TaskStatus {
    TO_DO("To do"),
    IN_PROCESS("In process"),
    COMPLETED("Completed");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}