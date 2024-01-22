package healthy.lifestyle.backend.mentals.controller;

import healthy.lifestyle.backend.mentals.dto.MentalResponseDto;
import healthy.lifestyle.backend.mentals.service.MentalService;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Validated
@RequestMapping("${api.basePath}/${api.version}/mentals")
public class MentalController {
    private final MentalService mentalService;

    public MentalController(MentalService mentalService) {
        this.mentalService = mentalService;
    }

    @GetMapping("/default/{mental_id}")
    public ResponseEntity<MentalResponseDto> getDefaultExerciseById(
            @PathVariable("mental_id") @PositiveOrZero long mental_id) {
        MentalResponseDto responseDto = mentalService.getMentalById(mental_id, true, null);
        return ResponseEntity.ok(responseDto);
    }
}
