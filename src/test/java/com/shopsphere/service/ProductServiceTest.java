package com.shopsphere.service;

import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.entity.Product;
import com.shopsphere.repository.ProductRepository;
import com.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldCreateProductSuccessfully() {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setSku("LAP-001");
        request.setPrice(new BigDecimal("55000.00"));
        request.setQuantity(10);

        when(productRepository.findBySku("LAP-001"))
                .thenReturn(Optional.empty());

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals("LAP-001", response.getSku());
        assertEquals(new BigDecimal("55000.00"), response.getPrice());
        assertEquals(10, response.getQuantity());
        assertTrue(response.getActive());

        verify(productRepository).findBySku("LAP-001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowExceptionWhenSkuAlreadyExists() {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setSku("LAP-001");
        request.setPrice(new BigDecimal("55000.00"));
        request.setQuantity(10);

        when(productRepository.findBySku("LAP-001"))
                .thenReturn(Optional.of(
                        new Product(
                                "Laptop",
                                "LAP-001",
                                new BigDecimal("55000.00"),
                                10
                        )
                ));

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(request)
        );

        // Verify
        verify(productRepository).findBySku("LAP-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProduct_shouldThrowExceptionWhenProductNotFound() {

        // Arrange
        Long productId = 100L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProduct(productId)
        );

        // Verify
        verify(productRepository).findById(productId);
    }

    @Test
    void getAllProducts_shouldReturnProductResponses() {

        // Arrange
        Product product1 = new Product(
                "Laptop", "LAP-001",
                new BigDecimal("55000.00"), 10
        );

        Product product2 = new Product(
                "Mouse", "MOU-001",
                new BigDecimal("500.00"), 20
        );

        when(productRepository.findAll())
                .thenReturn(List.of(product1, product2));

        // Act
        List<ProductResponse> responses = productService.getAllProducts();

        // Assert
        assertEquals(2, responses.size());
        assertEquals("Laptop", responses.get(0).getName());
        assertEquals("Mouse", responses.get(1).getName());

        verify(productRepository).findAll();
    }

    @Test
    void getProduct_shouldReturnProductWhenFound() {

        // Arrange
        Long productId = 1L;

        Product product = new Product(
                "Laptop",
                "LAP-001",
                new BigDecimal("55000.00"),
                10
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // Act
        ProductResponse response = productService.getProduct(productId);

        // Assert
        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals("LAP-001", response.getSku());
        assertEquals(new BigDecimal("55000.00"), response.getPrice());

        verify(productRepository).findById(productId);
    }

    @Test
    void createProduct_shouldDefaultActiveToTrueWhenNull() {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Keyboard");
        request.setSku("KEY-001");
        request.setPrice(new BigDecimal("1500.00"));
        request.setQuantity(5);
        request.setActive(null);

        when(productRepository.findBySku("KEY-001"))
                .thenReturn(Optional.empty());

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertTrue(response.getActive());
    }

    @Test
    void createProduct_shouldThrowDuplicateExceptionWhenDatabaseConstraintFails() {

        // Arrange
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setSku("LAP-001");
        request.setPrice(new BigDecimal("55000.00"));
        request.setQuantity(10);

        when(productRepository.findBySku("LAP-001"))
                .thenReturn(Optional.empty());

        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate SKU"));

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(request)
        );

        verify(productRepository).save(any(Product.class));
    }
}
