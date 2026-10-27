package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a teacher/instructor (table 'profesores').
 */
public class Teacher {

    private int id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String address;
    private String academicDegree;
    private String specialty;
    private LocalDate hireDate;
    private String status;

    public Teacher() {
    }

    public Teacher(int id, String documentType, String documentNumber, String firstName,
                   String middleName, String lastName, String secondLastName, String email,
                   String phone, String address, String academicDegree, String specialty,
                   LocalDate hireDate, String status) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.academicDegree = academicDegree;
        this.specialty = specialty;
        this.hireDate = hireDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAcademicDegree() { return academicDegree; }
    public void setAcademicDegree(String academicDegree) { this.academicDegree = academicDegree; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (middleName != null && !middleName.trim().isEmpty()) {
            sb.append(" ").append(middleName.trim());
        }
        sb.append(" ").append(lastName);
        if (secondLastName != null && !secondLastName.trim().isEmpty()) {
            sb.append(" ").append(secondLastName.trim());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + getFullName() + " (" + specialty + ") - " + status;
    }
}
