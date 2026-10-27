package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinancialController;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Validator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Interactive console interface for Financial Management, Payments, Morosos, and DIAN Electronic Invoices.
 */
public class FinancialView {

    private final FinancialController controller = new FinancialController();
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN FINANCIERA           ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Consultar catálogo de conceptos de pago");
            System.out.println("2. Registrar nuevo pago");
            System.out.println("3. Listar todos los pagos realizados");
            System.out.println("4. Consultar pagos de un estudiante");
            System.out.println("5. Consultar pagos asociados a un acudiente");
            System.out.println("6. Generar Factura Electrónica DIAN (PDF + CUFE + QR)");
            System.out.println("7. Reporte de estudiantes morosos (>30 días)");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 7);
            try {
                switch (option) {
                    case 1 -> listConcepts();
                    case 2 -> registerPayment();
                    case 3 -> listPayments();
                    case 4 -> viewStudentPayments();
                    case 5 -> viewGuardianPayments();
                    case 6 -> generateInvoice();
                    case 7 -> viewDefaultersReport();
                    default -> back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("FinancialView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            } catch (Exception e) {
                AppLogger.error("FinancialView unexpected error", e);
                System.out.println("\n[ERROR] Ocurrió un fallo en el procesamiento: " + e.getMessage());
            }
        }
    }

    private void listConcepts() throws SQLException {
        List<PaymentConcept> concepts = controller.listConcepts();
        System.out.println("\n" + String.format("%-4s | %-12s | %-30s | %-15s | %-10s", "ID", "CÓDIGO", "NOMBRE DEL CONCEPTO", "VALOR BASE", "RECURRENTE"));
        System.out.println("-----------------------------------------------------------------------------------");
        for (PaymentConcept c : concepts) {
            System.out.println(String.format("%-4d | %-12s | %-30s | %-15s | %-10s",
                    c.getId(),
                    c.getCode(),
                    c.getName(),
                    CURRENCY_FORMAT.format(c.getBaseAmount()),
                    c.isRecurrent() ? "Mensual" : "Único"));
        }
        InputReader.pause();
    }

    private void registerPayment() throws Exception {
        System.out.println("\n--- REGISTRO DE PAGO ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int guardianId = InputReader.readInt("ID del acudiente responsable (0 si no aplica): ", 0, Integer.MAX_VALUE);
        int conceptId = InputReader.readInt("ID del concepto a pagar: ", 1, Integer.MAX_VALUE);

        PaymentConcept c = controller.getConceptById(conceptId);
        if (c != null) {
            System.out.println("Valor base del concepto: " + CURRENCY_FORMAT.format(c.getBaseAmount()));
        }

        BigDecimal amount = InputReader.readBigDecimal("Monto pagado (permite descuentos o becas): ", 1.0, 100000000.0);
        String method = InputReader.readChoice("Método de pago:", Validator.PAYMENT_METHODS);
        int year = InputReader.readInt("Año al que aplica el pago: ", 2020, 2035);
        String period = InputReader.readChoice("Período:", List.of("I", "II", "III", "IV", "Anual"));
        String notes = InputReader.readOptional("Notas / Detalles del comprobante");

        int paymentId = controller.registerPayment(studentId, guardianId > 0 ? guardianId : null,
                conceptId, amount, method, year, period, notes);

        System.out.println("\n✔ Pago registrado exitosamente con ID #" + paymentId);

        // Offer immediate Electronic Invoice generation
        if (InputReader.readBoolean("¿Desea generar la Factura Electrónica oficial DIAN en PDF ahora mismo?")) {
            generateInvoiceFlow(paymentId);
        }
    }

    private void listPayments() throws SQLException {
        List<Payment> list = controller.listAllPayments();
        printPaymentTable(list);
    }

    private void viewStudentPayments() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        List<Payment> list = controller.getPaymentsByStudent(studentId);
        printPaymentTable(list);
    }

    private void viewGuardianPayments() throws SQLException {
        int guardianId = InputReader.readInt("ID del acudiente: ", 1, Integer.MAX_VALUE);
        List<Payment> list = controller.getPaymentsByGuardian(guardianId);
        printPaymentTable(list);
    }

    private void generateInvoice() throws Exception {
        int paymentId = InputReader.readInt("Ingrese el ID del pago para emitir factura: ", 1, Integer.MAX_VALUE);
        generateInvoiceFlow(paymentId);
    }

    private void generateInvoiceFlow(int paymentId) throws Exception {
        System.out.println("\nGenerando Factura Electrónica oficial...");
        Invoice invoice = controller.generateElectronicInvoice(paymentId);
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║   FACTURA ELECTRÓNICA DIAN GENERADA EXITOSAMENTE                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("  • Número de Factura : " + invoice.getInvoiceNumber());
        System.out.println("  • Fecha de Emisión  : " + invoice.getIssueDate());
        System.out.println("  • CUFE Simulado     : " + invoice.getCufe());
        System.out.println("  • Archivo PDF       : " + invoice.getPdfFileName());
        System.out.println("  • Ubicación Física  : " + invoice.getFilePath());
        System.out.println("------------------------------------------------------------------");
        System.out.println("El documento PDF ha sido guardado con la información reglamentaria,");
        System.out.println("el desglose de conceptos y el código QR generado.");
        InputReader.pause();
    }

    private void viewDefaultersReport() throws SQLException {
        System.out.println("\n--- IDENTIFICACIÓN DE ESTUDIANTES MOROSOS ---");
        int days = InputReader.readInt("Días de mora mínimos para alertar (ej: 30): ", 1, 365);
        List<FinancialController.DefaulterRecord> defaulters = controller.getDefaultersReport(days);

        if (defaulters.isEmpty()) {
            System.out.println("✔ No hay estudiantes con más de " + days + " días de mora en sus pagos.");
        } else {
            System.out.println("\nESTUDIANTES EN MORA IDENTIFICADOS (" + defaulters.size() + "):");
            for (FinancialController.DefaulterRecord d : defaulters) {
                System.out.println("  ⚠ " + d);
            }
        }
        InputReader.pause();
    }

    private void printPaymentTable(List<Payment> payments) {
        System.out.println("\n" + String.format("%-5s | %-14s | %-11s | %-26s | %-24s | %-13s | %-12s",
                "ID", "RECIBO", "FECHA", "ESTUDIANTE", "CONCEPTO", "MONTO", "MÉTODO"));
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (Payment p : payments) {
            System.out.println(String.format("%-5d | %-14s | %-11s | %-26s | %-24s | %-13s | %-12s",
                    p.getId(),
                    p.getReceiptNumber() != null ? p.getReceiptNumber() : "-",
                    p.getPaymentDate(),
                    p.getStudentName(),
                    p.getConceptName(),
                    CURRENCY_FORMAT.format(p.getAmountPaid()),
                    p.getPaymentMethod()));
        }
        System.out.println("(" + payments.size() + " pagos registrados)");
        InputReader.pause();
    }
}
