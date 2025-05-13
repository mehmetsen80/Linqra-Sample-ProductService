# Product Service

A microservice for managing product information as part of a microservices architecture.

## Overview

The Product Service provides RESTful APIs for creating, reading, updating, and deleting product information. It is designed to work with other microservices such as the Inventory Service to provide a complete product management solution.

## Project Folder Structure

```
LINQRA_PRODUCT_SERVICE/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .kube/
│   └── product/
│       └── Dockerfile
├── keys/
│   ├── client-truststore.jks
│   └── product-keystore-container.jks
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── lite/
│       │           └── product/
│       │               ├── ProductServiceApplication.java
│       │               ├── config/
│       │               │   ├── EurekaClientConfig.java
│       │               │   ├── RestTemplateConfig.java
│       │               │   └── SecurityConfig.java
│       │               ├── controller/
│       │               │   ├── ProductController.java
│       │               │   └── HealthController.java
│       │               ├── filter/
│       │               │   └── JwtRoleValidationFilter.java
│       │               ├── interceptor/
│       │               │   └── ServiceNameInterceptor.java
│       │               └── model/
│       │                   ├── ErrorResponse.java
│       │                   ├── HealthStatus.java
│       │                   ├── ProductAvailabilityResponse.java
│       │                   └── ProductInfo.java
│       └── resources/
│           └── application.yml
├── docker-compose.yml
├── docker-compose-ec2.yml
└── pom.xml
```

## Prerequisites

Before running this service, ensure that the following components are up and running:

1. **Discovery Server** - The Eureka service discovery server of the Linqra application must be running first to allow this service to register itself.

2. **API Gateway** - The API Gateway of the Linqra application must be running to properly route requests to this service.

These components are essential for the proper functioning of the microservices architecture. The Product Service will automatically register with the Discovery Server when it starts up, and all requests should be routed through the API Gateway.

## API Endpoints

Base URL: `https://localhost:7777/r/product-service`

### Get All Products

Retrieves all available products in the catalog.

- **URL**: `/api/product/products`
- **Method**: `GET`
- **Produces**: `application/json`
- **Response**: A ProductAvailabilityResponse object containing a list of products

**Sample Response:**
```json
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
    },
    {
      "id": "P002",
      "name": "Smartphone",
      "description": "Latest smartphone model",
      "price": 799.99,
      "category": "Electronics",
      "inStock": false,
      "availableQuantity": null,
      "estimatedDelivery": null,
      "warehouseLocation": null
    }
  ],
  "timestamp": "2023-07-24T14:15:22.123Z",
  "serviceSource": "product-service",
  "inventoryStatus": null
}
```

### Get Product by ID

Retrieves a specific product by its ID.

- **URL**: `/api/product/products/{productId}`
- **Method**: `GET`
- **Produces**: `application/json`
- **Path Parameters**:
  - `productId`: The unique identifier of the product
- **Response**: A ProductAvailabilityResponse object containing the requested product

### Create New Product

Creates a new product in the catalog.

- **URL**: `/api/product/products`
- **Method**: `POST`
- **Consumes**: `application/json`
- **Produces**: `application/json`
- **Request Body**: A ProductInfo object containing the new product details
- **Response**: Status 201 (Created) with a ProductAvailabilityResponse containing the newly created product

**Sample Request Body:**
```json
{
  "name": "Wireless Headphones",
  "description": "Noise cancelling wireless headphones",
  "price": 149.99,
  "category": "Electronics"
}
```

### Update Product

Updates an existing product in the catalog.

- **URL**: `/api/product/products/{productId}`
- **Method**: `PUT`
- **Consumes**: `application/json`
- **Produces**: `application/json`
- **Path Parameters**:
  - `productId`: The unique identifier of the product to update
- **Request Body**: A ProductInfo object containing the updated product details
- **Response**: A ProductAvailabilityResponse containing the updated product

**Sample Request Body:**
```json
{
  "name": "Gaming Laptop Pro",
  "description": "Updated high-end gaming laptop with RTX 4080 and 32GB RAM",
  "price": 2699.99,
  "category": "Electronics"
}
```

**Sample Response:**
```json
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
```

Note: The `id` in the request body is not required as it's taken from the path parameter `{productId}`. The inventory-related fields will be managed by the inventory service.

### Delete Product

Removes a product from the catalog.

- **URL**: `/api/product/products/{productId}`
- **Method**: `DELETE`
- **Path Parameters**:
  - `productId`: The unique identifier of the product to delete
- **Response**: Status 204 (No Content) on successful deletion

## Data Models

### ProductInfo

Represents the details of a product.

### ProductAvailabilityResponse

Response wrapper class containing lists of products and metadata.

## Key Components

1. **ProductServiceApplication.java**: The main Spring Boot application class that bootstraps the product service.

2. **EurekaClientConfig.java**: Configuration class for registering with the Eureka Discovery Server.

3. **ProductController.java**: REST controller that exposes endpoints for CRUD operations on products.

4. **Model Classes**:
   - **ProductInfo.java**: Represents a product entity with details like ID, name, description, price, etc.
   - **ProductAvailabilityResponse.java**: Response wrapper class containing lists of products and metadata.

5. **application.yml**: Configuration file for the service, including port, service name, and Eureka registration details.

Unlike the inventory-service, the product-service doesn't have an InventoryItem class as it's focused solely on product information. The inventory-related fields in ProductInfo (like inStock, availableQuantity) are initially set to null/false and are later populated by the inventory-service when it interacts with the product data.

## EC2 Deployment

### GitHub Actions Configuration

The service uses GitHub Actions for continuous integration and deployment to EC2. To set up the deployment pipeline, configure the following secrets in your GitHub repository:

1. `EC2_SSH_KEY_PROD`: Your EC2 instance's private key (PEM file content)
   ```bash
   # Get the content of your EC2 key file
   sudo cat /path/to/your-ec2-file.pem
   ```

2. `HOST_DNS_PROD`: Your EC2 instance's public DNS
   ```
   ec2-xx-xx-xx-xx.us-west-2.compute.amazonaws.com
   ```

3. `USERNAME_PROD`: EC2 instance username
   ```
   ubuntu
   ```

4. `TARGET_DIR_PROD`: Deployment directory on EC2
   ```
   /var/www/product-service
   ```

### Docker Management on EC2

#### Cleanup Docker Environment
To remove all unused Docker resources (networks, volumes, images):
```bash
docker system prune -a -f
```

#### Build and Run Service
To build and start only the product service:
```bash
sudo docker compose up -d --build product-service
```

#### Monitoring
To view service logs:
```bash
sudo docker logs product-service
```

#### Health Check
To verify the service health through the API Gateway container:
```bash
# Basic health check
docker exec -it linqra-api-gateway-service-1 curl -k -v https://api-gateway-service:7777/r/product-service/health

# Health check with JWT token (optional)
docker exec -it linqra-api-gateway-service-1 curl -k -H "Authorization: Bearer <ACCESS_TOKEN>" -v https://api-gateway-service:7777/r/product-service/health
```

**Note**: When checking health through the API Gateway container, authentication is not required as both services are on the same Docker network.