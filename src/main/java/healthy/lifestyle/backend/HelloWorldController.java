package healthy.lifestyle.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloWorldController {
    @GetMapping("/")
    public ResponseEntity<Dto> helloWorld() {
        return ResponseEntity.ok(new Dto("Hello World"));
    }
}

record Dto(String message) {}
