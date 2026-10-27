package com.littlehorizons.erp.model;

/**
 * Domain model representing a student's parent/guardian (table 'acudientes').
 */
public class Guardian {

    private int id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private String relationship;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String address;
    private String occupation;
    private boolean financialResponsible;

    public Guardian() {
    }

    public Guardian(int id, String documentType, String documentNumber, String firstName,
                    String middleName, String lastName, String secondLastName, String relationship,
                    String primaryPhone, String secondaryPhone, String email, String address,
                    String occupation, boolean financialResponsible) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.relationship = relationship;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
        this.financialResponsible = financialResponsible;
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

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPrimaryPhone() { return primaryPhone; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }

    public String getSecondaryPhone() { return secondaryPhone; }
    public void setSecondaryPhone(String secondaryPhone) { this.secondaryPhone = secondaryPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public boolean isFinancialResponsible() { return financialResponsible; }
    public void setFinancialResponsible(boolean financialResponsible) { this.financialResponsible = financialResponsible; }

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
        return "[" + id + "] " + getFullName() + " (" + relationship + " - Tel: " + primaryPhone + ")";
    }
}
