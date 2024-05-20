package healthy.lifestyle.backend.reminder.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import healthy.lifestyle.backend.activity.workout.api.WorkoutApi;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderCreateRequestDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderResponseDto;
import healthy.lifestyle.backend.reminder.workout.dto.WorkoutReminderUpdateRequestDto;
import healthy.lifestyle.backend.reminder.workout.model.WorkoutReminder;
import healthy.lifestyle.backend.reminder.workout.repository.WorkoutReminderRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.api.UserApi;
import healthy.lifestyle.backend.user.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkoutReminderServiceImpl implements WorkoutReminderService {

    @Autowired
    WorkoutApi workoutApi;

    @Autowired
    UserApi userApi;

    @Autowired
    DateTimeService dateTimeService;

    @Autowired
    WorkoutReminderRepository workoutReminderRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public WorkoutReminderResponseDto createWorkoutReminder(WorkoutReminderCreateRequestDto requestDto, long userId) {

        // Only one workout-reminder association is allowed
        workoutReminderRepository
                .findByUserIdAndWorkoutId(userId, requestDto.getWorkoutId())
                .ifPresent(reminder -> {
                    throw new ApiException(ErrorMessage.ALREADY_EXISTS, null, HttpStatus.BAD_REQUEST);
                });
        User user = userApi.getUserById(userId);
        Workout workout = workoutApi.getWorkoutById(requestDto.getWorkoutId());
        WorkoutReminder workoutReminder;

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(requestDto.getJsonDescription());

            ObjectNode childNode = mapper.createObjectNode();
            childNode.put("is_actual", true);
            childNode.put("data", requestDto.getJsonDescription());
            childNode.put("created_at", dateTimeService.getCurrentDate().toString());
            childNode.put("deactivated_at", "null");

            ObjectNode parentNode = mapper.createObjectNode();
            parentNode.putArray("time_descriptions").add(childNode);

            workoutReminder = WorkoutReminder.builder()
                    .jsonDescription(parentNode.toString())
                    .isActive(true)
                    .isPaused(false)
                    .createdAt(dateTimeService.getCurrentDate())
                    .workout(workout)
                    .user(user)
                    .build();

        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }

        WorkoutReminder workoutReminderSaved = workoutReminderRepository.save(workoutReminder);

        WorkoutReminderResponseDto responseDto =
                modelMapper.map(workoutReminderSaved, WorkoutReminderResponseDto.class);

        return responseDto;
    }

    @Override
    @Transactional
    public Page<WorkoutReminderResponseDto> getWorkoutRemindersWithFilter(
            long userId, Boolean isActive, String sortField, String sortDirection, int pageSize, int pageNumber) {

        Pageable pageable =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        Page<WorkoutReminder> workoutRemindersPage = workoutReminderRepository.findByUserId(userId, isActive, pageable);

        Page<WorkoutReminderResponseDto> responseDtoPage = workoutRemindersPage.map(workoutReminder -> {
            WorkoutReminderResponseDto responseDto = modelMapper.map(workoutReminder, WorkoutReminderResponseDto.class);
            return responseDto;
        });

        return responseDtoPage;
    }
    @Override
    @Transactional
    public WorkoutReminderResponseDto updateReminder(long userId, long workoutReminderId, WorkoutReminderUpdateRequestDto requestDto){

        WorkoutReminder reminder = workoutReminderRepository.findByUserIdAndWorkoutId(userId, workoutReminderId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_REMINDER_NOT_FOUND, workoutReminderId, HttpStatus.NOT_FOUND));

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(reminder.getJsonDescription());
            for(JsonNode desc : root.get("time_descriptions")){
                if(desc.get("is_actual").asBoolean()){
                    ((ObjectNode) desc).put("is_actual", false);
                    ((ObjectNode) desc).put("deactivated_at", dateTimeService.getCurrentDate().toString());
                }
            }

            ObjectNode node = mapper.createObjectNode();
            node.put("is_actual", true);
            node.put("data", requestDto.getJsonDescription());
            node.put("created_at",dateTimeService.getCurrentDate().toString());
            node.put("deactivated_at", "null");

            ArrayNode arrayNode = mapper.createArrayNode();
            root.get("time_descriptions").elements().forEachRemaining(arrayNode::add);
            arrayNode.add(node);

            ((ObjectNode)root).put("time_descriptions", arrayNode);

            reminder.setJsonDescription(root.toString());

        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }

        workoutReminderRepository.save(reminder);
        WorkoutReminderResponseDto responseDto = modelMapper.map(reminder, WorkoutReminderResponseDto.class);

        return responseDto;
    }
}
