package healthy.lifestyle.backend.security;

import org.springframework.beans.factory.annotation.Value;
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
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${api.basePath}/${api.version}/users/auth/**")
    private String authUrl;

    @Value("${api.basePath}/${api.version}/workouts/bodyParts")
    private String bodyPartsUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default")
    private String defaultExercisesUrl;

    @Value("${api.basePath}/${api.version}/users/countries")
    private String countriesUrl;

    @Value("${api.basePath}/${api.version}/workouts/exercises/default/{exercise_id}")
    private String defaultExerciseDetailsUrl;

    @Value("${api.basePath}/${api.version}/workouts/httpRefs/default")
    private String defaultHttpRefsUrl;

    @Value("${api.basePath}/${api.version}/workouts/default")
    private String defaultWorkoutsUrl;

    @Value("${api.basePath}/${api.version}/workouts/{workout_id}")
    private String workoutDetailsUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            JwtAuthFilter jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, authUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, bodyPartsUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, defaultExercisesUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, countriesUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, defaultExerciseDetailsUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, defaultHttpRefsUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, defaultWorkoutsUrl)
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, workoutDetailsUrl)
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
