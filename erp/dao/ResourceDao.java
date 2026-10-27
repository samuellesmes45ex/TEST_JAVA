package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceLoan;

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
 * Data Access Object for 'categorias_recurso', 'recursos', 'prestamos_estudiante', and 'prestamos_profesor'.
 */
public class ResourceDao {

    // === CATEGORÍAS ===

    public List<ResourceCategory> getAllCategories() throws SQLException {
        String sql = "SELECT * FROM categorias_recurso ORDER BY nombre_categoria";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ResourceCategory> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new ResourceCategory(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"),
                        rs.getString("descripcion")
                ));
            }
            return list;
        }
    }

    // === RECURSOS ===

    public int insertResource(Resource r) throws SQLException {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, "
                + "descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getCode());
            ps.setString(2, r.getName());
            ps.setInt(3, r.getCategoryId());
            ps.setString(4, r.getDescription());
            if (r.getAcquisitionDate() != null) {
                ps.setDate(5, Date.valueOf(r.getAcquisitionDate()));
            } else {
                ps.setDate(5, Date.valueOf(LocalDate.now()));
            }
            ps.setString(6, r.getPhysicalLocation());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "Disponible");
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

    public List<Resource> getAllResources() throws SQLException {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "ORDER BY r.codigo_recurso";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Resource> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toResource(rs));
            }
            return list;
        }
    }

    public Resource findById(int id) throws SQLException {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "WHERE r.id_recurso = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toResource(rs);
                }
            }
        }
        return null;
    }

    public Resource findByCode(String code) throws SQLException {
        String sql = "SELECT r.*, cr.nombre_categoria FROM recursos r "
                + "INNER JOIN categorias_recurso cr ON r.id_categoria = cr.id_categoria "
                + "WHERE r.codigo_recurso = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toResource(rs);
                }
            }
        }
        return null;
    }

    public void updateResourceStatus(int resourceId, String newStatus) throws SQLException {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, resourceId);
            ps.executeUpdate();
        }
    }

    // === PRÉSTAMOS ===

    public int loanToStudent(ResourceLoan loan) throws SQLException {
        String sql = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, "
                + "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getResourceId());
            ps.setInt(2, loan.getBorrowerId());
            ps.setDate(3, Date.valueOf(loan.getLoanDate()));
            ps.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
            ps.setString(5, "Activo");
            ps.setString(6, loan.getRemarks());
            ps.executeUpdate();

            updateResourceStatus(loan.getResourceId(), "Prestado");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    loan.setId(genId);
                    return genId;
                }
            }
        }
        return loan.getId();
    }

    public int loanToTeacher(ResourceLoan loan) throws SQLException {
        String sql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, "
                + "fecha_devolucion_esperada, estado_prestamo, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getResourceId());
            ps.setInt(2, loan.getBorrowerId());
            ps.setDate(3, Date.valueOf(loan.getLoanDate()));
            ps.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
            ps.setString(5, "Activo");
            ps.setString(6, loan.getRemarks());
            ps.executeUpdate();

            updateResourceStatus(loan.getResourceId(), "Prestado");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    loan.setId(genId);
                    return genId;
                }
            }
        }
        return loan.getId();
    }

    public boolean returnStudentLoan(int loanId) throws SQLException {
        String selectSql = "SELECT id_recurso FROM prestamos_estudiante WHERE id_prestamo = ? AND estado_prestamo = 'Activo'";
        int resourceId = -1;

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resourceId = rs.getInt("id_recurso");
                }
            }
        }

        if (resourceId == -1) {
            return false;
        }

        String updateSql = "UPDATE prestamos_estudiante SET estado_prestamo = 'Devuelto', "
                + "fecha_devolucion_real = ? WHERE id_prestamo = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }

        updateResourceStatus(resourceId, "Disponible");
        return true;
    }

    public boolean returnTeacherLoan(int loanId) throws SQLException {
        String selectSql = "SELECT id_recurso FROM prestamos_profesor WHERE id_prestamo = ? AND estado_prestamo = 'Activo'";
        int resourceId = -1;

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sqlSafe(selectSql))) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resourceId = rs.getInt("id_recurso");
                }
            }
        }

        if (resourceId == -1) {
            return false;
        }

        String updateSql = "UPDATE prestamos_profesor SET estado_prestamo = 'Devuelto', "
                + "fecha_devolucion_real = ? WHERE id_prestamo = ?";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }

        updateResourceStatus(resourceId, "Disponible");
        return true;
    }

    public List<ResourceLoan> getActiveLoans() throws SQLException {
        List<ResourceLoan> list = new ArrayList<>();
        String sql = "SELECT pe.id_prestamo, pe.id_recurso, 'Estudiante' AS tipo_prestatario, pe.id_estudiante AS id_prestatario, "
                + "pe.fecha_prestamo, pe.fecha_devolucion_esperada, pe.fecha_devolucion_real, pe.estado_prestamo, pe.observaciones, "
                + "r.codigo_recurso, r.nombre_recurso, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_prestatario "
                + "FROM prestamos_estudiante pe "
                + "INNER JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.estado_prestamo = 'Activo' "
                + "UNION ALL "
                + "SELECT pp.id_prestamo, pp.id_recurso, 'Profesor' AS tipo_prestatario, pp.id_profesor AS id_prestatario, "
                + "pp.fecha_prestamo, pp.fecha_devolucion_esperada, pp.fecha_devolucion_real, pp.estado_prestamo, pp.observaciones, "
                + "r.codigo_recurso, r.nombre_recurso, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prestatario "
                + "FROM prestamos_profesor pp "
                + "INNER JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "INNER JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "WHERE pp.estado_prestamo = 'Activo' "
                + "ORDER BY fecha_devolucion_esperada ASC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(toLoan(rs));
            }
        }
        return list;
    }

    public List<ResourceLoan> getOverdueLoans() throws SQLException {
        List<ResourceLoan> list = new ArrayList<>();
        String sql = "SELECT pe.id_prestamo, pe.id_recurso, 'Estudiante' AS tipo_prestatario, pe.id_estudiante AS id_prestatario, "
                + "pe.fecha_prestamo, pe.fecha_devolucion_esperada, pe.fecha_devolucion_real, pe.estado_prestamo, pe.observaciones, "
                + "r.codigo_recurso, r.nombre_recurso, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_prestatario "
                + "FROM prestamos_estudiante pe "
                + "INNER JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "INNER JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.estado_prestamo = 'Activo' AND pe.fecha_devolucion_esperada < CURDATE() "
                + "UNION ALL "
                + "SELECT pp.id_prestamo, pp.id_recurso, 'Profesor' AS tipo_prestatario, pp.id_profesor AS id_prestatario, "
                + "pp.fecha_prestamo, pp.fecha_devolucion_esperada, pp.fecha_devolucion_real, pp.estado_prestamo, pp.observaciones, "
                + "r.codigo_recurso, r.nombre_recurso, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prestatario "
                + "FROM prestamos_profesor pp "
                + "INNER JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "INNER JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "WHERE pp.estado_prestamo = 'Activo' AND pp.fecha_devolucion_esperada < CURDATE() "
                + "ORDER BY fecha_devolucion_esperada ASC";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(toLoan(rs));
            }
        }
        return list;
    }

    private String sqlSafe(String sql) {
        return sql;
    }

    private Resource toResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id_recurso"));
        r.setCode(rs.getString("codigo_recurso"));
        r.setName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date acq = rs.getDate("fecha_adquisicion");
        if (acq != null) r.setAcquisitionDate(acq.toLocalDate());
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(rs.getString("estado_recurso"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }

    private ResourceLoan toLoan(ResultSet rs) throws SQLException {
        ResourceLoan l = new ResourceLoan();
        l.setId(rs.getInt("id_prestamo"));
        l.setResourceId(rs.getInt("id_recurso"));
        l.setBorrowerType(rs.getString("tipo_prestatario"));
        l.setBorrowerId(rs.getInt("id_prestatario"));
        l.setLoanDate(rs.getDate("fecha_prestamo").toLocalDate());
        l.setExpectedReturnDate(rs.getDate("fecha_devolucion_esperada").toLocalDate());
        Date real = rs.getDate("fecha_devolucion_real");
        if (real != null) l.setActualReturnDate(real.toLocalDate());
        l.setStatus(rs.getString("estado_prestamo"));
        l.setRemarks(rs.getString("observaciones"));
        l.setResourceCode(rs.getString("codigo_recurso"));
        l.setResourceName(rs.getString("nombre_recurso"));
        l.setBorrowerName(rs.getString("nom_prestatario"));
        return l;
    }
}
