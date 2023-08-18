package healthy.lifestyle.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloWorld {
    @GetMapping("/helloworld")
    public ResponseEntity<HelloWorldResponseDto> helloWorld() {
        HelloWorldResponseDto responseDto = new HelloWorldResponseDto("Hello Endpoint!");
        return ResponseEntity.ok(responseDto);
    }
}

class HelloWorldResponseDto {
    private String message;

    public HelloWorldResponseDto() {}

    public HelloWorldResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
