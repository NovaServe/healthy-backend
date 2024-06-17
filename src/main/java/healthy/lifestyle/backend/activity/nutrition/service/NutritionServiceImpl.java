package healthy.lifestyle.backend.activity.nutrition.service;

import healthy.lifestyle.backend.activity.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.activity.nutrition.model.Nutrition;
import healthy.lifestyle.backend.activity.nutrition.repository.NutritionRepository;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.Comparator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class NutritionServiceImpl implements NutritionService {
    @Autowired
    NutritionRepository nutritionRepository;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public NutritionResponseDto getNutritionById(long nutritionId, boolean requiredDefault, Long userId) {
        Nutrition nutrition = nutritionRepository
                .findById(nutritionId)
                .orElseThrow(
                        () -> new ApiException(ErrorMessage.NUTRITION_NOT_FOUND, nutritionId, HttpStatus.NOT_FOUND));

        if (nutrition.isCustom() && requiredDefault)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!nutrition.isCustom() && !requiredDefault)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (userId != null) {
            User user = userService.getUserById(userId);
            if (nutrition.isCustom()
                    && (user.getNutritions() == null || !user.getNutritions().contains(nutrition)))
                throw new ApiException(ErrorMessage.USER_NUTRITION_MISMATCH, nutritionId, HttpStatus.BAD_REQUEST);
        }

        NutritionResponseDto nutritionResponseDto = modelMapper.map(nutrition, NutritionResponseDto.class);
        List<HttpRefResponseDto> httpRefsSorted = nutritionResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();

        nutritionResponseDto.setHttpRefs(httpRefsSorted);
        return nutritionResponseDto;
    }
}
