package me.comu.exeter.pagination.exception;

public class InvalidStateException extends RuntimeException {
    public InvalidStateException() {
        super("No active JDA client has been set");
    }
}