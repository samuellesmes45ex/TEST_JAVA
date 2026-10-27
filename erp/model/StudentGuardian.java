package com.littlehorizons.erp.model;

/**
 * Domain model representing the many-to-many link between Student and Guardian (table 'estudiante_acudiente').
 */
public class StudentGuardian {

    private int studentId;
    private int guardianId;
    private boolean isPrimary;
    private boolean isEmergencyContact;

    // Supplementary display fields populated during queries
    private String studentName;
    private String guardianName;
    private String relationship;
    private String guardianPhone;

    public StudentGuardian() {
    }

    public StudentGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergencyContact) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.isPrimary = isPrimary;
        this.isEmergencyContact = isEmergencyContact;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean isPrimary) { this.isPrimary = isPrimary; }

    public boolean isEmergencyContact() { return isEmergencyContact; }
    public void setEmergencyContact(boolean isEmergencyContact) { this.isEmergencyContact = isEmergencyContact; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
}
