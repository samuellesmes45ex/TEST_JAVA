package com.littlehorizons.erp.model;

import java.time.LocalTime;

/**
 * Domain model representing a teacher's subject schedule assignment (table 'asignaciones_profesor').
 */
public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int academicYear;
    private String section;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    // Supplementary display attributes
    private String teacherName;
    private String subjectName;

    public TeacherAssignment() {
    }

    public TeacherAssignment(int id, int teacherId, int subjectId, int academicYear,
                             String section, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.section = section;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    @Override
    public String toString() {
        return "[" + id + "] " + (subjectName != null ? subjectName : "Materia " + subjectId) +
                " | Prof: " + (teacherName != null ? teacherName : "ID " + teacherId) +
                " | Sección: " + section + " | " + dayOfWeek + " " + startTime + " - " + endTime + " (" + academicYear + ")";
    }
}
