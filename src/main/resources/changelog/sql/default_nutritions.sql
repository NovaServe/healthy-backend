INSERT INTO nutrition_types(id, name) VALUES
(1, 'Recipe'),
(2, 'Supplement');

ALTER sequence nutrition_types_id_seq restart with 3;

INSERT INTO nutritions(id, title, description, is_custom, nutrition_type_id) VALUES
(1, 'Protein-packed Chicken Salad', 'A nutritious salad loaded with grilled chicken, mixed greens, tomatoes, and a light vinaigrette.', false, 1),
(2, 'Vegan Quinoa Bowl', 'A wholesome bowl containing cooked quinoa, roasted vegetables, avocado, and a tahini dressing.', false, 1),
(3, 'Energizing Fruit Smoothie', 'A refreshing blend of mixed berries, banana, almond milk, and a scoop of protein powder.', false, 1),
(4, 'Whole Grain Pasta Salad', 'A hearty salad featuring whole grain pasta, cherry tomatoes, olives, and feta cheese with a lemon-herb dressing.', false, 1),
(5, 'Lean Turkey Wrap', 'A light and satisfying wrap filled with sliced turkey, lettuce, tomato, and a hint of mustard.', false, 1),
(6, 'Omega-3 Rich Salmon', 'Grilled salmon fillet served with steamed asparagus and a side of quinoa.', false, 1),
(7, 'Fiber-Rich Bean Soup', 'A comforting soup made with mixed beans, vegetables, and herbs, providing a good dose of fiber.', false, 1),
(8, 'Low-Carb Cauliflower Rice Stir-Fry', 'A delicious stir-fry dish using cauliflower rice, mixed vegetables, and lean chicken strips.', false, 1),
(9, 'Mediterranean Chickpea Salad', 'A vibrant salad packed with chickpeas, cucumbers, red onions, feta cheese, and a lemon dressing.', false, 1),
(10, 'High-Protein Egg Breakfast', 'A satisfying breakfast featuring scrambled eggs, spinach, tomatoes, and whole grain toast.', false, 1),
(11, 'Multivitamins and Minerals', 'Elevate your overall health with essential vitamins and minerals for a well-rounded nutritional foundation.', false, 2),
(12, 'Protein Shake', 'Fuel your body with the daily protein it needs for muscle repair, growth, and overall strength.', false, 2),
(13, 'Q10', 'Revitalize your energy levels and fortify your immune system with Q10, a powerful antioxidant supporting cellular energy production.', false, 2),
(14, 'Omega', 'Nourish your body with essential fatty acids for heart health, cognitive function, and overall well-being.', false, 2),
(15, 'Lecithin', 'Unlock the benefits of choline, an essential nutrient found in lecithin, supporting brain function and overall cognitive well-being.', false, 2),
(16, 'Collagen', 'Nurture your skin, hair, nails, and bones with collagen, promoting elasticity, strength, and structural integrity for a vibrant appearance and overall well-being.', false, 2);

ALTER sequence nutritions_id_seq restart with 17;
