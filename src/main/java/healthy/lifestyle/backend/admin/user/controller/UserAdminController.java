package healthy.lifestyle.backend.admin.user.controller;

import healthy.lifestyle.backend.admin.user.service.UserAdminService;
import healthy.lifestyle.backend.user.dto.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/admin")
public class UserAdminController {
    private final UserAdminService adminService;

    public UserAdminController(UserAdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Get users (admin)")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getUsersWithFilter(
            @RequestParam(name = "role", required = false) Long roleId,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "country", required = false) Long countryId,
            @RequestParam(name = "age", required = false) Integer age) {
        return ResponseEntity.ok(adminService.getUsersWithFilter(roleId, username, email, fullName, countryId, age));
    }
}
