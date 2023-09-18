# Healthy Lifestyle App

[REST API Endpoints](#rest-api-endpoints)<br>
[Database Schema](#database-schema)<br>
[Deployment Schema](#deployment-schema)<br>

## REST API Endpoints

### User Role

#### Users & Auth
Signup (create user): POST /api/v1/signup<br>
Login: POST /api/v1/login<br>
Is authenticated (validate token): GET /api/v1/authenticate (ROLE_USER)<br>
Get user details: GET /api/v1/users/{user_id} (ROLE_USER)<br>
Update user: PATCH /api/v1/users/{user_id} (ROLE_USER)<br>
Delete user: DELETE /api/v1/users/{user_id} (ROLE_USER)<br>

#### Body Parts
List all body parts: GET /api/v1/workouts/bodyParts<br>

#### Media References
List template references: GET /api/v1/workouts/httpRefs/templates<br>
List all references or custom only: GET /api/v1/workouts/httpRefs?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom reference: POST /api/v1/workouts/httpRefs (ROLE_USER_PRO)<br>
Update custom reference: PATCH /api/v1/workouts/httpRefs/{httpRef_id} (ROLE_USER_PRO)<br>
Delete custom reference: DELETE /api/v1/workouts/httpRefs/{httpRef_id} (ROLE_USER_PRO)<br>

#### Exercises
List template exercises: GET /api/v1/workouts/exercises/templates<br>
List all exercises or custom only: GET /api/v1/workouts/exercises?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom exercise: POST /api/v1/workouts/exercises (ROLE_USER_PRO)<br>
Associate exercise as completed:  POST /api/v1/workouts/exercises/{exercise_id}/{user_id} (ROLE_USER)<br>
Update custom exercise: PATCH /api/v1/workouts/exercises/{exercise_id} (ROLE_USER_PRO)<br>
Delete custom exercise: DELETE /api/v1/workouts/exercises/{exercise_id} (ROLE_USER_PRO)<br>

#### Workouts
List template workouts: GET /api/v1/workouts/templates<br>
List all workouts or custom only: GET /api/v1/workouts?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom workout: POST /api/v1/workouts (ROLE_USER_PRO)<br>
Update custom workout: PATCH /api/v1/workouts/{workout_id} (ROLE_USER_PRO)<br>
Delete custom workout: DELETE /api/v1/workouts/{workout_id} (ROLE_USER_PRO)<br>

#### Nutrition Recipes
List template recipes: GET /api/v1/nutrition/recipes/templates<br>
List all recipes or custom only: GET /api/v1/nutrition/recipes?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom recipe: POST /api/v1/nutrition/recipes (ROLE_USER_PRO)<br>
Associate recipe (dish) as completed:  POST /api/v1/nutrition/recipes/{recipe_id}/{user_id} (ROLE_USER)<br>
Update custom recipe: PATCH /api/v1/nutrition/recipes/{recipet_id} (ROLE_USER_PRO)<br>
Delete custom recipe: DELETE /api/v1/nutrition/recipes/{recipe_id} (ROLE_USER_PRO)<br>

#### Nutrition Supplements
List template supplements: GET /api/v1/nutrition/supplements/templates<br>
List all supplements or custom only: GET /api/v1/nutrition/supplements?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom supplement: POST /api/v1/nutrition/supplements (ROLE_USER_PRO)<br>
Associate supplement intake as completed:  POST /api/v1/nutrition/supplements/{supplement_id}/{user_id} (ROLE_USER)<br>
Update custom supplement: PATCH /api/v1/nutrition/supplements/{supplement_id} (ROLE_USER_PRO)<br>
Delete custom supplement: DELETE /api/v1/nutrition/supplements/{supplement_id} (ROLE_USER_PRO)<br>

#### Meditations
List template meditations: GET /api/v1/meditations/templates<br>
List all meditations or custom only: GET /api/v1/meditations?isCustomOnly= (ROLE_USER_PRO)<br>
Create custom meditation: POST /api/v1/meditations (ROLE_USER_PRO)<br>
Associate meditation as completed:  POST /api/v1/meditations/{meditation_id}/{user_id} (ROLE_USER)<br>
Update custom meditation: PATCH /api/v1/meditations/{meditation_id} (ROLE_USER_PRO)<br>
Delete custom meditation: DELETE /api/v1/meditations/{meditation_id} (ROLE_USER_PRO)<br>

#### Calendar
List user’s reminders: GET /api/v1/calendar/{user_id}?startDate=&endDate=&eventType=&eventStatus (ROLE_USER)<br>
Create reminder: POST /api/v1/calendar (ROLE_USER)<br>
Update reminder: PATCH /api/v1/calendar/{reminder_id} (ROLE_USER)<br>
Delete reminder: DELETE  /api/v1/calendar/{reminder_id} (ROLE_USER)<br>

#### Chats
List all user’s chats: GET /api/v1/chats/{user_id} (ROLE_USER_PRO)<br>
Get chat by id: GET /api/v1/chats/{chat_id} (ROLE_USER_PRO)<br>
Create chat: POST /api/v1/chats (ROLE_USER_PRO)<br>
Update chat (title only): PATCH /api/v1/chats/{chat_id} (ROLE_USER_PRO)<br>
Delete chat: DELETE /api/v1/chats/{chat_id} (ROLE_USER_PRO)<br>

#### Chats Messages
List all chat’s messages GET /api/v1/chats/{chat_id}/messages (ROLE_USER_PRO)<br>
Create message POST /api/v1/chats/{chat_id}/messages (ROLE_USER_PRO)<br>

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
