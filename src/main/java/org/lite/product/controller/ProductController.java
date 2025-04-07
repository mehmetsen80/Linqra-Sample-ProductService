package org.lite.product.controller;

import lombok.extern.slf4j.Slf4j;
import org.lite.product.model.ProductAvailabilityResponse;
import org.lite.product.model.ProductInfo;
import org.lite.product.model.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

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
@Tag(name = "Product", description = "APIs for managing product information in the catalog")
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
    
    @Operation(
        summary = "Get all products",
        description = "Retrieves all products from the catalog",
        tags = {"Product"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved all products",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "products": [
                        {
                          "id": "P001",
                          "name": "Laptop",
                          "description": "High-performance laptop",
                          "price": 1299.99,
                          "category": "Electronics",
                          "inStock": false,
                          "availableQuantity": null,
                          "estimatedDelivery": null,
                          "warehouseLocation": null
                        }
                      ],
                      "timestamp": "2024-03-19T10:30:22.123Z",
                      "serviceSource": "product-service",
                      "inventoryStatus": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "An unexpected error occurred while retrieving products",
                      "code": "INTERNAL_SERVER_ERROR",
                      "timestamp": "2024-03-19T10:30:22.123",
                      "path": "/api/product/products"
                    }
                    """
                )
            )
        )
    })
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> getAllProducts() {
        log.info("Retrieving all products");
        List<ProductInfo> products = new ArrayList<>(productDatabase.values());
        ProductAvailabilityResponse response = createResponse(products);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
    
    @Operation(summary = "Get product by ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved product",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "products": [
                        {
                          "id": "P001",
                          "name": "Laptop",
                          "description": "High-performance laptop",
                          "price": 1299.99,
                          "category": "Electronics",
                          "inStock": false,
                          "availableQuantity": null,
                          "estimatedDelivery": null,
                          "warehouseLocation": null
                        }
                      ],
                      "timestamp": "2024-03-19T10:30:22.123Z",
                      "serviceSource": "product-service",
                      "inventoryStatus": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "Product not found with ID: P999",
                      "code": "PRODUCT_NOT_FOUND",
                      "timestamp": "2024-03-19T10:30:22.123",
                      "path": "/api/product/products/P999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "An unexpected error occurred while retrieving product",
                      "code": "INTERNAL_SERVER_ERROR",
                      "timestamp": "2024-03-19T10:30:22.123",
                      "path": "/api/product/products/P001"
                    }
                    """
                )
            )
        )
    })
    @GetMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> getProductById(
        @Parameter(description = "ID of the product to retrieve", required = true)
        @PathVariable String productId) {
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
    
    @Operation(summary = "Create a new product")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product successfully created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "products": [
                        {
                          "id": "P004",
                          "name": "Gaming Laptop",
                          "description": "High-end gaming laptop with RTX 4080",
                          "price": 2499.99,
                          "category": "Electronics",
                          "inStock": false,
                          "availableQuantity": null,
                          "estimatedDelivery": null,
                          "warehouseLocation": null
                        }
                      ],
                      "timestamp": "2024-03-19T10:30:22.123Z",
                      "serviceSource": "product-service",
                      "inventoryStatus": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Product with same ID already exists",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "Product with ID P004 already exists",
                      "code": "DUPLICATE_PRODUCT",
                      "timestamp": "2024-03-19T10:30:22.123",
                      "path": "/api/product/products"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "An unexpected error occurred while creating product",
                      "code": "INTERNAL_SERVER_ERROR",
                      "timestamp": "2024-03-19T10:30:22.123",
                      "path": "/api/product/products"
                    }
                    """
                )
            )
        )
    })
    @PostMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> createProduct(
        @Parameter(description = "Product information", required = true)
        @RequestBody ProductInfo product) {
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
    
    @Operation(summary = "Update an existing product")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product successfully updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "products": [
                        {
                          "id": "P004",
                          "name": "Gaming Laptop Pro",
                          "description": "Updated high-end gaming laptop with RTX 4080 and 32GB RAM",
                          "price": 2699.99,
                          "category": "Electronics",
                          "inStock": false,
                          "availableQuantity": null,
                          "estimatedDelivery": null,
                          "warehouseLocation": null
                        }
                      ],
                      "timestamp": "2024-03-19T10:35:22.123Z",
                      "serviceSource": "product-service",
                      "inventoryStatus": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "Product not found with ID: P999",
                      "code": "PRODUCT_NOT_FOUND",
                      "timestamp": "2024-03-19T10:35:22.123",
                      "path": "/api/product/products/P999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "An unexpected error occurred while updating product",
                      "code": "INTERNAL_SERVER_ERROR",
                      "timestamp": "2024-03-19T10:35:22.123",
                      "path": "/api/product/products/P004"
                    }
                    """
                )
            )
        )
    })
    @PutMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductAvailabilityResponse> updateProduct(
        @Parameter(description = "ID of the product to update", required = true)
        @PathVariable String productId,
        @Parameter(description = "Updated product information", required = true)
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
    
    @Operation(summary = "Delete a product")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product successfully deleted"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "Product not found with ID: P999",
                      "code": "PRODUCT_NOT_FOUND",
                      "timestamp": "2024-03-19T10:35:22.123",
                      "path": "/api/product/products/P999"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "message": "An unexpected error occurred while deleting product",
                      "code": "INTERNAL_SERVER_ERROR",
                      "timestamp": "2024-03-19T10:35:22.123",
                      "path": "/api/product/products/P001"
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping(value = "/products/{productId}")
    public ResponseEntity<Void> deleteProduct(
        @Parameter(description = "ID of the product to delete", required = true)
        @PathVariable String productId) {
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
