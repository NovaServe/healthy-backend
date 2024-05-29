package healthy.lifestyle.backend.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import java.time.*;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WorkoutDayIdRepository workoutDayIdRepository;

    @Autowired
    DateTimeService dateTimeService;

    public List<JsonDescription> deserializeJsonStringToJsonDescriptionList(String jsonString)
            throws JsonProcessingException {
        TypeReference<List<JsonDescription>> typeReference = new TypeReference<List<JsonDescription>>() {};
        return objectMapper.readValue(jsonString, typeReference);
    }

    public List<JsonDescription> processJsonDescription(
            List<JsonDescription> jsonDescriptionList, ZoneId userTimeZone) {

        for (JsonDescription jsonDescription : jsonDescriptionList) {

            Optional<WorkoutPlanDayId> currentDayIdOptional =
                    workoutDayIdRepository.findAll().stream().findFirst();
            WorkoutPlanDayId currentDayId = currentDayIdOptional.orElseThrow(
                    () -> new ApiException(ErrorMessage.INTERNAL_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR));
            currentDayId.setJson_id(currentDayId.getJson_id() + 1);
            WorkoutPlanDayId newWorkoutPlanDayId = workoutDayIdRepository.save(currentDayId);

            // Convert day and time from user timezone to database timezone
            LocalDateTime userBaseDateTime = LocalDateTime.now(userTimeZone).with(jsonDescription.getDayOfWeek());
            LocalTime userTime = LocalTime.of(jsonDescription.getHours(), jsonDescription.getMinutes());
            LocalDateTime userDateTime = LocalDateTime.of(userBaseDateTime.toLocalDate(), userTime);
            ZonedDateTime userZonedDateTime = userDateTime.atZone(userTimeZone);
            ZonedDateTime databaseZonedDateTime =
                    dateTimeService.convertToNewZone(userZonedDateTime, dateTimeService.getDatabaseTimezone());

            jsonDescription.setJson_id(newWorkoutPlanDayId.getJson_id());
            jsonDescription.setDayOfWeek(databaseZonedDateTime.getDayOfWeek());
            jsonDescription.setHours(databaseZonedDateTime.getHour());
            jsonDescription.setMinutes(databaseZonedDateTime.getMinute());
        }

        return jsonDescriptionList;
    }

    public String serializeJsonDescriptionList(List<JsonDescription> jsonDescriptionList)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(jsonDescriptionList);
    }
}
