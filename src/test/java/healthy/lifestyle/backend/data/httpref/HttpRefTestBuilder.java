package healthy.lifestyle.backend.data.httpref;

import healthy.lifestyle.backend.data.shared.NestedCustomEntityTestWrapperBase;
import healthy.lifestyle.backend.data.shared.NestedDefaultEntityTestWrapperBase;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class HttpRefTestBuilder {
    public HttpRefWrapper getWrapper() {
        return new HttpRefWrapper();
    }

    public static class HttpRefWrapper
            implements NestedDefaultEntityTestWrapperBase<HttpRef>, NestedCustomEntityTestWrapperBase {
        private Boolean isCustom;

        private Integer idOrSeed;

        private Integer amountOfEntities;

        private HttpRef entity;

        private List<HttpRef> entities;

        private User user;

        @Override
        public HttpRefWrapper setIdOrSeed(int idOrSeed) {
            if (idOrSeed < 0) throw new IllegalArgumentException("Must be >= 0");
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public HttpRefWrapper setAmountOfEntities(int amountOfEntities) {
            if (amountOfEntities < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfEntities = amountOfEntities;
            return this;
        }

        @Override
        public HttpRef buildSingle() {
            if (this.idOrSeed == null) throw new IllegalStateException("Not all required parameters are set");
            if (this.isCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");
            this.entity = HttpRef.builder()
                    .id((long) this.idOrSeed)
                    .name("Name " + this.idOrSeed)
                    .description("Description " + this.idOrSeed)
                    .ref("https://reference-" + this.idOrSeed)
                    .isCustom(this.isCustom)
                    .user(user)
                    .build();
            return this.entity;
        }

        @Override
        public List<HttpRef> buildList() {
            if (this.idOrSeed == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");
            if (this.isCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.idOrSeed;
            for (int i = 0; i < this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                this.entities.add(HttpRef.builder()
                        .id((long) this.idOrSeed)
                        .name("Name " + this.idOrSeed)
                        .description("Description " + this.idOrSeed)
                        .ref("https://reference-" + this.idOrSeed)
                        .isCustom(this.isCustom)
                        .user(user)
                        .build());
                id++;
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
            if (this.entities == null || this.entities.isEmpty())
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

        public HttpRefWrapper setIsCustom(boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public HttpRefWrapper setUser(User user) {
            this.user = user;
            return this;
        }

        public User getUser() {
            return this.user;
        }
    }
}
