package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;

public interface MentalWorkoutService {

    MentalWorkoutResponseDto createCustomMentalWorkout(long userId, MentalWorkoutCreateRequestDto requestDto);

    MentalWorkoutResponseDto getMentalWorkoutById(long mentalWorkoutId, boolean requiredDefault, Long userId);
}
