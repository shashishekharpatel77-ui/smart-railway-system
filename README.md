# Smart Railway System 🚆

A web-based railway reservation application developed using Java, Spring Boot, MySQL, Thymeleaf, and Maven.

The application demonstrates railway reservation workflows, including train search, ticket booking, booking history, cancellation, waiting-list management, and route exploration using graph algorithms.

## 1. Project Overview

- **Project Title:** Smart Railway System
- **Project Type:** Academic Web Application
- **Technology Stack:** Java, Spring Boot, Spring MVC, Thymeleaf, MySQL, JDBC/JdbcTemplate, Maven, HTML, and CSS

### Team Members

1. Shashi Shekhar Patel
2. Leena Chandrakar
3. Vani Chandra
4. Arjun Saha
5. Rahul Pandey

### Project Objective

The objective of the Smart Railway System is to provide a web-based application for railway-related operations, including train search, ticket booking, booking-history management, cancellation, waiting-list management, and route exploration.

The project also demonstrates the application of graph algorithms for exploring routes between railway stations.

## 2. Problem Statement

Railway reservation workflows involve several operations, including finding suitable trains, recording passenger bookings, checking booking status, handling cancellations, and managing waiting lists.

The Smart Railway System demonstrates how these operations can be organized within a single web application. It combines a Java-based backend, database operations, and a web interface to support railway reservation workflows.

The project also explores how graph algorithms can be applied to route exploration between railway stations.

## 3. Features

- **Train Search:** Search for trains and view matching results.
- **Ticket Booking:** Enter passenger details through a booking form.
- **Booking Confirmation:** View booking results and status.
- **Booking History:** View existing booking records.
- **Ticket Cancellation:** Cancel eligible confirmed bookings.
- **Waiting-List Management:** View waiting-list status and waiting numbers.
- **Waiting-List Promotion:** Handle eligible waiting-list promotions when a seat becomes available.
- **Route Exploration:** Explore routes using BFS and Dijkstra algorithms.
- **Web Interface:** Access application pages through Thymeleaf templates and CSS.

## 4. Technology Stack

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| Spring Boot | Application framework and configuration |
| Spring MVC | Web request handling |
| Thymeleaf | Server-side HTML rendering |
| HTML and CSS | User interface |
| MySQL | Database |
| JDBC / JdbcTemplate | Database operations |
| Maven | Build and dependency management |
| Git and GitHub | Version control and source code hosting |

## 5. Project Structure

The repository contains the following main files and folders:

```text
smart-railway-system/
├── .mvn/
├── src/
├── screenshots/
├── .gitattributes
├── .gitignore
├── README.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

- **src/** contains the application's source code and resources.
- **screenshots/** contains the project's output screenshots.
- **.mvn/** contains Maven Wrapper support files.
- **mvnw and mvnw.cmd** are scripts for running Maven commands.
- **pom.xml** contains project dependencies and build configuration.
- **.gitignore** specifies files and folders that Git should exclude.
- **README.md** contains project documentation.

## 6. Installation and Execution

### Prerequisites

- Java JDK compatible with the project
- MySQL Server
- Git

The project includes a Maven Wrapper, so a separate Maven installation may not be required.

### Step 1: Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/shashishekharpatel77-ui/smart-railway-system.git
```

Navigate to the repository directory:

```bash
cd smart-railway-system
```

### Step 2: Create the Database

Start MySQL and create the database:

```sql
CREATE DATABASE railway_db;
```

Configure the required tables and initial train or station data according to the project's implementation.

### Step 3: Configure the Database Connection

Configure the local file:

`src/main/resources/application.properties`

Example configuration:

```properties
spring.application.name=smartrailway
spring.datasource.url=jdbc:mysql://localhost:3306/railway_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.thymeleaf.cache=false
```

Replace the username and password placeholders with your own MySQL credentials.

**Security:** Do not commit real database passwords or other secret credentials to GitHub. Keep local credentials excluded from version control.

### Step 4: Run the Application

Open a terminal in the directory containing `pom.xml`.

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

```bash
./mvnw spring-boot:run
```

Wait for Spring Boot to start successfully.

### Step 5: Open the Application

Open the following address in your browser:

http://localhost:8080

Ensure that MySQL is running and the required database tables and data are configured before starting the application.

## 7. DSA Concepts Used

The project identifies graph algorithms for railway route exploration.

### 7.1 Graphs

A graph consists of vertices and edges. In a railway route model, stations can be represented as vertices and connections between stations as edges.

### 7.2 Breadth-First Search (BFS)

Breadth-First Search explores a graph level by level. In an unweighted graph, BFS can find a path with the minimum number of edges between a starting vertex and a reachable destination.

### 7.3 Dijkstra's Algorithm

Dijkstra's algorithm finds shortest paths from a starting vertex in a graph with non-negative edge weights. Depending on the implementation, edge weights can represent distance, travel time, or another cost.

**Note:** BFS and Dijkstra are the route-exploration algorithms identified for this project. Their exact implementation and purpose should match the source code. Add other DSA topics only if they are actually used.

## 8. Learning Outcomes

Developing the Smart Railway System provides opportunities to learn and practice:

- Developing a web application using Java and Spring Boot.
- Understanding the Model-View-Controller (MVC) architecture.
- Creating web pages using Thymeleaf, HTML, and CSS.
- Connecting a Java application to a MySQL database using JDBC/JdbcTemplate.
- Organizing application code into controllers, models, repositories, and services.
- Understanding railway reservation workflows, including booking, cancellation, and booking-history management.
- Understanding graph representation and graph traversal.
- Learning how BFS and Dijkstra's algorithm can be applied to route-exploration problems.
- Managing source code using Git and GitHub.
- Running a Spring Boot application using the Maven Wrapper.

## 9. Future Scope

The following improvements may be considered in future versions:

- Deployment to a cloud hosting platform.
- User registration and authentication.
- Improved passenger and booking management.
- Enhanced train search and filtering.
- More detailed route information.
- Improved validation and error handling.
- A more responsive and user-friendly interface.
- Additional railway reservation capabilities.

These are potential enhancements and are not claims about currently implemented functionality.

## 10. References

The following official documentation resources are relevant to the technologies used in the project:

1. **Java Documentation:** https://docs.oracle.com/en/java/
2. **Spring Boot Documentation:** https://docs.spring.io/spring-boot/
3. **Spring Framework Documentation:** https://docs.spring.io/spring-framework/reference/
4. **Thymeleaf Documentation:** https://www.thymeleaf.org/documentation.html
5. **MySQL Documentation:** https://dev.mysql.com/doc/
6. **Apache Maven Documentation:** https://maven.apache.org/guides/
7. **Git Documentation:** https://git-scm.com/doc
8. **GitHub Documentation:** https://docs.github.com/

Add any additional tutorials, articles, APIs, or learning resources that were actually used while developing the project.

## 11. License

No license has been specified for this project.

A license may be added after the project owner or team decides which license applies. Do not claim that the project is released under MIT or another license unless that decision has been made.

---

**Project Name:** Smart Railway System

**Project Type:** Academic Web Application

**Primary Technologies:** Java, Spring Boot, MySQL, Thymeleaf, and Maven  
