package healthy.lifestyle.backend.testutil;

import healthy.lifestyle.backend.shared.util.JsonDescription;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class SharedUtil {
    public static String numberToText(int number) {
        if (number < 0 || number > 5) throw new IllegalArgumentException();
        Map<Integer, String> numbersToText = new HashMap<>();
        numbersToText.put(0, "Zero");
        numbersToText.put(1, "One");
        numbersToText.put(2, "Two");
        numbersToText.put(3, "Three");
        numbersToText.put(4, "Four");
        numbersToText.put(5, "Five");
        return numbersToText.get(number);
    }

    public static Map<String, String> seedToTimezone(int seed) {
        if (seed < 0 || seed > 5) throw new IllegalArgumentException();
        Map<String, String> result = new HashMap<>();
        switch (seed) {
            case 0 -> {
                result.put("GMT", "GMT+1:00");
                result.put("name", "Europe/Amsterdam");
            }
            case 1 -> {
                result.put("GMT", "GMT+1:00");
                result.put("name", "Europe/Andorra");
            }
            case 2 -> {
                result.put("GMT", "GMT+2:00");
                result.put("name", "Europe/Athens");
            }
            case 3 -> {
                result.put("GMT", "GMT0:00");
                result.put("name", "Europe/Belfast");
            }
            case 4 -> {
                result.put("GMT", "GMT+1:00");
                result.put("name", "Europe/Belgrade");
            }
            case 5 -> {
                result.put("GMT", "GMT+1:00");
                result.put("name", "Europe/Berlin");
            }
        }
        return result;
    }

    public static JsonDescription createJsonDescription(int seed) {
        return JsonDescription.builder()
                .json_id(seed)
                .dayOfWeek(DayOfWeek.of((seed % 7) + 1))
                .hours(seed % 24)
                .minutes(seed % 60)
                .build();
    }
}
