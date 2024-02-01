package healthy.lifestyle.backend.mental.controller;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.mental.service.MentalService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/mentals")
public class MentalController {
    private final MentalService mentalService;

    private final AuthUtil authUtil;

    public MentalController(MentalService mentalService, AuthUtil authUtil) {
        this.mentalService = mentalService;
        this.authUtil = authUtil;
    }

    @GetMapping("/default/{mental_id}")
    public ResponseEntity<MentalResponseDto> getDefaultMentalById(
            @PathVariable("mental_id") @PositiveOrZero long mental_id) {
        MentalResponseDto responseDto = mentalService.getMentalById(mental_id, true, null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{mental_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalResponseDto> getCustomMentalById(
            @PathVariable("mental_id") @PositiveOrZero long mental_id) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalResponseDto responseDto = mentalService.getMentalById(mental_id, false, userId);
        return ResponseEntity.ok(responseDto);
    }
}
