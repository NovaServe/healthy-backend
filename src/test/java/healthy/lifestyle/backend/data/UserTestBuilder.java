package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class UserTestBuilder {
    public UserWrapper getWrapper() {
        return new UserWrapper(new ExerciseTestBuilder().getWrapper());
    }

    public static class UserWrapper {
        private final ExerciseTestBuilder.ExerciseWrapper exerciseWrapper;

        private User user;

        private Integer userId;

        private String role;

        private int roleId;

        // Exercise related fields
        private Integer exerciseId;

        private Integer startIdForExerciseNestedEntities;

        private Integer amountOfExerciseNestedEntities;

        private Integer amountOfExercises;

        private Integer startIdForExercises;

        private Boolean exerciseCustom;

        private Boolean mediaCustom;

        private Boolean exerciseNeedsEquipment;

        public UserWrapper(ExerciseTestBuilder.ExerciseWrapper exerciseWrapper) {
            this.exerciseWrapper = exerciseWrapper;
        }

        /**
         * User Related Functionality
         */
        public UserWrapper setRoleWithId(String role, int roleId) {
            this.role = role;
            this.roleId = roleId;
            return this;
        }

        public UserWrapper setRoleUserWithId(int roleId) {
            this.role = "ROLE_USER";
            this.roleId = roleId;
            return this;
        }

        public UserWrapper setUserId(Integer userId) {
            if (userId < 0) throw new IllegalArgumentException("Must be >= 0");
            this.userId = userId;
            return this;
        }

        public UserWrapper buildUser() {
            if (this.userId == null || this.role == null)
                throw new IllegalStateException("Not all required parameters are set");
            this.user = User.builder()
                    .id((long) this.userId)
                    .fullName("Full name " + this.userId)
                    .age(ThreadLocalRandom.current().nextInt(18, 100))
                    .username("Username-" + this.userId)
                    .email("email-" + this.userId + "@email.com")
                    .password("Password-" + this.userId)
                    .role(Role.builder().id((long) this.roleId).name(this.role).build())
                    .country(Country.builder()
                            .id((long) this.userId)
                            .name("Country-" + ThreadLocalRandom.current().nextInt(1, 100))
                            .build())
                    .httpRefs(new HashSet<>())
                    .exercises(new HashSet<>())
                    .workouts(new HashSet<>())
                    .build();
            return this;
        }

        public UserWrapper addCustomMedias(List<HttpRef> medias) {
            this.user.getHttpRefs().addAll(medias);
            return this;
        }

        public User getUser() {
            return this.user;
        }

        public long getUserId() {
            if (this.user == null) throw new IllegalStateException("Not all required parameters are set");
            return this.user.getId();
        }

        /**
         * Common Exercise Functionality
         */
        public UserWrapper setExerciseNeedsEquipment(boolean exerciseNeedsEquipment) {
            this.exerciseNeedsEquipment = exerciseNeedsEquipment;
            return this;
        }

        public UserWrapper setExerciseCustom(Boolean exerciseCustom) {
            this.exerciseCustom = exerciseCustom;
            return this;
        }

        public UserWrapper setMediaCustom(Boolean mediaCustom) {
            this.mediaCustom = mediaCustom;
            return this;
        }

        public UserWrapper setAmountOfExerciseNestedEntities(Integer amountOfExerciseNestedEntities) {
            if (amountOfExerciseNestedEntities < 1) throw new IllegalArgumentException("Must be >= 1");
            this.amountOfExerciseNestedEntities = amountOfExerciseNestedEntities;
            return this;
        }

        public UserWrapper setStartIdForExerciseNestedEntities(Integer startIdForExerciseNestedEntities) {
            if (startIdForExerciseNestedEntities < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdForExerciseNestedEntities = startIdForExerciseNestedEntities;
            return this;
        }

        /**
         * Single Exercise Functionality
         */
        public UserWrapper setExerciseId(Integer exerciseId) {
            if (exerciseId < 0) throw new IllegalArgumentException("Must be >= 0");
            this.exerciseId = exerciseId;
            return this;
        }

        public UserWrapper buildUserAndAddSingleExercise() {
            this.buildUser();

            exerciseWrapper
                    .setId(this.exerciseId)
                    .setExerciseCustom(this.exerciseCustom)
                    .setNeedsEquipment(this.exerciseNeedsEquipment)
                    .setMediaCustom(this.mediaCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartIdForNestedEntities(this.startIdForExerciseNestedEntities)
                    .buildSingle();
            if (this.mediaCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getSortedMedias());
            }
            this.user.getExercises().addAll(exerciseWrapper.getExerciseCollection());
            return this;
        }

        public Exercise getSingleExercise() {
            Optional<Exercise> exerciseOptional =
                    this.user.getExercises().stream().findFirst();
            return exerciseOptional.orElse(null);
        }

        public Long getSingleExerciseId() {
            Optional<Exercise> exerciseOptional =
                    this.user.getExercises().stream().findFirst();
            return exerciseOptional.map(Exercise::getId).orElse(null);
        }

        public BodyPart getBodyPartFromSingleExercise(int bodyPartIndex) {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        public HttpRef getMediaFromSingleExercise(int mediaIndex) {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex);
        }

        public long getBodyPartIdFromSingleExercise(int bodyPartIndex) {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex)
                    .getId();
        }

        public long getMediaIdFromSingleExercise(int mediaIndex) {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex)
                    .getId();
        }

        public List<BodyPart> getBodyPartsSortedFromSingleExercise() {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();
        }

        public List<Long> getBodyPartsIdsFromSingleExercise() {
            return this.getSingleExercise().getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .map(BodyPart::getId)
                    .toList();
        }

        public List<HttpRef> getMediasSortedFromSingleExercise() {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();
        }

        public List<Long> getMediasIdsFromSingleExercise() {
            return this.getSingleExercise().getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .map(HttpRef::getId)
                    .toList();
        }

        /**
         * Exercises Functionality
         */
        public UserWrapper setAmountOfExercises(Integer amountOfExercises) {
            if (amountOfExercises < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfExercises = amountOfExercises;
            return this;
        }

        public UserWrapper setStartIdForExercises(Integer startIdForExercises) {
            if (startIdForExercises < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startIdForExercises = startIdForExercises;
            return this;
        }

        public UserWrapper buildUserAndAddExercises() {
            exerciseWrapper
                    .setAmountOfExercises(this.amountOfExercises)
                    .setStartIdForExercises(this.startIdForExercises)
                    .setExerciseCustom(this.exerciseCustom)
                    .setNeedsEquipment(this.exerciseNeedsEquipment)
                    .setMediaCustom(this.mediaCustom)
                    .setAmountOfNestedEntities(this.amountOfExerciseNestedEntities)
                    .setStartIdForNestedEntities(this.startIdForExerciseNestedEntities)
                    .buildSingle();
            if (this.exerciseCustom && this.mediaCustom) {
                this.user.getHttpRefs().addAll(exerciseWrapper.getDistinctMedias());
            }
            return this;
        }

        public UserWrapper addCustomExercises(List<Exercise> exercises) {
            if (exercises.size() < 1) throw new IllegalArgumentException("Must be >= 1 exercises");
            if (this.user == null) throw new IllegalStateException("User must not be null");
            this.user.getExercises().addAll(exercises);
            return this;
        }

        public List<Exercise> getExercisesSorted() {
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList();
        }

        public Exercise getExerciseFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
        }

        public long getExerciseIdFromSortedList(int exerciseIndex) {
            if (exerciseIndex < 0 || exerciseIndex >= this.user.getExercises().size())
                throw new IllegalArgumentException("exerciseIndex must be >= 0 and < exercises.size()");
            return this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex)
                    .getId();
        }

        public Set<BodyPart> getDistinctBodyParts() {
            return exerciseWrapper.getDistinctBodyParts();
        }

        public List<BodyPart> getDistinctBodyPartsSorted() {
            return exerciseWrapper.getDistinctBodyPartsSorted();
        }

        public Set<HttpRef> getDistinctMedias() {
            return exerciseWrapper.getDistinctMedias();
        }

        public List<HttpRef> getDistinctMediasSorted() {
            return exerciseWrapper.getDistinctMediasSorted();
        }

        public BodyPart getBodyPartFromExerciseList(int exerciseIndex, int bodyPartIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList()
                    .get(bodyPartIndex);
        }

        public HttpRef getMediaFromExerciseList(int exerciseIndex, int mediaIndex) {
            Exercise exercise = this.user.getExercises().stream()
                    .sorted(Comparator.comparingLong(Exercise::getId))
                    .toList()
                    .get(exerciseIndex);
            return exercise.getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList()
                    .get(mediaIndex);
        }
    }
}
