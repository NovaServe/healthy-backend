package healthy.lifestyle.backend.shared.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlanDayId;
import healthy.lifestyle.backend.plan.workout.repository.WorkoutDayIdRepository;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonUtilTest {
    @InjectMocks
    JsonUtil jsonUtil;

    @Mock
    WorkoutDayIdRepository workoutDayIdRepository;

    @Spy
    DateTimeService dateTimeService;

    @Spy
    ObjectMapper objectMapper;

    @Test
    void deserializeJsonStringToJsonDescriptionList_shouldDeserialize_whenValidInput() throws JsonProcessingException {
        // Given
        String initialJsonDescription = "[{\"hours\":\"10\",\"minutes\":\"20\",\"dayOfWeek\":\"MONDAY\"},"
                + "{\"hours\":\"15\",\"minutes\":\"30\",\"dayOfWeek\":\"TUESDAY\"}]";

        List<JsonDescription> expected = List.of(
                JsonDescription.builder()
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .hours(10)
                        .minutes(20)
                        .build(),
                JsonDescription.builder()
                        .dayOfWeek(DayOfWeek.TUESDAY)
                        .hours(15)
                        .minutes(30)
                        .build());
        // When
        List<JsonDescription> actual = jsonUtil.deserializeJsonStringToJsonDescriptionList(initialJsonDescription);

        // Then
        assertThat(actual).usingRecursiveComparison().ignoringFields("json_id").isEqualTo(expected);
    }

    @Test
    void processJsonDescription_shouldConvertUserTimezoneToDbTimezoneAndAddJsonIds_whenValidInput() {
        // Given
        ZoneId userTimeZone = TimeZone.getTimeZone("Europe/Kyiv").toZoneId();
        List<JsonDescription> initial = List.of(JsonDescription.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .hours(1)
                .minutes(20)
                .build());

        long currentJsonId = 10L;
        List<JsonDescription> expected = List.of(JsonDescription.builder()
                .json_id(currentJsonId + 1)
                .hours(23)
                .minutes(20)
                .dayOfWeek(DayOfWeek.SUNDAY)
                .build());

        WorkoutPlanDayId workoutPlanDayId =
                WorkoutPlanDayId.builder().json_id(currentJsonId).build();
        when(workoutDayIdRepository.findAll()).thenReturn(List.of(workoutPlanDayId));

        when(workoutDayIdRepository.save(any(WorkoutPlanDayId.class))).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return args[0];
        });

        // When
        List<JsonDescription> actual = jsonUtil.processJsonDescription(initial, userTimeZone);
        assertThat(actual).usingRecursiveComparison().ignoringFields("id").isEqualTo(expected);
    }
}
