package healthy.lifestyle.backend.activity.mental.service;

import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalWorkoutResponseDto;
import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
import healthy.lifestyle.backend.activity.mental.model.MentalWorkout;
import healthy.lifestyle.backend.activity.mental.repository.MentalActivityRepository;
import healthy.lifestyle.backend.activity.mental.repository.MentalWorkoutRepository;
import healthy.lifestyle.backend.activity.workout.dto.*;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentalWorkoutServiceImpl implements MentalWorkoutService {
    @Autowired
    MentalWorkoutRepository mentalWorkoutRepository;

    @Autowired
    MentalActivityRepository mentalActivityRepository;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    VerificationUtil verificationUtil;

    @Override
    @Transactional
    public MentalWorkoutResponseDto createCustomMentalWorkout(long userId, MentalWorkoutCreateRequestDto requestDto) {
        List<MentalWorkout> mentalWorkoutsWithSameTitle =
                mentalWorkoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);

        if (!mentalWorkoutsWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        Set<MentalActivity> mentalActivitySet = new HashSet<>();

        for (long mentalActivityId : requestDto.getMentalActivityIds()) {
            MentalActivity mentalActivity = mentalActivityRepository
                    .findById(mentalActivityId)
                    .orElseThrow(() ->
                            new ApiException(ErrorMessage.MENTAL_NOT_FOUND, mentalActivityId, HttpStatus.NOT_FOUND));

            if (mentalActivity.isCustom() && !mentalActivity.getUser().equals(user))
                throw new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, mentalActivityId, HttpStatus.BAD_REQUEST);

            mentalActivitySet.add(mentalActivity);
        }

        MentalWorkout mentalWorkout = MentalWorkout.builder()
                .isCustom(true)
                .user(user)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .mentalActivities(mentalActivitySet)
                .build();
        MentalWorkout savedMentalWorkout = mentalWorkoutRepository.save(mentalWorkout);
        userService.addMentalWorkoutToUser(user, savedMentalWorkout);

        MentalWorkoutResponseDto mentalWorkoutResponseDto =
                modelMapper.map(savedMentalWorkout, MentalWorkoutResponseDto.class);

        List<MentalActivityResponseDto> mentalActivityResponseDtoList =
                savedMentalWorkout.getMentalActivitiesSortedById().stream()
                        .map(mentalActivity -> modelMapper.map(mentalActivity, MentalActivityResponseDto.class))
                        .peek(elt -> {
                            List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
                                    .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                                    .toList();
                            elt.setHttpRefs(httpRefsSorted);
                        })
                        .toList();

        mentalWorkoutResponseDto.setMentalActivities(mentalActivityResponseDtoList);
        return mentalWorkoutResponseDto;
    }
}
