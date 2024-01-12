package healthy.lifestyle.backend.nutrition.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.nutrition.model.Nutrition;
import healthy.lifestyle.backend.nutrition.repository.NutritionRepository;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionServiceImpl implements NutritionService {

    private final NutritionRepository nutritionRepository;
    private final UserService userService;

    private final ModelMapper modelMapper;

    public NutritionServiceImpl(
            NutritionRepository nutritionRepository, UserService userService, ModelMapper modelMapper) {
        this.nutritionRepository = nutritionRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
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

        return nutritionResponseDto;
    }
}
