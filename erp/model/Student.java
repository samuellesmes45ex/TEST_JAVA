package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a student (mapped to the 'estudiantes' table).
 */
public class Student {

    private int id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String phone;
    private String email;
    private String bloodType;
    private String allergies;
    private String medicalConditions;
    private LocalDate admissionDate;
    private String status;

    public Student() {
    }

    public Student(int id, String documentType, String documentNumber, String firstName,
                   String middleName, String lastName, String secondLastName, LocalDate birthDate,
                   String gender, String address, String phone, String email, String bloodType,
                   String allergies, String medicalConditions, LocalDate admissionDate, String status) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.admissionDate = admissionDate;
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

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Builds and returns full formatted name (e.g., "Juan Carlos Perez Gomez").
     */
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
        return "[" + id + "] " + getFullName() + " (" + documentType + ": " + documentNumber + ") - " + status;
    }
}
