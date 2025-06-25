package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        User newUser = new User();
        
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getPassword());
        assertNotNull(newUser.getRoles());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    void testParameterizedConstructor() {
        String username = "testuser";
        String password = "password123";
        
        User newUser = new User(username, password);
        
        assertNull(newUser.getId());
        assertEquals(username, newUser.getUsername());
        assertEquals(password, newUser.getPassword());
        assertNotNull(newUser.getRoles());
        assertTrue(newUser.getRoles().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        Long id = 1L;
        String username = "john.doe";
        String password = "securePassword";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(Role.ERole.ROLE_EMPLOYEE));

        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRoles(roles);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(roles, user.getRoles());
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void testSetRolesWithMultipleRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(Role.ERole.ROLE_ADMIN));
        roles.add(new Role(Role.ERole.ROLE_EMPLOYEE));
        roles.add(new Role(Role.ERole.ROLE_VIEWER));

        user.setRoles(roles);

        assertEquals(3, user.getRoles().size());
    }

    @Test
    void testToString() {
        user.setId(1L);
        user.setUsername("testuser");
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(Role.ERole.ROLE_EMPLOYEE));
        user.setRoles(roles);

        String result = user.toString();
        assertTrue(result.contains("User{id=1"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("roles="));
    }
}
