# MyHalalEco Backend

A Spring Boot backend service for the MyHalalEco application running on port 5000.

## Prerequisites

- Java 17 or higher (JDK & JRE)
- Maven 3.6 or higher

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/myhalal/eco/
│   │   │       ├── Application.java          # Main application class
│   │   │       ├── config/
│   │   │       │   └── CorsConfig.java       # CORS configuration
│   │   │       └── controller/
│   │   │           └── HomeController.java   # REST endpoints
│   │   └── resources/
│   │       └── application.properties        # Application configuration
├── pom.xml                                   # Maven configuration
└── README.md
```

## Getting Started

### 1. Install Dependencies

```bash
cd backend
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on port 5000.

### 3. Verify the Setup

Once the application is running, you can test the endpoints:

- **Health Check**: `GET http://localhost:5000/api/health`
- **Home**: `GET http://localhost:5000/api/`
- **Info**: `GET http://localhost:5000/api/info`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/`  | Welcome message and basic info |
| GET    | `/api/health` | Health check endpoint |
| GET    | `/api/info` | Application information |

## Configuration

The application is configured to:
- Run on port 5000
- Accept CORS requests from `http://localhost:3000` (frontend)
- Use `/api` as the context path
- Enable debug logging for web requests

## Development

### Hot Reload

The application includes Spring Boot DevTools for automatic restart during development.

### Building for Production

```bash
mvn clean package
java -jar target/eco-backend-1.0.0.jar
```

## Technology Stack

- **Java 17**: Programming language
- **Spring Boot 3.2.0**: Framework
- **Maven**: Build tool
- **Spring Web**: REST API development
- **Spring DevTools**: Development utilities
