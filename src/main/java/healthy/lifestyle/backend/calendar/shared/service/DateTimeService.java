package healthy.lifestyle.backend.calendar.shared.service;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DateTimeService {
    public TimeZone getServerTimezone() {
        // GMT 0:00
        return TimeZone.getTimeZone("Europe/London");
    }

    public LocalDate getCurrentDate() {
        return LocalDate.now(getServerTimezone().toZoneId());
    }

    public int getCurrentDayOfMonth() {
        return LocalDate.now(getServerTimezone().toZoneId()).getDayOfMonth();
    }

    public DayOfWeek getCurrentDayOfWeek() {
        return LocalDate.now(getServerTimezone().toZoneId()).getDayOfWeek();
    }

    public int getCurrentMonth() {
        return LocalDate.now(getServerTimezone().toZoneId()).getMonthValue();
    }

    public int getCurrentYear() {
        return LocalDate.now(getServerTimezone().toZoneId()).getYear();
    }

    public ZonedDateTime getCurrentServerZonedDateTime() {
        TimeZone timeZone = getServerTimezone();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(timeZone);
        long currentTimeMillis = calendar.getTimeInMillis();
        ZoneId zoneId = timeZone.toZoneId();
        return ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentTimeMillis), zoneId);
    }

    public ZonedDateTime convertToNewZone(ZonedDateTime sourceDateTime, ZoneId targetZone) {
        return sourceDateTime.withZoneSameInstant(targetZone);
    }

    public ZonedDateTime convertToNewZone(ZonedDateTime sourceDateTime, TimeZone targetZone) {
        return sourceDateTime.withZoneSameInstant(targetZone.toZoneId());
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

        DayOfWeek currentDayOfWeek = getCurrentDayOfWeek();
        if (!(dayOfWeek == null || (dayOfWeek.equals(currentDayOfWeek)))) {
            return false;
        }

        ZonedDateTime serverZonedDateTime = getCurrentServerZonedDateTime();

        LocalDateTime userLocalDateTime = LocalDateTime.of(
                serverZonedDateTime.getYear(),
                serverZonedDateTime.getMonth(),
                serverZonedDateTime.getDayOfMonth(),
                hour,
                minutes);
        ZonedDateTime userZonedDateTime = ZonedDateTime.of(userLocalDateTime, userTimeZone.toZoneId());
        ZonedDateTime userZonedDateTimeInServerZone = convertToNewZone(userZonedDateTime, getServerTimezone());

        if (serverZonedDateTime.getHour() == userZonedDateTimeInServerZone.getHour()) {
            return true;
        }

        return false;
    }
}
