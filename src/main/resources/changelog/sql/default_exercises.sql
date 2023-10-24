INSERT INTO exercises(id, title, description, is_custom, needs_equipment) VALUES
(1, 'Reverse Snow Angel', '', false, false),
(2, 'Side Bends', '', false, false),
(3, 'Superman Elbow to Knee Touch', '', false, false),

(4, 'Diamond Push Ups', '', false, false),
(5, 'Tricep Dips', '', false, false),
(6, 'Reverse Push-Ups', '', false, false),

(7, 'Elevated Pike Push Ups', '', false, false),
(8, 'Shoulder Blade Squeeze', '', false, false),
(9, 'Shoulder Circle', '', false, false),
(10, 'Neck Rotation', '', false, false),

(11, 'Glute Bridge', '', false, false),
(12, 'Side Lunges', '', false, false),
(13, 'Squats', '', false, false),
(14, 'Assisted Pistol Squats', '', false, false),
(15, 'Forward Lunges', '', false, false),
(16, 'Calves Exercises', '', false, false),
(17, 'Knees Rotation', '', false, false),

(18, 'Normal Push Ups', '', false, false),

(19, 'Legs Down Hold', '', false, false),
(20, 'Leg Flutters', '', false, false),
(21, 'Leg Raise', '', false, false),
(22, 'Lay Hip Raise', '', false, false),
(23, 'Twists', '', false, false),
(24, 'Crunches', '', false, false),
(25, 'Bicycle Twists', '', false, false),
(26, 'Mountain Climbers', '', false, false),
(27, 'Plank Hold', '', false, false);

ALTER sequence exercises_id_seq restart with 28;

INSERT INTO exercises_body_parts(exercise_id, body_part_id) VALUES
(1, 7),
(2, 7),
(3, 7),

(4, 4),
(4, 6),
(5, 4),
(5, 6),
(6, 4),
(6, 5),

(7, 2),
(8, 10),
(9, 2),
(10, 1),

(11, 9),
(12, 9),
(13, 9),
(14, 9),
(15, 9),
(16, 9),
(16, 13),
(17, 9),
(17, 11),

(18, 3),

(19, 8),
(20, 8),
(21, 8),
(22, 8),
(23, 8),
(24, 8),
(25, 8),
(26, 8),
(27, 8);

INSERT INTO exercises_http_refs(exercise_id, http_ref_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(3, 4),

(4, 5),
(5, 6),
(6, 7),

(7, 8),
(8, 9),
(9, 10),
(10, 11),

(11, 12),
(12, 13),
(13, 14),
(14, 15),
(15, 16),
(15, 17),
(16, 18),
(17, 19),

(18, 20),

(19, 21),
(20, 22),
(21, 23),
(22, 24),
(23, 25),
(24, 26),
(25, 27),
(26, 28),
(27, 29);
