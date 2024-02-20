INSERT INTO mental_types(id, name) VALUES
(1, 'MEDITATION'),
(2, 'AFFIRMATION');

ALTER sequence mental_types_id_seq restart with 3;

INSERT INTO mentals(id, title, description, is_custom, type_id) VALUES
(1, 'I am the architect of my destiny', 'Empower yourself with this affirmation. Take control of your life and create the future you desire.', false, 2),
(2, 'I embrace my uniqueness and individuality.', 'Celebrate your uniqueness and authenticity with this affirmation. Embrace your quirks and differences.', false, 2),
(3, 'I choose happiness and inner peace.', 'Prioritize your well-being with this affirmation. Cultivate inner peace and happiness amidst lifes ups and downs.', false, 2),
(4, 'I am resilient and adaptable.', 'Strengthen your resilience with this affirmation. Face challenges with confidence and flexibility.', false, 2),
(5, 'I trust the journey of life.', 'Release fear and uncertainty by affirming your trust in the journey of life. Embrace change and growth.', false, 2),
(6, 'Relaxation Meditation: Serene Forest', 'Transport yourself to a tranquil forest setting with this soothing meditation. Let go of tension and find inner calm.', false, 1),
(7, 'Breath Awareness Meditation', 'Connect with your breath and center yourself in the present moment with this mindfulness practice. Experience deep relaxation.', false, 1),
(8, 'Body Scan Meditation: Deep Relaxation', 'Scan your body from head to toe, releasing tension and promoting relaxation. Experience profound physical and mental relaxation.', false, 1),
(9, 'Chakra Balancing Meditation: Energy Alignment', 'Balance your chakras and harmonize your energy centers with this transformative meditation. Enhance vitality and well-being.', false, 1),
(10, 'Sleep Meditation: Tranquil Slumber', 'Prepare your mind and body for restful sleep with this calming meditation. Drift into a state of deep relaxation and ease.', false, 1);

ALTER sequence mentals_id_seq restart with 11;