package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.common.AuthUtil;
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.UpdateHttpRefRequestDto;
import healthy.lifestyle.backend.workout.service.HttpRefService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts/httpRefs")
public class HttpRefController {
    private final HttpRefService httpRefService;
    private final AuthUtil authUtil;

    public HttpRefController(HttpRefService httpRefService, AuthUtil authUtil) {
        this.httpRefService = httpRefService;
        this.authUtil = authUtil;
    }

    @GetMapping("/default")
    public ResponseEntity<List<HttpRefResponseDto>> getDefaultHttpRefs() {
        return new ResponseEntity<>(
                httpRefService.getDefaultHttpRefs(Sort.by(Sort.Direction.ASC, "id")), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<HttpRefResponseDto>> getCustomHttpRefs(
            @RequestParam(name = "sortBy", required = false) String sortBy) {
        if (isNull(sortBy)) sortBy = "id";
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(httpRefService.getCustomHttpRefs(userId, "id"), HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<HttpRefResponseDto> createCustomHttpRef(@RequestBody @Valid CreateHttpRequestDto requestDto) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(httpRefService.createCustomHttpRef(userId, requestDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{httpRefId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<HttpRefResponseDto> updateCustomHttpRef(
            @PathVariable Long httpRefId, @RequestBody @Valid UpdateHttpRefRequestDto requestDto) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(httpRefService.updateCustomHttpRef(userId, httpRefId, requestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{httpRefId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Long> deleteCustomHttpRef(@PathVariable Long httpRefId) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(httpRefService.deleteCustomHttpRef(userId, httpRefId), HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{httpRefId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<HttpRefResponseDto> getCustomHttpRefById(@PathVariable Long httpRefId) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(httpRefService.getCustomHttpRefById(userId, httpRefId), HttpStatus.OK);
    }
}
