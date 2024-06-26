package healthy.lifestyle.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.exception.ExceptionDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ExceptionDto exceptionDto = new ExceptionDto(authException.getMessage());

        PrintWriter out = response.getWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        out.write(objectMapper.writeValueAsString(exceptionDto));
        out.flush();

        // Alternative
        // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
