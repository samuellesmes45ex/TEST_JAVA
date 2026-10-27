package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing school events (table 'eventos').
 */
public class SchoolEvent {

    private int id;
    private String name;
    private String description;
    private String eventType; // 'Académico', 'Cultural', 'Deportivo', 'Social', 'Científico'
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private Integer organizerId;
    private boolean active;

    // Display fields
    private String organizerName;
    private int participantCount;

    public SchoolEvent() {
    }

    public SchoolEvent(int id, String name, String description, String eventType,
                       LocalDate eventDate, String location, boolean hasCompetition,
                       Integer organizerId, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
        this.organizerId = organizerId;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isHasCompetition() { return hasCompetition; }
    public void setHasCompetition(boolean hasCompetition) { this.hasCompetition = hasCompetition; }

    public Integer getOrganizerId() { return organizerId; }
    public void setOrganizerId(Integer organizerId) { this.organizerId = organizerId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " (" + eventType + ") - Fecha: " + eventDate +
                (hasCompetition ? " [Con Competencia]" : "") + " | Participantes: " + participantCount;
    }
}
