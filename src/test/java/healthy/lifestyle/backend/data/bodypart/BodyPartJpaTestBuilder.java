package healthy.lifestyle.backend.data.bodypart;

import healthy.lifestyle.backend.data.shared.NestedDefaultEntityTestWrapperBase;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class BodyPartJpaTestBuilder {
    @Autowired
    private BodyPartRepository bodyPartRepository;

    public BodyPartJpaTestBuilder() {}

    public BodyPartWrapper getWrapper() {
        return new BodyPartWrapper(this.bodyPartRepository);
    }

    public static class BodyPartWrapper implements NestedDefaultEntityTestWrapperBase<BodyPart> {
        private Integer idOrSeed;

        private Integer amountOfEntities;

        private BodyPart entity;

        private List<BodyPart> entities;

        private final BodyPartRepository bodyPartRepository;

        public BodyPartWrapper(BodyPartRepository bodyPartRepository) {
            this.bodyPartRepository = bodyPartRepository;
        }

        @Override
        public BodyPartWrapper setIdOrSeed(int idOrSeed) {
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
            BodyPart bodyPart = BodyPart.builder().name("Name " + this.idOrSeed).build();
            this.entity = bodyPartRepository.save(bodyPart);
            return this.entity;
        }

        @Override
        public List<BodyPart> buildList() {
            if (this.idOrSeed == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int startSeed = this.idOrSeed;
            for (int i = 0; i < this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                BodyPart bodyPart = bodyPartRepository.save(
                        BodyPart.builder().name("Name " + startSeed).build());
                this.entities.add(bodyPart);
                startSeed++;
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
