package healthy.lifestyle.backend.admin.users.controller;

import healthy.lifestyle.backend.admin.users.service.UserAdminService;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("${api.basePath}/${api.version}/admin")
public class UserAdminController {
    private final UserAdminService adminService;

    public UserAdminController(UserAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(name = "role", required = true) Role role,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "country", required = false) Country country,
            @RequestParam(name = "age", required = false) Integer age) {
        return ResponseEntity.ok(adminService.getUsersByFilters(role, username, email, fullName, country, age));
    }
}
