package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.AcademicRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'registros_academicos'.
 */
public class AcademicRecordDao {

    public int insertOrUpdate(AcademicRecord r) throws SQLException {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, "
                + "anio_academico, periodo_academico, valor_nota, observaciones, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE valor_nota = VALUES(valor_nota), observaciones = VALUES(observaciones), "
                + "id_profesor = VALUES(id_profesor), fecha_registro = VALUES(fecha_registro)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStudentId());
            ps.setInt(2, r.getSubjectId());
            ps.setInt(3, r.getTeacherId());
            ps.setInt(4, r.getAcademicYear());
            ps.setString(5, r.getAcademicPeriod());
            ps.setBigDecimal(6, r.getScore().setScale(1, RoundingMode.HALF_UP));
            ps.setString(7, r.getRemarks());
            ps.setDate(8, Date.valueOf(r.getRecordDate()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    r.setId(genId);
                    return genId;
                }
            }
        }
        return r.getId();
    }

    public List<AcademicRecord> getRecordsByStudent(int studentId) throws SQLException {
        String sql = "SELECT ra.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM registros_academicos ra "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_estudiante = ? ORDER BY ra.anio_academico DESC, ra.periodo_academico, m.nombre_materia";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<AcademicRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toRecord(rs));
                }
                return list;
            }
        }
    }

    public List<AcademicRecord> getRecordsByStudentAndPeriod(int studentId, int year, String period) throws SQLException {
        String sql = "SELECT ra.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM registros_academicos ra "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_estudiante = ? AND ra.anio_academico = ? AND ra.periodo_academico = ? "
                + "ORDER BY m.nombre_materia";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                List<AcademicRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toRecord(rs));
                }
                return list;
            }
        }
    }

    public List<AcademicRecord> getRecordsBySubjectAndPeriod(int subjectId, int year, String period) throws SQLException {
        String sql = "SELECT ra.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM registros_academicos ra "
                + "INNER JOIN materias m ON ra.id_materia = m.id_materia "
                + "INNER JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "INNER JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_materia = ? AND ra.anio_academico = ? AND ra.periodo_academico = ? "
                + "ORDER BY e.primer_apellido, e.primer_nombre";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                List<AcademicRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toRecord(rs));
                }
                return list;
            }
        }
    }

    public BigDecimal calculateStudentSubjectAverage(int studentId, int subjectId, int year) throws SQLException {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_estudiante = ? "
                + "AND id_materia = ? AND anio_academico = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateStudentPeriodGpa(int studentId, int year, String period) throws SQLException {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_estudiante = ? "
                + "AND anio_academico = ? AND periodo_academico = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateCourseSubjectAverage(int subjectId, int year, String period) throws SQLException {
        String sql = "SELECT AVG(valor_nota) FROM registros_academicos WHERE id_materia = ? "
                + "AND anio_academico = ? AND periodo_academico = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBigDecimal(1) != null) {
                    return rs.getBigDecimal(1).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private AcademicRecord toRecord(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setId(rs.getInt("id_registro"));
        r.setStudentId(rs.getInt("id_estudiante"));
        r.setSubjectId(rs.getInt("id_materia"));
        r.setTeacherId(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("anio_academico"));
        r.setAcademicPeriod(rs.getString("periodo_academico"));
        r.setScore(rs.getBigDecimal("valor_nota"));
        r.setRemarks(rs.getString("observaciones"));
        r.setRecordDate(rs.getDate("fecha_registro").toLocalDate());
        r.setSubjectName(rs.getString("nombre_materia"));
        r.setStudentName(rs.getString("nom_est"));
        r.setTeacherName(rs.getString("nom_prof"));
        return r;
    }
}
