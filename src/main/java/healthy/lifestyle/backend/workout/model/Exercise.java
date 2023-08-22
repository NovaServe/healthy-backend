package healthy.lifestyle.backend.workout.model;

import healthy.lifestyle.backend.users.model.User;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", unique = false, nullable = false)
    private String title;

    @Column(name = "description", unique = false, nullable = true)
    private String description;

    @Column(name = "is_custom", unique = false, nullable = false)
    private boolean isCustom;

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("id")
    @JoinTable(
            name = "exercises_body_parts",
            joinColumns = @JoinColumn(name = "exercise_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "body_part_id", referencedColumnName = "id"))
    private Set<BodyPart> bodyParts;

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("id")
    @JoinTable(
            name = "exercises_http_refs",
            joinColumns = @JoinColumn(name = "exercise_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "http_ref_id", referencedColumnName = "id"))
    private Set<HttpRef> httpRefs;

    @ManyToMany(mappedBy = "exercises")
    @OrderBy("id")
    private Set<User> users;

    public Exercise() {}

    public Exercise(
            Long id,
            String title,
            String description,
            boolean isCustom,
            Set<BodyPart> bodyParts,
            Set<HttpRef> httpRefs) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isCustom = isCustom;
        this.bodyParts = bodyParts;
        this.httpRefs = httpRefs;
    }

    public Exercise(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.isCustom = builder.isCustom;
        this.bodyParts = builder.bodyParts;
        this.httpRefs = builder.httpRefs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public Set<BodyPart> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(Set<BodyPart> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public Set<HttpRef> getHttpRefs() {
        return httpRefs;
    }

    public void setHttpRefs(Set<HttpRef> httpRefs) {
        this.httpRefs = httpRefs;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private boolean isCustom;
        private Set<BodyPart> bodyParts;
        private Set<HttpRef> httpRefs;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
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

        public Builder bodyParts(Set<BodyPart> bodyParts) {
            this.bodyParts = bodyParts;
            return this;
        }

        public Builder httpRefs(Set<HttpRef> httpRefs) {
            this.httpRefs = httpRefs;
            return this;
        }

        public Exercise build() {
            return new Exercise(this);
        }
    }
}
