package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BodyPartServiceImpl implements BodyPartService {
    private final BodyPartRepository bodyPartRepository;

    public BodyPartServiceImpl(BodyPartRepository bodyPartRepository) {
        this.bodyPartRepository = bodyPartRepository;
    }

    @Override
    public List<BodyPartResponseDto> getBodyParts() {
        List<BodyPart> bodyParts = bodyPartRepository.findAll();
        return mapBodyPartToBodyPartResponseDto(bodyParts);
    }

    private List<BodyPartResponseDto> mapBodyPartToBodyPartResponseDto(List<BodyPart> bodyParts) {
        if (isNull(bodyParts) || bodyParts.size() == 0)
            throw new ApiException(ErrorMessage.SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

        List<BodyPartResponseDto> responseDto = new ArrayList<>();
        for (BodyPart bodyPart : bodyParts) {
            BodyPartResponseDto dto = new BodyPartResponseDto.Builder()
                    .id(bodyPart.getId())
                    .name(bodyPart.getName())
                    .build();
            responseDto.add(dto);
        }
        responseDto.sort(Comparator.comparingLong(BodyPartResponseDto::getId));
        return responseDto;
    }
}
