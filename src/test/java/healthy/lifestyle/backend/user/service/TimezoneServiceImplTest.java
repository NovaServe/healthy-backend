package healthy.lifestyle.backend.user.service;

import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.dto.TimezoneResponseDto;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.repository.TimezoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimezoneServiceImplTest {
    @InjectMocks
    TimezoneServiceImpl timezoneService;

    @Mock
    TimezoneRepository timezoneRepository;

    @Spy
    ModelMapper modelMapper;

    TestUtil testUtil = new TestUtil();

    @Test
    void getTimezones_shouldReturnDtoList_whenValidRequest() {
        // Given
        Timezone timezone1 = testUtil.createTimezone(1L, "GMT0:00", "Europe/London");
        Timezone timezone2 = testUtil.createTimezone(2L, "GMT+1:00", "Europe/Luxembourg");
        Timezone timezone3 = testUtil.createTimezone(3L, "GMT+1:00", "Europe/Madrid");
        List<Timezone> timezones = List.of(timezone1, timezone2, timezone3);
        when(timezoneRepository.findAll()).thenReturn(timezones);

        // When
        List<TimezoneResponseDto> timezoneResponseDtoList = timezoneService.getTimezones();

        // Then
        verify(timezoneRepository, times(1)).findAll();
        assertEquals(timezones.size(), timezoneResponseDtoList.size());
        assertThat(timezoneResponseDtoList)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(timezones);

    }
}