package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.PdfInvoiceGenerator;
import com.littlehorizons.erp.util.Validator;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business controller for Financial operations, Payment records, Defaulter detection (using Java Streams),
 * and DIAN Electronic Invoice PDF generation.
 */
public class FinancialController {

    private final PaymentDao paymentDao = new PaymentDao();
    private final StudentDao studentDao = new StudentDao();
    private final GuardianDao guardianDao = new GuardianDao();

    public List<PaymentConcept> listConcepts() throws SQLException {
        return paymentDao.getAllConcepts();
    }

    public PaymentConcept getConceptById(int id) throws SQLException {
        return paymentDao.getConceptById(id);
    }

    public int registerPayment(int studentId, Integer guardianId, int conceptId,
                               BigDecimal amount, String method, Integer academicYear,
                               String period, String notes) throws SQLException {
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        PaymentConcept c = paymentDao.getConceptById(conceptId);
        if (c == null) {
            throw new IllegalArgumentException("El concepto de pago no existe.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }
        if (!Validator.PAYMENT_METHODS.contains(method)) {
            throw new IllegalArgumentException("Método de pago no válido: " + method);
        }

        String receipt = "REC-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis() % 1000000;

        Payment p = new Payment();
        p.setStudentId(studentId);
        p.setGuardianId(guardianId);
        p.setConceptId(conceptId);
        p.setAmountPaid(amount);
        p.setPaymentDate(LocalDate.now());
        p.setPaymentMethod(method);
        p.setReceiptNumber(receipt);
        p.setAcademicYear(academicYear != null ? academicYear : LocalDate.now().getYear());
        p.setAcademicPeriod(period != null ? period : "Anual");
        p.setNotes(notes);

        return paymentDao.insertPayment(p);
    }

    public List<Payment> getPaymentsByStudent(int studentId) throws SQLException {
        return paymentDao.getPaymentsByStudent(studentId);
    }

    public List<Payment> getPaymentsByGuardian(int guardianId) throws SQLException {
        return paymentDao.getPaymentsByGuardian(guardianId);
    }

    public List<Payment> listAllPayments() throws SQLException {
        return paymentDao.getAllPayments();
    }

    /**
     * Generates a DIAN Electronic Invoice PDF for a confirmed payment and records it in the database.
     */
    public Invoice generateElectronicInvoice(int paymentId) throws Exception {
        Payment payment = paymentDao.findPaymentById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("No se encontró el pago con ID " + paymentId);
        }

        Invoice existing = paymentDao.findInvoiceByPaymentId(paymentId);
        if (existing != null) {
            return existing;
        }

        Student student = studentDao.findById(payment.getStudentId());
        Guardian guardian = payment.getGuardianId() != null ? guardianDao.findById(payment.getGuardianId()) : null;

        int seq = paymentDao.getNextInvoiceSequence();
        String invNum = String.format("FAC-%06d", seq);

        File pdfFile = PdfInvoiceGenerator.generateInvoicePdf(invNum, payment, student, guardian);
        String cufe = PdfInvoiceGenerator.calculateCufe(invNum, LocalDate.now(), payment.getAmountPaid().toString());

        Invoice inv = new Invoice();
        inv.setInvoiceNumber(invNum);
        inv.setPaymentId(paymentId);
        inv.setIssueDate(LocalDate.now());
        inv.setCufe(cufe);
        inv.setPdfFileName(pdfFile.getName());
        inv.setFilePath(pdfFile.getAbsolutePath());

        paymentDao.insertInvoice(inv);
        inv.setPayment(payment);
        return inv;
    }

    /**
     * Identifies students who are overdue/defaulters (> 30 days since last payment or no payments at all).
     * Implemented using Java Stream API and Lambdas.
     */
    public List<DefaulterRecord> getDefaultersReport(int daysThreshold) throws SQLException {
        List<Student> activeStudents = studentDao.findAll().stream()
                .filter(s -> "Activo".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        List<Payment> allPayments = paymentDao.getAllPayments();

        // Group payments by student ID
        Map<Integer, List<Payment>> paymentsByStudent = allPayments.stream()
                .collect(Collectors.groupingBy(Payment::getStudentId));

        LocalDate today = LocalDate.now();

        return activeStudents.stream()
                .map(student -> {
                    List<Payment> studentPayments = paymentsByStudent.get(student.getId());
                    LocalDate lastPaymentDate = null;
                    if (studentPayments != null && !studentPayments.isEmpty()) {
                        lastPaymentDate = studentPayments.stream()
                                .map(Payment::getPaymentDate)
                                .max(Comparator.naturalOrder())
                                .orElse(null);
                    }

                    long daysPending = lastPaymentDate != null ? ChronoUnit.DAYS.between(lastPaymentDate, today) : 999;
                    return new DefaulterRecord(student, lastPaymentDate, daysPending);
                })
                .filter(rec -> rec.daysPending >= daysThreshold)
                .sorted(Comparator.comparingLong(DefaulterRecord::getDaysPending).reversed())
                .collect(Collectors.toList());
    }

    public static class DefaulterRecord {
        private final Student student;
        private final LocalDate lastPaymentDate;
        private final long daysPending;

        public DefaulterRecord(Student student, LocalDate lastPaymentDate, long daysPending) {
            this.student = student;
            this.lastPaymentDate = lastPaymentDate;
            this.daysPending = daysPending;
        }

        public Student getStudent() { return student; }
        public LocalDate getLastPaymentDate() { return lastPaymentDate; }
        public long getDaysPending() { return daysPending; }

        @Override
        public String toString() {
            String last = lastPaymentDate != null ? lastPaymentDate.toString() : "Sin pagos registrados";
            return student.getFullName() + " (Doc: " + student.getDocumentNumber() + ") - Último pago: " + last + " (" + daysPending + " días de mora)";
        }
    }
}
