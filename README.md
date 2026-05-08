# CoupleApp Backend

A comprehensive relationship management platform built with Spring Boot, designed to help couples organize their shared life together.

## Features

- **Shared Calendar** - Coordinate events, dates, and important milestones
- **Task Management** - Organize household chores and responsibilities
- **Finance Tracking** - Manage shared expenses and financial goals
- **Activity Suggestions** - Get personalized date and activity recommendations
- **Wishlist** - Share and track desired experiences and items
- **Secure Authentication** - JWT-based user authentication and couple pairing

## Tech Stack

- **Java 21** - Modern Java features and performance
- **Spring Boot 3.2.4** - Enterprise-grade framework
- **PostgreSQL** - Reliable relational database (Neon)
- **Spring Security** - JWT authentication
- **Hibernate/JPA** - ORM for database interactions
- **Maven** - Dependency management and build tool

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database (or Neon account)
- Google Places API key (for location-based suggestions)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd coupleapp-backend
```

### 2. Configure Environment Variables

Copy `.env.example` to `.env` and update with your credentials:

```bash
cp .env.example .env
```

Edit `.env` with your database and API credentials:

```properties
DATABASE_URL=jdbc:postgresql://your-host/your_database
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
JWT_SECRET=your_secure_random_jwt_secret
GOOGLE_PLACES_API_KEY=your_google_api_key
```

### 3. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info

### Couple Management
- `POST /api/couples` - Create/join couple
- `GET /api/couples/my-couple` - Get couple details
- `PUT /api/couples/{id}` - Update couple information

### Tasks
- `GET /api/tasks` - List all tasks
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### Calendar
- `GET /api/calendar/events` - List events
- `POST /api/calendar/events` - Create event
- `PUT /api/calendar/events/{id}` - Update event
- `DELETE /api/calendar/events/{id}` - Delete event

### Finance
- `GET /api/finance/transactions` - List transactions
- `POST /api/finance/transactions` - Add transaction
- `GET /api/finance/goals` - List financial goals
- `POST /api/finance/goals` - Create goal

### Suggestions
- `POST /api/suggestions` - Get activity suggestions
- `GET /api/suggestions/history` - View suggestion history

### Wishlist
- `GET /api/wishlist` - List wishlist items
- `POST /api/wishlist` - Add wishlist item
- `PUT /api/wishlist/{id}` - Update item
- `DELETE /api/wishlist/{id}` - Remove item

## Database Schema

The application uses the following main entities:

- **User** - User accounts with authentication
- **Couple** - Relationship pairing between users
- **Task** - Shared to-do items
- **CalendarEvent** - Scheduled events and dates
- **FinanceTransaction** - Expense tracking
- **FinanceGoal** - Savings and financial targets
- **SharedActivity** - Activity history
- **WishlistItem** - Desired items and experiences
- **SuggestionPreferences** - User preferences for recommendations

## Configuration

Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Google Places API
app.google.places.api-key=${GOOGLE_PLACES_API_KEY}
```

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -DskipTests
```

The JAR file will be created in `target/coupleapp-backend-0.0.1-SNAPSHOT.jar`

### Docker Deployment

Build and run with Docker:

```bash
docker build -t coupleapp-backend .
docker run -p 8080:8080 --env-file .env coupleapp-backend
```

## Project Structure

```
src/
├── main/
│   ├── java/com/coupleapp/
│   │   ├── controller/     # REST API endpoints
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Database access
│   │   ├── entity/         # JPA entities
│   │   ├── dto/            # Data transfer objects
│   │   ├── security/       # Authentication & authorization
│   │   └── exception/      # Custom exceptions
│   └── resources/
│       └── application.properties  # Configuration
└── test/                   # Unit and integration tests
```

## Security

- All endpoints except `/api/auth/*` require JWT authentication
- Passwords are encrypted using BCrypt
- JWT tokens expire after 24 hours (configurable)
- HTTPS recommended for production deployments

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please open an issue on GitHub.
