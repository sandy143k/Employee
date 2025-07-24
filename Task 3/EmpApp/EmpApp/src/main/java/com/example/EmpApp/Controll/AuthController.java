package com.example.EmpApp.Controll;

import com.example.EmpApp.Security.JwtTokenHelper;
import com.example.EmpApp.dto.AuthRequest;
import com.example.EmpApp.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenHelper jwtTokenHelper,
                          UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenHelper = jwtTokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            String accessToken = jwtTokenHelper.generateAccessToken(userDetails);
            String refreshToken = jwtTokenHelper.generateRefreshToken(userDetails);
            Date accessTokenExpiry = jwtTokenHelper.getExpirationDateFromToken(accessToken);

            return ResponseEntity.ok(
                    new AuthResponse(
                            userDetails.getUsername(),
                            accessToken,
                            refreshToken,
                            accessTokenExpiry
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "error", "Authentication failed",
                            "message", "Invalid email or password"
                    ));
        }
    }
}