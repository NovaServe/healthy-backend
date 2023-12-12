package healthy.lifestyle.backend.exception;

import org.springframework.http.HttpStatus;

public class ApiExceptionCustomMessage extends RuntimeException {
    protected String message;
    protected HttpStatus httpStatus;

    public ApiExceptionCustomMessage(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
