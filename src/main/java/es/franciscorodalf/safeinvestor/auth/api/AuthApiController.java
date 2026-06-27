package es.franciscorodalf.safeinvestor.auth.api;

import es.franciscorodalf.safeinvestor.auth.api.dto.LoginRequest;
import es.franciscorodalf.safeinvestor.auth.api.dto.LoginResponse;
import es.franciscorodalf.safeinvestor.auth.api.dto.RegisterRequest;
import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import es.franciscorodalf.safeinvestor.auth.service.JwtService;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Registro, login y emisión de JWT")
@SecurityRequirements // endpoints públicos: no requieren bearer token
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthApiController(UsuarioService usuarioService,
                             UsuarioRepository usuarios,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        Usuario u = usuarioService.register(req.email(), req.nombre(), req.password());
        String token = jwtService.issue(u.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new LoginResponse(token, u.getEmail(), u.getNombre()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email())
            .orElseThrow(() -> new BadCredentialsException());
        if (!u.isActivo() || !passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new BadCredentialsException();
        }
        String token = jwtService.issue(u.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, u.getEmail(), u.getNombre()));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public void handleBadCredentials() {}

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UsuarioService.EmailAlreadyRegisteredException.class)
    public void handleEmailTaken() {}

    private static class BadCredentialsException extends RuntimeException {}
}
