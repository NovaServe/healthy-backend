package healthy.lifestyle.backend.admin.users.controller;

import healthy.lifestyle.backend.admin.users.service.AdminService;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.model.Country;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "fullName", required = false) String fullName,
            @RequestParam(name = "country", required = false) Country country,
            @RequestParam(name = "age", required = false) Integer age) {
        return ResponseEntity.ok(adminService.getUsersByFilters(username, email, fullName, country, age));
    }
}
