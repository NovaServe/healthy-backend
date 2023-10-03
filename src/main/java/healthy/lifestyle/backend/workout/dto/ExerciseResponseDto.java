package healthy.lifestyle.backend.workout.dto;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExerciseResponseDto {
    private Long id;

    private String title;

    private String description;

    private List<BodyPartResponseDto> bodyParts;

    private List<HttpRefResponseDto> httpRefs;

    //    public ExerciseResponseDto() {
    //    }
    //
    //    public ExerciseResponseDto(
    //            Long id,
    //            String title,
    //            String description,
    //            List<BodyPartResponseDto> bodyParts,
    //            List<HttpRefResponseDto> httpRefs) {
    //        this.id = id;
    //        this.title = title;
    //        this.description = description;
    //        this.bodyParts = bodyParts;
    //        this.httpRefs = httpRefs;
    //    }
    //
    //    public ExerciseResponseDto(Builder builder) {
    //        this.id = builder.id;
    //        this.title = builder.title;
    //        this.description = builder.description;
    //        this.bodyParts = builder.bodyParts;
    //        this.httpRefs = builder.httpRefs;
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
    //    public String getTitle() {
    //        return title;
    //    }
    //
    //    public void setTitle(String title) {
    //        this.title = title;
    //    }
    //
    //    public String getDescription() {
    //        return description;
    //    }
    //
    //    public void setDescription(String description) {
    //        this.description = description;
    //    }
    //
    //    public List<BodyPartResponseDto> getBodyParts() {
    //        return bodyParts;
    //    }
    //
    //    public void setBodyParts(List<BodyPartResponseDto> bodyParts) {
    //        this.bodyParts = bodyParts;
    //    }
    //
    //    public List<HttpRefResponseDto> getHttpRefs() {
    //        return httpRefs;
    //    }
    //
    //    public void setHttpRefs(List<HttpRefResponseDto> httpRefs) {
    //        this.httpRefs = httpRefs;
    //    }
    //
    //    public static class Builder {
    //        private Long id;
    //        private String title;
    //        private String description;
    //        private List<BodyPartResponseDto> bodyParts;
    //        private List<HttpRefResponseDto> httpRefs;
    //
    //        public Builder id(Long id) {
    //            this.id = id;
    //            return this;
    //        }
    //
    //        public Builder title(String title) {
    //            this.title = title;
    //            return this;
    //        }
    //
    //        public Builder description(String description) {
    //            this.description = description;
    //            return this;
    //        }
    //
    //        public Builder bodyParts(List<BodyPartResponseDto> bodyParts) {
    //            this.bodyParts = bodyParts;
    //            return this;
    //        }
    //
    //        public Builder httpRefs(List<HttpRefResponseDto> httpRefs) {
    //            this.httpRefs = httpRefs;
    //            return this;
    //        }
    //
    //        public ExerciseResponseDto build() {
    //            return new ExerciseResponseDto(this);
    //        }
    //    }
}
