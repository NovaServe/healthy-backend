package healthy.lifestyle.backend.activity.mental.controller;

import healthy.lifestyle.backend.activity.mental.dto.MentalCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.service.MentalService;
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
@RequestMapping("${api.basePath}/${api.version}/mentals")
public class MentalController {
    @Autowired
    MentalService mentalService;

    @Autowired
    AuthUtil authUtil;

    @Operation(summary = "Get default mental activity by id")
    @GetMapping("/default/{mental_id}")
    public ResponseEntity<MentalResponseDto> getDefaultMentalById(
            @PathVariable("mental_id") @IdValidation long mental_id) {
        MentalResponseDto responseDto = mentalService.getMentalById(mental_id, true, null);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get custom mental activity by id")
    @GetMapping("/{mental_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalResponseDto> getCustomMentalById(
            @PathVariable("mental_id") @IdValidation long mental_id) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalResponseDto responseDto = mentalService.getMentalById(mental_id, false, userId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get default mental activities")
    @GetMapping("/all_mentals")
    public ResponseEntity<Page<MentalResponseDto>> getAllMentals(
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        Page<MentalResponseDto> responseDtoPage =
                mentalService.getMentals(userId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(responseDtoPage);
    }

    @Operation(summary = "Get custom mental activity by id")
    @PatchMapping("/{mentalId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalResponseDto> updateCustomMental(
            @PathVariable("mentalId") long mentalId, @Valid @RequestBody MentalUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalResponseDto responseDto = mentalService.updateCustomMental(userId, mentalId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Delete custom mental activity by id")
    @DeleteMapping("/{mentalId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomMental(@PathVariable("mentalId") @IdValidation long mentalId) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        mentalService.deleteCustomMental(mentalId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Create custom mental activity")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MentalResponseDto> createCustomMental(@Valid @RequestBody MentalCreateRequestDto requestDto) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        MentalResponseDto responseDto = mentalService.createCustomMental(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get default and custom mentals")
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<MentalResponseDto>> getMentals(
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
        Page<MentalResponseDto> dtoPage = mentalService.getMentalsWithFilter(
                isCustom, userId, title, description, mentalTypeId, sortField, sortDirection, pageNumber, pageSize);
        return ResponseEntity.ok(dtoPage);
    }
}