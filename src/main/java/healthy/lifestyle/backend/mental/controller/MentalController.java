package healthy.lifestyle.backend.mental.controller;

import healthy.lifestyle.backend.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.mental.service.MentalService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<MentalResponseDto>> getMentalsWithFilter(
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) @TitleValidation String title,
            @RequestParam(required = false) @DescriptionValidation String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = null;
        if (isCustom == null || isCustom)
            userId = authUtil.getUserIdFromAuthentication(
                    SecurityContextHolder.getContext().getAuthentication());
        Page<MentalResponseDto> dtoPage = mentalService.getMentalWithFilter(
                isCustom, userId, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/default")
    public ResponseEntity<Page<MentalResponseDto>> getDefaultMentals(
            @RequestParam(required = false) @TitleValidation String title,
            @RequestParam(required = false) @DescriptionValidation String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Page<MentalResponseDto> dtoPage = mentalService.getMentalWithFilter(
                false, null, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/custom")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<MentalResponseDto>> getCustomMentals(
            @RequestParam(required = false) @TitleValidation String title,
            @RequestParam(required = false) @DescriptionValidation String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        Page<MentalResponseDto> responseDtoPage = mentalService.getMentalWithFilter(
                true, userId, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(responseDtoPage);
    }
}
