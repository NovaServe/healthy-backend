package healthy.lifestyle.backend.shared.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    JwtAuthEntryPoint jwtAuthEntryPoint;

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Autowired
    ApiUrl apiUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, apiUrl.getAuthUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, apiUrl.getSignupUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getBodyPartsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultExercisesUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getCountriesUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultExerciseDetailsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultHttpRefsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultWorkoutsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultWorkoutDetailsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultNutritionDetailsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getDefaultMentalDetailsUrl())
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, apiUrl.getErrorUrl())
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        httpSecurity.authenticationProvider(authenticationProvider());
        httpSecurity.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
