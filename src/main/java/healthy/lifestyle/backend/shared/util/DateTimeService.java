package healthy.lifestyle.backend.shared.util;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DateTimeService {
    public TimeZone getDatabaseTimezone() {
        return TimeZone.getTimeZone("UTC");
    }

    public LocalDate getCurrentDatabaseDate() {
        return LocalDate.now(getDatabaseTimezone().toZoneId());
    }

    public int getCurrentDatabaseDayOfMonth() {
        return LocalDate.now(getDatabaseTimezone().toZoneId()).getDayOfMonth();
    }

    public DayOfWeek getCurrentDatabaseDayOfWeek() {
        return LocalDate.now(getDatabaseTimezone().toZoneId()).getDayOfWeek();
    }

    public int getCurrentDatabaseMonth() {
        return LocalDate.now(getDatabaseTimezone().toZoneId()).getMonthValue();
    }

    public int getCurrentDatabaseYear() {
        return LocalDate.now(getDatabaseTimezone().toZoneId()).getYear();
    }

    public ZonedDateTime getCurrentDatabaseZonedDateTime() {
        TimeZone timeZone = getDatabaseTimezone();
        ZoneId zoneId = timeZone.toZoneId();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(timeZone);
        long currentTimeMillis = calendar.getTimeInMillis();
        return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentTimeMillis), zoneId);
    }

    public LocalDate getCurrentDBDate() {
        return getCurrentDatabaseZonedDateTime().toLocalDate();
    }

    public ZonedDateTime convertToNewZone(ZonedDateTime sourceDateTime, TimeZone targetZone) {
        return sourceDateTime.withZoneSameInstant(targetZone.toZoneId());
    }

    public LocalDate convertToDBDate(LocalDate sourceDate, String userTimezoneName) {
        ZoneId userZone = TimeZone.getTimeZone(userTimezoneName).toZoneId();
        return sourceDate
                .atTime(LocalTime.NOON)
                .atZone(userZone)
                .withZoneSameInstant(this.getDatabaseTimezone().toZoneId())
                .toLocalDate();
    }

    public LocalDate convertToUserDate(LocalDate dbSourceDate, String userTimezoneName) {
        ZoneId userZone = TimeZone.getTimeZone(userTimezoneName).toZoneId();
        return dbSourceDate
                .atTime(LocalTime.NOON)
                .atZone(this.getDatabaseTimezone().toZoneId())
                .withZoneSameInstant(userZone)
                .toLocalDate();
    }

    public LocalDateTime convertToUserDateTime(LocalDateTime dbSourceDateTime, String userTimezoneName) {
        ZoneId userZone = TimeZone.getTimeZone(userTimezoneName).toZoneId();
        return dbSourceDateTime
                .atZone(this.getDatabaseTimezone().toZoneId())
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }

    public void displayTimeZone() {

        for (String timeZoneId : TimeZone.getAvailableIDs()) {

            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            long hours = TimeUnit.MILLISECONDS.toHours(timeZone.getRawOffset());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeZone.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours);
            minutes = Math.abs(minutes); // avoid -4:-30 issue

            if (timeZoneId.contains("Europe") || timeZoneId.contains("Canada") || timeZoneId.contains("US")) {
                String timeZoneText = "";
                if (hours > 0) {
                    timeZoneText = String.format("('%s', 'GMT+%d:%02d'),", timeZone.getID(), hours, minutes);
                } else {
                    timeZoneText = String.format("('%s', 'GMT%d:%02d'),", timeZone.getID(), hours, minutes);
                }

                System.out.println(timeZoneText);
            }
        }
    }

    public Map<String, Integer> subtractMinutes(int hour, int minutes, int notifyBeforeInMinutes) {

        if ((hour < 0 || hour > 23)
                || (minutes < 0 || minutes > 59)
                || (notifyBeforeInMinutes < 0 || notifyBeforeInMinutes > 60)) {
            throw new IllegalArgumentException("Subtract time illegal argument");
        }

        int newHours;
        int newMinutes;

        if (minutes >= notifyBeforeInMinutes) {
            newMinutes = minutes - notifyBeforeInMinutes;
            newHours = hour;
        } else {
            newMinutes = 60 + (minutes - notifyBeforeInMinutes);
            if (hour == 0) {
                newHours = 23;
            } else {
                newHours = hour - 1;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("hours", newHours);
        result.put("minutes", newMinutes);
        return result;
    }

    public boolean isTimeInBetweenSchedulers(DayOfWeek dayOfWeek, int hour, int minutes, TimeZone userTimeZone) {

        DayOfWeek currentDayOfWeek = getCurrentDatabaseDayOfWeek();
        if (!(dayOfWeek == null || (dayOfWeek.equals(currentDayOfWeek)))) {
            return false;
        }

        ZonedDateTime serverZonedDateTime = getCurrentDatabaseZonedDateTime();

        LocalDateTime userLocalDateTime = LocalDateTime.of(
                serverZonedDateTime.getYear(),
                serverZonedDateTime.getMonth(),
                serverZonedDateTime.getDayOfMonth(),
                hour,
                minutes);
        ZonedDateTime userZonedDateTime = ZonedDateTime.of(userLocalDateTime, userTimeZone.toZoneId());
        ZonedDateTime userZonedDateTimeInServerZone = convertToNewZone(userZonedDateTime, getDatabaseTimezone());

        if (serverZonedDateTime.getHour() == userZonedDateTimeInServerZone.getHour()) {
            return true;
        }

        return false;
    }
}
