package com.log430.tp6.personnel.service;

import com.log430.tp6.application.service.PersonnelService;
import com.log430.tp6.domain.personnel.Personnel;
import com.log430.tp6.infrastructure.repository.PersonnelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonnelServiceTest {

    @Mock
    private PersonnelRepository personnelRepository;

    @InjectMocks
    private PersonnelService personnelService;

    private Personnel testPersonnel;

    @BeforeEach
    void setUp() {
        testPersonnel = new Personnel();
        testPersonnel.setId(1L);
        testPersonnel.setNom("John Doe");
        testPersonnel.setIdentifiant("EMP001");
        testPersonnel.setUsername("johndoe");
        testPersonnel.setActif(true);
    }

    @Test
    void getAllActivePersonnel_ShouldReturnOnlyActivePersonnel() {
        // Given
        List<Personnel> activePersonnel = Arrays.asList(testPersonnel);
        when(personnelRepository.findByActifTrue()).thenReturn(activePersonnel);

        // When
        List<Personnel> result = personnelService.getAllActivePersonnel();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNom()).isEqualTo("John Doe");
        assertThat(result.get(0).getActif()).isTrue();
        verify(personnelRepository).findByActifTrue();
    }

    @Test
    void getPersonnelById_WhenPersonnelExists_ShouldReturnPersonnel() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));

        // When
        Optional<Personnel> result = personnelService.getPersonnelById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("John Doe");
        verify(personnelRepository).findById(1L);
    }

    @Test
    void getPersonnelById_WhenPersonnelNotExists_ShouldReturnEmpty() {
        // Given
        when(personnelRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Personnel> result = personnelService.getPersonnelById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(personnelRepository).findById(999L);
    }

    @Test
    void createPersonnel_WithValidData_ShouldCreateAndReturnPersonnel() {
        // Given
        when(personnelRepository.existsByIdentifiant("EMP002")).thenReturn(false);
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        Personnel result = personnelService.createPersonnel("Jane Smith", "EMP002");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("John Doe");
        verify(personnelRepository).existsByIdentifiant("EMP002");
        verify(personnelRepository).save(any(Personnel.class));
    }

    @Test
    void createPersonnel_WithNullName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> personnelService.createPersonnel(null, "EMP002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createPersonnel_WithEmptyName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> personnelService.createPersonnel("", "EMP002"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nom");
    }

    @Test
    void createPersonnel_WithNullIdentifiant_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> personnelService.createPersonnel("John Doe", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identifiant");
    }

    @Test
    void createPersonnel_WithDuplicateIdentifiant_ShouldThrowException() {
        // Given
        when(personnelRepository.existsByIdentifiant("EMP001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> personnelService.createPersonnel("Jane Smith", "EMP001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createPersonnelWithLogin_WithValidData_ShouldCreatePersonnelWithLogin() {
        // Given
        when(personnelRepository.existsByIdentifiant("EMP002")).thenReturn(false);
        when(personnelRepository.existsByUsername("janesmith")).thenReturn(false);
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        Personnel result = personnelService.createPersonnelWithLogin("Jane Smith", "EMP002", "janesmith", "password123");

        // Then
        assertThat(result).isNotNull();
        verify(personnelRepository).existsByIdentifiant("EMP002");
        verify(personnelRepository).existsByUsername("janesmith");
        verify(personnelRepository).save(any(Personnel.class));
    }

    @Test
    void createPersonnelWithLogin_WithDuplicateUsername_ShouldThrowException() {
        // Given
        when(personnelRepository.existsByIdentifiant("EMP002")).thenReturn(false);
        when(personnelRepository.existsByUsername("johndoe")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> personnelService.createPersonnelWithLogin("Jane Smith", "EMP002", "johndoe", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void updatePersonnel_WhenPersonnelExists_ShouldUpdateAndReturnPersonnel() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        Personnel result = personnelService.updatePersonnel(1L, "John Updated", "EMP001", "johnupdated");

        // Then
        assertThat(result).isNotNull();
        verify(personnelRepository).findById(1L);
        verify(personnelRepository).save(testPersonnel);
    }

    @Test
    void updatePersonnel_WhenPersonnelNotExists_ShouldThrowException() {
        // Given
        when(personnelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> personnelService.updatePersonnel(999L, "John Updated", "EMP001", "johnupdated"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void setPassword_WhenPersonnelExists_ShouldSetPassword() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        Personnel result = personnelService.setPassword(1L, "newpassword123");

        // Then
        assertThat(result).isNotNull();
        verify(personnelRepository).findById(1L);
        verify(personnelRepository).save(testPersonnel);
    }

    @Test
    void setPassword_WithNullPassword_ShouldThrowException() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));

        // When & Then
        assertThatThrownBy(() -> personnelService.setPassword(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password");
    }

    @Test
    void setPassword_WithEmptyPassword_ShouldThrowException() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));

        // When & Then
        assertThatThrownBy(() -> personnelService.setPassword(1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password");
    }

    @Test
    void findByUsername_WhenPersonnelExists_ShouldReturnPersonnel() {
        // Given
        when(personnelRepository.findByUsername("johndoe")).thenReturn(Optional.of(testPersonnel));

        // When
        Optional<Personnel> result = personnelService.findByUsername("johndoe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("johndoe");
        verify(personnelRepository).findByUsername("johndoe");
    }

    @Test
    void findByIdentifiant_WhenPersonnelExists_ShouldReturnPersonnel() {
        // Given
        when(personnelRepository.findByIdentifiant("EMP001")).thenReturn(Optional.of(testPersonnel));

        // When
        Optional<Personnel> result = personnelService.findByIdentifiant("EMP001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIdentifiant()).isEqualTo("EMP001");
        verify(personnelRepository).findByIdentifiant("EMP001");
    }

    @Test
    void getPersonnelWithLoginAccess_ShouldReturnPersonnelWithUsername() {
        // Given
        List<Personnel> personnelWithLogin = Arrays.asList(testPersonnel);
        when(personnelRepository.findByUsernameIsNotNull()).thenReturn(personnelWithLogin);

        // When
        List<Personnel> result = personnelService.getPersonnelWithLoginAccess();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isNotNull();
        verify(personnelRepository).findByUsernameIsNotNull();
    }

    @Test
    void findByRoleName_ShouldReturnPersonnelWithRole() {
        // Given
        List<Personnel> personnelWithRole = Arrays.asList(testPersonnel);
        when(personnelRepository.findByRoleName("MANAGER")).thenReturn(personnelWithRole);

        // When
        List<Personnel> result = personnelService.findByRoleName("MANAGER");

        // Then
        assertThat(result).hasSize(1);
        verify(personnelRepository).findByRoleName("MANAGER");
    }

    @Test
    void deactivatePersonnel_WhenPersonnelExists_ShouldDeactivatePersonnel() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        personnelService.deactivatePersonnel(1L);

        // Then
        verify(personnelRepository).findById(1L);
        verify(personnelRepository).save(testPersonnel);
    }

    @Test
    void deactivatePersonnel_WhenPersonnelNotExists_ShouldThrowException() {
        // Given
        when(personnelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> personnelService.deactivatePersonnel(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void activatePersonnel_WhenPersonnelExists_ShouldActivatePersonnel() {
        // Given
        testPersonnel.setActif(false);
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));
        when(personnelRepository.save(any(Personnel.class))).thenReturn(testPersonnel);

        // When
        personnelService.activatePersonnel(1L);

        // Then
        verify(personnelRepository).findById(1L);
        verify(personnelRepository).save(testPersonnel);
    }

    @Test
    void getAllPersonnel_ShouldReturnAllPersonnel() {
        // Given
        Personnel inactivePersonnel = new Personnel();
        inactivePersonnel.setActif(false);
        List<Personnel> allPersonnel = Arrays.asList(testPersonnel, inactivePersonnel);
        when(personnelRepository.findAll()).thenReturn(allPersonnel);

        // When
        List<Personnel> result = personnelService.getAllPersonnel();

        // Then
        assertThat(result).hasSize(2);
        verify(personnelRepository).findAll();
    }

    @Test
    void isPersonnelActive_WhenPersonnelExists_ShouldReturnActiveStatus() {
        // Given
        when(personnelRepository.findById(1L)).thenReturn(Optional.of(testPersonnel));

        // When
        boolean result = personnelService.isPersonnelActive(1L);

        // Then
        assertThat(result).isTrue();
        verify(personnelRepository).findById(1L);
    }

    @Test
    void isPersonnelActive_WhenPersonnelNotExists_ShouldReturnFalse() {
        // Given
        when(personnelRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        boolean result = personnelService.isPersonnelActive(999L);

        // Then
        assertThat(result).isFalse();
        verify(personnelRepository).findById(999L);
    }

    @Test
    void getPersonnelCount_ShouldReturnTotalCount() {
        // Given
        when(personnelRepository.count()).thenReturn(10L);

        // When
        long result = personnelService.getPersonnelCount();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(personnelRepository).count();
    }

    @Test
    void getActivePersonnelCount_ShouldReturnActiveCount() {
        // Given
        when(personnelRepository.countByActifTrue()).thenReturn(8L);

        // When
        long result = personnelService.getActivePersonnelCount();

        // Then
        assertThat(result).isEqualTo(8L);
        verify(personnelRepository).countByActifTrue();
    }
}