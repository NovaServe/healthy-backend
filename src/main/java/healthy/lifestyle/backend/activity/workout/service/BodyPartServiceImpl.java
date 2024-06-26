package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
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
