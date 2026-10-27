package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'grados' and 'matriculas'.
 */
public class EnrollmentDao {

    public List<GradeLevel> getAllGrades() throws SQLException {
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico ASC";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<GradeLevel> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new GradeLevel(
                        rs.getInt("id_grado"),
                        rs.getString("nombre_grado"),
                        rs.getInt("nivel_numerico")
                ));
            }
            return list;
        }
    }

    public GradeLevel getGradeById(int gradeId) throws SQLException {
        String sql = "SELECT * FROM grados WHERE id_grado = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GradeLevel(
                            rs.getInt("id_grado"),
                            rs.getString("nombre_grado"),
                            rs.getInt("nivel_numerico")
                    );
                }
            }
        }
        return null;
    }

    public int enrollStudent(Enrollment e) throws SQLException {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, anio_academico, estado_matricula, "
                + "fecha_matricula, fecha_retiro, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getGradeId());
            ps.setInt(3, e.getAcademicYear());
            ps.setString(4, e.getStatus());
            ps.setDate(5, Date.valueOf(e.getEnrollmentDate()));
            if (e.getWithdrawalDate() != null) {
                ps.setDate(6, Date.valueOf(e.getWithdrawalDate()));
            } else {
                ps.setNull(6, java.sql.Types.DATE);
            }
            ps.setString(7, e.getRemarks());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    e.setId(genId);
                    return genId;
                }
            }
        }
        return e.getId();
    }

    public boolean isStudentEnrolled(int studentId, int academicYear) throws SQLException {
        String sql = "SELECT COUNT(*) FROM matriculas WHERE id_estudiante = ? AND anio_academico = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Enrollment> getEnrollmentsByStudent(int studentId) throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM matriculas m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "WHERE m.id_estudiante = ? ORDER BY m.anio_academico DESC";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Enrollment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toEnrollment(rs));
                }
                return list;
            }
        }
    }

    public List<Enrollment> getEnrollmentsByGradeAndYear(int gradeId, int academicYear) throws SQLException {
        String sql = "SELECT m.*, g.nombre_grado, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM matriculas m "
                + "INNER JOIN grados g ON m.id_grado = g.id_grado "
                + "INNER JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "WHERE m.id_grado = ? AND m.anio_academico = ? ORDER BY e.primer_apellido, e.primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            ps.setInt(2, academicYear);
            try (ResultSet rs = ps.executeQuery()) {
                List<Enrollment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toEnrollment(rs));
                }
                return list;
            }
        }
    }

    private Enrollment toEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id_matricula"));
        e.setStudentId(rs.getInt("id_estudiante"));
        e.setGradeId(rs.getInt("id_grado"));
        e.setAcademicYear(rs.getInt("anio_academico"));
        e.setStatus(rs.getString("estado_matricula"));
        e.setEnrollmentDate(rs.getDate("fecha_matricula").toLocalDate());
        Date wd = rs.getDate("fecha_retiro");
        if (wd != null) {
            e.setWithdrawalDate(wd.toLocalDate());
        }
        e.setRemarks(rs.getString("observaciones"));
        e.setGradeName(rs.getString("nombre_grado"));
        e.setStudentName(rs.getString("nom_est"));
        return e;
    }
}
