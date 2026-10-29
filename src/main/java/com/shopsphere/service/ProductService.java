package com.shopsphere.service;

import com.shopsphere.dto.ProductRequest;
import com.shopsphere.dto.ProductResponse;
import com.shopsphere.entity.Product;
import com.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.repository.ProductRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new DuplicateResourceException(
                    "Product SKU already exists: " + request.getSku()
            );
        }

        Product product = new Product(
                request.getName(),
                request.getSku(),
                request.getPrice(),
                request.getQuantity()
        );

        product.setActive(
                request.getActive() != null
                        ? request.getActive()
                        : true
        );

        try {

            Product savedProduct = productRepository.save(product);

            return new ProductResponse(savedProduct);

        } catch (DataIntegrityViolationException ex) {

            throw new DuplicateResourceException(
                    "Product SKU already exists: " + request.getSku()
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );

        return new ProductResponse(product);
    }
}
