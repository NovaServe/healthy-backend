package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.common.AuthUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.service.HttpRefService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts/httpRefs")
public class HttpRefController {
    private final HttpRefService httpRefService;
    private final AuthService authService;

    private final AuthUtil authUtil;

    public HttpRefController(HttpRefService httpRefService, AuthService authService, AuthUtil authUtil) {
        this.httpRefService = httpRefService;
        this.authService = authService;
        this.authUtil = authUtil;
    }

    @GetMapping("/default")
    public ResponseEntity<List<HttpRefResponseDto>> getDefaultHttpRefs() {
        return new ResponseEntity<>(
                httpRefService.getDefaultHttpRefs(Sort.by(Sort.Direction.ASC, "id")), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<HttpRefResponseDto>> getHttpRefs(
            @RequestParam(name = "isDefaultOnly", required = false) Boolean isDefaultOnly) {

        if (isNull(isDefaultOnly)) isDefaultOnly = false;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (nonNull(authentication) && authentication.isAuthenticated()) {
            String usernameOrEmail = authentication.getName();
            Optional<User> userOptional = authService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
            if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

            List<HttpRefResponseDto> responseDtoList = httpRefService.getHttpRefs(
                    userOptional.get().getId(), Sort.by(Sort.Direction.ASC, "id"), isDefaultOnly);

            return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
        }

        throw new ApiException(ErrorMessage.AUTHENTICATION_ERROR, HttpStatus.UNAUTHORIZED);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<HttpRefResponseDto> createCustomHttpRef(@RequestBody @Valid CreateHttpRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (isNull(userId)) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(httpRefService.createCustomHttpRef(userId, requestDto), HttpStatus.CREATED);
    }
}
