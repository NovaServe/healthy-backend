package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import java.util.Comparator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BodyPartServiceImpl implements BodyPartService {
    @Autowired
    BodyPartRepository bodyPartRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<BodyPartResponseDto> getBodyParts() {
        List<BodyPart> bodyParts = bodyPartRepository.findAll();
        if (bodyParts.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);

        List<BodyPartResponseDto> responseDtoList = bodyParts.stream()
                .map(elt -> modelMapper.map(elt, BodyPartResponseDto.class))
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList();
        return responseDtoList;
    }
}
