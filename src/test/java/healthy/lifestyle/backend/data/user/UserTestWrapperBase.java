package healthy.lifestyle.backend.data.user;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.Set;

public interface UserTestWrapperBase {
    UserTestWrapperBase setUserIdOrSeed(int userId);

    UserTestWrapperBase setCountryIdOrSeed(int countryIdOrSeed);

    UserTestWrapperBase setUserRole();

    UserTestWrapperBase setAdminRole();

    UserTestWrapperBase setRoleId(int roleId);

    UserTestWrapperBase setIsRoleAlreadyCreated(boolean isRoleAlreadyCreated);

    UserTestWrapperBase setExerciseIdOrSeed(int exerciseIdOrSeed);

    UserTestWrapperBase setIsExerciseCustom(boolean isExerciseCustom);

    UserTestWrapperBase setIsExerciseNeedsEquipment(boolean IsExerciseNeedsEquipment);

    UserTestWrapperBase setIsExerciseHttpRefsCustom(boolean isExerciseHttpRefsCustom);

    UserTestWrapperBase setAmountOfExercises(int amountOfExercises);

    UserTestWrapperBase setAmountOfExerciseNestedEntities(int amountOfExerciseNestedEntities);

    UserTestWrapperBase setStartIdOrSeedForExerciseNestedEntities(int startIdOrSeedForExerciseNestedEntities);

    User buildUser();

    User buildUserAndAddSingleExercise();

    User buildUserAndAddMultipleExercises();

    User getUser();

    long getUserId();

    Country getCountry();

    long getCountryId();

    UserTestWrapperBase addCustomExercises(List<Exercise> exercises);

    UserTestWrapperBase addCustomHttpRefs(List<HttpRef> httpRefs);

    Exercise getExerciseSingle();

    Long getExerciseIdSingle();

    Exercise getExerciseFromSortedList(int exerciseIndex);

    Long getExerciseIdFromSortedList(int exerciseIndex);

    List<Exercise> getAllExercisesSorted();

    BodyPart getBodyPartByIndexFromSingleExercise(int bodyPartIndex);

    long getBodyPartIdByIndexFromSingleExercise(int bodyPartIndex);

    List<BodyPart> getBodyPartsSortedFromSingleExercise();

    List<Long> getBodyPartsIdsSortedFromSingleExercise();

    List<BodyPart> getBodyPartsSortedFromExerciseListByIndex(int exerciseIndex);

    List<Long> getBodyPartsIdsSortedFromExerciseListByIndex(int exerciseIndex);

    BodyPart getBodyPartByIndexFromExerciseList(int exerciseIndex, int bodyPartIndex);

    Set<BodyPart> getDistinctBodyPartsFromExerciseList();

    List<BodyPart> getDistinctSortedBodyPartsFromExerciseList();

    HttpRef getHttpRefByIndexFromSingleExercise(int httpRefIndex);

    long getHttpRefIdByIndexFromSingleExercise(int httpRefIndex);

    List<HttpRef> getHttpRefsSortedFromSingleExercise();

    List<Long> getHttpRefsIdsSortedFromSingleExercise();

    List<HttpRef> getHttpRefsSortedFromExerciseListByIndex(int exerciseIndex);

    List<Long> getHttpRefsIdsSortedFromExerciseListByIndex(int exerciseIndex);

    HttpRef getHttpRefByIndexFromExercisesList(int exerciseIndex, int httpRefIndex);

    Set<HttpRef> getDistinctHttpRefsFromExerciseList();

    List<HttpRef> getDistinctSortedHttpRefsFromExerciseList();

    void setFieldValue(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException;

    Object getFieldValue(String fieldName) throws NoSuchFieldException, IllegalAccessException;
}
