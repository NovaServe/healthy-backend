package healthy.lifestyle.backend.workout.repository;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ExerciseJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    private final BodyPartRepository bodyPartRepository;

    public ExerciseJdbcRepository(JdbcTemplate jdbcTemplate, BodyPartRepository bodyPartRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.bodyPartRepository = bodyPartRepository;
    }

    public PageJdbc<Exercise> findWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            boolean needsEquipment,
            List<Long> bodyPartsIds,
            Pageable pageable) {

        String query = buildQuery(isCustom, userId, title, description, needsEquipment, bodyPartsIds, pageable);

        String countQuery = buildCountQuery(query);

        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class);
        List<ExerciseResultSetRow> exercisesRows = jdbcTemplate.query(query, new ExerciseRowMapper());
        List<Exercise> exercises = mapToExercises(exercisesRows);
        return null;
    }

    private String buildQuery(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            boolean needsEquipment,
            List<Long> bodyPartsIds,
            Pageable pageable) {

        String base = "SELECT e.*, ebp.exercise_id, ebp.body_part_id, bp.name FROM exercises AS e \n"
                + "INNER JOIN exercises_body_parts AS ebp ON e.id = ebp.exercise_id \n"
                + "INNER JOIN body_parts AS bp ON ebp.body_part_id = bp.id \n";
        String where = "WHERE ";
        String and = "AND ";
        String customAndDefaultFilter = "((e.is_custom = false) OR (e.is_custom = true AND e.user_id = ?)) \n";
        String customOnlyFilter = "(e.is_custom = true AND e.user_id = ?) \n";
        String defaultOnlyFilter = "e.is_custom = false \n";
        String needsEquipmentFilterMandatory = "e.needs_equipment = ? \n";
        String bodyPartsFilterOptional = "ebp.body_part_id IN (?) \n";
        String titleFilterOptional = "e.title ILIKE ? \n";
        String descriptionFilterOptional = "e.description ILIKE ? \n";
        String orderBy = "ORDER BY ? \n";
        String offset = "OFFSET ? \n";
        String limit = "LIMIT ?;";

        StringBuilder query = new StringBuilder();
        query.append(base);
        query.append(where);

        // Default or custom
        if (isCustom == null && userId != null)
            query.append(customAndDefaultFilter.replace("?", String.valueOf(userId)));

        if (isCustom != null && isCustom && userId != null) {
            query.append(customOnlyFilter.replace("?", String.valueOf(userId)));
        }

        if (isCustom != null && !isCustom) {
            query.append(defaultOnlyFilter);
        }

        // Needs equipment
        query.append(and);
        query.append(needsEquipmentFilterMandatory.replace("?", String.valueOf(needsEquipment)));

        // Title
        if (title != null) {
            query.append(and);
            query.append(titleFilterOptional);
        }

        // Description
        if (description != null) {
            query.append(and);
            query.append(descriptionFilterOptional);
        }

        // Body parts
        if (bodyPartsIds != null && bodyPartsIds.size() > 0) {
            query.append(and);
            StringBuilder ids = new StringBuilder();
            for (Long bodyPartId : bodyPartsIds) {
                ids.append(String.valueOf(bodyPartId));
                ids.append(", ");
            }
            ids.deleteCharAt(ids.length() - 1);
            ids.deleteCharAt(ids.length() - 1);
            query.append(bodyPartsFilterOptional.replace("?", ids.toString()));
        }

        // Order by
        if (pageable.getSort().isEmpty()) {
            query.append(orderBy.replace("?", "name ASC"));
        } else {
            StringBuilder orderBuilder = new StringBuilder();
            for (Sort.Order order : pageable.getSort()) {
                String sortField = order.getProperty();
                String sortFieldDirection = order.getDirection().toString();
                orderBuilder
                        .append("e.")
                        .append(sortField)
                        .append(" ")
                        .append(sortFieldDirection)
                        .append(", ");
            }
            orderBuilder.deleteCharAt(orderBuilder.length() - 1);
            orderBuilder.deleteCharAt(orderBuilder.length() - 1);
            query.append(orderBy.replace("?", orderBuilder.toString()));
        }

        // Offset
        if (pageable.getOffset() != 0) {
            query.append(offset.replace("?", String.valueOf(pageable.getOffset())));
        }

        // Limit
        int limitNumber = pageable.getPageSize() * (pageable.getPageNumber() + 1);
        query.append(limit.replace("?", String.valueOf(limitNumber)));

        return query.toString();
    }

    private String buildCountQuery(String query) {
        String initialQuery = query.substring(0, query.length() - 1);
        initialQuery = initialQuery.replaceAll("OFFSET.*\\n", "");
        initialQuery = initialQuery.replaceAll("LIMIT.*\\n", "");
        StringBuilder countQuery = new StringBuilder();
        countQuery.append("SELECT COUNT(*) FROM (\n");
        countQuery.append(initialQuery);
        countQuery.append(") AS subquery;");
        return countQuery.toString();
    }

    // SELECT e.*, ebp.exercise_id, ebp.body_part_id, bp.name FROM exercises AS e
    //    INNER JOIN exercises_body_parts AS ebp ON e.id = ebp.exercise_id
    //    INNER JOIN body_parts AS bp ON ebp.body_part_id = bp.id
    //    LIMIT 10
    //    OFFSET 3
    //    -- WHERE ebp.body_part_id IN (4, 6)
    //    -- AND e.title ILIKE '%circle%'
    //    -- AND e.is_custom = true
    //    ;

    //    SELECT COUNT(*) FROM (...)

    private List<Exercise> mapToExercises(List<ExerciseResultSetRow> exercisesRows) {
        List<Exercise> exercises = new ArrayList<>();

        for (ExerciseResultSetRow exerciseRow : exercisesRows) {
            Optional<Exercise> exerciseOptional = exercises.stream()
                    .filter(exercise -> Objects.equals(exercise.getId(), exerciseRow.getId()))
                    .findFirst();

            BodyPart bodyPart = bodyPartRepository
                    .findById(exerciseRow.getBodyPartId())
                    .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND));

            if (exerciseOptional.isEmpty()) {
                Exercise exercise = Exercise.builder()
                        .id(exerciseRow.getId())
                        .title(exerciseRow.getTitle())
                        .description(exerciseRow.getTitle())
                        .needsEquipment(exerciseRow.isNeedsEquipment())
                        .isCustom(exerciseRow.isCustom)
                        .build();
                exercise.addBodyPart(bodyPart);
                exercises.add(exercise);
            } else {
                Exercise exercise = exerciseOptional.get();
                exercise.addBodyPart(bodyPart);
            }
        }

        return exercises;
    }

    private static class ExerciseRowMapper implements RowMapper<ExerciseResultSetRow> {
        @Override
        public ExerciseResultSetRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            ExerciseResultSetRow exerciseResultSetRow = ExerciseResultSetRow.builder()
                    .id(rs.getLong("id"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .needsEquipment(rs.getBoolean("needs_equipment"))
                    .isCustom(rs.getBoolean("is_custom"))
                    .bodyPartId(rs.getLong("body_part_id"))
                    .build();
            return exerciseResultSetRow;
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ExerciseResultSetRow {
        private Long id;

        private String title;

        private String description;

        private boolean needsEquipment;

        private boolean isCustom;

        private Long bodyPartId;
    }
}
