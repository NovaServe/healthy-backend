package healthy.lifestyle.backend.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    protected ErrorMessage message;
    protected HttpStatus httpStatus;

    public ApiException(ErrorMessage message, HttpStatus httpStatus) {
        super(message.getName());
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message.getName();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
