package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinancialController;
import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interactive console interface for Consolidated Reports, Academic Rankings, and Alerts.
 */
public class ReportView {

    private final ReportController controller = new ReportController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: REPORTES Y ESTADÍSTICAS      ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Cuadro de Honor / Ranking de Mejores Estudiantes");
            System.out.println("2. Alerta de Inasistencias Críticas");
            System.out.println("3. Reporte de Cartera y Estudiantes Morosos");
            System.out.println("4. Reporte de Préstamos de Biblioteca Vencidos");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 4);
            try {
                switch (option) {
                    case 1 -> viewTopStudents();
                    case 2 -> viewAttendanceAlerts();
                    case 3 -> viewFinancialDefaulters();
                    case 4 -> viewOverdueLoans();
                    default -> back = true;
                }
            } catch (SQLException e) {
                AppLogger.error("ReportView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void viewTopStudents() throws SQLException {
        System.out.println("\n--- CUADRO DE HONOR Y EXCELENCIA ACADÉMICA ---");
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período a consultar:", Validator.GRADE_PERIODS);
        int topN = InputReader.readInt("Número de puestos a mostrar (ej: 5 o 10): ", 1, 50);

        List<ReportController.StudentGpaRank> ranks = controller.getTopStudents(year, period, topN);
        if (ranks.isEmpty()) {
            System.out.println("No se encontraron registros de calificaciones para ese período.");
        } else {
            System.out.println("\nRANKING DE ESTUDIANTES DESTACADOS (Año " + year + " - Período " + period + "):");
            int pos = 1;
            for (ReportController.StudentGpaRank r : ranks) {
                System.out.println(String.format("  Puesto #%02d: %s (Promedio: %.2f en %d materias)",
                        pos++, r.getStudent().getFullName(), r.getGpa(), r.getSubjectCount()));
            }
        }
        InputReader.pause();
    }

    private void viewAttendanceAlerts() throws SQLException {
        System.out.println("\n--- ALERTAS DE INASISTENCIA ---");
        LocalDate start = InputReader.readDate("Fecha de inicio");
        LocalDate end = InputReader.readDate("Fecha de fin");
        int min = InputReader.readInt("Mínimo de ausencias para alertar: ", 1, 30);

        Map<String, Integer> alerts = controller.getAttendanceAlerts(start, end, min);
        if (alerts.isEmpty()) {
            System.out.println("✔ Ningún estudiante supera el límite de " + min + " ausencias en las fechas dadas.");
        } else {
            System.out.println("\nESTUDIANTES CON INASISTENCIAS PREOCUPANTES:");
            alerts.forEach((info, count) -> System.out.println("  ⚠ " + info + " -> " + count + " faltas"));
        }
        InputReader.pause();
    }

    private void viewFinancialDefaulters() throws SQLException {
        System.out.println("\n--- REPORTE DE MOROSIDAD EN PAGOS ---");
        int days = InputReader.readInt("Días de atraso mínimos (ej: 30): ", 1, 365);
        List<FinancialController.DefaulterRecord> morosos = controller.getFinancialMorosos(days);

        if (morosos.isEmpty()) {
            System.out.println("✔ Cartera al día. Ningún estudiante supera " + days + " días de mora.");
        } else {
            System.out.println("\nESTUDIANTES EN MORA IDENTIFICADOS (" + morosos.size() + "):");
            for (FinancialController.DefaulterRecord d : morosos) {
                System.out.println("  ⚠ " + d);
            }
        }
        InputReader.pause();
    }

    private void viewOverdueLoans() throws SQLException {
        System.out.println("\n--- REPORTE DE PRÉSTAMOS VENCIDOS (BIBLIOTECA / LABORATORIOS) ---");
        List<ResourceLoan> overdue = controller.getOverdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("✔ No existen préstamos vencidos en este momento. Todos los recursos se han devuelto a tiempo.");
        } else {
            System.out.println("\nPRÉSTAMOS PENDIENTES DE DEVOLUCIÓN URGENTE (" + overdue.size() + "):");
            for (ResourceLoan l : overdue) {
                System.out.println("  ⚠ Recurso: " + l.getResourceName() + " (" + l.getResourceCode() + ") | " +
                        l.getBorrowerType() + ": " + l.getBorrowerName() + " | Fecha pactada: " + l.getExpectedReturnDate());
            }
        }
        InputReader.pause();
    }
}
