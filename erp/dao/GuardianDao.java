package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'acudientes'.
 */
public class GuardianDao {

    public int insert(Guardian g) throws SQLException {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, "
                + "correo_electronico, direccion, ocupacion, es_responsable_financiero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getDocumentType());
            ps.setString(2, g.getDocumentNumber());
            ps.setString(3, g.getFirstName());
            ps.setString(4, g.getMiddleName());
            ps.setString(5, g.getLastName());
            ps.setString(6, g.getSecondLastName());
            ps.setString(7, g.getRelationship());
            ps.setString(8, g.getPrimaryPhone());
            ps.setString(9, g.getSecondaryPhone());
            ps.setString(10, g.getEmail());
            ps.setString(11, g.getAddress());
            ps.setString(12, g.getOccupation());
            ps.setBoolean(13, g.isFinancialResponsible());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    g.setId(genId);
                    return genId;
                }
            }
        }
        return g.getId();
    }

    public Guardian findById(int id) throws SQLException {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toGuardian(rs);
                }
            }
        }
        return null;
    }

    public Guardian findByDocument(String documentNumber) throws SQLException {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toGuardian(rs);
                }
            }
        }
        return null;
    }

    public List<Guardian> findAll() throws SQLException {
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Guardian> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toGuardian(rs));
            }
            return list;
        }
    }

    public List<Guardian> search(String text) throws SQLException {
        String sql = "SELECT * FROM acudientes WHERE primer_nombre LIKE ? OR primer_apellido LIKE ? "
                + "OR numero_documento LIKE ? ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + text + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Guardian> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toGuardian(rs));
                }
                return list;
            }
        }
    }

    public boolean documentExists(String documentNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM acudientes WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public List<Student> getStudentsForGuardian(int guardianId) throws SQLException {
        String sql = "SELECT e.* FROM estudiantes e "
                + "INNER JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY e.primer_apellido, e.primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    Student s = new Student();
                    s.setId(rs.getInt("id_estudiante"));
                    s.setDocumentType(rs.getString("tipo_documento"));
                    s.setDocumentNumber(rs.getString("numero_documento"));
                    s.setFirstName(rs.getString("primer_nombre"));
                    s.setMiddleName(rs.getString("segundo_nombre"));
                    s.setLastName(rs.getString("primer_apellido"));
                    s.setSecondLastName(rs.getString("segundo_apellido"));
                    s.setBirthDate(rs.getDate("fecha_nacimiento").toLocalDate());
                    s.setGender(rs.getString("genero"));
                    s.setStatus(rs.getString("estado"));
                    list.add(s);
                }
                return list;
            }
        }
    }

    private Guardian toGuardian(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setMiddleName(rs.getString("segundo_nombre"));
        g.setLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setPrimaryPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }
}
