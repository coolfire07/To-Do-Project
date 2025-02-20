package org.example.todobek.entities;

public enum TaskStatus {
    TO_DO, IN_PROCESS, COMPLETED;

    @Override
    public String toString() {
        switch(this) {
            case IN_PROCESS: return "In process";
            case TO_DO: return "To do";
            case COMPLETED: return "Completed";
            default: return super.toString();
        }
    }
}
