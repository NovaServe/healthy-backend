package healthy.lifestyle.backend.exception;

import static java.util.Objects.nonNull;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionDto> handleApiException(ApiException exception, WebRequest webRequest) {
        logger.error(
                "{}, {}, {}", webRequest.getDescription(false), exception.getHttpStatus(), exception.getStackTrace());

        String message;
        if (exception.getResourceId() != null) {
            message = exception.getMessageWithResourceId();
        } else {
            message = exception.getMessage();
        }
        ExceptionDto exceptionDto = new ExceptionDto(message);
        return new ResponseEntity<>(exceptionDto, exception.getHttpStatus());
    }

    @ExceptionHandler(ApiExceptionCustomMessage.class)
    public ResponseEntity<ExceptionDto> handleApiExceptionCustomMessage(
            ApiExceptionCustomMessage exception, WebRequest webRequest) {
        logger.error(
                "{}, {}, {}", webRequest.getDescription(false), exception.getHttpStatus(), exception.getStackTrace());

        ExceptionDto exceptionDto = new ExceptionDto(exception.getMessage());
        return new ResponseEntity<>(exceptionDto, exception.getHttpStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        Map<String, String> validationResult = new HashMap<>();
        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            String constraintMessage = constraintViolation.getMessage();
            String paramName = constraintViolation.getPropertyPath().toString().split("\\.")[1];
            validationResult.put(paramName, constraintMessage);
        }
        return new ResponseEntity<>(validationResult, HttpStatus.BAD_REQUEST);
    }

    /**
     * @see FieldError
     * @see org.springframework.validation.ObjectError
     * @see SpringValidatorAdapter
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = "Method argument not valid";
            if (error instanceof FieldError) {
                fieldName = ((FieldError) error).getField();
            } else {
                if (nonNull(error.getArguments()) && error.getArguments().length > 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 1; i < error.getArguments().length; i++) {
                        stringBuilder.append(error.getArguments()[i].toString());
                        stringBuilder.append(",");
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    fieldName = stringBuilder.toString();
                }
            }
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
            logger.error("{}: {} - {}", fieldName, message, HttpStatus.BAD_REQUEST.value());
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
