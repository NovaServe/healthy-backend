package healthy.lifestyle.backend.shared.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class VerificationUtil {
    public boolean areFieldsNull(Object object, String... fieldsNames)
            throws NoSuchFieldException, IllegalAccessException {

        boolean allFieldsAreNull = true;
        Class<?> clazz = object.getClass();
        for (String fieldName : fieldsNames) {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            if (fieldValue != null) allFieldsAreNull = false;
        }
        return allFieldsAreNull;
    }

    public List<String> getFieldsWithSameValues(Object o1, Object o2, String... fieldsNames)
            throws NoSuchFieldException, IllegalAccessException {
        // Returns a list of the fields with equal non-null values.
        List<String> result = new ArrayList<>();
        Class<?> class1 = o1.getClass();
        Class<?> class2 = o2.getClass();
        for (String fieldName : fieldsNames) {
            Field field1 = class1.getDeclaredField(fieldName);
            field1.setAccessible(true);
            Object fieldValue1 = field1.get(o1);

            Field field2 = class2.getDeclaredField(fieldName);
            field2.setAccessible(true);
            Object fieldValue2 = field2.get(o2);

            if (fieldValue1 != null && fieldValue2 != null && fieldValue1.equals(fieldValue2)) {
                result.add(fieldName);
            }
        }
        return result;
    }

    public boolean areNestedEntitiesDifferent(List<Long> ids1, List<Long> ids2) {
        if (ids1.equals(ids2)) return false;
        return true;
    }
}
