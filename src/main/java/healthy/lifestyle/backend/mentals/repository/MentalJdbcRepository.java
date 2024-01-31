package healthy.lifestyle.backend.mentals.repository;

import healthy.lifestyle.backend.mentals.model.Mental;
import healthy.lifestyle.backend.mentals.model.MentalType;
import healthy.lifestyle.backend.workout.repository.PageJdbc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class MentalJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private final MentalTypeRepository mentalTypeRepository;

    public MentalJdbcRepository(JdbcTemplate jdbcTemplate, MentalTypeRepository mentalTypeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.mentalTypeRepository = mentalTypeRepository;
    }

    public PageJdbc<Mental> findWithFilter(
            Boolean isCustom, Long userId, String title, String description, MentalType mentalType, Pageable pageable) {

        String query = buildQuery(isCustom, userId, title, description, mentalType, pageable);

        String countQuery = buildCountQuery(query);

        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class);
        List<MentalJdbcRepository.MentalResultSetRow> mentalRows =
                jdbcTemplate.query(query, new MentalJdbcRepository.MentalRowMapper());
        List<Mental> mentals = mapToMentals(mentalRows);
        return null;
    }

    private String buildQuery(
            Boolean isCustom, Long userId, String title, String description, MentalType mentalType, Pageable pageable) {

        String base = "SELECT m.* FROM mentals AS m \n";
        String where = "WHERE ";
        String and = "AND ";
        String customAndDefaultFilter = "((m.is_custom = false) OR (m.is_custom = true AND m.user_id = ?)) \n";
        String customOnlyFilter = "(m.is_custom = true AND m.user_id = ?) \n";
        String defaultOnlyFilter = "m.is_custom = false \n";
        String titleFilterOptional = "m.title ILIKE ? \n";
        String descriptionFilterOptional = "m.description ILIKE ? \n";
        String mentalTypeFilterOptional = "m.type IS NULL OR m.type = :type \n";
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

        // MentalType
        if (mentalType != null) {
            query.append(and);
            query.append(mentalTypeFilterOptional);
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
                        .append("m.")
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

    private List<Mental> mapToMentals(List<MentalJdbcRepository.MentalResultSetRow> mentalsRows) {
        List<Mental> mentals = new ArrayList<>();

        for (MentalJdbcRepository.MentalResultSetRow mentalRow : mentalsRows) {
            Optional<Mental> mentalOptional = mentals.stream()
                    .filter(mental -> Objects.equals(mental.getId(), mentalRow.getId()))
                    .findFirst();

            if (mentalOptional.isEmpty()) {
                Mental mental = Mental.builder()
                        .id(mentalRow.getId())
                        .title(mentalRow.getTitle())
                        .description(mentalRow.getTitle())
                        .isCustom(mentalRow.isCustom)
                        .type(mentalRow.getMentalType())
                        .build();

                mentals.add(mental);
            } else {
                Mental mental = mentalOptional.get();
            }
        }

        return mentals;
    }

    private static class MentalRowMapper implements RowMapper<MentalJdbcRepository.MentalResultSetRow> {
        @Override
        public MentalJdbcRepository.MentalResultSetRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            MentalJdbcRepository.MentalResultSetRow mentalResultSetRow =
                    MentalJdbcRepository.MentalResultSetRow.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .description(rs.getString("description"))
                            .isCustom(rs.getBoolean("is_custom"))
                            .mentalType((MentalType) rs.getObject(String.valueOf(MentalType.class)))
                            .build();
            return mentalResultSetRow;
        }
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class MentalResultSetRow {
        private Long id;

        private String title;

        private String description;

        private boolean isCustom;

        private MentalType mentalType;
    }
}
