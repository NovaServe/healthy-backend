package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * Creates custom exercise. Http refs are optional.
     *
     * @throws healthy.lifestyle.backend.exception.ApiException ErrorMessage.INVALID_SYMBOLS, HttpStatus.BAD_REQUEST.
     * If there are invalid input symbols.
     *
     * @throws healthy.lifestyle.backend.exception.ApiException ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST.
     * If the user already has an exercise with the same title.
     *
     * @throws healthy.lifestyle.backend.exception.ApiException ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST.
     * If there are invalid ids of body parts or (if present) http refs.
     *
     * @see CreateExerciseRequestDto
     * @see healthy.lifestyle.backend.workout.dto.BodyPartRequestDto
     * @see healthy.lifestyle.backend.workout.dto.HttpRefRequestDto
     */
    @PostMapping("/")
    private ResponseEntity<?> createCustomExercise(@RequestBody CreateExerciseRequestDto requestDto) {
        return null;
    }
}
