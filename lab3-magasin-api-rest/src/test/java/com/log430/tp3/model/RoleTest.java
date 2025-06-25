package com.log430.tp3.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void testDefaultConstructor() {
        Role role = new Role();
        
        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    void testParameterizedConstructor() {
        Role.ERole roleName = Role.ERole.ROLE_ADMIN;
        Role role = new Role(roleName);
        
        assertNull(role.getId());
        assertEquals(roleName, role.getName());
    }

    @Test
    void testSettersAndGetters() {
        Role role = new Role();
        Integer id = 1;
        Role.ERole roleName = Role.ERole.ROLE_EMPLOYEE;

        role.setId(id);
        role.setName(roleName);

        assertEquals(id, role.getId());
        assertEquals(roleName, role.getName());
    }

    @Test
    void testAllRoleEnumValues() {
        // Test that all enum values work correctly
        Role adminRole = new Role(Role.ERole.ROLE_ADMIN);
        Role employeeRole = new Role(Role.ERole.ROLE_EMPLOYEE);
        Role viewerRole = new Role(Role.ERole.ROLE_VIEWER);

        assertEquals(Role.ERole.ROLE_ADMIN, adminRole.getName());
        assertEquals(Role.ERole.ROLE_EMPLOYEE, employeeRole.getName());
        assertEquals(Role.ERole.ROLE_VIEWER, viewerRole.getName());
    }

    @Test
    void testEnumValueNames() {
        // Test the actual string values of the enum
        assertEquals("ROLE_ADMIN", Role.ERole.ROLE_ADMIN.name());
        assertEquals("ROLE_EMPLOYEE", Role.ERole.ROLE_EMPLOYEE.name());
        assertEquals("ROLE_VIEWER", Role.ERole.ROLE_VIEWER.name());
    }

    @Test
    void testEnumValuesArray() {
        Role.ERole[] values = Role.ERole.values();
        assertEquals(3, values.length);
        
        // Verify all expected values are present
        boolean hasAdmin = false;
        boolean hasEmployee = false;
        boolean hasViewer = false;
        
        for (Role.ERole value : values) {
            if (value == Role.ERole.ROLE_ADMIN) hasAdmin = true;
            if (value == Role.ERole.ROLE_EMPLOYEE) hasEmployee = true;
            if (value == Role.ERole.ROLE_VIEWER) hasViewer = true;
        }
        
        assertTrue(hasAdmin);
        assertTrue(hasEmployee);
        assertTrue(hasViewer);
    }

    @Test
    void testEnumValueOf() {
        // Test enum valueOf method
        assertEquals(Role.ERole.ROLE_ADMIN, Role.ERole.valueOf("ROLE_ADMIN"));
        assertEquals(Role.ERole.ROLE_EMPLOYEE, Role.ERole.valueOf("ROLE_EMPLOYEE"));
        assertEquals(Role.ERole.ROLE_VIEWER, Role.ERole.valueOf("ROLE_VIEWER"));
    }
}
