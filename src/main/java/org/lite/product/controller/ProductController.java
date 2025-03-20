package org.lite.product.controller;

import lombok.extern.slf4j.Slf4j;
import org.lite.product.model.ProductAvailabilityResponse;
import org.lite.product.model.ProductInfo;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
@RequestMapping("/api/product")
public class ProductController {
    
    // Simulated product database
    private final Map<String, ProductInfo> productDatabase = new ConcurrentHashMap<>();
    
    public ProductController() {
        // Initialize with some sample products
        initializeProducts();
    }
    
    private void initializeProducts() {
        ProductInfo product1 = new ProductInfo("P001", "Laptop", "High-performance laptop", 
                new BigDecimal("1299.99"), "Electronics", false, null, null, null);
        ProductInfo product2 = new ProductInfo("P002", "Smartphone", "Latest smartphone model", 
                new BigDecimal("799.99"), "Electronics", false, null, null, null);
        ProductInfo product3 = new ProductInfo("P003", "Coffee Maker", "Automatic coffee machine", 
                new BigDecimal("129.99"), "Home Appliances", false, null, null, null);
        
        productDatabase.put(product1.getId(), product1);
        productDatabase.put(product2.getId(), product2);
        productDatabase.put(product3.getId(), product3);
    }
    
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> getAllProducts() {
        log.info("Retrieving all products");
        List<ProductInfo> products = new ArrayList<>(productDatabase.values());
        ProductAvailabilityResponse response = createResponse(products);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @GetMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> getProductById(@PathVariable String productId) {
        log.info("Retrieving product with ID: {}", productId);
        ProductInfo product = productDatabase.get(productId);
        List<ProductInfo> products = new ArrayList<>();
        
        if (product != null) {
            products.add(product);
        } else {
            log.warn("Product with ID {} not found", productId);
        }
        
        ProductAvailabilityResponse response = createResponse(products);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @PostMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> createProduct(@RequestBody ProductInfo product) {
        log.info("Creating new product: {}", product);
        
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            // Generate a simple ID if not provided
            product.setId("P" + (productDatabase.size() + 1));
        }
        
        // Check if product with same ID already exists
        if (productDatabase.containsKey(product.getId())) {
            log.warn("Product with ID {} already exists", product.getId());
            return ResponseEntity.badRequest().build();
        }
        
        productDatabase.put(product.getId(), product);
        
        List<ProductInfo> products = new ArrayList<>();
        products.add(product);
        ProductAvailabilityResponse response = createResponse(products);
        
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @PutMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> updateProduct(
            @PathVariable String productId,
            @RequestBody ProductInfo updatedProduct) {
        log.info("Updating product with ID {}: {}", productId, updatedProduct);
        
        if (!productDatabase.containsKey(productId)) {
            log.warn("Product with ID {} not found for update", productId);
            return ResponseEntity.notFound().build();
        }
        
        // Ensure the ID in the path matches the product
        updatedProduct.setId(productId);
        productDatabase.put(productId, updatedProduct);
        
        List<ProductInfo> products = new ArrayList<>();
        products.add(updatedProduct);
        ProductAvailabilityResponse response = createResponse(products);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @DeleteMapping(value = "/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        log.info("Deleting product with ID: {}", productId);
        
        if (!productDatabase.containsKey(productId)) {
            log.warn("Product with ID {} not found for deletion", productId);
            return ResponseEntity.notFound().build();
        }
        
        productDatabase.remove(productId);
        
        return ResponseEntity.noContent().build();
    }
    
    private ProductAvailabilityResponse createResponse(List<ProductInfo> products) {
        ProductAvailabilityResponse response = new ProductAvailabilityResponse();
        response.setProducts(products);
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        response.setServiceSource("product-service");
        
        return response;
    }
}
