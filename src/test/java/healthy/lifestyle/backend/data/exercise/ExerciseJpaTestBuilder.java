package healthy.lifestyle.backend.data.exercise;

import static java.util.Objects.nonNull;

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

    public static class ExerciseWrapper implements ExerciseTestWrapperBase {
        private Integer idOrSeed;

        private Boolean isExerciseCustom;

        private Integer amountOfExercises;

        private Boolean needsEquipment;

        private Boolean isHttpRefCustom;

        private Integer amountOfNestedEntities;

        private Integer startIdOrSeedForNestedEntities;

        private Exercise entity;

        private List<Exercise> entities;

        private final ExerciseRepository exerciseRepository;

        private final BodyPartRepository bodyPartRepository;

        private final HttpRefRepository httpRefRepository;

        public ExerciseWrapper(
                ExerciseRepository exerciseRepository,
                BodyPartRepository bodyPartRepository,
                HttpRefRepository httpRefRepository) {
            this.exerciseRepository = exerciseRepository;
            this.bodyPartRepository = bodyPartRepository;
            this.httpRefRepository = httpRefRepository;
        }

        @Override
        public ExerciseWrapper setIdOrSeed(int idOrSeed) {
            if (idOrSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public ExerciseWrapper setIsExerciseCustom(boolean isExerciseCustom) {
            this.isExerciseCustom = isExerciseCustom;
            return this;
        }

        @Override
        public ExerciseWrapper setAmountOfExercises(int amountOfExercises) {
            if (amountOfExercises < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfExercises = amountOfExercises;
            return this;
        }

        @Override
        public ExerciseWrapper setNeedsEquipment(boolean needsEquipment) {
            this.needsEquipment = needsEquipment;
            return this;
        }

        public ExerciseWrapper setIsHttpRefCustom(boolean isHttpRefCustom) {
            this.isHttpRefCustom = isHttpRefCustom;
            return this;
        }

        @Override
        public ExerciseWrapper setAmountOfNestedEntities(int amountOfNestedEntities) {
            if (amountOfNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfNestedEntities = amountOfNestedEntities;
            return this;
        }

        @Override
        public ExerciseWrapper setStartIdOrSeedForNestedEntities(int startIdOrSeedForNestedEntities) {
            if (startIdOrSeedForNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdOrSeedForNestedEntities = startIdOrSeedForNestedEntities;
            return this;
        }

        @Override
        public Exercise buildSingle() {
            if (this.idOrSeed == null) throw new IllegalStateException("Not all required parameters are set");

            Exercise exercise = Exercise.builder()
                    .title("Title " + this.idOrSeed)
                    .description("Description " + this.idOrSeed)
                    .bodyParts(new HashSet<>())
                    .httpRefs(new HashSet<>())
                    .isCustom(isExerciseCustom)
                    .needsEquipment(this.needsEquipment)
                    .build();
            Exercise exerciseSaved = exerciseRepository.save(exercise);

            int startSeed = this.startIdOrSeedForNestedEntities;
            for (int i = 0; i < this.amountOfNestedEntities; i++) {
                BodyPart bodyPart = BodyPart.builder().name("Name " + startSeed).build();
                BodyPart bodyPartSaved = bodyPartRepository.save(bodyPart);
                exerciseSaved.getBodyParts().add(bodyPartSaved);

                HttpRef httpRef = HttpRef.builder()
                        .name("Name " + startSeed)
                        .description("Description " + startSeed)
                        .isCustom(this.isHttpRefCustom)
                        .ref("https://reference-" + startSeed)
                        .build();
                HttpRef httpRefSaved = httpRefRepository.save(httpRef);
                exerciseSaved.getHttpRefs().add(httpRefSaved);
                startSeed++;
            }

            this.entity = exerciseRepository.save(exerciseSaved);
            return this.entity;
        }

        @Override
        public List<Exercise> buildList() {
            if (this.idOrSeed == null
                    || this.amountOfExercises == null
                    || this.startIdOrSeedForNestedEntities == null
                    || this.amountOfNestedEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.idOrSeed;
            this.entities = new ArrayList<>();
            for (int i = 0; i < this.amountOfExercises; i++) {
                Exercise exercise = Exercise.builder()
                        .title("Title " + id)
                        .description("Description " + id)
                        .bodyParts(new HashSet<>())
                        .httpRefs(new HashSet<>())
                        .isCustom(isExerciseCustom)
                        .needsEquipment(this.needsEquipment)
                        .build();
                Exercise exerciseSaved = exerciseRepository.save(exercise);

                int startSeed = this.startIdOrSeedForNestedEntities;
                for (int j = 0; j < this.amountOfNestedEntities; j++) {
                    BodyPart bodyPart = BodyPart.builder()
                            .name(String.format("Name %s %s", startSeed, i))
                            .build();
                    BodyPart bodyPartSaved = bodyPartRepository.save(bodyPart);
                    exerciseSaved.getBodyParts().add(bodyPartSaved);

                    HttpRef httpRef = HttpRef.builder()
                            .name("Name " + startSeed)
                            .description("Description " + startSeed)
                            .isCustom(this.isHttpRefCustom)
                            .ref("https://reference-" + startSeed)
                            .build();
                    HttpRef httpRefSaved = httpRefRepository.save(httpRef);
                    exerciseSaved.getHttpRefs().add(httpRefSaved);
                    startSeed++;
                }

                this.entities.add(exerciseRepository.save(exerciseSaved));
                id++;
            }

            return this.entities;
        }

        @Override
        public Exercise getExerciseSingle() {
            return this.entity;
        }

        @Override
        public List<Exercise> getExerciseSingleAsList() {
            List<Exercise> list = new ArrayList<>();
            list.add(this.entity);
            return list;
        }

        @Override
        public Long getExerciseIdSingle() {
            if (nonNull(this.entity)) return this.entity.getId();
            return null;
        }

        @Override
        public List<Exercise> getExercisesAll() {
            return this.entities;
        }

        @Override
        public BodyPart getBodyPartByIndexFromSingle(int bodyPartIndex) {
            return this.entity.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        @Override
        public List<BodyPart> getBodyPartsSortedFromSingle() {
            return this.entity.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        @Override
        public Integer getBodyPartsSizeFromSingle() {
            if (nonNull(this.entity.getBodyParts()))
                return this.entity.getBodyParts().size();
            return null;
        }

        @Override
        public BodyPart getBodyPartByIndexFromListElt(int exerciseIndex, int bodyPartIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            if (bodyPartIndex < 0
                    || bodyPartIndex
                            >= this.entities.get(exerciseIndex).getBodyParts().size())
                throw new IllegalArgumentException("Must be >= 0 and < set size");

            return this.entities.get(exerciseIndex).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        @Override
        public List<BodyPart> getBodyPartsSortedFromListElt(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            return this.entities.get(exerciseIndex).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        @Override
        public Set<BodyPart> getDistinctBodyPartsFromList() {
            Set<BodyPart> distinctBodyParts = new HashSet<>();
            this.entities.forEach(elt -> distinctBodyParts.addAll(elt.getBodyParts()));
            return distinctBodyParts;
        }

        @Override
        public List<BodyPart> getDistinctBodyPartsSortedFromList() {
            return getDistinctBodyPartsFromList().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        @Override
        public HttpRef getHttpRefByIndexFromSingle(int httpRefIndex) {
            return this.entity.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(httpRefIndex);
        }

        @Override
        public List<HttpRef> getHttpRefsSortedFromSingle() {
            return this.entity.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        @Override
        public Integer getHttpRefSizeFromSingle() {
            if (nonNull(this.entities)) return this.entity.getHttpRefs().size();
            return null;
        }

        @Override
        public HttpRef getHttpRefByIndexFromListElt(int exerciseIndex, int httpRefIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            if (httpRefIndex < 0
                    || httpRefIndex
                            >= this.entities.get(exerciseIndex).getHttpRefs().size())
                throw new IllegalArgumentException("Must be >= 0 and < set size");

            return this.entities.get(exerciseIndex).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(httpRefIndex);
        }

        @Override
        public List<HttpRef> getHttpRefsSortedFromListElt(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            return this.entities.get(exerciseIndex).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        @Override
        public Set<HttpRef> getDistinctHttpRefsFromList() {
            Set<HttpRef> distinctMedias = new HashSet<>();
            this.entities.forEach(elt -> distinctMedias.addAll(elt.getHttpRefs()));
            return distinctMedias;
        }

        @Override
        public List<HttpRef> getDistinctHttpRefsSortedFromList() {
            return getDistinctHttpRefsFromList().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }
    }
}
