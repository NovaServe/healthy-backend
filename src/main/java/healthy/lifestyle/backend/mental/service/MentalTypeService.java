package healthy.lifestyle.backend.mental.service;

import healthy.lifestyle.backend.mental.dto.MentalTypeResponseDto;
import java.util.List;

public interface MentalTypeService {
    List<MentalTypeResponseDto> getMentalType();
}
