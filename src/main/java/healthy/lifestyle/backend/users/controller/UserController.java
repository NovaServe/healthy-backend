package healthy.lifestyle.backend.users.controller;

import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.service.CountryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.basePath}/${api.version}/users")
public class UserController {
    private final CountryService countryService;

    public UserController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponseDto>> countries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
}
