package healthy.lifestyle.backend.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.*;
import java.util.List;

import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WorkoutDayIdRepository workoutDayIdRepository;

    public List<JsonDescription> deserializeJsonStringToJsonDescriptionList(String jsonString)
            throws JsonProcessingException {
        TypeReference<List<JsonDescription>> typeReference = new TypeReference<List<JsonDescription>>() {};
        return objectMapper.readValue(jsonString, typeReference);
    }

    public List<JsonDescription> processJsonDescription(List<JsonDescription> jsonDescriptionList, ZoneId scrZone) {

        for(JsonDescription day: jsonDescriptionList){
            // Generate unique id for plan day
            WorkoutPlanDayId dayId = WorkoutPlanDayId.builder().json_id(1L).build();
            dayId = workoutDayIdRepository.save(dayId);
            // After that just delete this entity from database and update counter
            workoutDayIdRepository.delete(dayId);

            // Convert day and time from user timezone to server timezone
            DayOfWeek dayOfWeek = day.getDayOfWeek();
            LocalDateTime targetDate = LocalDateTime.now(scrZone).with(dayOfWeek);
            LocalTime localTime = LocalTime.of(day.getHours(), day.getMinutes());
            LocalDateTime localDateTime = LocalDateTime.of(targetDate.toLocalDate(), localTime);

            ZonedDateTime sourceZonedDateTime = localDateTime.atZone(scrZone);
            ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(ZoneId.of("Europe/London"));

            day.setJson_id(dayId.getId());
            day.setDayOfWeek(targetZonedDateTime.getDayOfWeek());
            day.setHours(targetZonedDateTime.getHour());
            day.setMinutes(targetZonedDateTime.getMinute());

        }

        return jsonDescriptionList;
    }

    public String serializeJsonDescriptionList(List<JsonDescription> jsonDescriptionList)
            throws JsonProcessingException {
        return objectMapper.writeValueAsString(jsonDescriptionList);
    }
}
