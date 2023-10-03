package healthy.lifestyle.backend.workout.model;

import jakarta.persistence.*;
import java.util.Set;
import lombok.*;

/**
 * body_parts table contains default values only.
 * Users cannot change values in this table.
 */
@Entity
@Table(name = "body_parts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BodyPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "bodyParts")
    @OrderBy("id")
    private Set<Exercise> exercises;

    //    public BodyPart() {}
    //
    //    public BodyPart(Long id, String name) {
    //        this.id = id;
    //        this.name = name;
    //    }
    //
    //    private BodyPart(Builder builder) {
    //        this.id = builder.id;
    //        this.name = builder.name;
    //    }
    //
    //    public Long getId() {
    //        return id;
    //    }
    //
    //    public void setId(Long id) {
    //        this.id = id;
    //    }
    //
    //    public String getName() {
    //        return name;
    //    }
    //
    //    public void setName(String name) {
    //        this.name = name;
    //    }
    //
    //    public void setExercises(Set<Exercise> exercises) {
    //        this.exercises = exercises;
    //    }
    //
    //    public Set<Exercise> getExercises() {
    //        return exercises;
    //    }
    //
    //    public static class Builder {
    //        private Long id;
    //        private String name;
    //
    //        public Builder id(Long id) {
    //            this.id = id;
    //            return this;
    //        }
    //
    //        public Builder name(String name) {
    //            this.name = name;
    //            return this;
    //        }
    //
    //        public BodyPart build() {
    //            return new BodyPart(this);
    //        }
    //    }
}
