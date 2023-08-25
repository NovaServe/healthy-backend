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

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @ManyToMany(mappedBy = "httpRefs")
    @OrderBy("id")
    private Set<Exercise> exercises;

    public HttpRef() {}

    public HttpRef(Long id, String name, String ref, String description, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.ref = ref;
        this.description = description;
        this.isCustom = isCustom;
    }

    private HttpRef(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.ref = builder.ref;
        this.description = builder.description;
        this.isCustom = builder.isCustom;
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

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public Set<Exercise> getExercises() {
        return exercises;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String ref;
        private String description;

        private boolean isCustom;

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

        public Builder isCustom(boolean isCustom) {
            this.isCustom = isCustom;
            return this;
        }

        public HttpRef build() {
            return new HttpRef(this);
        }
    }
}
