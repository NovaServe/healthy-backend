package healthy.lifestyle.backend.testutil;

import java.util.HashMap;
import java.util.Map;

public class Shared {
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
}
