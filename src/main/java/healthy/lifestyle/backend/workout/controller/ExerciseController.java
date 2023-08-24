package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.CreateExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.GetExercisesResponseDto;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final AuthService authService;

    public ExerciseController(ExerciseService exerciseService, AuthService authService) {
        this.exerciseService = exerciseService;
        this.authService = authService;
    }

    /**
     * Create custom exercise, ROLE_USER access is required
     * @param requestDto Http refs are optional
     * @return CreateExerciseResponseDto, 201 Created
     * @throws healthy.lifestyle.backend.exception.GlobalExceptionHandler If validation error occurs<br>
     * @throws healthy.lifestyle.backend.exception.ApiException
     * If the user already has the exercise with the same title, ErrorMessage.TITLE_DUPLICATE, 400 Bad Request;<br>
     * If there are invalid ids of body parts (or, if present, http refs), ErrorMessage.INVALID_NESTED_OBJECT, 400 Bad Request<br>
     * @see CreateExerciseRequestDto
     * @see healthy.lifestyle.backend.workout.dto.CreateExerciseResponseDto
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CreateExerciseResponseDto> createCustomExercise(
            @RequestBody CreateExerciseRequestDto requestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (nonNull(authentication) && authentication.isAuthenticated()) {
            String usernameOrEmail = authentication.getName();
            Optional<User> userOptional = authService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
            if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            CreateExerciseResponseDto responseDto = exerciseService.createExercise(
                    requestDto, userOptional.get().getId());
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        }
        throw new ApiException(ErrorMessage.AUTHENTICATION_ERROR, HttpStatus.UNAUTHORIZED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<GetExercisesResponseDto> getExercises(
            @RequestParam(name = "isCustomOnly", required = false) Boolean isCustomOnly) {
        if (isNull(isCustomOnly)) isCustomOnly = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (nonNull(authentication) && authentication.isAuthenticated()) {
            String usernameOrEmail = authentication.getName();
            Optional<User> userOptional = authService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
            if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);
            GetExercisesResponseDto responseDtoList =
                    exerciseService.getExercises(userOptional.get().getId(), isCustomOnly);
            return ResponseEntity.ok(responseDtoList);
        }
        throw new ApiException(ErrorMessage.AUTHENTICATION_ERROR, HttpStatus.UNAUTHORIZED);
    }
}
