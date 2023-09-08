# Healthy Lifestyle App

[REST API Endpoints](#rest-api-endpoints)<br>
[Database Schema](#database-schema)<br>
[Deployment Schema](#deployment-schema)<br>

## REST API Endpoints

### User Role

#### Users & Auth
Signup (create user): POST /api/v1/signup<br>
Login: POST /api/v1/login<br>
Is authenticated (validate token): GET /api/v1/authenticate (JWT)<br>
Get user details: GET /api/v1/users/{user_id} (JWT)<br>
Update user: PATCH /api/v1/users/{user_id} (JWT)<br>
Delete user: DELETE /api/v1/users/{user_id} (JWT)<br>

#### Body Parts
List all body parts: GET /api/v1/workouts/bodyParts<br>

#### Media References
List template references: GET /api/v1/workouts/httpRefs/templates<br>
List all references or custom only: GET /api/v1/workouts/httpRefs?isCustomOnly= (JWT)<br>
Create custom reference: POST /api/v1/workouts/httpRefs (JWT)<br>
Update custom reference: PATCH /api/v1/workouts/httpRefs/{httpRef_id} (JWT)<br>
Delete custom reference: DELETE /api/v1/workouts/httpRefs/{httpRef_id} (JWT)<br>

#### Exercises
List template exercises: GET /api/v1/workouts/exercises/templates<br>
List all exercises or custom only: GET /api/v1/workouts/exercises?isCustomOnly= (JWT)<br>
Create custom exercise: POST /api/v1/workouts/exercises (JWT)<br>
Associate exercise as completed:  POST /api/v1/workouts/exercises/{exercise_id}/{user_id} (JWT)<br>
Update custom exercise: PATCH /api/v1/workouts/exercises/{exercise_id} (JWT)<br>
Delete custom exercise: DELETE /api/v1/workouts/exercises/{exercise_id} (JWT)<br>

#### Workouts
List template workouts: GET /api/v1/workouts/templates<br>
List all workouts or custom only: GET /api/v1/workouts?isCustomOnly= (JWT)<br>
Create custom workout: POST /api/v1/workouts (JWT)<br>
Update custom workout: PATCH /api/v1/workouts/{workout_id} (JWT)<br>
Delete custom workout: DELETE /api/v1/workouts/{workout_id} (JWT)<br>

#### Nutrition Recipes
List template recipes: GET /api/v1/nutrition/recipes/templates<br>
List all recipes or custom only: GET /api/v1/nutrition/recipes?isCustomOnly= (JWT)<br>
Create custom recipe: POST /api/v1/nutrition/recipes (JWT)<br>
Associate recipe (dish) as completed:  POST /api/v1/nutrition/recipes/{recipe_id}/{user_id} (JWT)<br>
Update custom recipe: PATCH /api/v1/nutrition/recipes/{recipet_id} (JWT)<br>
Delete custom recipe: DELETE /api/v1/nutrition/recipes/{recipe_id} (JWT)<br>

#### Nutrition Supplements
List template supplements: GET /api/v1/nutrition/supplements/templates<br>
List all supplements or custom only: GET /api/v1/nutrition/supplements?isCustomOnly= (JWT)<br>
Create custom supplement: POST /api/v1/nutrition/supplements (JWT)<br>
Associate supplement intake as completed:  POST /api/v1/nutrition/supplements/{supplement_id}/{user_id} (JWT)<br>
Update custom supplement: PATCH /api/v1/nutrition/supplements/{supplement_id} (JWT)<br>
Delete custom supplement: DELETE /api/v1/nutrition/supplements/{supplement_id} (JWT)<br>

#### Meditations
List template meditations: GET /api/v1/meditations/templates<br>
List all meditations or custom only: GET /api/v1/meditations?isCustomOnly= (JWT)<br>
Create custom meditation: POST /api/v1/meditations (JWT)<br>
Associate meditation as completed:  POST /api/v1/meditations/{meditation_id}/{user_id} (JWT)<br>
Update custom meditation: PATCH /api/v1/meditations/{meditation_id} (JWT)<br>
Delete custom meditation: DELETE /api/v1/meditations/{meditation_id} (JWT)<br>

#### Calendar
List user’s reminders: GET /api/v1/calendar/{user_id}?startDate=&endDate=&eventType=&eventStatus (JWT)<br>
Create reminder: POST /api/v1/calendar (JWT)<br>
Update reminder: PATCH /api/v1/calendar/{reminder_id} (JWT)<br>
Delete reminder: DELETE  /api/v1/calendar/{reminder_id} (JWT)<br>

#### Chats
List all user’s chats: GET /api/v1/chats/{user_id} (JWT)<br>
Get chat by id: GET /api/v1/chats/{chat_id} (JWT)<br>
Create chat: POST /api/v1/chats (JWT)<br>
Update chat (title only): PATCH /api/v1/chats/{chat_id} (JWT)<br>
Delete chat: DELETE /api/v1/chats/{chat_id} (JWT)<br>

#### Chats Messages
List all chat’s messages GET /api/v1/chats/{chat_id}/messages (JWT)<br>
Create message  POST /api/v1/chats/{chat_id}/messages (JWT)<br>

[Top](#healthy-lifestyle-app)

### Admin Role (Admin Panel)

#### Manage Users

#### Manage Template Body Parts

#### Manage Template Media References

#### Manage Template Exercises

#### Manage Template Workouts

#### Manage Template Recipes

#### Manage Template Supplements

#### Manage Template Meditations

#### Manage Chats

[Top](#healthy-lifestyle-app)

## Database Schema

### Users
![Users](docs/users.jpg)
<br>
[Top](#healthy-lifestyle-app)

### Workouts
![Workouts](docs/workouts.jpg)
<br>
[Top](#healthy-lifestyle-app)

### Nutrition
![Nutrition](docs/nutrition.jpg)
<br>
[Top](#healthy-lifestyle-app)

### Meditations
![Meditations](docs/meditations.jpg)
<br>
[Top](#healthy-lifestyle-app)

### Calendar
![Calendar](docs/calendar.jpg)
<br>
[Top](#healthy-lifestyle-app)

### Chats
![Chats](docs/chats.jpg)
<br>
[Top](#healthy-lifestyle-app)

## Deployment Schema
![Deployment](docs/deployment.jpg)
<br>
[Top](#healthy-lifestyle-app)
