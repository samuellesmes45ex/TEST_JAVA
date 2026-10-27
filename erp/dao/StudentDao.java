package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'estudiantes' and the 'estudiante_acudiente' relation.
 * All queries strictly use PreparedStatement and try-with-resources.
 */
public class StudentDao {

    public int insert(Student s) throws SQLException {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, "
                + "correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getDocumentType());
            ps.setString(2, s.getDocumentNumber());
            ps.setString(3, s.getFirstName());
            ps.setString(4, s.getMiddleName());
            ps.setString(5, s.getLastName());
            ps.setString(6, s.getSecondLastName());
            ps.setDate(7, Date.valueOf(s.getBirthDate()));
            ps.setString(8, s.getGender());
            ps.setString(9, s.getAddress());
            ps.setString(10, s.getPhone());
            ps.setString(11, s.getEmail());
            ps.setString(12, s.getBloodType());
            ps.setString(13, s.getAllergies());
            ps.setString(14, s.getMedicalConditions());
            ps.setDate(15, Date.valueOf(s.getAdmissionDate()));
            ps.setString(16, s.getStatus());
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

    public void update(Student s) throws SQLException {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, primer_nombre = ?, segundo_nombre = ?, "
                + "primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, direccion = ?, "
                + "telefono = ?, correo_electronico = ?, tipo_sangre = ?, alergias = ?, condiciones_medicas = ?, "
                + "estado = ? WHERE id_estudiante = ?";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getDocumentType());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getMiddleName());
            ps.setString(4, s.getLastName());
            ps.setString(5, s.getSecondLastName());
            ps.setDate(6, Date.valueOf(s.getBirthDate()));
            ps.setString(7, s.getGender());
            ps.setString(8, s.getAddress());
            ps.setString(9, s.getPhone());
            ps.setString(10, s.getEmail());
            ps.setString(11, s.getBloodType());
            ps.setString(12, s.getAllergies());
            ps.setString(13, s.getMedicalConditions());
            ps.setString(14, s.getStatus());
            ps.setInt(15, s.getId());
            ps.executeUpdate();
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM estudiantes WHERE id_estudiante = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toStudent(rs);
                }
            }
        }
        return null;
    }

    public Student findByDocument(String documentNumber) throws SQLException {
        String sql = "SELECT * FROM estudiantes WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toStudent(rs);
                }
            }
        }
        return null;
    }

    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM estudiantes ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toStudent(rs));
            }
            return list;
        }
    }

    public List<Student> search(String text) throws SQLException {
        String sql = "SELECT * FROM estudiantes WHERE primer_nombre LIKE ? OR primer_apellido LIKE ? "
                + "OR numero_documento LIKE ? ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + text + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toStudent(rs));
                }
                return list;
            }
        }
    }

    public boolean documentExists(String documentNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiantes WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void linkGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) throws SQLException {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE es_principal = VALUES(es_principal), es_contacto_emergencia = VALUES(es_contacto_emergencia)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, isPrimary);
            ps.setBoolean(4, isEmergency);
            ps.executeUpdate();
        }
    }

    public List<Guardian> getGuardiansForStudent(int studentId) throws SQLException {
        String sql = "SELECT a.* FROM acudientes a "
                + "INNER JOIN estudiante_acudiente ea ON a.id_acudiente = ea.id_acudiente "
                + "WHERE ea.id_estudiante = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Guardian> list = new ArrayList<>();
                while (rs.next()) {
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
                    list.add(g);
                }
                return list;
            }
        }
    }

    private Student toStudent(ResultSet rs) throws SQLException {
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
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        s.setAdmissionDate(rs.getDate("fecha_ingreso").toLocalDate());
        s.setStatus(rs.getString("estado"));
        return s;
    }
}
