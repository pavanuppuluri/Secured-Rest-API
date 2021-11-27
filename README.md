**Secured Application**

This application expose REST API that allows users registration.

In this I implemented 2 roles - USER, ADMIN.

- **User** can perform CRUD on his user data
- **Admin** can perform CRUD on all users

This repo covers -

- API security 
- Input validations
- User CRUD
- User Registration
- User Login
- User Logout
- Unit tests
- Flyway database scripts
- Jasypt secured properties

**APIs -**

| Feature | Method | Endpoint| Required Role  |Sample Payload | 
| ------ | ------ | ------ | ------ | ------ |
| User Registration | POST | **_/user/register_** | All|{"username":"lasyapriya","password":"welcome123", "firstname":"Las","lastname":"Pri","email":"las@test.com"} |
| Login | POST | **_/user/login_** | All|{"username":"lasyapriya","password":"welcome123"} |
| Get User Data | GET | **_/user_** | USER/ADMIN |username=lasyapriya| 
| Update User Data | PUT | **_/user/update_** | USER/ADMIN |{"username":"lasyapriya", "firstname":"Las","lastname":"Pri","email":"las.u@test.com"} |
| Delete User Data | DELETE | **_/user_** | USER/ADMIN |username=lasyapriya| 
| Get All Users | GET | **_/user/getallusers_** | ADMIN || 
| Logout | DELETE | **_/logout_** | USER/ADMIN |username=lasyapriya| 


**Note**
- Except first 2 endpoints (user registration/login) all the other endpoints require authentication
- To access these endpoints we need to pass JWT token
- When a user with USER role login, above endpoints allows USER to perform operations on their owned records only
- When a user with ADMIN role login, above endpoints allows ADMIN to perform operations on all the records
- Get All Users endpoint can be accessed by ADMIN only


Thank You.

