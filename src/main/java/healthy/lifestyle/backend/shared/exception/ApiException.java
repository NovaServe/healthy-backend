package healthy.lifestyle.backend.shared.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    protected ErrorMessage message;
    protected Long resourceId;
    protected HttpStatus httpStatus;

    public ApiException(ErrorMessage message, Long resourceId, HttpStatus httpStatus) {
        super(message.getName());
        this.message = message;
        this.resourceId = resourceId;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getMessage() {
        return message.getName();
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getMessageWithResourceId() {
        return String.format(this.message.getName(), this.resourceId);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getHttpStatusValue() {
        return httpStatus.value();
    }
}
