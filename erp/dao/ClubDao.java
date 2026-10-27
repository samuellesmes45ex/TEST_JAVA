package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'clubes' and 'membresias_club'.
 */
public class ClubDao {

    public int insertClub(Club c) throws SQLException {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, "
                + "capacidad_maxima, horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            if (c.getSupervisorTeacherId() != null) {
                ps.setInt(3, c.getSupervisorTeacherId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, c.getMaxCapacity());
            ps.setString(5, c.getMeetingSchedule());
            ps.setBoolean(6, c.isActive());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    c.setId(genId);
                    return genId;
                }
            }
        }
        return c.getId();
    }

    public List<Club> getAllClubs() throws SQLException {
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS miembros_activos "
                + "FROM clubes c "
                + "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "ORDER BY c.nombre_club";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Club> list = new ArrayList<>();
            while (rs.next()) {
                list.add(toClub(rs));
            }
            return list;
        }
    }

    public Club findById(int id) throws SQLException {
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS miembros_activos "
                + "FROM clubes c "
                + "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "WHERE c.id_club = ?";

        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return toClub(rs);
                }
            }
        }
        return null;
    }

    public int getActiveMembersCount(int clubId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int enrollStudentInClub(ClubMembership m) throws SQLException {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, "
                + "estado_membresia) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getStudentId());
            ps.setInt(2, m.getClubId());
            ps.setDate(3, Date.valueOf(m.getJoinDate()));
            ps.setString(4, m.getStatus() != null ? m.getStatus() : "Activa");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    m.setId(genId);
                    return genId;
                }
            }
        }
        return m.getId();
    }

    public boolean isStudentActiveInClub(int studentId, int clubId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void withdrawStudentFromClub(int studentId, int clubId, Date withdrawalDate, String reason) throws SQLException {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = ?, motivo_retiro = ? "
                + "WHERE id_estudiante = ? AND id_club = ? AND estado_membresia = 'Activa'";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, withdrawalDate);
            ps.setString(2, reason);
            ps.setInt(3, studentId);
            ps.setInt(4, clubId);
            ps.executeUpdate();
        }
    }

    public List<ClubMembership> getMembershipsForStudent(int studentId) throws SQLException {
        String sql = "SELECT mc.*, c.nombre_club, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM membresias_club mc "
                + "INNER JOIN clubes c ON mc.id_club = c.id_club "
                + "INNER JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante "
                + "WHERE mc.id_estudiante = ? ORDER BY mc.fecha_ingreso DESC";
        try (Connection con = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<ClubMembership> list = new ArrayList<>();
                while (rs.next()) {
                    ClubMembership m = new ClubMembership();
                    m.setId(rs.getInt("id_membresia"));
                    m.setStudentId(rs.getInt("id_estudiante"));
                    m.setClubId(rs.getInt("id_club"));
                    m.setJoinDate(rs.getDate("fecha_ingreso").toLocalDate());
                    m.setStatus(rs.getString("estado_membresia"));
                    Date wd = rs.getDate("fecha_retiro");
                    if (wd != null) m.setWithdrawalDate(wd.toLocalDate());
                    m.setWithdrawalReason(rs.getString("motivo_retiro"));
                    m.setClubName(rs.getString("nombre_club"));
                    m.setStudentName(rs.getString("nom_est"));
                    list.add(m);
                }
                return list;
            }
        }
    }

    private Club toClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        int sup = rs.getInt("id_profesor_supervisor");
        if (!rs.wasNull()) {
            c.setSupervisorTeacherId(sup);
        }
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setSupervisorTeacherName(rs.getString("nom_prof"));
        c.setActiveMembersCount(rs.getInt("miembros_activos"));
        return c;
    }
}
