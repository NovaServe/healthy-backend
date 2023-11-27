package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.workout.model.BodyPart;
import java.util.ArrayList;
import java.util.List;

public class BodyPartTestBuilder {
    public BodyPartWrapper getWrapper() {
        return new BodyPartWrapper();
    }

    public static class BodyPartWrapper {
        private BodyPart entity;

        private List<BodyPart> entities;

        private Integer id;

        private Integer startId;

        private Integer amountOfEntities;

        /**
         * Single BodyPart Functionality
         */
        public BodyPartWrapper setId(int id) {
            if (id < 0) throw new IllegalArgumentException("Must be >= 0");
            this.id = id;
            return this;
        }

        public BodyPart buildSingleBodyPart() {
            if (this.id == null) throw new IllegalStateException("Not all required parameters are set");
            this.entity = BodyPart.builder()
                    .id((long) this.id)
                    .name("Name " + this.id)
                    .build();
            return this.entity;
        }

        public BodyPart getSingle() {
            return this.entity;
        }

        public long getSingleId() {
            return this.entity.getId();
        }

        /**
         * BodyParts Functionality
         */
        public BodyPartWrapper setAmountOfEntities(int amountOfEntities) {
            if (amountOfEntities < 2) throw new IllegalArgumentException("Must be >= 2");
            this.amountOfEntities = amountOfEntities;
            return this;
        }

        public BodyPartWrapper setStartId(int startId) {
            if (startId < 0) throw new IllegalArgumentException("Must be >= 0");
            this.startId = startId;
            return this;
        }

        public List<BodyPart> buildBodyPartsList() {
            if (this.startId == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int id = this.startId;
            for (int i = 1; i <= this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                this.entities.add(
                        BodyPart.builder().id((long) id).name("Name " + id).build());
                id++;
            }
            return this.entities;
        }

        public List<BodyPart> getEntities() {
            return this.entities;
        }

        public Integer size() {
            if (this.entities == null) return null;
            return this.entities.size();
        }

        public BodyPart getByIndex(int index) {
            if (this.entities == null || this.entities.size() == 0)
                throw new IllegalStateException("Not all required parameters are set");
            return this.entities.get(index);
        }
    }
}
