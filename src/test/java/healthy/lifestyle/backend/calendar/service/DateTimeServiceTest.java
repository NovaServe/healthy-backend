package healthy.lifestyle.backend.calendar.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import healthy.lifestyle.backend.calendar.shared.service.DateTimeService;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateTimeServiceTest {
    @Spy
    DateTimeService dateTimeService;

    @Test
    void getCurrentServerZonedDateTime() {
        ZonedDateTime serverZonedDateTime = dateTimeService.getCurrentServerZonedDateTime();
        assertNotNull(serverZonedDateTime);
    }

    @Test
    void convertZonedDateTime() {
        ZonedDateTime serverZonedDateTime = dateTimeService.getCurrentServerZonedDateTime();
        ZonedDateTime convertedZonedDateTime =
                dateTimeService.convertToNewZone(serverZonedDateTime, TimeZone.getTimeZone("Europe/Kyiv"));
        assertNotNull(convertedZonedDateTime);
    }
}
