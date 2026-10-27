package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a student's grade enrollment for a school year (table 'matriculas').
 */
public class Enrollment {

    private int id;
    private int studentId;
    private int gradeId;
    private int academicYear;
    private String status;
    private LocalDate enrollmentDate;
    private LocalDate withdrawalDate;
    private String remarks;

    // Supplementary display attributes
    private String studentName;
    private String gradeName;

    public Enrollment() {
    }

    public Enrollment(int id, int studentId, int gradeId, int academicYear,
                      String status, LocalDate enrollmentDate, LocalDate withdrawalDate, String remarks) {
        this.id = id;
        this.studentId = studentId;
        this.gradeId = gradeId;
        this.academicYear = academicYear;
        this.status = status;
        this.enrollmentDate = enrollmentDate;
        this.withdrawalDate = withdrawalDate;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public LocalDate getWithdrawalDate() { return withdrawalDate; }
    public void setWithdrawalDate(LocalDate withdrawalDate) { this.withdrawalDate = withdrawalDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    @Override
    public String toString() {
        return "[" + id + "] Estudiante: " + studentName + " | Grado: " + gradeName +
                " | Año: " + academicYear + " | Estado: " + status;
    }
}
