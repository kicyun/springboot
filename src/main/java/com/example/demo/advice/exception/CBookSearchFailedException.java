package com.example.demo.advice.exception;

public class CBookSearchFailedException extends RuntimeException{
    public CBookSearchFailedException(String msg, Throwable t) {
        super(msg, t);
    }

    public CBookSearchFailedException(String msg) {
        super(msg);
    }

    public CBookSearchFailedException() {
        super();
    }
}
