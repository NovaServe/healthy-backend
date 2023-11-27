package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class ExerciseJpaTestBuilder {
    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private BodyPartRepository bodyPartRepository;

    @Autowired
    private HttpRefRepository httpRefRepository;

    public ExerciseWrapper getWrapper() {
        return new ExerciseWrapper(this.exerciseRepository, this.bodyPartRepository, this.httpRefRepository);
    }

    public ExerciseWrapper getWrapper(
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            HttpRefRepository httpRefRepository) {
        return new ExerciseWrapper(exerciseRepository, bodyPartRepository, httpRefRepository);
    }

    public static class ExerciseWrapper {
        private final ExerciseRepository exerciseRepository;

        private final BodyPartRepository bodyPartRepository;

        private final HttpRefRepository httpRefRepository;

        private Exercise entity;

        private List<Exercise> entities;

        private Integer seed;

        private Integer startSeedForNestedEntities;

        private Integer amountOfNestedEntities;

        private Integer amountOfExercises;

        private Integer startSeedForExercises;

        private Boolean exerciseCustom;

        private Boolean mediaCustom;

        private Boolean needsEquipment;

        public ExerciseWrapper(
                ExerciseRepository exerciseRepository,
                BodyPartRepository bodyPartRepository,
                HttpRefRepository httpRefRepository) {
            this.exerciseRepository = exerciseRepository;
            this.bodyPartRepository = bodyPartRepository;
            this.httpRefRepository = httpRefRepository;
        }

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
        public ExerciseWrapper setSeed(Integer seed) {
            if (seed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.seed = seed;
            return this;
        }

        public ExerciseWrapper setStartSeedForNestedEntities(Integer startSeedForNestedEntities) {
            if (startSeedForNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startSeedForNestedEntities = startSeedForNestedEntities;
            return this;
        }

        public ExerciseWrapper setAmountOfNestedEntities(Integer amountOfNestedEntities) {
            if (amountOfNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfNestedEntities = amountOfNestedEntities;
            return this;
        }

        public Exercise buildSingle() {
            if (this.seed == null) throw new IllegalStateException("Not all required parameters are set");

            Exercise exercise = Exercise.builder()
                    .title("Title " + this.seed)
                    .description("Description " + this.seed)
                    .bodyParts(new HashSet<>())
                    .httpRefs(new HashSet<>())
                    .isCustom(exerciseCustom)
                    .needsEquipment(this.needsEquipment)
                    .build();
            Exercise exerciseSaved = exerciseRepository.save(exercise);

            int startSeed = this.startSeedForNestedEntities;
            for (int i = 0; i < this.amountOfNestedEntities; i++) {
                BodyPart bodyPart = BodyPart.builder().name("Name " + startSeed).build();
                BodyPart bodyPartSaved = bodyPartRepository.save(bodyPart);
                exerciseSaved.getBodyParts().add(bodyPartSaved);

                HttpRef httpRef = HttpRef.builder()
                        .name("Name " + startSeed)
                        .description("Description " + startSeed)
                        .isCustom(this.mediaCustom)
                        .ref("https://reference-" + startSeed)
                        .build();
                HttpRef httpRefSaved = httpRefRepository.save(httpRef);
                exerciseSaved.getHttpRefs().add(httpRefSaved);
                startSeed++;
            }

            this.entity = exerciseRepository.save(exerciseSaved);
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

        public ExerciseWrapper setStartSeedForExercises(Integer startSeedForExercises) {
            if (startSeedForExercises < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startSeedForExercises = startSeedForExercises;
            return this;
        }

        public List<Exercise> buildList() {
            if (this.startSeedForExercises == null
                    || this.amountOfExercises == null
                    || this.startSeedForNestedEntities == null
                    || this.amountOfNestedEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.startSeedForExercises;
            this.entities = new ArrayList<>();
            for (int i = 0; i < this.amountOfExercises; i++) {
                Exercise exercise = Exercise.builder()
                        .title("Title " + this.seed)
                        .description("Description " + this.seed)
                        .bodyParts(new HashSet<>())
                        .httpRefs(new HashSet<>())
                        .isCustom(exerciseCustom)
                        .needsEquipment(this.needsEquipment)
                        .build();
                Exercise exerciseSaved = exerciseRepository.save(exercise);

                int startSeed = this.startSeedForNestedEntities;
                for (int j = 0; j < this.amountOfNestedEntities; i++) {
                    BodyPart bodyPart =
                            BodyPart.builder().name("Name " + startSeed).build();
                    BodyPart bodyPartSaved = bodyPartRepository.save(bodyPart);
                    exerciseSaved.getBodyParts().add(bodyPartSaved);

                    HttpRef httpRef = HttpRef.builder()
                            .name("Name " + startSeed)
                            .description("Description " + startSeed)
                            .isCustom(this.mediaCustom)
                            .ref("https://reference-" + startSeed)
                            .build();
                    HttpRef httpRefSaved = httpRefRepository.save(httpRef);
                    exerciseSaved.getHttpRefs().add(httpRefSaved);
                    startSeed++;
                }

                this.entities.add(exerciseRepository.save(exerciseSaved));
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
