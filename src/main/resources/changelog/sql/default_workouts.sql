INSERT INTO workouts(id, title, description, is_custom) VALUES
(1, 'Workout for Arms', 'Arms workouts refer to a set of exercises specifically designed to target and strengthen the muscles of the upper extremities, including the biceps, triceps, shoulders, and forearms. These workouts can involve various movements, resistance training, and bodyweight exercises aimed at improving arm strength, muscle definition, and overall tone. Common exercises in arms workouts include bicep curls, tricep dips, shoulder presses, push-ups, hammer curls, and forearm exercises. Whether using free weights, resistance bands, or bodyweight, arms workouts are a key component of overall strength training and can be tailored to individual fitness goals, such as building muscle mass, toning, or improving functional strength.', false),
(2, 'Workout for Back', 'Back workouts are exercise routines specifically designed to target and strengthen the muscles of the back. The back is composed of various muscle groups, including the latissimus dorsi, rhomboids, traps, and erector spinae. Effective back workouts help improve posture, enhance upper body strength, and contribute to a well-balanced physique.', false),
(3, 'Workout for Legs', 'Legs workouts focus on exercises that target the muscles of the lower body, including the quadriceps, hamstrings, glutes, and calves. These workouts are essential for building lower body strength, improving muscle tone, and enhancing overall functional fitness.', false),
(4, 'Workout for Shoulders and Neck', 'Shoulders and neck workouts consist of exercises designed to strengthen and tone the muscles around the shoulders, including the deltoids, trapezius, and upper back, as well as addressing the muscles of the neck. These workouts can improve shoulder stability, enhance posture, and reduce tension in the neck and upper back.', false),
(5, 'Workout for Chest', 'Chest workouts focus on exercises that target the muscles of the chest, primarily the pectoralis major and pectoralis minor. Strengthening the chest muscles is essential for improving upper body strength, enhancing posture, and contributing to a well-rounded physique.', false),
(6, 'Workout for Abs', 'Abs workouts focus on exercises that engage and strengthen the muscles of the abdominal region, including the rectus abdominis, obliques, and transverse abdominis. A well-structured abs workout routine can contribute to core strength, stability, and improved aesthetics.', false);

ALTER sequence workouts_id_seq restart with 7;

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
