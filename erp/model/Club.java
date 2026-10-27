package com.littlehorizons.erp.model;

/**
 * Domain model representing an extracurricular club (table 'clubes').
 */
public class Club {

    private int id;
    private String name;
    private String description;
    private Integer supervisorTeacherId;
    private int maxCapacity;
    private String meetingSchedule;
    private boolean active;

    // Display fields
    private String supervisorTeacherName;
    private int activeMembersCount;

    public Club() {
    }

    public Club(int id, String name, String description, Integer supervisorTeacherId,
                int maxCapacity, String meetingSchedule, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.supervisorTeacherId = supervisorTeacherId;
        this.maxCapacity = maxCapacity;
        this.meetingSchedule = meetingSchedule;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSupervisorTeacherId() { return supervisorTeacherId; }
    public void setSupervisorTeacherId(Integer supervisorTeacherId) { this.supervisorTeacherId = supervisorTeacherId; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getMeetingSchedule() { return meetingSchedule; }
    public void setMeetingSchedule(String meetingSchedule) { this.meetingSchedule = meetingSchedule; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getSupervisorTeacherName() { return supervisorTeacherName; }
    public void setSupervisorTeacherName(String supervisorTeacherName) { this.supervisorTeacherName = supervisorTeacherName; }

    public int getActiveMembersCount() { return activeMembersCount; }
    public void setActiveMembersCount(int activeMembersCount) { this.activeMembersCount = activeMembersCount; }

    public boolean hasAvailableSpot() {
        return activeMembersCount < maxCapacity;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + " (Cupos: " + activeMembersCount + "/" + maxCapacity + ") - Supervisor: " +
                (supervisorTeacherName != null ? supervisorTeacherName : "Sin asignar");
    }
}
