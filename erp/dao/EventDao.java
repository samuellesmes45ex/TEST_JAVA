package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'eventos' and 'participantes_evento'.
 */
public class EventDao {

    public int insertEvent(SchoolEvent ev) throws SQLException {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, "
                + "lugar, tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getName());
            ps.setString(2, ev.getDescription());
            ps.setString(3, ev.getEventType());
            ps.setDate(4, Date.valueOf(ev.getEventDate()));
            ps.setString(5, ev.getLocation());
            ps.setBoolean(6, ev.isHasCompetition());
            if (ev.getOrganizerId() != null) {
                ps.setInt(7, ev.getOrganizerId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setBoolean(8, ev.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    ev.setId(genId);
                    return genId;
                }
            }
        }
        return ev.getId();
    }

    public List<SchoolEvent> getAllEvents() throws SQLException {
        String sql = "SELECT e.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM participantes_evento pe WHERE pe.id_evento = e.id_evento) AS total_participantes "
                + "FROM eventos e "
                + "LEFT JOIN profesores p ON e.id_organizador = p.id_profesor "
                + "ORDER BY e.fecha_evento DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<SchoolEvent> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toEvent(rs));
            }
            return list;
        }
    }

    public SchoolEvent findById(int id) throws SQLException {
        String sql = "SELECT e.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM participantes_evento pe WHERE pe.id_evento = e.id_evento) AS total_participantes "
                + "FROM eventos e "
                + "LEFT JOIN profesores p ON e.id_organizador = p.id_profesor "
                + "WHERE e.id_evento = ?";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toEvent(rs);
                }
            }
        }
        return null;
    }

    public int registerParticipant(EventParticipant ep) throws SQLException {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, "
                + "posicion_obtenida, observaciones) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE rol_participacion = VALUES(rol_participacion), "
                + "posicion_obtenida = VALUES(posicion_obtenida), observaciones = VALUES(observaciones)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ep.getEventId());
            ps.setInt(2, ep.getStudentId());
            ps.setString(3, ep.getRole());
            ps.setString(4, ep.getPosition());
            ps.setString(5, ep.getRemarks());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    ep.setId(genId);
                    return genId;
                }
            }
        }
        return ep.getId();
    }

    public List<EventParticipant> getParticipantsForEvent(int eventId) throws SQLException {
        String sql = "SELECT pe.*, e.nombre_evento, CONCAT(est.primer_nombre, ' ', est.primer_apellido) AS nom_est "
                + "FROM participantes_evento pe "
                + "INNER JOIN eventos e ON pe.id_evento = e.id_evento "
                + "INNER JOIN estudiantes est ON pe.id_estudiante = est.id_estudiante "
                + "WHERE pe.id_evento = ? ORDER BY pe.posicion_obtenida, est.primer_apellido";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<EventParticipant> list = new ArrayList<>();
                while (rs.next()) {
                    EventParticipant ep = new EventParticipant();
                    ep.setId(rs.getInt("id_participacion"));
                    ep.setEventId(rs.getInt("id_evento"));
                    ep.setStudentId(rs.getInt("id_estudiante"));
                    ep.setRole(rs.getString("rol_participacion"));
                    ep.setPosition(rs.getString("posicion_obtenida"));
                    ep.setRemarks(rs.getString("observaciones"));
                    ep.setEventName(rs.getString("nombre_evento"));
                    ep.setStudentName(rs.getString("nom_est"));
                    list.add(ep);
                }
                return list;
            }
        }
    }

    private SchoolEvent toEvent(ResultSet rs) throws SQLException {
        SchoolEvent e = new SchoolEvent();
        e.setId(rs.getInt("id_evento"));
        e.setName(rs.getString("nombre_evento"));
        e.setDescription(rs.getString("descripcion"));
        e.setEventType(rs.getString("tipo_evento"));
        e.setEventDate(rs.getDate("fecha_evento").toLocalDate());
        e.setLocation(rs.getString("lugar"));
        e.setHasCompetition(rs.getBoolean("tiene_competencia"));
        int org = rs.getInt("id_organizador");
        if (!rs.wasNull()) {
            e.setOrganizerId(org);
        }
        e.setActive(rs.getBoolean("esta_activo"));
        e.setOrganizerName(rs.getString("nom_prof"));
        e.setParticipantCount(rs.getInt("total_participantes"));
        return e;
    }
}
