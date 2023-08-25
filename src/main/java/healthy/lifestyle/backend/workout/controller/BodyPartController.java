package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.service.BodyPartService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${api.basePath}/${api.version}/exercises/bodyParts")
public class BodyPartController {
    private final BodyPartService bodyPartService;

    public BodyPartController(BodyPartService bodyPartService) {
        this.bodyPartService = bodyPartService;
    }

    /**
     * Retrieve all body parts, ROLE_USER access is required
     * @return List<BodyPartResponseDto>, 200 Ok
     * @throws healthy.lifestyle.backend.exception.ApiException
     * If body parts not found, ErrorMessage.SERVER_ERROR, 500 Internal server error
     * @see BodyPartResponseDto
     * @see healthy.lifestyle.backend.workout.service.BodyPartServiceImpl
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<BodyPartResponseDto>> getBodyParts() {
        List<BodyPartResponseDto> responseDto = bodyPartService.getBodyParts();
        return ResponseEntity.ok(responseDto);
    }
}
