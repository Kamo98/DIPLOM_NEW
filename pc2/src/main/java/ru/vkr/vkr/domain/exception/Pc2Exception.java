package ru.vkr.vkr.domain.exception;

import java.io.IOException;

public class Pc2Exception extends IOException {
    public Pc2Exception() {
        super();
    }

    public Pc2Exception(String s) {
        super(s);
    }

    public Pc2Exception(String s, Throwable throwable) {
        super(s, throwable);
    }

    public Pc2Exception(Throwable throwable) {
        super(throwable);
    }
}
