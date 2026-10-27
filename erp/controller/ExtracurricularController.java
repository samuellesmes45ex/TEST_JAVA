package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.Validator;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business controller for Clubs, Memberships (capacity validation), Events, and Competitive rankings.
 */
public class ExtracurricularController {

    private final ClubDao clubDao = new ClubDao();
    private final EventDao eventDao = new EventDao();
    private final StudentDao studentDao = new StudentDao();

    // === CLUBS ===

    public int createClub(Club c) throws SQLException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del club no puede estar vacío.");
        }
        if (c.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser mayor a 0.");
        }
        c.setActive(true);
        return clubDao.insertClub(c);
    }

    public List<Club> listAllClubs() throws SQLException {
        return clubDao.getAllClubs();
    }

    public Club findClubById(int id) throws SQLException {
        return clubDao.findById(id);
    }

    public int enrollStudentInClub(int studentId, int clubId) throws SQLException {
        Student student = studentDao.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        Club club = clubDao.findById(clubId);
        if (club == null) {
            throw new IllegalArgumentException("El club no existe.");
        }
        if (!club.isActive()) {
            throw new IllegalArgumentException("El club '" + club.getName() + "' se encuentra inactivo.");
        }
        if (clubDao.isStudentActiveInClub(studentId, clubId)) {
            throw new IllegalArgumentException("El estudiante ya es miembro activo de este club.");
        }
        // Validation of maximum capacity
        int currentMembers = clubDao.getActiveMembersCount(clubId);
        if (currentMembers >= club.getMaxCapacity()) {
            throw new IllegalArgumentException("¡Cupo agotado! El club '" + club.getName() +
                    "' ya alcanzó su capacidad máxima de " + club.getMaxCapacity() + " estudiantes.");
        }

        ClubMembership m = new ClubMembership();
        m.setStudentId(studentId);
        m.setClubId(clubId);
        m.setJoinDate(LocalDate.now());
        m.setStatus("Activa");
        return clubDao.enrollStudentInClub(m);
    }

    public void withdrawStudentFromClub(int studentId, int clubId, String reason) throws SQLException {
        if (!clubDao.isStudentActiveInClub(studentId, clubId)) {
            throw new IllegalArgumentException("El estudiante no cuenta con membresía activa en este club.");
        }
        clubDao.withdrawStudentFromClub(studentId, clubId, Date.valueOf(LocalDate.now()), reason);
    }

    public List<ClubMembership> getStudentClubs(int studentId) throws SQLException {
        return clubDao.getMembershipsForStudent(studentId);
    }

    // === EVENTS ===

    public int createEvent(SchoolEvent ev) throws SQLException {
        if (ev.getName() == null || ev.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio.");
        }
        if (!Validator.EVENT_TYPES.contains(ev.getEventType())) {
            throw new IllegalArgumentException("Tipo de evento no válido: " + ev.getEventType());
        }
        ev.setActive(true);
        return eventDao.insertEvent(ev);
    }

    public List<SchoolEvent> listAllEvents() throws SQLException {
        return eventDao.getAllEvents();
    }

    public SchoolEvent findEventById(int id) throws SQLException {
        return eventDao.findById(id);
    }

    public int registerEventParticipant(int eventId, int studentId, String role, String position, String remarks) throws SQLException {
        SchoolEvent ev = eventDao.findById(eventId);
        if (ev == null) {
            throw new IllegalArgumentException("El evento seleccionado no existe.");
        }
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        if (role != null && !Validator.EVENT_ROLES.contains(role)) {
            throw new IllegalArgumentException("Rol no válido: " + role);
        }
        if (ev.isHasCompetition() && position != null && !Validator.EVENT_POSITIONS.contains(position)) {
            throw new IllegalArgumentException("Posición no válida en competencia. Opciones: " + Validator.EVENT_POSITIONS);
        }

        EventParticipant ep = new EventParticipant();
        ep.setEventId(eventId);
        ep.setStudentId(studentId);
        ep.setRole(role != null ? role : "Participante");
        ep.setPosition(position);
        ep.setRemarks(remarks);
        return eventDao.registerParticipant(ep);
    }

    public List<EventParticipant> getEventParticipants(int eventId) throws SQLException {
        return eventDao.getParticipantsForEvent(eventId);
    }
}
