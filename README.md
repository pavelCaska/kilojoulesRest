# Kilojoules Stream Backend Application

## Technologies Used
- **Spring Boot:** Web framework
- **MySQL and H2:** Database Systems
- **Hibernate:** Object-Relational Mapping (ORM) Framework
- **Bean Validation:** For validation rules definition
- **Spring Security + JWT:** For secure access to your application. JWT tokens are issued upon successful login and stored in the client side. Every subsequent request requires this token.  
- **JUnit:** for the testing framework
- **Lombok:** To avoid boilerplate code

## Objective
This application is designed to be a RESTful API for tracking nutrition intake and providing statistical analysis. This backend API provides resources that would be needed by a frontend designed similar to the Thymeleaf frontend in the previous version of the Kilojoules app, from which the Thymeleaf part has now been separated.

## Current Development State:

- Populate database with sample data
- Create, update, delete, list foods
- Create, delete portions
- Create, update, delete, list meals
- Pagination for food list, meal list
- Food, Portion, Meal
  - Unit tests for repository and service layer
  - Integration tests for controller layer 
- Journal
  - add, update, delete food or meal to/from Journal
  - basic & advanced statistics
- User management
  - UserProfile - create, update, read
  - User - register, delete

## Next Steps:

- Unit tests for repository, service & control layer (journal-related entities)
- Integration testing for Journal
