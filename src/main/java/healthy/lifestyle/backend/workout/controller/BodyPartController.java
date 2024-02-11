package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.service.BodyPartService;
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

    @GetMapping
    public ResponseEntity<List<BodyPartResponseDto>> getBodyParts() {
        List<BodyPartResponseDto> responseDto = bodyPartService.getBodyParts();
        return ResponseEntity.ok(responseDto);
    }
}
