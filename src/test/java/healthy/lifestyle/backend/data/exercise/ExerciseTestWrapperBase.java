package healthy.lifestyle.backend.data.exercise;

import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.Set;

public interface ExerciseTestWrapperBase {
    ExerciseTestWrapperBase setIdOrSeed(int id);

    ExerciseTestWrapperBase setIsExerciseCustom(boolean isExerciseCustom);

    ExerciseTestWrapperBase setAmountOfExercises(int amountOfExercises);

    ExerciseTestWrapperBase setNeedsEquipment(boolean needsEquipment);

    ExerciseTestWrapperBase setIsHttpRefCustom(boolean isHttpRefCustom);

    ExerciseTestWrapperBase setAmountOfNestedEntities(int amountOfNestedEntities);

    ExerciseTestWrapperBase setStartIdOrSeedForNestedEntities(int startIdForNestedEntities);

    Exercise buildSingle();

    List<Exercise> buildList();

    Exercise getExerciseSingle();

    List<Exercise> getExerciseSingleAsList();

    Long getExerciseIdSingle();

    List<Exercise> getExercisesAll();

    BodyPart getBodyPartByIndexFromSingle(int bodyPartIndex);

    List<BodyPart> getBodyPartsSortedFromSingle();

    Integer getBodyPartsSizeFromSingle();

    BodyPart getBodyPartByIndexFromListElt(int exerciseIndex, int bodyPartIndex);

    List<BodyPart> getBodyPartsSortedFromListElt(int exerciseIndex);

    Set<BodyPart> getDistinctBodyPartsFromList();

    List<BodyPart> getDistinctBodyPartsSortedFromList();

    HttpRef getHttpRefByIndexFromSingle(int httpRefIndex);

    List<HttpRef> getHttpRefsSortedFromSingle();

    Integer getHttpRefSizeFromSingle();

    HttpRef getHttpRefByIndexFromListElt(int exerciseIndex, int httpRefIndex);

    List<HttpRef> getHttpRefsSortedFromListElt(int exerciseIndex);

    Set<HttpRef> getDistinctHttpRefsFromList();

    List<HttpRef> getDistinctHttpRefsSortedFromList();
}
