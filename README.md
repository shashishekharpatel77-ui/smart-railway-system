# Smart Railway System 🚆

A web-based railway reservation application developed using Java, Spring Boot, MySQL, Thymeleaf, and Maven.

The project demonstrates railway reservation workflows, including train search, ticket booking, booking history, cancellation, waiting-list management, and route exploration.

## Features

* **Train Search:** Search for trains and view matching results.
* **Ticket Booking:** Submit passenger details through a booking form.
* **Booking Confirmation:** View booking results and status.
* **Booking History:** View existing booking records.
* **Ticket Cancellation:** Cancel eligible confirmed bookings.
* **Waiting-List Management:** View waiting-list status and waiting numbers.
* **Waiting-List Promotion:** Handle eligible waiting-list promotions when a seat becomes available.
* **Route Exploration:** Explore routes using BFS and Dijkstra algorithms.
* **Web Interface:** Access application pages through Thymeleaf templates and CSS.

## Technology Stack

| Technology          | Purpose                                 |
| ------------------- | --------------------------------------- |
| Java                | Backend programming                     |
| Spring Boot         | Application framework                   |
| Spring MVC          | Web request handling                    |
| Thymeleaf           | Server-side HTML rendering              |
| HTML and CSS        | User interface                          |
| MySQL               | Database                                |
| JDBC / JdbcTemplate | Database operations                     |
| Maven               | Build and dependency management         |
| Git and GitHub      | Version control and source code hosting |

## Project Structure

```text
smartrailway/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/railway/smartrailway/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── SmartrailwayApplication.java
│   │   └── resources/
│   │       ├── static/css/
│   │       └── templates/
│   └── test/
├── screenshots/
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Team Members

1. Shashi Shekhar Patel
2. Leena Chandrakar
3. Vani Chandra
4. Arjun Saha
5. Rahul Pandey

## Prerequisites

* Java JDK compatible with the project
* MySQL Server
* Git

The project includes a Maven Wrapper, so a separate Maven installation may not be required.

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/shashishekharpatel77-ui/smart-railway-system.git
```

Navigate to the project directory:

```bash
cd smart-railway-system/smartrailway
```

### 2. Create the Database

Create the database in MySQL:

```sql
CREATE DATABASE railway_db;
```

The required tables and initial train or station data must also be configured according to the project's implementation.

### 3. Configure the Database Connection

Create or configure the local file:

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

**Security:** Do not commit real database passwords or other secret credentials to GitHub. Keep your local `application.properties` excluded from version control.

## Running the Application

Open a terminal in the directory containing `pom.xml`.

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

```bash
./mvnw spring-boot:run
```

After Spring Boot starts successfully, open:

http://localhost:8080

Ensure that MySQL is running and the required database tables and data are configured before starting the application.

## Future Enhancements

Potential improvements include:

* Deployment to a cloud hosting platform
* Additional railway reservation capabilities
* Further user-interface improvements

These are possible future enhancements, not claims about currently implemented functionality.

---

**Project Type:** Academic Web Application
**Primary Technologies:** Java, Spring Boot, MySQL, Thymeleaf
