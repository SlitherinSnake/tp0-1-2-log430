package com.log430.tp4.api.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp4.api.dto.LoginRequest;
import com.log430.tp4.api.dto.SignupRequest;
import com.log430.tp4.model.Role;
import com.log430.tp4.model.Role.ERole;
import com.log430.tp4.model.User;
import com.log430.tp4.repository.RoleRepository;
import com.log430.tp4.repository.UserRepository;
import com.log430.tp4.security.jwt.JwtUtils;
import com.log430.tp4.security.services.UserDetailsImpl;

/**
 * Tests unitaires pour AuthController
 * Teste les endpoints d'authentification :
 * - POST /api/v1/auth/login (authentification)
 * - POST /api/v1/auth/signup (inscription)
 */
@WebMvcTest(value = {AuthController.class, GlobalExceptionHandler.class}, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private AuthenticationManager authenticationManager;

    @SuppressWarnings("removal")
    @MockBean
    private UserRepository userRepository;

    @SuppressWarnings("removal")
    @MockBean
    private RoleRepository roleRepository;

    @SuppressWarnings("removal")
    @MockBean
    private PasswordEncoder passwordEncoder;

    @SuppressWarnings("removal")
    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private User user;
    private Role viewerRole;
    private Role adminRole;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        // Données de test
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setPassword("password123");

        user = new User("testuser", "encodedPassword");
        user.setId(1L);

        // Rôles
        viewerRole = new Role(ERole.ROLE_VIEWER);
        adminRole = new Role(ERole.ROLE_ADMIN);
        employeeRole = new Role(ERole.ROLE_EMPLOYEE);
    }

    @Test
    void testLogin_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        // Given
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.accessToken", is("fake-jwt-token")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.roles").isArray());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - Requête avec données manquantes
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername(""); // Username vide
        invalidRequest.setPassword(""); // Password vide

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_WithValidData_ShouldCreateUser() throws Exception {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName(ERole.ROLE_VIEWER)).thenReturn(Optional.of(viewerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("User registered successfully!")));

        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_WithExistingUsername_ShouldReturn400() throws Exception {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("Error: Username is already taken!")));

        verify(userRepository, times(1)).existsByUsername("newuser");
    }

    @Test
    void testSignup_WithAdminRole_ShouldCreateUserWithAdminRole() throws Exception {
        // Given
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        signupRequest.setRoles(roles);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("User registered successfully!")));

        verify(roleRepository, times(1)).findByName(ERole.ROLE_ADMIN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_WithEmployeeRole_ShouldCreateUserWithEmployeeRole() throws Exception {
        // Given
        Set<String> roles = new HashSet<>();
        roles.add("employee");
        signupRequest.setRoles(roles);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName(ERole.ROLE_EMPLOYEE)).thenReturn(Optional.of(employeeRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("User registered successfully!")));

        verify(roleRepository, times(1)).findByName(ERole.ROLE_EMPLOYEE);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_WithInvalidRole_ShouldUseDefaultViewerRole() throws Exception {
        // Given
        Set<String> roles = new HashSet<>();
        roles.add("unknownrole");
        signupRequest.setRoles(roles);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(roleRepository.findByName(ERole.ROLE_VIEWER)).thenReturn(Optional.of(viewerRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.message", is("User registered successfully!")));

        verify(roleRepository, times(1)).findByName(ERole.ROLE_VIEWER);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSignup_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - Requête avec données invalides
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername("ab"); // Trop court (min 3)
        invalidRequest.setPassword("123"); // Trop court (min 6)

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_WithEmptyUsername_ShouldReturn400() throws Exception {
        // Given
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername(""); // Username vide
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignup_WithEmptyPassword_ShouldReturn400() throws Exception {
        // Given
        SignupRequest invalidRequest = new SignupRequest();
        invalidRequest.setUsername("newuser");
        invalidRequest.setPassword(""); // Password vide

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
