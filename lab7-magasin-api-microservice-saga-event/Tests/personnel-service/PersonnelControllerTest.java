package com.log430.tp7.personnel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.log430.tp7.application.service.PersonnelService;
import com.log430.tp7.domain.personnel.Personnel;
import com.log430.tp7.presentation.api.PersonnelController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PersonnelControllerTest {

    @Mock
    private PersonnelService personnelService;

    @InjectMocks
    private PersonnelController personnelController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Personnel testPersonnel;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(personnelController).build();
        objectMapper = new ObjectMapper();
        
        testPersonnel = new Personnel();
        testPersonnel.setId(1L);
        testPersonnel.setNom("John Doe");
        testPersonnel.setIdentifiant("EMP001");
        testPersonnel.setUsername("johndoe");
        testPersonnel.setActif(true);
    }

    @Test
    void getAllActivePersonnel_ShouldReturnAllActivePersonnel() throws Exception {
        // Given
        List<Personnel> personnel = Arrays.asList(testPersonnel);
        when(personnelService.getAllActivePersonnel()).thenReturn(personnel);

        // When & Then
        mockMvc.perform(get("/api/personnel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("John Doe"));

        verify(personnelService).getAllActivePersonnel();
    }

    @Test
    void getPersonnelById_WhenPersonnelExists_ShouldReturnPersonnel() throws Exception {
        // Given
        when(personnelService.getPersonnelById(1L)).thenReturn(Optional.of(testPersonnel));

        // When & Then
        mockMvc.perform(get("/api/personnel/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("John Doe"));

        verify(personnelService).getPersonnelById(1L);
    }

    @Test
    void getPersonnelById_WhenPersonnelNotExists_ShouldReturn404() throws Exception {
        // Given
        when(personnelService.getPersonnelById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/personnel/999"))
                .andExpect(status().isNotFound());

        verify(personnelService).getPersonnelById(999L);
    }

    @Test
    void createPersonnel_WithValidData_ShouldCreatePersonnel() throws Exception {
        // Given
        PersonnelController.CreatePersonnelRequest request = new PersonnelController.CreatePersonnelRequest(
                "Jane Smith", "EMP002"
        );
        when(personnelService.createPersonnel("Jane Smith", "EMP002")).thenReturn(testPersonnel);

        // When & Then
        mockMvc.perform(post("/api/personnel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("John Doe"));

        verify(personnelService).createPersonnel("Jane Smith", "EMP002");
    }

    @Test
    void createPersonnel_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        PersonnelController.CreatePersonnelRequest request = new PersonnelController.CreatePersonnelRequest(
                "", "EMP002"
        );
        when(personnelService.createPersonnel("", "EMP002"))
                .thenThrow(new IllegalArgumentException("Invalid name"));

        // When & Then
        mockMvc.perform(post("/api/personnel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(personnelService).createPersonnel("", "EMP002");
    }

    @Test
    void createPersonnelWithLogin_WithValidData_ShouldCreatePersonnelWithLogin() throws Exception {
        // Given
        PersonnelController.CreatePersonnelWithLoginRequest request = new PersonnelController.CreatePersonnelWithLoginRequest(
                "Jane Smith", "EMP002", "janesmith", "password123"
        );
        when(personnelService.createPersonnelWithLogin("Jane Smith", "EMP002", "janesmith", "password123"))
                .thenReturn(testPersonnel);

        // When & Then
        mockMvc.perform(post("/api/personnel/with-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("John Doe"));

        verify(personnelService).createPersonnelWithLogin("Jane Smith", "EMP002", "janesmith", "password123");
    }

    @Test
    void updatePersonnel_WithValidData_ShouldUpdatePersonnel() throws Exception {
        // Given
        PersonnelController.UpdatePersonnelRequest request = new PersonnelController.UpdatePersonnelRequest(
                "John Updated", "EMP001", "johnupdated"
        );
        when(personnelService.updatePersonnel(1L, "John Updated", "EMP001", "johnupdated"))
                .thenReturn(testPersonnel);

        // When & Then
        mockMvc.perform(put("/api/personnel/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("John Doe"));

        verify(personnelService).updatePersonnel(1L, "John Updated", "EMP001", "johnupdated");
    }

    @Test
    void setPassword_WithValidPassword_ShouldSetPassword() throws Exception {
        // Given
        Map<String, String> request = Map.of("password", "newpassword123");
        when(personnelService.setPassword(1L, "newpassword123")).thenReturn(testPersonnel);

        // When & Then
        mockMvc.perform(patch("/api/personnel/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("John Doe"));

        verify(personnelService).setPassword(1L, "newpassword123");
    }

    @Test
    void setPassword_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> request = Map.of("password", "");

        // When & Then
        mockMvc.perform(patch("/api/personnel/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(personnelService, never()).setPassword(anyLong(), anyString());
    }

    @Test
    void findByUsername_WhenPersonnelExists_ShouldReturnPersonnel() throws Exception {
        // Given
        when(personnelService.findByUsername("johndoe")).thenReturn(Optional.of(testPersonnel));

        // When & Then
        mockMvc.perform(get("/api/personnel/by-username?username=johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

        verify(personnelService).findByUsername("johndoe");
    }

    @Test
    void findByUsername_WhenPersonnelNotExists_ShouldReturn404() throws Exception {
        // Given
        when(personnelService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/personnel/by-username?username=nonexistent"))
                .andExpect(status().isNotFound());

        verify(personnelService).findByUsername("nonexistent");
    }

    @Test
    void findByIdentifiant_WhenPersonnelExists_ShouldReturnPersonnel() throws Exception {
        // Given
        when(personnelService.findByIdentifiant("EMP001")).thenReturn(Optional.of(testPersonnel));

        // When & Then
        mockMvc.perform(get("/api/personnel/by-identifiant?identifiant=EMP001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifiant").value("EMP001"));

        verify(personnelService).findByIdentifiant("EMP001");
    }

    @Test
    void getPersonnelWithLoginAccess_ShouldReturnPersonnelWithLogin() throws Exception {
        // Given
        List<Personnel> personnelWithLogin = Arrays.asList(testPersonnel);
        when(personnelService.getPersonnelWithLoginAccess()).thenReturn(personnelWithLogin);

        // When & Then
        mockMvc.perform(get("/api/personnel/with-login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").value("johndoe"));

        verify(personnelService).getPersonnelWithLoginAccess();
    }

    @Test
    void findByRoleName_ShouldReturnPersonnelWithRole() throws Exception {
        // Given
        List<Personnel> personnelWithRole = Arrays.asList(testPersonnel);
        when(personnelService.findByRoleName("MANAGER")).thenReturn(personnelWithRole);

        // When & Then
        mockMvc.perform(get("/api/personnel/by-role?roleName=MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("John Doe"));

        verify(personnelService).findByRoleName("MANAGER");
    }

    @Test
    void deactivatePersonnel_WithValidId_ShouldDeactivatePersonnel() throws Exception {
        // Given
        doNothing().when(personnelService).deactivatePersonnel(1L);

        // When & Then
        mockMvc.perform(delete("/api/personnel/1"))
                .andExpect(status().isNoContent());

        verify(personnelService).deactivatePersonnel(1L);
    }

    @Test
    void deactivatePersonnel_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("Personnel not found"))
                .when(personnelService).deactivatePersonnel(999L);

        // When & Then
        mockMvc.perform(delete("/api/personnel/999"))
                .andExpect(status().isNotFound());

        verify(personnelService).deactivatePersonnel(999L);
    }

    @Test
    void activatePersonnel_WithValidId_ShouldActivatePersonnel() throws Exception {
        // Given
        doNothing().when(personnelService).activatePersonnel(1L);

        // When & Then
        mockMvc.perform(patch("/api/personnel/1/activate"))
                .andExpect(status().isNoContent());

        verify(personnelService).activatePersonnel(1L);
    }

    @Test
    void getAllPersonnel_ShouldReturnAllPersonnel() throws Exception {
        // Given
        List<Personnel> allPersonnel = Arrays.asList(testPersonnel);
        when(personnelService.getAllPersonnel()).thenReturn(allPersonnel);

        // When & Then
        mockMvc.perform(get("/api/personnel/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nom").value("John Doe"));

        verify(personnelService).getAllPersonnel();
    }

    @Test
    void isPersonnelActive_ShouldReturnActiveStatus() throws Exception {
        // Given
        when(personnelService.isPersonnelActive(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/personnel/1/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        verify(personnelService).isPersonnelActive(1L);
    }

    @Test
    void getPersonnelStats_ShouldReturnStatistics() throws Exception {
        // Given
        when(personnelService.getPersonnelCount()).thenReturn(10L);
        when(personnelService.getActivePersonnelCount()).thenReturn(8L);

        // When & Then
        mockMvc.perform(get("/api/personnel/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPersonnel").value(10))
                .andExpect(jsonPath("$.activePersonnel").value(8))
                .andExpect(jsonPath("$.inactivePersonnel").value(2));

        verify(personnelService).getPersonnelCount();
        verify(personnelService).getActivePersonnelCount();
    }

    @Test
    void testEndpoint_ShouldReturnOkStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/personnel/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Personnel API is working"));
    }
}