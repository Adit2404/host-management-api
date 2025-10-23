# Host Management API

Spring Boot REST API for managing and consolidating network host records with automated similarity detection.

[![Java](https://img.shields.io/badge/Java-11-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.14-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Similarity Algorithm](#similarity-algorithm)
- [Testing](#testing)
- [Docker](#docker)
- [Project Structure](#project-structure)

## 🎯 Overview

This REST API provides endpoints for managing network host records. It automatically groups similar hosts based on their properties (IP address, domain names, roles, protocols, OS) and allows users to merge duplicate records into consolidated entries.


## ✨ Features

- 🔍 **Automated Host Grouping** - Uses point-based similarity algorithm
- 🔄 **Host Merging** - Consolidate multiple records into one
- 🔎 **Search & Filter** - Find hosts by any property
- 📊 **In-Memory Storage** - No database required for demo
- 📚 **OpenAPI/Swagger** - Interactive API documentation
- 🐳 **Docker Ready** - Containerized deployment

## 🛠️ Technology Stack

- **Java**: 11
- **Framework**: Spring Boot 2.7.14
- **Build Tool**: Maven 3.6+
- **Testing**: JUnit 5, Mockito
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Utilities**: Lombok

## 🚀 Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository**
   ```bash
   git clone (https://github.com/Adit2404/host-management-api)
   cd host-management-api
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the API**
   - API Base URL: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

### Quick Test

```bash
# Test the API
curl http://localhost:8080/api/hosts/groups

# Expected: JSON response with host groups
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. GET /api/hosts/groups
Get all host groups with optional search.

**Query Parameters:**
- `search` (optional): Keyword to filter hosts

**Response:** `200 OK`
```json
[
  {
    "groupId": "string",
    "primaryHost": {
      "id": "string",
      "ip_address": "192.168.1.10",
      "names": ["web01.example.com"],
      "roles": ["web_server"],
      "protocols": ["HTTP", "HTTPS"],
      "os": "Ubuntu 22.04",
      "consolidated": false
    },
    "similarHosts": [...],
    "totalRecords": 3,
    "consolidated": false
  }
]
```

**Example:**
```bash
curl http://localhost:8080/api/hosts/groups
curl http://localhost:8080/api/hosts/groups?search=web
```

#### 2. GET /api/hosts/groups/{groupId}
Get details of a specific host group.

**Path Parameters:**
- `groupId`: ID of the host group

**Response:** `200 OK` | `404 Not Found`

**Example:**
```bash
curl http://localhost:8080/api/hosts/groups/abc-123
```

#### 3. POST /api/hosts/merge
Merge multiple hosts into a consolidated record.

**Request Body:**
```json
{
  "hostIds": ["id1", "id2", "id3"]
}
```

**Response:** `200 OK` | `400 Bad Request`
```json
{
  "id": "merged-id",
  "ip_address": "192.168.1.10",
  "names": ["web01.example.com", "api.example.com"],
  "roles": ["web_server", "api_server"],
  "protocols": ["HTTP", "HTTPS"],
  "os": "Ubuntu 22.04",
  "consolidated": true,
  "sourceRecordIds": ["id1", "id2", "id3"]
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/hosts/merge \
  -H "Content-Type: application/json" \
  -d '{"hostIds": ["id1", "id2", "id3"]}'
```

### Interactive Documentation

Visit **Swagger UI** for interactive API testing:
```
http://localhost:8080/swagger-ui.html
```

## 🎯 Similarity Algorithm

The API uses a **simple point-based scoring system** to detect similar hosts:

### Scoring Rules

| Property Match | Points | Logic |
|---------------|--------|-------|
| **IP Address** | 40 points | Same IP = definitely same host |
| **Any Name** | 30 points | Any shared domain name |
| **Any Role** | 15 points | Any shared role |
| **Any Protocol** | 10 points | Any shared protocol |
| **OS Match** | 5 points | Same OS family |

**Maximum Score:** 100 points  
**Threshold:** 50 points (hosts scoring 50+ are grouped together)

### Examples

**Example 1: High Similarity (70 points)**
```
Host A: IP=192.168.1.10, Name=web.example.com
Host B: IP=192.168.1.10, Name=web.example.com
Score: 40 (IP) + 30 (Name) = 70 ✅ Grouped
```

**Example 2: Low Similarity (40 points)**
```
Host A: IP=192.168.1.10, Name=web.example.com
Host B: IP=192.168.1.10, Name=api.example.com
Score: 40 (IP) = 40 ❌ Not Grouped
```

### Customization

Edit `HostSimilarityService.java`:
```java
private static final int IP_MATCH_SCORE = 40;      // Change points
private static final int NAME_MATCH_SCORE = 30;    // Change points
private static final int SIMILARITY_THRESHOLD = 50; // Change threshold
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test
```bash
mvn test -Dtest=HostServiceTest
mvn test -Dtest=HostSimilarityServiceTest
```

### Test Coverage
```bash
mvn test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

### Test Structure

```
src/test/java/com/hostmanagement/
├── service/
│   ├── HostServiceTest.java           # Business logic tests
│   └── HostSimilarityServiceTest.java # Algorithm tests
```

**Coverage:**
- Service Layer: 85%+
- Similarity Algorithm: 90%+

## 🐳 Docker

### Build Docker Image

```bash
docker build -t host-management-api .
```

### Run with Docker

```bash
docker run -p 8080:8080 host-management-api
```

## 📁 Project Structure

```
host-management-api/
├── src/
│   ├── main/
│   │   ├── java/com/hostmanagement/
│   │   │   ├── HostManagementApplication.java    # Main class
│   │   │   ├── controller/
│   │   │   │   └── HostController.java           # REST endpoints (3)
│   │   │   ├── service/
│   │   │   │   ├── HostService.java              # Business logic
│   │   │   │   └── HostSimilarityService.java    # Similarity algorithm
│   │   │   ├── repository/
│   │   │   │   └── HostRepository.java           # In-memory storage
│   │   │   ├── model/
│   │   │   │   └── Host.java                     # Entity
│   │   │   └── dto/
│   │   │       ├── HostGroup.java                # Group DTO
│   │   │       └── MergeRequest.java             # Merge request DTO
│   │   └── resources/
│   │       └── application.yml                    # Configuration
│   └── test/
│       └── java/com/hostmanagement/
│           └── service/                           # Unit tests
├── Dockerfile                                      # Docker configuration
├── pom.xml                                         # Maven dependencies
└── README.md                                       # This file
```

## ⚙️ Configuration

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: host-management-api

logging:
  level:
    com.hostmanagement: INFO
```

### Change Port

Edit `application.yml`:
```yaml
server:
  port: 9090  # Your custom port
```

Or use environment variable:
```bash
SERVER_PORT=9090 mvn spring-boot:run
```

## 📊 Sample Data

The application initializes with sample host records:

| Group | IP Address | Names | Records | Status |
|-------|-----------|-------|---------|--------|
| Web Servers | 192.168.1.10 | web01, www, api | 3 | Pending |
| Database Servers | 192.168.1.20 | db01, database | 2 | Pending |
| Mail Server | 192.168.1.30 | mail, smtp | 1 | Pending |
| DNS Server | 192.168.1.40 | dns01 | 1 | Pending |
| App Server | 192.168.1.100 | app01 | 1 | Consolidated |

## 🔧 Development

### IDE Setup

#### IntelliJ IDEA
1. Open project
2. Maven will auto-import dependencies
3. Install Lombok plugin
4. Enable annotation processing

#### Eclipse
1. Import as Maven project
2. Install Lombok
3. Project → Clean

### Hot Reload

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

## 🚀 Deployment

### Production Build

```bash
mvn clean package -DskipTests
```

Output: `target/host-management-api-1.0.0.jar`

### Run Production JAR

```bash
java -jar target/host-management-api-1.0.0.jar
```

### Environment Variables

```bash
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod
java -jar target/host-management-api-1.0.0.jar
```

## 🔗 Related Repositories

- **Frontend:** [host-management-ui](https://github.com/YOUR_USERNAME/host-management-ui) - React + TypeScript + Material-UI interface

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- Your Name - [GitHub Profile](https://github.com/Adit2404)

**Built with ❤️ using Spring Boot**
