INSERT INTO workouts(id, title, description, is_custom) VALUES
(1, 'Workout for Arms', '', false),
(2, 'Workout for Back', '', false),
(3, 'Workout for Legs', '', false),
(4, 'Workout for Shoulders and Neck', '', false),
(5, 'Workout for Chest', '', false),
(6, 'Workout for Abs', '', false);

INSERT INTO workouts_exercises(workout_id, exercise_id) VALUES
(1, 4),
(1, 5),
(1, 6),

(2, 1),
(2, 2),
(2, 3),

(3, 11),
(3, 12),
(3, 13),
(3, 14),
(3, 15),
(3, 16),
(3, 17),

(4, 7),
(4, 8),
(4, 9),
(4, 10),

(5, 18),

(6, 19),
(6, 20),
(6, 21),
(6, 22),
(6, 23),
(6, 24),
(6, 25),
(6, 26),
(6, 27);
