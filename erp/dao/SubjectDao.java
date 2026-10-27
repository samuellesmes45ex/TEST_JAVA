package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'materias' and 'inscripciones_materia'.
 */
public class SubjectDao {

    public List<Subject> getAllSubjects() throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "ORDER BY g.nivel_numerico, m.nombre_materia";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Subject> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toSubject(rs));
            }
            return list;
        }
    }

    public List<Subject> getSubjectsByGrade(int gradeId) throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_grado = ? AND m.esta_activa = TRUE ORDER BY m.nombre_materia";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Subject> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toSubject(rs));
                }
                return list;
            }
        }
    }

    public Subject findById(int id) throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.id_materia = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toSubject(rs);
                }
            }
        }
        return null;
    }

    public Subject findByCode(String code) throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "WHERE m.codigo_materia = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toSubject(rs);
                }
            }
        }
        return null;
    }

    public int insertSubject(Subject s) throws SQLException {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getCode());
            ps.setString(2, s.getName());
            ps.setString(3, s.getDescription());
            ps.setInt(4, s.getGradeId());
            ps.setInt(5, s.getWeeklyHours());
            ps.setBoolean(6, s.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    s.setId(genId);
                    return genId;
                }
            }
        }
        return s.getId();
    }

    public int enrollStudentInSubject(SubjectEnrollment se) throws SQLException {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, anio_academico, "
                + "periodo_academico, seccion, fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, se.getStudentId());
            ps.setInt(2, se.getSubjectId());
            ps.setInt(3, se.getAcademicYear());
            ps.setString(4, se.getAcademicPeriod());
            ps.setString(5, se.getSection() != null ? se.getSection() : "A");
            ps.setDate(6, Date.valueOf(se.getEnrollmentDate()));
            ps.setString(7, se.getStatus() != null ? se.getStatus() : "Activa");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    se.setId(genId);
                    return genId;
                }
            }
        }
        return se.getId();
    }

    public boolean isStudentEnrolledInSubject(int studentId, int subjectId, int year, String period) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ? "
                + "AND anio_academico = ? AND periodo_academico = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, period);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<SubjectEnrollment> getSubjectEnrollmentsForStudent(int studentId, int year, String period) throws SQLException {
        String sql = "SELECT im.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM inscripciones_materia im "
                + "INNER JOIN materias m ON im.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON im.id_estudiante = e.id_estudiante "
                + "WHERE im.id_estudiante = ? AND im.anio_academico = ? AND im.periodo_academico = ? "
                + "ORDER BY m.nombre_materia";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                List<SubjectEnrollment> list = new ArrayList<>();
                while (rs.next()) {
                    SubjectEnrollment se = new SubjectEnrollment();
                    se.setId(rs.getInt("id_inscripcion"));
                    se.setStudentId(rs.getInt("id_estudiante"));
                    se.setSubjectId(rs.getInt("id_materia"));
                    se.setAcademicYear(rs.getInt("anio_academico"));
                    se.setAcademicPeriod(rs.getString("periodo_academico"));
                    se.setSection(rs.getString("seccion"));
                    se.setEnrollmentDate(rs.getDate("fecha_inscripcion").toLocalDate());
                    se.setStatus(rs.getString("estado"));
                    se.setSubjectName(rs.getString("nombre_materia"));
                    se.setStudentName(rs.getString("nom_est"));
                    list.add(se);
                }
                return list;
            }
        }
    }

    private Subject toSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id_materia"));
        s.setCode(rs.getString("codigo_materia"));
        s.setName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setGradeId(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        s.setGradeName(rs.getString("nombre_grado"));
        return s;
    }
}
