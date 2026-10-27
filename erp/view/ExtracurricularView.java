package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Interactive console interface for Extracurricular Clubs, Capacity Enforcement, Events, and Competitions.
 */
public class ExtracurricularView {

    private final ExtracurricularController controller = new ExtracurricularController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║   MÓDULO: ACTIVIDADES EXTRACURRICULARES      ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo club");
            System.out.println("2. Listar clubes y disponibilidad de cupos");
            System.out.println("3. Inscribir estudiante en club (control de aforo)");
            System.out.println("4. Retirar estudiante de un club");
            System.out.println("5. Consultar clubes de un estudiante");
            System.out.println("6. Registrar nuevo evento escolar");
            System.out.println("7. Listar calendario de eventos");
            System.out.println("8. Inscribir participante en evento");
            System.out.println("9. Consultar participantes y posiciones de un evento");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 9);
            try {
                switch (option) {
                    case 1 -> createClub();
                    case 2 -> listClubs();
                    case 3 -> enrollStudentInClub();
                    case 4 -> withdrawStudentFromClub();
                    case 5 -> viewStudentClubs();
                    case 6 -> createEvent();
                    case 7 -> listEvents();
                    case 8 -> registerParticipant();
                    case 9 -> viewEventParticipants();
                    default -> back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("ExtracurricularView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void createClub() throws SQLException {
        System.out.println("\n--- CREACIÓN DE CLUB EXTRACURRICULAR ---");
        Club c = new Club();
        c.setName(InputReader.readText("Nombre del club: "));
        c.setDescription(InputReader.readOptional("Descripción de las actividades"));
        int supId = InputReader.readInt("ID del profesor supervisor (0 si no asigna aún): ", 0, Integer.MAX_VALUE);
        if (supId > 0) c.setSupervisorTeacherId(supId);
        c.setMaxCapacity(InputReader.readInt("Capacidad máxima de estudiantes: ", 1, 100));
        c.setMeetingSchedule(InputReader.readOptional("Horario de reunión (ej: Martes 3-5 PM)"));

        int id = controller.createClub(c);
        System.out.println("\n✔ Club registrado exitosamente con ID: " + id);
    }

    private void listClubs() throws SQLException {
        List<Club> list = controller.listAllClubs();
        System.out.println("\n" + String.format("%-4s | %-25s | %-12s | %-20s | %-15s", "ID", "NOMBRE DEL CLUB", "CUPOS (ACT/MAX)", "PROFESOR SUPERVISOR", "ESTADO"));
        System.out.println("---------------------------------------------------------------------------------------");
        for (Club c : list) {
            String cupos = c.getActiveMembersCount() + "/" + c.getMaxCapacity();
            String disp = c.hasAvailableSpot() ? "Disponible" : "¡LLENO!";
            System.out.println(String.format("%-4d | %-25s | %-15s | %-20s | %-15s",
                    c.getId(),
                    c.getName(),
                    cupos + " (" + disp + ")",
                    c.getSupervisorTeacherName() != null ? c.getSupervisorTeacherName() : "Sin asignar",
                    c.isActive() ? "Activo" : "Inactivo"));
        }
        InputReader.pause();
    }

    private void enrollStudentInClub() throws SQLException {
        System.out.println("\n--- INSCRIPCIÓN EN CLUB ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int clubId = InputReader.readInt("ID del club al que desea ingresar: ", 1, Integer.MAX_VALUE);

        controller.enrollStudentInClub(studentId, clubId);
        System.out.println("\n✔ Estudiante inscrito exitosamente en el club.");
    }

    private void withdrawStudentFromClub() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int clubId = InputReader.readInt("ID del club: ", 1, Integer.MAX_VALUE);
        String reason = InputReader.readOptional("Motivo de retiro");

        controller.withdrawStudentFromClub(studentId, clubId, reason);
        System.out.println("\n✔ Membresía retirada exitosamente.");
    }

    private void viewStudentClubs() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        List<ClubMembership> list = controller.getStudentClubs(studentId);
        System.out.println("\nClubes del estudiante:");
        if (list.isEmpty()) {
            System.out.println("  (El estudiante no registra clubes)");
        } else {
            for (ClubMembership m : list) {
                System.out.println("  • " + m.getClubName() + " | Estado: " + m.getStatus() + " | Ingreso: " + m.getJoinDate());
            }
        }
        InputReader.pause();
    }

    private void createEvent() throws SQLException {
        System.out.println("\n--- REGISTRO DE EVENTO ESCOLAR ---");
        SchoolEvent ev = new SchoolEvent();
        ev.setName(InputReader.readText("Nombre del evento: "));
        ev.setDescription(InputReader.readOptional("Descripción del evento"));
        ev.setEventType(InputReader.readChoice("Tipo de evento:", Validator.EVENT_TYPES));
        ev.setEventDate(InputReader.readDate("Fecha del evento"));
        ev.setLocation(InputReader.readOptional("Lugar de realización"));
        ev.setHasCompetition(InputReader.readBoolean("¿Es un evento con competencia / premiación por posiciones?"));
        int orgId = InputReader.readInt("ID del profesor organizador (0 si no aplica): ", 0, Integer.MAX_VALUE);
        if (orgId > 0) ev.setOrganizerId(orgId);

        int id = controller.createEvent(ev);
        System.out.println("\n✔ Evento programado exitosamente con ID: " + id);
    }

    private void listEvents() throws SQLException {
        List<SchoolEvent> list = controller.listAllEvents();
        System.out.println("\n" + String.format("%-4s | %-28s | %-12s | %-12s | %-12s | %-10s", "ID", "NOMBRE DEL EVENTO", "TIPO", "FECHA", "COMPETENCIA", "PARTICIPANTES"));
        System.out.println("---------------------------------------------------------------------------------------------------");
        for (SchoolEvent e : list) {
            System.out.println(String.format("%-4d | %-28s | %-12s | %-12s | %-12s | %-10d",
                    e.getId(),
                    e.getName(),
                    e.getEventType(),
                    e.getEventDate(),
                    e.isHasCompetition() ? "Sí" : "No",
                    e.getParticipantCount()));
        }
        InputReader.pause();
    }

    private void registerParticipant() throws SQLException {
        System.out.println("\n--- REGISTRO DE PARTICIPANTE EN EVENTO ---");
        int eventId = InputReader.readInt("ID del evento: ", 1, Integer.MAX_VALUE);
        SchoolEvent ev = controller.findEventById(eventId);
        if (ev == null) {
            System.out.println("Evento no encontrado.");
            return;
        }
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        String role = InputReader.readChoice("Rol del estudiante en el evento:", Validator.EVENT_ROLES);
        String position = null;
        if (ev.isHasCompetition()) {
            position = InputReader.readChoice("Posición obtenida en la competencia:", Validator.EVENT_POSITIONS);
        }
        String remarks = InputReader.readOptional("Observaciones o reconocimientos");

        controller.registerEventParticipant(eventId, studentId, role, position, remarks);
        System.out.println("\n✔ Participación registrada correctamente.");
    }

    private void viewEventParticipants() throws SQLException {
        int eventId = InputReader.readInt("ID del evento: ", 1, Integer.MAX_VALUE);
        SchoolEvent ev = controller.findEventById(eventId);
        if (ev == null) {
            System.out.println("Evento no encontrado.");
            return;
        }
        List<EventParticipant> participants = controller.getEventParticipants(eventId);
        System.out.println("\nParticipantes de '" + ev.getName() + "':");
        if (participants.isEmpty()) {
            System.out.println("  (No hay participantes registrados)");
        } else {
            for (EventParticipant ep : participants) {
                System.out.println("  • " + ep.getStudentName() + " | Rol: " + ep.getRole() +
                        (ep.getPosition() != null ? " | Posición: " + ep.getPosition() : "") +
                        (ep.getRemarks() != null ? " (" + ep.getRemarks() + ")" : ""));
            }
        }
        InputReader.pause();
    }
}
