package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Attendance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for 'asistencias'.
 */
public class AttendanceDao {

    public int recordAttendance(Attendance a) throws SQLException {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, "
                + "estado_asistencia, observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), "
                + "observaciones = VALUES(observaciones), registrado_por = VALUES(registrado_por)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getSubjectId());
            ps.setDate(3, Date.valueOf(a.getAttendanceDate()));
            ps.setString(4, a.getStatus());
            ps.setString(5, a.getRemarks());
            if (a.getRecordedBy() != null) {
                ps.setInt(6, a.getRecordedBy());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    a.setId(genId);
                    return genId;
                }
            }
        }
        return a.getId();
    }

    public List<Attendance> getAttendanceByStudentAndSubject(int studentId, int subjectId) throws SQLException {
        String sql = "SELECT a.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asistencias a "
                + "INNER JOIN materias m ON a.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "LEFT JOIN profesores p ON a.registrado_por = p.id_profesor "
                + "WHERE a.id_estudiante = ? AND a.id_materia = ? ORDER BY a.fecha_asistencia DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Attendance> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toAttendance(rs));
                }
                return list;
            }
        }
    }

    public List<Attendance> getAttendanceBySubjectAndDate(int subjectId, LocalDate date) throws SQLException {
        String sql = "SELECT a.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asistencias a "
                + "INNER JOIN materias m ON a.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "LEFT JOIN profesores p ON a.registrado_por = p.id_profesor "
                + "WHERE a.id_materia = ? AND a.fecha_asistencia = ? ORDER BY e.primer_apellido, e.primer_nombre";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<Attendance> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toAttendance(rs));
                }
                return list;
            }
        }
    }

    /**
     * Finds students with more than 'minAbsences' within the given date window.
     */
    public Map<String, Integer> getStudentsWithHighAbsences(LocalDate startDate, LocalDate endDate, int minAbsences) throws SQLException {
        String sql = "SELECT CONCAT(e.primer_nombre, ' ', e.primer_apellido, ' (Doc: ', e.numero_documento, ')') AS info, "
                + "COUNT(*) AS total_ausencias "
                + "FROM asistencias a "
                + "INNER JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "WHERE a.estado_asistencia = 'Ausente' "
                + "AND a.fecha_asistencia BETWEEN ? AND ? "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.primer_apellido, e.numero_documento "
                + "HAVING total_ausencias >= ? "
                + "ORDER BY total_ausencias DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ps.setInt(3, minAbsences);
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Integer> result = new HashMap<>();
                while (rs.next()) {
                    result.put(rs.getString("info"), rs.getInt("total_ausencias"));
                }
                return result;
            }
        }
    }

    private Attendance toAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id_asistencia"));
        a.setStudentId(rs.getInt("id_estudiante"));
        a.setSubjectId(rs.getInt("id_materia"));
        a.setAttendanceDate(rs.getDate("fecha_asistencia").toLocalDate());
        a.setStatus(rs.getString("estado_asistencia"));
        a.setRemarks(rs.getString("observaciones"));
        int reg = rs.getInt("registrado_por");
        if (!rs.wasNull()) {
            a.setRecordedBy(reg);
        }
        a.setSubjectName(rs.getString("nombre_materia"));
        a.setStudentName(rs.getString("nom_est"));
        a.setTeacherName(rs.getString("nom_prof"));
        return a;
    }
}
