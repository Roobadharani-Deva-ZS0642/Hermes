package org.intics.hermes.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HermesException extends RuntimeException {

    protected Integer errorCode;
    protected String errorMsg;

    public HermesException() {
        super();
    }

    public HermesException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public HermesException(final String msg) {
        super(msg);
        this.errorCode = HttpStatus.BAD_REQUEST.value();
        this.errorMsg = msg;
    }

    public HermesException(final String msg, final Integer errorCode) {
        super(msg);
        this.errorCode = errorCode;
        this.errorMsg = msg;
    }
}

