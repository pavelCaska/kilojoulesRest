# Kilojoules Stream Backend Application

## Technologies Used
- Spring Boot: Web framework
- MySQL and H2: Database Systems
- Hibernate: Object-Relational Mapping (ORM) Framework
- Bean Validation: For validation rules definition
- Spring Security: For secure access to your application
- JWT token

## Objective
This application is designed to be a RESTful API for tracking nutrition intake and providing statistical analysis. This backend API provides resources that would be needed by a frontend designed similar to the Thymeleaf frontend in the previous version of the Kilojoules app, from which the Thymeleaf part has now been separated.

## Current Development State:

- Populate database with sample data
- Create, update, delete, list foods
- Create, delete portions
- Create, update, delete, list meals
- Pagination for food list, meal list
- Entities, models and services for Journal
- Unit tests for repository layer

## Next Steps:

- Create rest controllers (journal and statistics)
- Adjust related models and services

- User management
- Unit tests for service & control layer (user, food, meal, portions)
- Unit tests for repository, service & control layer (journal-related entities)
- Integration testing