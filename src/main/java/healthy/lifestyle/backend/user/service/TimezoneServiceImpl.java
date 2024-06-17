package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.user.dto.TimezoneResponseDto;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.repository.TimezoneRepository;
import java.util.Comparator;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TimezoneServiceImpl implements TimezoneService {

    @Autowired
    TimezoneRepository timezoneRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public List<TimezoneResponseDto> getTimezones() {
        List<Timezone> timezones = timezoneRepository.findAll();
        if (timezones.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);

        return timezones.stream()
                .map(timezone -> modelMapper.map(timezone, TimezoneResponseDto.class))
                .sorted(Comparator.comparing(TimezoneResponseDto::getId))
                .toList();
    }
}
