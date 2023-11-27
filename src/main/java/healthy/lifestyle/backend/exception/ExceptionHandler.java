package healthy.lifestyle.backend.exception;

import java.util.Optional;
import org.springframework.http.HttpStatus;

public class ExceptionHandler<T> {
    public static <T extends ExceptionGeneric> ExceptionNested<T> of(Class<T> type) {
        return ExceptionNested.ofType(type);
    }

    public static class ExceptionNested<T extends ExceptionGeneric> {
        private boolean shouldThrow;

        private T value;

        private Class<T> type;

        private ErrorMessage message;

        private HttpStatus httpStatus;

        public ExceptionNested(Class<T> type) {
            this.type = type;
        }

        public ExceptionNested<T> message(ErrorMessage message) {
            this.message = message;
            return this;
        }

        public ExceptionNested<T> status(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public ExceptionNested<T> condition(boolean conditionExpression) {
            if (conditionExpression) shouldThrow = true;
            return this;
        }

        public void throwException() {
            if (this.shouldThrow) throw new ApiException(this.message, this.httpStatus);
        }

        public ExceptionNested<T> conditionOptional(Optional<T> optional) {
            this.value = (T) optional.orElse(null);
            return this;
        }

        public T getOrThrow() {
            if (this.value == null) {
                throw new ApiException(this.message, this.httpStatus);
            }
            return this.value;
        }

        public void throwIfPresent() {
            if (this.value != null) {
                throw new ApiException(this.message, this.httpStatus);
            }
        }

        public static <T extends ExceptionGeneric> ExceptionNested<T> ofType(Class<T> type) {
            return new ExceptionNested<>(type);
        }

        /**
         * Messages
         */
        public ExceptionNested<T> messageNotFound() {
            this.message = ErrorMessage.NOT_FOUND;
            return this;
        }

        public ExceptionNested<T> messageEmptyRequest() {
            this.message = ErrorMessage.EMPTY_REQUEST;
            return this;
        }

        public ExceptionNested<T> messageUserResourceMismatch() {
            this.message = ErrorMessage.USER_RESOURCE_MISMATCH;
            return this;
        }

        public ExceptionNested<T> messageTitleDuplicate() {
            this.message = ErrorMessage.TITLE_DUPLICATE;
            return this;
        }

        public ExceptionNested<T> messageInvalidNestedObject() {
            this.message = ErrorMessage.INVALID_NESTED_OBJECT;
            return this;
        }

        /**
         * Status codes
         */
        public ExceptionNested<T> statusNotFound() {
            this.httpStatus = HttpStatus.NOT_FOUND;
            return this;
        }

        public ExceptionNested<T> statusBadRequest() {
            this.httpStatus = HttpStatus.BAD_REQUEST;
            return this;
        }
    }
}
