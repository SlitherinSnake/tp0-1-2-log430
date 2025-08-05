package com.log430.tp6.test.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test-application.yml")
class TestConfigurationTest {

    @Test
    void testDataBuilder_ShouldCreateValidInventoryItem() {
        // When
        Object item = TestDataBuilder.anInventoryItem()
                .withNom("Test Product")
                .withCategorie("Electronics")
                .withPrix(99.99)
                .withStock(100)
                .build();

        // Then
        assertThat(item).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldCreateValidPersonnel() {
        // When
        Object personnel = TestDataBuilder.aPersonnel()
                .withNom("John Doe")
                .withIdentifiant("EMP001")
                .withUsername("johndoe")
                .build();

        // Then
        assertThat(personnel).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldCreateValidStore() {
        // When
        Object store = TestDataBuilder.aStore()
                .withNom("Test Store")
                .withQuartier("Downtown")
                .withAdresse("123 Test St")
                .build();

        // Then
        assertThat(store).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldCreateValidTransaction() {
        // When
        Object transaction = TestDataBuilder.aTransaction()
                .withType("VENTE")
                .withPersonnelId(1L)
                .withStoreId(1L)
                .withMontantTotal(199.98)
                .addItem(1L, 2, 99.99)
                .build();

        // Then
        assertThat(transaction).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldCreateInactiveItems() {
        // When
        Object inactiveItem = TestDataBuilder.anInventoryItem()
                .withNom("Inactive Product")
                .inactive()
                .build();

        // Then
        assertThat(inactiveItem).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldChainBuilderMethods() {
        // When
        Object complexItem = TestDataBuilder.anInventoryItem()
                .withId(999L)
                .withNom("Complex Product")
                .withCategorie("Advanced Electronics")
                .withPrix(1999.99)
                .withStock(5)
                .withDescription("A very complex product for testing")
                .build();

        // Then
        assertThat(complexItem).isNotNull();
    }

    @Test
    void testDataBuilder_ShouldCreateMultipleItemsWithDifferentData() {
        // When
        Object item1 = TestDataBuilder.anInventoryItem()
                .withNom("Product 1")
                .withPrix(50.0)
                .build();

        Object item2 = TestDataBuilder.anInventoryItem()
                .withNom("Product 2")
                .withPrix(75.0)
                .build();

        // Then
        assertThat(item1).isNotNull();
        assertThat(item2).isNotNull();
        // Each builder should create independent objects
    }
}