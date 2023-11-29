package healthy.lifestyle.backend.data.shared;

import java.util.List;

public interface NestedDefaultEntityTestWrapperBase<T> {
    NestedDefaultEntityTestWrapperBase<T> setIdOrSeed(int idOrSeed);

    NestedDefaultEntityTestWrapperBase<T> setAmountOfEntities(int amountOfEntities);

    T buildSingle();

    List<T> buildList();

    T getSingle();

    List<T> getSingleAsList();

    T getByIndexFromList(int index);

    Long getSingleId();

    List<T> getAll();

    Integer size();

    Object getFieldValueFromListElt(int eltIndex, String fieldName) throws IllegalAccessException, NoSuchFieldException;

    void setFieldValueToListElt(int eltIndex, String fieldName, Object value)
            throws IllegalAccessException, NoSuchFieldException;
}
