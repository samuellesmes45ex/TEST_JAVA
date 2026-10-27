package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Teacher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'profesores'.
 */
public class TeacherDao {

    public int insert(Teacher t) throws SQLException {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, correo_electronico, telefono, direccion, titulo_academico, "
                + "especialidad, fecha_contratacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getDocumentType());
            ps.setString(2, t.getDocumentNumber());
            ps.setString(3, t.getFirstName());
            ps.setString(4, t.getMiddleName());
            ps.setString(5, t.getLastName());
            ps.setString(6, t.getSecondLastName());
            ps.setString(7, t.getEmail());
            ps.setString(8, t.getPhone());
            ps.setString(9, t.getAddress());
            ps.setString(10, t.getAcademicDegree());
            ps.setString(11, t.getSpecialty());
            ps.setDate(12, Date.valueOf(t.getHireDate()));
            ps.setString(13, t.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    t.setId(genId);
                    return genId;
                }
            }
        }
        return t.getId();
    }

    public void update(Teacher t) throws SQLException {
        String sql = "UPDATE profesores SET tipo_documento = ?, primer_nombre = ?, segundo_nombre = ?, "
                + "primer_apellido = ?, segundo_apellido = ?, correo_electronico = ?, telefono = ?, "
                + "direccion = ?, titulo_academico = ?, especialidad = ?, estado = ? WHERE id_profesor = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getDocumentType());
            ps.setString(2, t.getFirstName());
            ps.setString(3, t.getMiddleName());
            ps.setString(4, t.getLastName());
            ps.setString(5, t.getSecondLastName());
            ps.setString(6, t.getEmail());
            ps.setString(7, t.getPhone());
            ps.setString(8, t.getAddress());
            ps.setString(9, t.getAcademicDegree());
            ps.setString(10, t.getSpecialty());
            ps.setString(11, t.getStatus());
            ps.setInt(12, t.getId());
            ps.executeUpdate();
        }
    }

    public Teacher findById(int id) throws SQLException {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toTeacher(rs);
                }
            }
        }
        return null;
    }

    public Teacher findByDocument(String doc) throws SQLException {
        String sql = "SELECT * FROM profesores WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toTeacher(rs);
                }
            }
        }
        return null;
    }

    public List<Teacher> findAll() throws SQLException {
        String sql = "SELECT * FROM profesores ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Teacher> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toTeacher(rs));
            }
            return list;
        }
    }

    public List<Teacher> search(String text) throws SQLException {
        String sql = "SELECT * FROM profesores WHERE primer_nombre LIKE ? OR primer_apellido LIKE ? "
                + "OR numero_documento LIKE ? OR especialidad LIKE ? ORDER BY primer_apellido, primer_nombre";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + text + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Teacher> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toTeacher(rs));
                }
                return list;
            }
        }
    }

    public boolean documentExists(String doc) throws SQLException {
        String sql = "SELECT COUNT(*) FROM profesores WHERE numero_documento = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private Teacher toTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setMiddleName(rs.getString("segundo_nombre"));
        t.setLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setAddress(rs.getString("direccion"));
        t.setAcademicDegree(rs.getString("titulo_academico"));
        t.setSpecialty(rs.getString("especialidad"));
        t.setHireDate(rs.getDate("fecha_contratacion").toLocalDate());
        t.setStatus(rs.getString("estado"));
        return t;
    }
}
