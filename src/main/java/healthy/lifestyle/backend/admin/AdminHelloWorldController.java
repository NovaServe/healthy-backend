package healthy.lifestyle.backend.admin;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/admin")
public class AdminHelloWorldController {
    @PostMapping("/hello-world")
    public ResponseEntity<AdminHelloWorldDto> login(@Valid @RequestBody AdminHelloWorldDto requestDto) {
        return ResponseEntity.ok(requestDto);
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class AdminHelloWorldDto {
    private String hello;
}
