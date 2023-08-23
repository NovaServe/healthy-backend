package healthy.lifestyle.backend.workout.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "http_refs")
public class HttpRef {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = false, nullable = false)
    private String name;

    @Column(name = "ref", unique = false, nullable = false)
    private String ref;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @ManyToMany(mappedBy = "httpRefs")
    @OrderBy("id")
    private Set<Exercise> exercises;

    public HttpRef() {}

    public HttpRef(Long id, String name, String ref, String description) {
        this.id = id;
        this.name = name;
        this.ref = ref;
        this.description = description;
    }

    public HttpRef(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.ref = builder.ref;
        this.description = builder.description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Exercise> getExercises() {
        return exercises;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String ref;
        private String description;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public HttpRef build() {
            return new HttpRef(this);
        }
    }
}
