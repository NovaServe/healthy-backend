package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalTypeResponseDto;
import healthy.lifestyle.backend.mental.model.MentalType;
import healthy.lifestyle.backend.mental.repository.MentalTypeRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import java.util.Comparator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MentalTypeServiceImpl implements MentalTypeService {

    private final MentalTypeRepository mentalTypeRepository;
    private final ModelMapper modelMapper;

    public MentalTypeServiceImpl(MentalTypeRepository mentalTypeRepository, ModelMapper modelMapper) {
        this.mentalTypeRepository = mentalTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<MentalTypeResponseDto> getMentalType() {
        List<MentalType> mentalTypes = mentalTypeRepository.findAll();
        if (mentalTypes.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);

        return mentalTypes.stream()
                .map(mentalType -> modelMapper.map(mentalType, MentalTypeResponseDto.class))
                .sorted(Comparator.comparing(MentalTypeResponseDto::getName))
                .toList();
    }
}
