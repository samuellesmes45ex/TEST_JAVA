package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a resource loan to either a student or a teacher
 * (maps to 'prestamos_estudiante' or 'prestamos_profesor').
 */
public class ResourceLoan {

    private int id;
    private int resourceId;
    private String borrowerType; // 'Estudiante' or 'Profesor'
    private int borrowerId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status; // 'Activo', 'Devuelto', 'Vencido'
    private String remarks;

    // Display fields
    private String resourceCode;
    private String resourceName;
    private String borrowerName;

    public ResourceLoan() {
    }

    public ResourceLoan(int id, int resourceId, String borrowerType, int borrowerId,
                        LocalDate loanDate, LocalDate expectedReturnDate,
                        LocalDate actualReturnDate, String status, String remarks) {
        this.id = id;
        this.resourceId = resourceId;
        this.borrowerType = borrowerType;
        this.borrowerId = borrowerId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getBorrowerType() { return borrowerType; }
    public void setBorrowerType(String borrowerType) { this.borrowerType = borrowerType; }

    public int getBorrowerId() { return borrowerId; }
    public void setBorrowerId(int borrowerId) { this.borrowerId = borrowerId; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public boolean isOverdue() {
        if ("Devuelto".equalsIgnoreCase(status)) {
            return false;
        }
        return expectedReturnDate != null && expectedReturnDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "[" + id + "] " + (resourceName != null ? resourceName : "Recurso " + resourceId) +
                " -> " + borrowerType + ": " + (borrowerName != null ? borrowerName : borrowerId) +
                " | Prestado: " + loanDate + " | Entrega pactada: " + expectedReturnDate +
                " | Estado: " + (isOverdue() ? "VENCIDO" : status);
    }
}
