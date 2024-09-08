package ua.glek.notes.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ua.glek.notes.Jwt.JwtUtils;
import ua.glek.notes.Model.Dto.LogInDto;
import ua.glek.notes.Model.Dto.SignUpDto;
import ua.glek.notes.Model.Roles;
import ua.glek.notes.Model.Token;
import ua.glek.notes.Model.Users;
import ua.glek.notes.Repository.RoleRepo;
import ua.glek.notes.Repository.TokenRepo;
import ua.glek.notes.Repository.UsersRepo;
import ua.glek.notes.Service.UserService;

import java.util.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UsersRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    UserService userService;
    @Autowired
    private TokenRepo tokenRepo;

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@RequestBody LogInDto sign, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(sign.getUsername(), sign.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        final UserDetails userDetails = userService.loadUserByUsername(sign.getUsername());
        final String jwt = jwtUtils.generateTokenFromUsername(userDetails);

        Date createdDate = new Date();
        Date expiryDate = new Date(createdDate.getTime() + jwtUtils.getJwtExpirationMs());

        Token token = new Token(jwt, sign.getUsername(), true, createdDate, expiryDate);
        tokenRepo.save(token);

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .httpOnly(true)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return ResponseEntity.ok("User signed in successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto sign) {
        if (userRepo.existsByUsername(sign.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: Username is already taken!");
        }

        Users user = new Users();
        user.setUsername(sign.getUsername());
        user.setPassword(passwordEncoder.encode(sign.getPassword()));
        user.setEmail(sign.getEmail());

        Set<Roles> roles = new HashSet<>();
        List<String> strRoles = sign.getRoles();

        if (strRoles == null) {
            Roles userRole = roleRepo.findByName("ROLE_USER");
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Roles roleEntity = roleRepo.findByName(role);
                roles.add(roleEntity);
            });
        }

        user.setRoles(roles);
        userRepo.save(user);
        return new ResponseEntity<>("Register successful", HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        String jwt = parseJwt(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            Optional<Token> tokenOpt = tokenRepo.findByToken(jwt);
            if (tokenOpt.isPresent()) {
                Token token = tokenOpt.get();
                token.setActive(false);
                tokenRepo.save(token);

                ResponseCookie jwtCookie = ResponseCookie.from("jwt", null)
                        .path("/")
                        .maxAge(0)
                        .httpOnly(true)
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

                return ResponseEntity.ok("User logged out successfully");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: Token not found in database");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: Token validation failed");
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        if (request.getCookies() != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst();
            if (jwtCookie.isPresent()) {
                return jwtCookie.get().getValue();
            }
        }

        return null;
    }
}

