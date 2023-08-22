package healthy.lifestyle.backend.workout.dto;

import java.util.ArrayList;
import java.util.List;

public class GetExercisesResponseDto {
    private List<ExerciseResponseDto> exercises;

    public GetExercisesResponseDto() {}

    public GetExercisesResponseDto(List<ExerciseResponseDto> exercises) {
        this.exercises = exercises;
    }

    public GetExercisesResponseDto(Builder builder) {
        this.exercises = builder.exercises;
    }

    public List<ExerciseResponseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseResponseDto> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(ExerciseResponseDto exercise) {
        if (exercises == null) exercises = new ArrayList<>();
        exercises.add(exercise);
    }

    public static class Builder {
        private List<ExerciseResponseDto> exercises;

        public Builder exercises(List<ExerciseResponseDto> exercises) {
            this.exercises = exercises;
            return this;
        }

        public GetExercisesResponseDto build() {
            return new GetExercisesResponseDto(this);
        }
    }
}
