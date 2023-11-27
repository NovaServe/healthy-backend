package healthy.lifestyle.backend.data;

import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
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

    public static class BodyPartWrapper {
        private final BodyPartRepository bodyPartRepository;

        private BodyPart entity;

        private List<BodyPart> entities;

        private Integer seed;

        private Integer amountOfEntities;

        public BodyPartWrapper(BodyPartRepository bodyPartRepository) {
            this.bodyPartRepository = bodyPartRepository;
        }

        /**
         * Single BodyPart Functionality
         */
        public BodyPart buildSingleBodyPart() {
            if (this.seed == null) throw new IllegalStateException("Not all required parameters are set");
            BodyPart bodyPart = BodyPart.builder().name("Name " + this.seed).build();
            this.entity = bodyPartRepository.save(bodyPart);
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

        public BodyPartWrapper setSeed(int seed) {
            this.seed = seed;
            return this;
        }

        public List<BodyPart> buildBodyPartsList() {
            if (this.seed == null || this.amountOfEntities == null)
                throw new IllegalStateException("Not all required parameters are set");

            int startSeed = this.seed;
            for (int i = 0; i < this.amountOfEntities; i++) {
                if (this.entities == null) this.entities = new ArrayList<>();
                BodyPart bodyPart = bodyPartRepository.save(
                        BodyPart.builder().name("Name " + startSeed).build());
                this.entities.add(bodyPart);
                startSeed++;
            }
            return this.entities;
        }

        public List<BodyPart> getBodyPartsSorted() {
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
