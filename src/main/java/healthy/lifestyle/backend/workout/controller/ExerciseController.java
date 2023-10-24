package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.common.AuthUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final AuthService authService;
    private final AuthUtil authUtil;

    public ExerciseController(ExerciseService exerciseService, AuthService authService, AuthUtil authUtil) {
        this.exerciseService = exerciseService;
        this.authService = authService;
        this.authUtil = authUtil;
    }

    /**
     * ErrorMessage.TITLE_DUPLICATE (400 Bad Request). If a user already has an exercise with the same title<br>
     * ErrorMessage.INVALID_NESTED_OBJECT (400 Bad Request). If there are invalid ids of bodyParts or httpRefs
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> createExercise(@RequestBody CreateExerciseRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (isNull(userId)) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

        ExerciseResponseDto exerciseResponseDto = exerciseService.createExercise(requestDto, userId);

        return new ResponseEntity<>(exerciseResponseDto, HttpStatus.CREATED);
    }

    @GetMapping("/default")
    public ResponseEntity<List<ExerciseResponseDto>> getDefaultExercises() {
        return ResponseEntity.ok(exerciseService.getDefaultExercises());
    }

    @GetMapping("/default/{exercise_id}")
    public ResponseEntity<ExerciseResponseDto> getExerciseById(@PathVariable("exercise_id") long exercise_id) {
        return ResponseEntity.ok(exerciseService.getDefaultExerciseById(exercise_id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<ExerciseResponseDto>> getCustomExercises() {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (isNull(userId)) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(exerciseService.getCustomExercises(userId));
    }
}
