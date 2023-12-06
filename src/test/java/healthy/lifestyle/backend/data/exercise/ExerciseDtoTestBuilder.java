package healthy.lifestyle.backend.data.exercise;

import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class ExerciseDtoTestBuilder {
    public <T> ExerciseDtoWrapper<T> getWrapper(Class<T> type) {
        return new ExerciseDtoWrapper<>(type);
    }

    public static class ExerciseDtoWrapper<T> {
        private T dto;

        private Class<T> type;

        private String title;

        private String description;

        private Boolean isCustom;

        private Boolean needsEquipment;

        private String seed;

        private List<Long> mediasIds;

        private List<Long> bodyPartsIds;

        public ExerciseDtoWrapper(Class<T> type) {
            this.type = type;
        }

        public ExerciseDtoWrapper<T> setCustom(boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public ExerciseDtoWrapper<T> setNeedsEquipment(boolean needsEquipment) {
            this.needsEquipment = needsEquipment;
            return this;
        }

        public ExerciseDtoWrapper<T> setSeed(String seed) {
            this.seed = seed;
            return this;
        }

        public ExerciseDtoWrapper<T> setMediasIds(List<Long> mediasIds) {
            this.mediasIds = mediasIds;
            return this;
        }

        public ExerciseDtoWrapper<T> setBodyPartsIds(List<Long> bodyPartsIds) {
            this.bodyPartsIds = bodyPartsIds;
            return this;
        }

        public ExerciseDtoWrapper<T> buildExerciseUpdateRequestDto() {
            if (this.seed == null || this.needsEquipment == null || this.mediasIds == null || this.bodyPartsIds == null)
                throw new IllegalStateException("Not all required parameters are set");

            this.dto = this.type.cast(ExerciseUpdateRequestDto.builder()
                    .title("Title " + seed)
                    .description("Description " + seed)
                    .needsEquipment(this.needsEquipment)
                    .httpRefIds(this.mediasIds)
                    .bodyPartIds(this.bodyPartsIds)
                    .build());
            return this;
        }

        public ExerciseDtoWrapper<T> buildEmptyExerciseUpdateRequestDto() {
            this.dto = this.type.cast(ExerciseUpdateRequestDto.builder()
                    .title(null)
                    .description(null)
                    .needsEquipment(null)
                    .httpRefIds(Collections.emptyList())
                    .bodyPartIds(Collections.emptyList())
                    .build());
            return this;
        }

        public ExerciseDtoWrapper<T> buildRandomExerciseUpdateRequestDto() {
            int intRand = ThreadLocalRandom.current().nextInt(10, 20);
            boolean boolRand = ThreadLocalRandom.current().nextInt(0, 1) != 0;

            this.dto = type.cast(ExerciseUpdateRequestDto.builder()
                    .title("Title " + intRand)
                    .description("Description " + intRand)
                    .needsEquipment(boolRand)
                    .httpRefIds(List.of((long) ThreadLocalRandom.current().nextInt(1, 10), (long)
                            ThreadLocalRandom.current().nextInt(11, 20)))
                    .bodyPartIds(List.of((long) ThreadLocalRandom.current().nextInt(1, 10), (long)
                            ThreadLocalRandom.current().nextInt(11, 20)))
                    .build());
            return this;
        }

        public T getDto() {
            return this.dto;
        }

        public void setFieldValue(String fieldName, Object value) throws IllegalAccessException, NoSuchFieldException {
            if (this.dto == null) throw new IllegalStateException("Not all required parameters are set");

            T obj = this.dto;
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }

        public Object getFieldValue(String fieldName) throws IllegalAccessException, NoSuchFieldException {
            if (this.dto == null) throw new IllegalStateException("Not all required parameters are set");

            T obj = this.dto;
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }
    }
}
