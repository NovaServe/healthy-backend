package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;

public class ExerciseTestBuilder {
    public ExerciseWrapper getWrapper() {
        return new ExerciseWrapper();
    }

    public static class ExerciseWrapper {
        private Exercise entity;

        private List<Exercise> entities;

        private Integer id;

        private Integer startIdForNestedEntities;

        private Integer amountOfNestedEntities;

        private Integer amountOfExercises;

        private Integer startIdForExercises;

        private Boolean exerciseCustom;

        private Boolean mediaCustom;

        private Boolean needsEquipment;

        /**
         * Common Exercise Functionality
         */
        public ExerciseWrapper setExerciseCustom(boolean exerciseCustom) {
            this.exerciseCustom = exerciseCustom;
            return this;
        }

        public ExerciseWrapper setMediaCustom(boolean mediaCustom) {
            this.mediaCustom = mediaCustom;
            return this;
        }

        public ExerciseWrapper setNeedsEquipment(boolean needsEquipment) {
            this.needsEquipment = needsEquipment;
            return this;
        }

        /**
         * Single Exercise Functionality
         */
        public ExerciseWrapper setId(Integer id) {
            if (id < 0) throw new IllegalArgumentException("Must be >= 0");
            this.id = id;
            return this;
        }

        public ExerciseWrapper setStartIdForNestedEntities(Integer startIdForNestedEntities) {
            if (startIdForNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdForNestedEntities = startIdForNestedEntities;
            return this;
        }

        public ExerciseWrapper setAmountOfNestedEntities(Integer amountOfNestedEntities) {
            if (amountOfNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfNestedEntities = amountOfNestedEntities;
            return this;
        }

        public Exercise buildSingle() {
            if (this.id == null) throw new IllegalStateException("Not all required parameters are set");

            Exercise exercise = Exercise.builder()
                    .id((long) this.id)
                    .title("Title " + this.id)
                    .description("Description " + this.id)
                    .bodyParts(new HashSet<>())
                    .httpRefs(new HashSet<>())
                    .isCustom(exerciseCustom)
                    .needsEquipment(this.needsEquipment)
                    .build();

            int id = this.startIdForNestedEntities;
            for (int i = 0; i < this.amountOfNestedEntities; i++) {
                exercise.getBodyParts()
                        .add(BodyPart.builder().id((long) id).name("Name " + id).build());

                exercise.getHttpRefs()
                        .add(HttpRef.builder()
                                .id((long) id)
                                .name("Name " + id)
                                .description("Description " + id)
                                .isCustom(this.mediaCustom)
                                .ref("https://reference-" + id)
                                .build());
                id++;
            }

            this.entity = exercise;
            return this.entity;
        }

        public List<HttpRef> getSortedMedias() {
            return this.entity.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        public List<BodyPart> getSortedBodyParts() {
            return this.entity.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        public HttpRef getSortedMediaByIndex(int index) {
            return this.entity.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(index);
        }

        public BodyPart getSortedBodyPartByIndex(int index) {
            return this.entity.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(index);
        }

        public Integer getMediasSize() {
            if (this.entity == null) return null;
            return this.entity.getHttpRefs().size();
        }

        public Integer getBodyPartsSize() {
            if (this.entity == null) return null;
            return this.entity.getBodyParts().size();
        }

        public Exercise getSingleExercise() {
            return this.entity;
        }

        public List<Exercise> getSingleExerciseCollection() {
            List<Exercise> collection = new ArrayList<>();
            collection.add(this.entity);
            return collection;
        }

        public List<Exercise> getExerciseCollection() {
            List<Exercise> exercisesCollection = new ArrayList<>();
            exercisesCollection.add(this.entity);
            return exercisesCollection;
        }

        /**
         * Exercises Functionality
         */
        public ExerciseWrapper setAmountOfExercises(Integer amountOfExercises) {
            if (amountOfExercises < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfExercises = amountOfExercises;
            return this;
        }

        public ExerciseWrapper setStartIdForExercises(Integer startIdForExercises) {
            if (startIdForExercises < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdForExercises = startIdForExercises;
            return this;
        }

        public List<Exercise> buildList() {
            if (this.startIdForExercises == null
                    || this.amountOfExercises == null
                    || this.startIdForNestedEntities == null
                    || this.amountOfNestedEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.startIdForExercises;
            this.entities = new ArrayList<>();
            for (int i = 0; i < this.amountOfExercises; i++) {
                Exercise exercise = Exercise.builder()
                        .id((long) this.id)
                        .title("Title " + this.id)
                        .description("Description " + this.id)
                        .bodyParts(new HashSet<>())
                        .httpRefs(new HashSet<>())
                        .isCustom(exerciseCustom)
                        .needsEquipment(this.needsEquipment)
                        .build();

                int nestedId = this.startIdForNestedEntities;
                for (int j = 0; i < this.amountOfNestedEntities; i++) {
                    exercise.getBodyParts()
                            .add(BodyPart.builder()
                                    .id((long) nestedId)
                                    .name("Name " + nestedId)
                                    .build());

                    exercise.getHttpRefs()
                            .add(HttpRef.builder()
                                    .id((long) nestedId)
                                    .name("Name " + nestedId)
                                    .description("Description " + nestedId)
                                    .isCustom(this.mediaCustom)
                                    .ref("https://reference-" + nestedId)
                                    .build());
                }

                this.entities.add(exercise);
            }

            return this.entities;
        }

        public List<Exercise> getExercises() {
            return this.entities;
        }

        public List<BodyPart> getBodyPartsSortedFromList(int eltIndex) {
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            return this.entities.get(eltIndex).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        public List<HttpRef> getMediasSortedFromList(int eltIndex) {
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            return this.entities.get(eltIndex).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        public BodyPart getBodyPartFromList(int eltIndex, int bodyPartIndex) {
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            if (bodyPartIndex < 0
                    || bodyPartIndex
                            >= this.entities.get(eltIndex).getBodyParts().size())
                throw new IllegalArgumentException("Must be >= 0 and < set size");

            return this.entities.get(eltIndex).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        public HttpRef getMediaFromList(int eltIndex, int mediaIndex) {
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            if (mediaIndex < 0
                    || mediaIndex >= this.entities.get(eltIndex).getHttpRefs().size())
                throw new IllegalArgumentException("Must be >= 0 and < set size");

            return this.entities.get(eltIndex).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex);
        }

        public Set<HttpRef> getDistinctMedias() {
            Set<HttpRef> distinctMedias = new HashSet<>();
            this.entities.forEach(elt -> distinctMedias.addAll(elt.getHttpRefs()));
            return distinctMedias;
        }

        public List<HttpRef> getDistinctMediasSorted() {
            return getDistinctMedias().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        public Set<BodyPart> getDistinctBodyParts() {
            Set<BodyPart> distinctBodyParts = new HashSet<>();
            this.entities.forEach(elt -> distinctBodyParts.addAll(elt.getBodyParts()));
            return distinctBodyParts;
        }

        public List<BodyPart> getDistinctBodyPartsSorted() {
            return getDistinctBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }
    }
}
