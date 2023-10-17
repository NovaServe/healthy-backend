package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.service.WorkoutService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping("/default")
    public ResponseEntity<List<WorkoutResponseDto>> getDefaultWorkouts(
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName) {
        if (isNull(sortFieldName)) sortFieldName = "id";
        return ResponseEntity.ok(workoutService.getDefaultWorkouts(sortFieldName));
    }
}
