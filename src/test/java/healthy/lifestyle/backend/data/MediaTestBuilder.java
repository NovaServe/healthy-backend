package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class MediaTestBuilder {
    public MediaWrapper getWrapper() {
        return new MediaWrapper();
    }

    public static class MediaWrapper {
        private HttpRef entity;

        private List<HttpRef> entities;

        private Boolean mediaCustom;

        private Integer id;

        private Integer startId;

        private Integer amountOfEntities;

        private User user;

        /**
         * Single Media Functionality
         */
        public MediaWrapper setId(Integer id) {
            if (id < 0) throw new IllegalArgumentException("Must be >= 0");
            this.id = id;
            return this;
        }

        public HttpRef buildSingleMedia() {
            if (this.id == null) throw new IllegalStateException("Not all required parameters are set");
            if (this.mediaCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");
            this.entity = HttpRef.builder()
                    .id((long) this.id)
                    .name("Name " + this.id)
                    .description("Description " + this.id)
                    .ref("https://reference-" + this.id)
                    .isCustom(this.mediaCustom)
                    .user(user)
                    .build();
            return this.entity;
        }

        public HttpRef getSingle() {
            return this.entity;
        }

        public Long getSingleId() {
            return this.entity.getId();
        }

        public List<HttpRef> getEntityCollection() {
            List<HttpRef> entityCollection = new ArrayList<>();
            entityCollection.add(this.entity);
            return entityCollection;
        }

        /**
         * Medias Functionality
         */
        public MediaWrapper setAmountOfEntities(int amountOfEntities) {
            if (amountOfEntities < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfEntities = amountOfEntities;
            return this;
        }

        public MediaWrapper setStartId(Integer startId) {
            if (startId < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startId = startId;
            return this;
        }

        public List<HttpRef> buildMediasList() {
            if (this.startId == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");
            if (this.mediaCustom && this.user == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.startId;
            for (int i = 0; i < this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                this.entities.add(HttpRef.builder()
                        .id((long) this.id)
                        .name("Name " + this.id)
                        .description("Description " + this.id)
                        .ref("https://reference-" + this.id)
                        .isCustom(this.mediaCustom)
                        .user(user)
                        .build());
                id++;
            }
            return this.entities;
        }

        public List<HttpRef> getEntities() {
            return entities;
        }

        public HttpRef getByIndex(int index) {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (index < 0 || index >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");
            return this.entities.get(index);
        }

        public Integer size() {
            if (this.entities == null) return null;
            return this.entities.size();
        }

        public Object getFieldValueFromListElt(int index, String fieldName)
                throws IllegalAccessException, NoSuchFieldException {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (index < 0 || index >= this.entities.size())
                throw new IllegalArgumentException("Must be >= 0 and < list size");

            HttpRef obj = this.entities.get(index);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }

        public void setFieldValueToListElt(int index, String fieldName, Object value)
                throws IllegalAccessException, NoSuchFieldException {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            if (index < 0 || index >= this.entities.size())
                throw new IllegalArgumentException("Index must be >= 0 and < list size");

            HttpRef obj = this.entities.get(index);
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }

        /**
         * User Related Functionality
         */
        public MediaWrapper setMediaCustom(boolean mediaCustom) {
            this.mediaCustom = mediaCustom;
            return this;
        }

        public MediaWrapper setUser(User user) {
            this.user = user;
            return this;
        }

        public User getUser() {
            return this.user;
        }
    }
}
