package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'conceptos_pago', 'pagos', and 'facturas'.
 */
public class PaymentDao {

    // === CONCEPTOS DE PAGO ===

    public List<PaymentConcept> getAllConcepts() throws SQLException {
        String sql = "SELECT * FROM conceptos_pago WHERE esta_activo = TRUE ORDER BY id_concepto";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PaymentConcept> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new PaymentConcept(
                        rs.getInt("id_concepto"),
                        rs.getString("codigo_concepto"),
                        rs.getString("nombre_concepto"),
                        rs.getString("descripcion"),
                        rs.getBigDecimal("monto_base"),
                        rs.getBoolean("es_recurrente"),
                        rs.getBoolean("esta_activo")
                ));
            }
            return list;
        }
    }

    public PaymentConcept getConceptById(int id) throws SQLException {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PaymentConcept(
                            rs.getInt("id_concepto"),
                            rs.getString("codigo_concepto"),
                            rs.getString("nombre_concepto"),
                            rs.getString("descripcion"),
                            rs.getBigDecimal("monto_base"),
                            rs.getBoolean("es_recurrente"),
                            rs.getBoolean("esta_activo")
                    );
                }
            }
        }
        return null;
    }

    // === PAGOS ===

    public int insertPayment(Payment p) throws SQLException {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, "
                + "fecha_pago, metodo_pago, numero_recibo, anio_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getStudentId());
            if (p.getGuardianId() != null) {
                ps.setInt(2, p.getGuardianId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, p.getConceptId());
            ps.setBigDecimal(4, p.getAmountPaid());
            ps.setDate(5, Date.valueOf(p.getPaymentDate()));
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getReceiptNumber());
            if (p.getAcademicYear() != null) {
                ps.setInt(8, p.getAcademicYear());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setString(9, p.getAcademicPeriod());
            ps.setString(10, p.getNotes());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    p.setId(genId);
                    return genId;
                }
            }
        }
        return p.getId();
    }

    public Payment findPaymentById(int id) throws SQLException {
        String sql = "SELECT p.*, cp.nombre_concepto, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_pago = ?";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toPayment(rs);
                }
            }
        }
        return null;
    }

    public List<Payment> getPaymentsByStudent(int studentId) throws SQLException {
        String sql = "SELECT p.*, cp.nombre_concepto, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toPayment(rs));
                }
                return list;
            }
        }
    }

    public List<Payment> getPaymentsByGuardian(int guardianId) throws SQLException {
        String sql = "SELECT p.*, cp.nombre_concepto, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_acudiente = ? ORDER BY p.fecha_pago DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(toPayment(rs));
                }
                return list;
            }
        }
    }

    public List<Payment> getAllPayments() throws SQLException {
        String sql = "SELECT p.*, cp.nombre_concepto, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "INNER JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "INNER JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "ORDER BY p.fecha_pago DESC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Payment> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toPayment(rs));
            }
            return list;
        }
    }

    // === FACTURAS ELECTRÓNICAS ===

    public int insertInvoice(Invoice inv) throws SQLException {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, "
                + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getInvoiceNumber());
            ps.setInt(2, inv.getPaymentId());
            ps.setDate(3, Date.valueOf(inv.getIssueDate()));
            ps.setString(4, inv.getCufe());
            ps.setString(5, inv.getPdfFileName());
            ps.setString(6, inv.getFilePath());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    inv.setId(genId);
                    return genId;
                }
            }
        }
        return inv.getId();
    }

    public Invoice findInvoiceByPaymentId(int paymentId) throws SQLException {
        String sql = "SELECT * FROM facturas WHERE id_pago = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Invoice(
                            rs.getInt("id_factura"),
                            rs.getString("numero_factura"),
                            rs.getInt("id_pago"),
                            rs.getDate("fecha_emision").toLocalDate(),
                            rs.getString("cufe"),
                            rs.getString("nombre_archivo_pdf"),
                            rs.getString("ruta_archivo")
                    );
                }
            }
        }
        return null;
    }

    public int getNextInvoiceSequence() throws SQLException {
        String sql = "SELECT COUNT(*) FROM facturas";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        }
        return 1;
    }

    private Payment toPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id_pago"));
        p.setStudentId(rs.getInt("id_estudiante"));
        int acud = rs.getInt("id_acudiente");
        if (!rs.wasNull()) p.setGuardianId(acud);
        p.setConceptId(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        p.setPaymentDate(rs.getDate("fecha_pago").toLocalDate());
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        int yr = rs.getInt("anio_academico");
        if (!rs.wasNull()) p.setAcademicYear(yr);
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        p.setConceptName(rs.getString("nombre_concepto"));
        p.setStudentName(rs.getString("nom_est"));
        p.setGuardianName(rs.getString("nom_acud"));
        return p;
    }
}
