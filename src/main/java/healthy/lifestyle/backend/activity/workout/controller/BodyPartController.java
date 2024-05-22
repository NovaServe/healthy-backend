package healthy.lifestyle.backend.activity.workout.controller;

import healthy.lifestyle.backend.activity.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.activity.workout.service.BodyPartService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts/bodyParts")
public class BodyPartController {
    @Autowired
    BodyPartService bodyPartService;

    @Operation(summary = "Get a list of body parts")
    @GetMapping
    public ResponseEntity<List<BodyPartResponseDto>> getBodyParts() {
        List<BodyPartResponseDto> responseDto = bodyPartService.getBodyParts();
        return ResponseEntity.ok(responseDto);
    }
}
