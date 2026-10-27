package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.TeacherAssignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'asignaciones_profesor'.
 */
public class TeacherAssignmentDao {

    public int assign(TeacherAssignment a) throws SQLException {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, anio_academico, "
                + "seccion, dia_semana, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getTeacherId());
            ps.setInt(2, a.getSubjectId());
            ps.setInt(3, a.getAcademicYear());
            ps.setString(4, a.getSection());
            ps.setString(5, a.getDayOfWeek());
            if (a.getStartTime() != null) {
                ps.setTime(6, Time.valueOf(a.getStartTime()));
            } else {
                ps.setNull(6, java.sql.Types.TIME);
            }
            if (a.getEndTime() != null) {
                ps.setTime(7, Time.valueOf(a.getEndTime()));
            } else {
                ps.setNull(7, java.sql.Types.TIME);
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

    /**
     * Checks whether the teacher already has a conflicting class scheduled on the same day and time.
     */
    public boolean hasScheduleConflict(int teacherId, int academicYear, String dayOfWeek, LocalTime start, LocalTime end) throws SQLException {
        if (dayOfWeek == null || start == null || end == null) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM asignaciones_profesor WHERE id_profesor = ? "
                + "AND anio_academico = ? AND dia_semana = ? "
                + "AND NOT (hora_fin <= ? OR hora_inicio >= ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, academicYear);
            ps.setString(3, dayOfWeek);
            ps.setTime(4, Time.valueOf(start));
            ps.setTime(5, Time.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<TeacherAssignment> getAssignmentsByTeacher(int teacherId, int academicYear) throws SQLException {
        String sql = "SELECT ap.*, m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "WHERE ap.id_profesor = ? AND ap.anio_academico = ? "
                + "ORDER BY FIELD(ap.dia_semana, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'), ap.hora_inicio";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                List<TeacherAssignment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toAssignment(rs));
                }
                return list;
            }
        }
    }

    public List<TeacherAssignment> getAssignmentsBySubject(int subjectId, int academicYear) throws SQLException {
        String sql = "SELECT ap.*, m.nombre_materia, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asignaciones_profesor ap "
                + "INNER JOIN materias m ON ap.id_materia = m.id_materia "
                + "INNER JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "WHERE ap.id_materia = ? AND ap.anio_academico = ? "
                + "ORDER BY ap.seccion, ap.dia_semana";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                List<TeacherAssignment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toAssignment(rs));
                }
                return list;
            }
        }
    }

    private TeacherAssignment toAssignment(ResultSet rs) throws SQLException {
        TeacherAssignment a = new TeacherAssignment();
        a.setId(rs.getInt("id_asignacion"));
        a.setTeacherId(rs.getInt("id_profesor"));
        a.setSubjectId(rs.getInt("id_materia"));
        a.setAcademicYear(rs.getInt("anio_academico"));
        a.setSection(rs.getString("seccion"));
        a.setDayOfWeek(rs.getString("dia_semana"));
        Time st = rs.getTime("hora_inicio");
        if (st != null) a.setStartTime(st.toLocalTime());
        Time et = rs.getTime("hora_fin");
        if (et != null) a.setEndTime(et.toLocalTime());
        a.setSubjectName(rs.getString("nombre_materia"));
        a.setTeacherName(rs.getString("nom_prof"));
        return a;
    }
}
