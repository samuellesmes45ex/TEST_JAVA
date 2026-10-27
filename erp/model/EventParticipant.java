package com.littlehorizons.erp.model;

/**
 * Domain model representing a student participating in an event (table 'participantes_evento').
 */
public class EventParticipant {

    private int id;
    private int eventId;
    private int studentId;
    private String role; // 'Participante', 'Competidor', 'Organizador', 'Asistente'
    private String position; // '1er Lugar', '2do Lugar', '3er Lugar', 'Participante'
    private String remarks;

    // Display fields
    private String eventName;
    private String studentName;

    public EventParticipant() {
    }

    public EventParticipant(int id, int eventId, int studentId, String role, String position, String remarks) {
        this.id = id;
        this.eventId = eventId;
        this.studentId = studentId;
        this.role = role;
        this.position = position;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    @Override
    public String toString() {
        return "[" + id + "] " + studentName + " en " + eventName +
                " | Rol: " + role + (position != null ? " | Posición: " + position : "");
    }
}
