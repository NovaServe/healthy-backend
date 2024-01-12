INSERT INTO nutrition_types(id, name, supplement, recipe) VALUES
(1, 'Protein-rich Meal', 'Whey protein, chicken breast', 'Combine grilled chicken with leafy greens, tomatoes, and a light dressing.'),
(2, 'Vegan Delight', 'Tofu, quinoa', 'Mix cooked quinoa with roasted veggies, avocado, and a creamy tahini sauce.'),
(3, 'Smoothie Boost', 'Berries, protein powder', 'Blend mixed berries, banana, almond milk, and protein powder until smooth.'),
(4, 'Whole Grain Goodness', 'Whole grain pasta, olive oil', 'Toss whole grain pasta with cherry tomatoes, olives, feta, and a lemon-herb dressing.'),
(5, 'Lean Protein Wrap', 'Turkey slices, whole wheat wrap', 'Wrap sliced turkey, lettuce, and tomato in a whole wheat wrap with a hint of mustard.'),
(6, 'Fish Lovers Delight', 'Salmon fillet, asparagus', 'Grill salmon and serve with steamed asparagus and a side of quinoa.'),
(7, 'Hearty Bean Dish', 'Mixed beans, herbs', 'Simmer mixed beans with vegetables and herbs to create a comforting soup.'),
(8, 'Low-Carb Creations', 'Cauliflower rice, chicken', 'Stir-fry cauliflower rice with mixed veggies and lean chicken strips.'),
(9, 'Mediterranean Magic', 'Chickpeas, feta', 'Combine chickpeas, cucumbers, red onions, and feta cheese with a zesty lemon dressing.'),
(10, 'Egg-cellent Breakfast', 'Eggs, spinach', 'Scramble eggs with spinach and tomatoes, serve with whole grain toast.');

ALTER sequence nutrition_types_id_seq restart with 11;

INSERT INTO nutritions(id, title, description, is_custom, nutrition_type_id) VALUES
(1, 'Protein-packed Chicken Salad', 'A nutritious salad loaded with grilled chicken, mixed greens, tomatoes, and a light vinaigrette.', false, 1),
(2, 'Vegan Quinoa Bowl', 'A wholesome bowl containing cooked quinoa, roasted vegetables, avocado, and a tahini dressing.', false, 2),
(3, 'Energizing Fruit Smoothie', 'A refreshing blend of mixed berries, banana, almond milk, and a scoop of protein powder.', false, 3),
(4, 'Whole Grain Pasta Salad', 'A hearty salad featuring whole grain pasta, cherry tomatoes, olives, and feta cheese with a lemon-herb dressing.', false, 4),
(5, 'Lean Turkey Wrap', 'A light and satisfying wrap filled with sliced turkey, lettuce, tomato, and a hint of mustard.', false, 5),
(6, 'Omega-3 Rich Salmon', 'Grilled salmon fillet served with steamed asparagus and a side of quinoa.', false, 6),
(7, 'Fiber-Rich Bean Soup', 'A comforting soup made with mixed beans, vegetables, and herbs, providing a good dose of fiber.', false, 7),
(8, 'Low-Carb Cauliflower Rice Stir-Fry', 'A delicious stir-fry dish using cauliflower rice, mixed vegetables, and lean chicken strips.', false, 8),
(9, 'Mediterranean Chickpea Salad', 'A vibrant salad packed with chickpeas, cucumbers, red onions, feta cheese, and a lemon dressing.', false, 9),
(10, 'High-Protein Egg Breakfast', 'A satisfying breakfast featuring scrambled eggs, spinach, tomatoes, and whole grain toast.', false, 10);
