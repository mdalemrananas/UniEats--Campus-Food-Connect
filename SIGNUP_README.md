# UniEats Signup System

This document describes the signup system implementation for the UniEats application.

## Features

- **User Registration**: Complete user registration with form validation
- **SQLite Database**: Persistent storage using SQLite database
- **Password Security**: SHA-256 password hashing for security
- **Form Validation**: Real-time validation with visual feedback
- **Mobile-First Design**: Responsive UI designed for mobile devices

## Components

### 1. User Model (`User.java`)
- Represents user data with fields: id, username, email, password, fullName, phoneNumber, createdAt, updatedAt
- Includes constructors and getter/setter methods

### 2. Database Manager (`DatabaseManager.java`)
- Singleton class managing SQLite database operations
- Handles CRUD operations for users
- Includes methods for checking duplicate usernames and emails
- Automatically creates database and tables on first run

### 3. Signup Controller (`SignupController.java`)
- Manages the signup form logic
- Implements real-time form validation
- Handles user creation and database operations
- Provides navigation between pages

### 4. Password Utility (`PasswordUtil.java`)
- Implements SHA-256 password hashing
- Includes methods for password verification
- Provides salt generation for future enhancements

### 5. UI Components
- **Home Page** (`home.fxml`): Main landing page with signup button
- **Signup Page** (`signup.fxml`): Complete registration form

## Database Schema

```sql
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
```

## Usage

### Running the Application
1. Ensure you have Java 17+ and Maven installed
2. Run `mvn clean install` to build the project
3. Run `mvn javafx:run` to start the application

### User Registration Flow
1. Click "Sign Up" button on the home page
2. Fill out the registration form:
   - **Full Name** (required): User's full name
   - **Username** (required): Unique username (min 3 characters)
   - **Email** (required): Valid email address
   - **Phone Number** (optional): User's phone number
   - **Password** (required): Password (min 6 characters)
   - **Confirm Password** (required): Password confirmation
3. Click "Create Account" to register
4. Success message will appear and redirect to home page

### Form Validation
- **Real-time validation**: Fields show red borders for invalid input
- **Username**: Minimum 3 characters, must be unique
- **Email**: Must be valid email format, must be unique
- **Password**: Minimum 6 characters
- **Confirm Password**: Must match password field
- **Full Name**: Required field

## Security Features

- **Password Hashing**: All passwords are hashed using SHA-256 before storage
- **Input Validation**: Comprehensive client-side and server-side validation
- **SQL Injection Prevention**: Uses prepared statements for all database queries
- **Unique Constraints**: Database enforces unique usernames and emails

## Testing

Run the test suite to verify database functionality:
```bash
mvn test
```

Tests cover:
- User creation
- Duplicate username/email prevention
- User updates
- User deletion
- Data integrity

## File Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/unieats/
│   │       ├── User.java
│   │       ├── DatabaseManager.java
│   │       ├── UniEatsApp.java
│   │       ├── controllers/
│   │       │   ├── HomeController.java
│   │       │   └── SignupController.java
│   │       └── util/
│   │           └── PasswordUtil.java
│   └── resources/
│       └── fxml/
│           ├── home.fxml
│           └── signup.fxml
└── test/
    └── java/
        └── com/unieats/
            └── DatabaseTest.java
```

## Dependencies

- **JavaFX**: UI framework
- **SQLite JDBC**: Database driver
- **JUnit 5**: Testing framework

## Future Enhancements

- **Email Verification**: Send confirmation emails
- **Password Reset**: Forgot password functionality
- **Social Login**: Google, Facebook integration
- **Profile Management**: User profile editing
- **Enhanced Security**: Salt-based password hashing, JWT tokens

## Troubleshooting

### Common Issues

1. **Database Connection Error**: Ensure SQLite JDBC dependency is included
2. **Module Access Error**: Check module-info.java includes `requires java.sql`
3. **FXML Loading Error**: Verify file paths in FXML loaders
4. **Validation Issues**: Check field IDs match between FXML and Controller

### Database File Location
The SQLite database file (`unieats.db`) is created in the project root directory when the application first runs.

## Support

For issues or questions regarding the signup system, check the console output for error messages and ensure all dependencies are properly configured. 