package healthy.lifestyle.backend.data.httpref;

import healthy.lifestyle.backend.data.shared.NestedCustomEntityTestWrapperBase;
import healthy.lifestyle.backend.data.shared.NestedDefaultEntityTestWrapperBase;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class HttpRefJpaTestBuilder {
    @Autowired
    HttpRefRepository httpRefRepository;

    public HttpRefWrapper getWrapper() {
        return new HttpRefWrapper(this.httpRefRepository);
    }

    public static class HttpRefWrapper
            implements NestedDefaultEntityTestWrapperBase<HttpRef>, NestedCustomEntityTestWrapperBase {
        private Integer idOrSeed;

        private Boolean isCustom;

        private Integer amountOfEntities;

        private HttpRef entity;

        private List<HttpRef> entities;

        private User user;

        private final HttpRefRepository httpRefRepository;

        public HttpRefWrapper(HttpRefRepository httpRefRepository) {
            this.httpRefRepository = httpRefRepository;
        }

        @Override
        public HttpRefWrapper setIdOrSeed(int idOrSeed) {
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public HttpRefWrapper setAmountOfEntities(int amountOfEntities) {
            this.amountOfEntities = amountOfEntities;
            return this;
        }

        @Override
        public HttpRef buildSingle() {
            if (this.idOrSeed == null || this.isCustom == null)
                throw new IllegalStateException("Not all required parameters are set");
            if (this.isCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");

            HttpRef httpRef = HttpRef.builder()
                    .name("Name " + this.idOrSeed)
                    .description("Description " + this.idOrSeed)
                    .ref("https://reference-" + this.idOrSeed)
                    .isCustom(this.isCustom)
                    .user(user)
                    .build();

            this.entity = httpRefRepository.save(httpRef);
            return this.entity;
        }

        @Override
        public List<HttpRef> buildList() {
            if (this.idOrSeed == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");
            if (this.isCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");

            int startSeed = this.idOrSeed;
            for (int i = 0; i < this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                HttpRef httpRef = httpRefRepository.save(HttpRef.builder()
                        .name("Name " + this.idOrSeed)
                        .description("Description " + this.idOrSeed)
                        .ref("https://reference-" + this.idOrSeed)
                        .isCustom(this.isCustom)
                        .user(user)
                        .build());
                this.entities.add(httpRef);
                startSeed++;
            }
            return this.entities;
        }

        @Override
        public HttpRef getSingle() {
            return this.entity;
        }

        @Override
        public List<HttpRef> getSingleAsList() {
            List<HttpRef> list = new ArrayList<>();
            list.add(this.entity);
            return list;
        }

        @Override
        public HttpRef getByIndexFromList(int index) {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (index < 0 || index >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");
            return this.entities.get(index);
        }

        @Override
        public Long getSingleId() {
            return this.entity.getId();
        }

        @Override
        public List<HttpRef> getAll() {
            return entities;
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

            HttpRef obj = this.entities.get(eltIndex);
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

            HttpRef obj = this.entities.get(eltIndex);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }

        @Override
        public HttpRefWrapper setIsCustom(boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        @Override
        public HttpRefWrapper setUser(User user) {
            this.user = user;
            return this;
        }

        @Override
        public User getUser() {
            return this.user;
        }
    }
}
