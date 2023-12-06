package healthy.lifestyle.backend.data.bodypart;

import healthy.lifestyle.backend.data.shared.NestedDefaultEntityTestWrapperBase;
import healthy.lifestyle.backend.workout.model.BodyPart;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BodyPartTestBuilder {
    public BodyPartWrapper getWrapper() {
        return new BodyPartWrapper();
    }

    public static class BodyPartWrapper implements NestedDefaultEntityTestWrapperBase<BodyPart> {
        private Integer idOrSeed;

        private Integer amountOfEntities;

        private BodyPart entity;

        private List<BodyPart> entities;

        @Override
        public BodyPartWrapper setIdOrSeed(int idOrSeed) {
            if (idOrSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public BodyPartWrapper setAmountOfEntities(int amountOfEntities) {
            if (amountOfEntities < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfEntities = amountOfEntities;
            return this;
        }

        @Override
        public BodyPart buildSingle() {
            if (this.idOrSeed == null) throw new IllegalStateException("Not all required parameters are set");
            this.entity = BodyPart.builder()
                    .id((long) this.idOrSeed)
                    .name("Name " + this.idOrSeed)
                    .build();
            return this.entity;
        }

        @Override
        public List<BodyPart> buildList() {
            if (this.idOrSeed == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.idOrSeed;
            for (int i = 1; i <= this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                this.entities.add(
                        BodyPart.builder().id((long) id).name("Name " + id).build());
                id++;
            }
            return this.entities;
        }

        @Override
        public BodyPart getSingle() {
            return this.entity;
        }

        @Override
        public List<BodyPart> getSingleAsList() {
            List<BodyPart> list = new ArrayList<>();
            list.add(this.entity);
            return list;
        }

        @Override
        public BodyPart getByIndexFromList(int index) {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            return this.entities.get(index);
        }

        @Override
        public Long getSingleId() {
            return this.entity.getId();
        }

        @Override
        public List<BodyPart> getAll() {
            return this.entities;
        }

        @Override
        public Integer size() {
            if (this.entities == null) return null;
            return this.entities.size();
        }

        @Override
        public Object getFieldValueFromListElt(int eltIndex, String fieldName)
                throws IllegalAccessException, NoSuchFieldException {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            BodyPart obj = this.entities.get(eltIndex);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }

        @Override
        public void setFieldValueToListElt(int eltIndex, String fieldName, Object value)
                throws IllegalAccessException, NoSuchFieldException {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (eltIndex < 0 || eltIndex >= this.entities.size())
                throw new IllegalArgumentException("Index must be >= 0 and < list size");

            BodyPart obj = this.entities.get(eltIndex);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }
    }
}
