package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a generated DIAN Electronic Invoice (table 'facturas').
 */
public class Invoice {

    private int id;
    private String invoiceNumber;
    private int paymentId;
    private LocalDate issueDate;
    private String cufe;
    private String pdfFileName;
    private String filePath;

    // Display fields
    private Payment payment;

    public Invoice() {
    }

    public Invoice(int id, String invoiceNumber, int paymentId, LocalDate issueDate,
                   String cufe, String pdfFileName, String filePath) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.paymentId = paymentId;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.pdfFileName = pdfFileName;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getCufe() { return cufe; }
    public void setCufe(String cufe) { this.cufe = cufe; }

    public String getPdfFileName() { return pdfFileName; }
    public void setPdfFileName(String pdfFileName) { this.pdfFileName = pdfFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    @Override
    public String toString() {
        return "[" + id + "] Factura: " + invoiceNumber + " | Fecha: " + issueDate +
                " | CUFE: " + (cufe != null ? cufe.substring(0, Math.min(cufe.length(), 16)) + "..." : "N/A") +
                " | PDF: " + pdfFileName;
    }
}
