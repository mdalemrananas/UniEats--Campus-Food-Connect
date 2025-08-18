# UniEats - University Canteen System

A modern JavaFX application for managing a university canteen, featuring a user-friendly interface for students and staff to view menus, place orders, and manage meal plans.

## Features

- Modern, responsive UI with JavaFX
- View daily menus and meal options
- Order food for pickup or delivery
- Manage meal plans and balances
- User authentication system
- Admin dashboard for canteen management

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- JavaFX 17 or higher (included in the dependencies)

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/UniEats.git
   cd UniEats
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

## Project Structure

```
src/main/java/com/unieats/
├── UniEatsApp.java          # Main application class
├── controllers/             # FXML controllers
│   └── HomeController.java
├── models/                  # Data models
└── services/                # Business logic and services

src/main/resources/
├── fxml/                   # FXML view files
│   └── home.fxml
├── css/                    # Stylesheets
│   └── styles.css
└── images/                 # Application images and icons
```

## Dependencies

- JavaFX 21.0.6
- ControlsFX 11.2.1
- JUnit 5.12.1 (for testing)
- FormsFX 11.6.0 (for form handling)
- Ikonli 12.3.1 (for icons)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
