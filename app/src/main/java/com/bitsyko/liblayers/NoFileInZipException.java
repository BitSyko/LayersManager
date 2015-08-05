package com.bitsyko.liblayers;

public class NoFileInZipException extends RuntimeException {
    public NoFileInZipException(String message) {
        super(message);
    }
}
