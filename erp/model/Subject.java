package com.littlehorizons.erp.model;

/**
 * Domain model representing an academic course/subject (table 'materias').
 */
public class Subject {

    private int id;
    private String code;
    private String name;
    private String description;
    private int gradeId;
    private int weeklyHours;
    private boolean active;

    // Supplementary display field
    private String gradeName;

    public Subject() {
    }

    public Subject(int id, String code, String name, String description, int gradeId, int weeklyHours, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.gradeId = gradeId;
        this.weeklyHours = weeklyHours;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    @Override
    public String toString() {
        return "[" + id + "] " + code + " - " + name + " (" + (gradeName != null ? gradeName : "Grado " + gradeId) + ") - " + weeklyHours + "h/sem";
    }
}
