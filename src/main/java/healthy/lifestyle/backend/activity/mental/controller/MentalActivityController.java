package healthy.lifestyle.backend.activity.mental.controller;

import healthy.lifestyle.backend.activity.mental.dto.MentalActivityCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.service.MentalActivityService;
import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import healthy.lifestyle.backend.user.service.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/mental_activities")
public class MentalActivityController {
    @Autowired
    MentalActivityService mentalService;

    @Autowired
    AuthUtil authUtil;

    @Operation(summary = "Get default mental activity by id")
    @GetMapping("/default/{mental_activity_id}")
    public ResponseEntity<MentalActivityResponseDto> getDefaultMentalActivityById(
            @PathVariable("mental_activity_id") @IdValidation long mental_id) {
        MentalActivityResponseDto responseDto = mentalService.getMentalActivityById(mental_id, true, null);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get custom mental activity by id")
    @GetMapping("/{mental_activity_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalActivityResponseDto> getCustomMentalActivityById(
            @PathVariable("mental_activity_id") @IdValidation long mental_id) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalActivityResponseDto responseDto = mentalService.getMentalActivityById(mental_id, false, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get default mental activities")
    @GetMapping("/all_mental_activities")
    public ResponseEntity<Page<MentalActivityResponseDto>> getAllMentalActivities(
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        Page<MentalActivityResponseDto> responseDtoPage =
                mentalService.getMentalActivities(userId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(responseDtoPage);
    }

    @Operation(summary = "Get custom mental activity by id")
    @PatchMapping("/{mental_activity_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalActivityResponseDto> updateCustomMentalActivity(
            @PathVariable("mental_activity_id") long mentalId,
            @Valid @RequestBody MentalActivityUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalActivityResponseDto responseDto = mentalService.updateCustomMentalActivity(userId, mentalId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Delete custom mental activity by id")
    @DeleteMapping("/{mental_activity_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomMentalActivity(
            @PathVariable("mental_activity_id") @IdValidation long mentalId) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        mentalService.deleteCustomMentalActivity(mentalId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Create custom mental activity")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalActivityResponseDto> createCustomMental(
            @Valid @RequestBody MentalActivityCreateRequestDto requestDto) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalActivityResponseDto responseDto = mentalService.createCustomMental(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get default and custom mental activities")
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<MentalActivityResponseDto>> getMentalActivities(
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) @TitleOptionalValidation(min = 2) String title,
            @RequestParam(required = false) @DescriptionOptionalValidation(min = 2) String description,
            @RequestParam(required = false) Long mentalTypeId,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = null;
        if (isCustom == null || isCustom)
            userId = authUtil.getUserIdFromAuthentication(
                    SecurityContextHolder.getContext().getAuthentication());
        Page<MentalActivityResponseDto> dtoPage = mentalService.getMentalActivitiesWithFilter(
                isCustom, userId, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(dtoPage);
    }
}
