# Assessment Microservices Project

This project contains two Spring Boot microservices that work together:

- **Inventory Service**  
  Manages items and their stock levels. Exposes gRPC endpoints for checking and consuming stock.

- **Order Service**  
  Handles order creation and queries inventory using gRPC.

Both services are containerized with Docker and orchestrated via Docker Compose.

---

## ⚙️ Tech Stack
- Java 21
- Spring Boot
- Spring Data JPA
- gRPC (net.devh grpc-spring-boot-starter)
- ModelMapper
- Docker & Docker Compose
- Maven

---

## 📂 Project Structure
assessment/
│── inventory-service/ # Inventory microservice
│ ├── src/...
│ ├── pom.xml
│ └── Dockerfile
│
│── order-service/ # Order microservice
│ ├── src/...
│ ├── pom.xml
│ └── Dockerfile
│
│── docker-compose.yml # Runs both services together
│── README.md


---

## 🚀 Getting Started

### 1. Prerequisites
Make sure you have the following installed:

- [Java 21](https://adoptium.net/)  
- [Maven 3.9+](https://maven.apache.org/)  
- [Docker Desktop](https://www.docker.com/products/docker-desktop) (with WSL2 enabled if using Windows)  

Verify installations:
```bash
java -version
mvn -v
docker -v

### 2. Build Services

From each service folder (inventory-service/ and order-service/):

open bash and run  ->  mvn clean package -DskipTests

This generates the runnable JARs:

inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar

order-service/target/order-service-0.0.1-SNAPSHOT.jar

### 3. Run All Services with Docker Compose

From the root folder (assessment/):

open bash and run  -> docker-compose up --build

This will:

Build Docker images for both services.

Start inventory-service:

REST → http://localhost:8081

gRPC → localhost:9090

Start order-service:

REST → http://localhost:8080



### 4. Testing the APIs via Swagger

Once both services are running with Docker Compose:

Order Service Swagger: http://localhost:8080/swagger-ui/index.html

Inventory Service Swagger: http://localhost:8081/swagger-ui/index.html

👉 Open these in your browser to test all REST APIs interactively.



### 5. stop services:

docker-compose down

===========>>>>>> important note <<<<<<================

- in application.properties for Order Service

# For local dev (running Inventory service directly on your machine)
grpc.client.itemStockService.address=static://localhost:9090

# For Docker (services communicate by container name in the Docker network)
grpc.client.itemStockService.address=static://inventory-service:9090