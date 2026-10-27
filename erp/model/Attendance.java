package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing student attendance records (table 'asistencias').
 */
public class Attendance {

    private int id;
    private int studentId;
    private int subjectId;
    private LocalDate attendanceDate;
    private String status; // 'Presente', 'Ausente', 'Tardanza', 'Excusa Médica'
    private String remarks;
    private Integer recordedBy; // teacher id

    // Display fields
    private String studentName;
    private String subjectName;
    private String teacherName;

    public Attendance() {
    }

    public Attendance(int id, int studentId, int subjectId, LocalDate attendanceDate,
                      String status, String remarks, Integer recordedBy) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.remarks = remarks;
        this.recordedBy = recordedBy;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Integer getRecordedBy() { return recordedBy; }
    public void setRecordedBy(Integer recordedBy) { this.recordedBy = recordedBy; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return "[" + id + "] " + attendanceDate + " | " + studentName + " | " + subjectName + " : " + status;
    }
}
