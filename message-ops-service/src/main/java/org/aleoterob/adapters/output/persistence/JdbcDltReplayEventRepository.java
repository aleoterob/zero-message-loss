package org.aleoterob.adapters.output.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.aleoterob.application.mapper.TransferEventMapper;
import org.aleoterob.application.model.DltReplayEvent;
import org.aleoterob.application.model.DltReplayStatus;
import org.aleoterob.application.model.TransferEventDto;
import org.aleoterob.application.ports.output.DltReplayEventRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JdbcDltReplayEventRepository implements DltReplayEventRepository {
    private static final Logger log = Logger.getLogger(JdbcDltReplayEventRepository.class);

    private final DataSource dataSource;

    public JdbcDltReplayEventRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void savePending(DltReplayEvent event) {
        execute("""
                INSERT INTO dlt_replay_events (
                    event_id,
                    transfer_id,
                    event_key,
                    payload,
                    delivery_state,
                    replay_attempts
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id) DO UPDATE SET
                    event_key = EXCLUDED.event_key,
                    payload = EXCLUDED.payload,
                    delivery_state = CASE
                        WHEN dlt_replay_events.delivery_state = ? THEN dlt_replay_events.delivery_state
                        ELSE EXCLUDED.delivery_state
                    END,
                    updated_at = now()
                """,
                statement -> {
                    statement.setString(1, event.eventId());
                    statement.setString(2, event.dto().transferId());
                    statement.setString(3, event.key());
                    statement.setBytes(4, event.payload());
                    statement.setString(5, DltReplayStatus.PENDING.name());
                    statement.setInt(6, event.replayAttempts());
                    statement.setString(7, DltReplayStatus.CONFIRMED.name());
                });
    }

    @Override
    public List<DltReplayEvent> findPending() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT event_id, event_key, payload, replay_attempts, last_attempt_at
                        FROM dlt_replay_events
                        WHERE delivery_state = ?
                        ORDER BY created_at
                        """)) {
            statement.setString(1, DltReplayStatus.PENDING.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<DltReplayEvent> events = new ArrayList<>();
                while (resultSet.next()) {
                    events.add(toEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not fetch pending DLT replay events", e);
        }
    }

    @Override
    public void markReplayAttempt(DltReplayEvent event) {
        execute("""
                UPDATE dlt_replay_events
                SET replay_attempts = ?,
                    last_attempt_at = ?,
                    updated_at = now()
                WHERE event_id = ?
                """,
                statement -> {
                    statement.setInt(1, event.replayAttempts());
                    statement.setTimestamp(2, timestamp(event.lastAttemptAt()));
                    statement.setString(3, event.eventId());
                });
    }

    @Override
    public void markReplayed(DltReplayEvent event) {
        execute("""
                UPDATE dlt_replay_events
                SET delivery_state = ?,
                    replay_attempts = ?,
                    last_attempt_at = ?,
                    replayed_at = now(),
                    updated_at = now()
                WHERE event_id = ?
                """,
                statement -> {
                    statement.setString(1, DltReplayStatus.REPLAYED.name());
                    statement.setInt(2, event.replayAttempts());
                    statement.setTimestamp(3, timestamp(event.lastAttemptAt()));
                    statement.setString(4, event.eventId());
                });
    }

    @Override
    public void markConfirmed(String eventId) {
        execute("""
                UPDATE dlt_replay_events
                SET delivery_state = ?,
                    confirmed_at = now(),
                    updated_at = now()
                WHERE event_id = ?
                """,
                statement -> {
                    statement.setString(1, DltReplayStatus.CONFIRMED.name());
                    statement.setString(2, eventId);
                });
    }

    private void execute(String sql, StatementBinder binder) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update DLT replay event state", e);
        }
    }

    private DltReplayEvent toEvent(ResultSet resultSet) throws SQLException {
        byte[] payload = resultSet.getBytes("payload");
        try {
            TransferEventDto dto = TransferEventMapper.toDto(
                    payload,
                    true,
                    "DLT_PENDING",
                    resultSet.getInt("replay_attempts"));
            return new DltReplayEvent(
                    resultSet.getString("event_id"),
                    resultSet.getString("event_key"),
                    payload,
                    dto,
                    resultSet.getInt("replay_attempts"),
                    false,
                    instant(resultSet.getTimestamp("last_attempt_at")));
        } catch (Exception e) {
            log.error("Could not deserialize persisted DLT event " + resultSet.getString("event_id"), e);
            throw new SQLException("Could not deserialize persisted DLT event", e);
        }
    }

    private static Timestamp timestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private static Instant instant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
