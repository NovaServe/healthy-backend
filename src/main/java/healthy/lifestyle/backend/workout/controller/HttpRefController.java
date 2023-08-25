package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.service.HttpRefService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.basePath}/${api.version}/exercises/httpRefs")
public class HttpRefController {
    private final HttpRefService httpRefService;
    private final AuthService authService;

    public HttpRefController(HttpRefService httpRefService, AuthService authService) {
        this.httpRefService = httpRefService;
        this.authService = authService;
    }

    /**
     * Retrieve all http refs, ROLE_USER access is required
     * @return List<HttpRefResponseDto>, 200 OK List of default and user's custom exercises
     * @throws ApiException If default http refs not found, ErrorMessage.SERVER_ERROR 500 Internal server error
     * @see HttpRefResponseDto
     * @see healthy.lifestyle.backend.workout.service.HttpRefServiceImpl
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<HttpRefResponseDto>> getHttpRefs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (nonNull(authentication) && authentication.isAuthenticated()) {
            String usernameOrEmail = authentication.getName();
            Optional<User> userOptional = authService.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
            if (userOptional.isEmpty()) throw new ApiException(ErrorMessage.USER_NOT_FOUND, HttpStatus.BAD_REQUEST);

            List<HttpRefResponseDto> responseDtoList =
                    httpRefService.getHttpRefs(userOptional.get().getId(), Sort.by(Sort.Direction.ASC, "id"));
            return new ResponseEntity<>(responseDtoList, HttpStatus.OK);
        }

        throw new ApiException(ErrorMessage.AUTHENTICATION_ERROR, HttpStatus.UNAUTHORIZED);
    }
}
