package me.comu.exeter.pagination.exception;

public class EmptyPageCollectionException extends RuntimeException {
    public EmptyPageCollectionException() {
        super("The informed collection does not contain any Page");
    }
}