package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.otus.hw.logging.TrackTime;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    @Override
    @TrackTime
    public List<Genre> findAll() {
        return namedParameterJdbcOperations.getJdbcOperations()
                .query("select id, name from genres",
                        new GnreRowMapper());
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        Map<String, Object> params = Collections.singletonMap("ids", ids);

        return namedParameterJdbcOperations
                .query("select id, name from genres where id in (:ids)",
                        params,
                        new GnreRowMapper());
    }

    private static class GnreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            long id = rs.getLong("id");
            String name = rs.getString("name");

            return new Genre(id, name);
        }
    }

}
