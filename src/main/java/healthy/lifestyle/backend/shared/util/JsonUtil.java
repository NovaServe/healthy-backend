package healthy.lifestyle.backend.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
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

            // Convert day and time from user's timezone to database's timezone
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

    public String serializeJsonDescriptionList(List<JsonDescription> jsonDescriptionList, String userTimezoneName)
            throws JsonProcessingException {

        for (JsonDescription jsonDescription : jsonDescriptionList) {
            // Convert day and time from db's timezone to user's timezone
            LocalDateTime DBBaseDateTime = LocalDateTime.now(
                            dateTimeService.getDatabaseTimezone().toZoneId())
                    .with(jsonDescription.getDayOfWeek());
            LocalTime DBTime = LocalTime.of(jsonDescription.getHours(), jsonDescription.getMinutes());
            LocalDateTime DBDateTime = LocalDateTime.of(DBBaseDateTime.toLocalDate(), DBTime);
            ZonedDateTime DBZonedDateTime =
                    DBDateTime.atZone(dateTimeService.getDatabaseTimezone().toZoneId());
            ZonedDateTime userZonedDateTime =
                    dateTimeService.convertToNewZone(DBZonedDateTime, TimeZone.getTimeZone(userTimezoneName));

            jsonDescription.setDayOfWeek(userZonedDateTime.getDayOfWeek());
            jsonDescription.setHours(userZonedDateTime.getHour());
            jsonDescription.setMinutes(userZonedDateTime.getMinute());
        }

        return objectMapper.writeValueAsString(jsonDescriptionList);
    }
}
