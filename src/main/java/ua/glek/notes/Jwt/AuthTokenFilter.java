package ua.glek.notes.Jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.glek.notes.Model.Token;
import ua.glek.notes.Repository.TokenRepo;
import ua.glek.notes.Service.UserDetailsImpl;
import ua.glek.notes.Service.UserService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    TokenRepo tokenRepo;


    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwtFromCookies(request);
            if (jwt != null) {
                System.out.println("JWT found in cookies: " + jwt);
                if (jwtUtils.validateJwtToken(jwt)) {
                    Optional<Token> tokenOpt = tokenRepo.findByToken(jwt);
                    if (tokenOpt.isPresent()) {
                        Token token = tokenOpt.get();
                        System.out.println("Token found in DB: " + token);
                        if (token.isActive() && token.getExpiryDate().after(new Date())) {
                            String username = jwtUtils.extractUsername(jwt);
                            System.out.println("Username extracted from token: " + username);

                            List<String> roles = jwtUtils.extractRoles(jwt);
                            UserDetails userDetails = userService.loadUserByUsername(username);
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            System.out.println("Authentication set in SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());
                        } else {
                            System.err.println("Token is inactive or expired.");
                        }
                    } else {
                        System.err.println("Token not found in database.");
                    }
                } else {
                    System.err.println("JWT token validation failed.");
                }
            } else {
                System.err.println("JWT not found in cookies.");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        chain.doFilter(request, response);
    }

    private String parseJwtFromCookies(HttpServletRequest request) {
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