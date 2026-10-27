package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Validator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interactive console interface for Grades, Automatic Averages Calculation, and Attendance Control.
 */
public class AcademicView {

    private final AcademicController controller = new AcademicController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN ACADÉMICA            ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar calificación (0.0 a 5.0)");
            System.out.println("2. Consultar notas de un estudiante por período");
            System.out.println("3. Consultar notas de un curso/materia");
            System.out.println("4. Consultar historial académico completo");
            System.out.println("5. Calcular promedio por materia de un estudiante");
            System.out.println("6. Calcular promedio general (GPA) de un estudiante");
            System.out.println("7. Calcular promedio general del curso en una materia");
            System.out.println("8. Marcar asistencia diaria");
            System.out.println("9. Consultar asistencia por materia y fecha");
            System.out.println("10. Generar reporte de inasistencias preocupantes");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 10);
            try {
                switch (option) {
                    case 1 -> recordGrade();
                    case 2 -> viewStudentPeriodGrades();
                    case 3 -> viewCourseGrades();
                    case 4 -> viewStudentAcademicHistory();
                    case 5 -> calculateSubjectAverage();
                    case 6 -> calculatePeriodGpa();
                    case 7 -> calculateCourseAverage();
                    case 8 -> markAttendance();
                    case 9 -> viewAttendanceByDate();
                    case 10 -> highAbsencesReport();
                    default -> back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("AcademicView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void recordGrade() throws SQLException {
        System.out.println("\n--- REGISTRO DE CALIFICACIÓN ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        int teacherId = InputReader.readInt("ID del profesor evaluador: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período evaluado:", Validator.GRADE_PERIODS);
        BigDecimal score = InputReader.readBigDecimal("Nota numérica (0.0 - 5.0): ", 0.0, 5.0);
        String remarks = InputReader.readOptional("Observaciones sobre el desempeño");

        int id = controller.recordGrade(studentId, subjectId, teacherId, year, period, score, remarks);
        System.out.println("\n✔ Calificación guardada exitosamente (Registro #" + id + ").");
    }

    private void viewStudentPeriodGrades() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período:", Validator.GRADE_PERIODS);

        List<AcademicRecord> list = controller.getStudentGradesByPeriod(studentId, year, period);
        printGradesTable(list);

        BigDecimal gpa = controller.calculateStudentPeriodGpa(studentId, year, period);
        System.out.println(">> PROMEDIO GENERAL DEL PERÍODO (GPA): " + gpa + (gpa.compareTo(new BigDecimal("3.0")) >= 0 ? " [APROBADO]" : " [EN RIESGO]"));
        InputReader.pause();
    }

    private void viewCourseGrades() throws SQLException {
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período:", Validator.GRADE_PERIODS);

        List<AcademicRecord> list = controller.getCourseGrades(subjectId, year, period);
        printGradesTable(list);

        BigDecimal avg = controller.calculateCourseSubjectAverage(subjectId, year, period);
        System.out.println(">> PROMEDIO GENERAL DEL CURSO: " + avg);
        InputReader.pause();
    }

    private void viewStudentAcademicHistory() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        List<AcademicRecord> history = controller.getStudentHistory(studentId);
        System.out.println("\n--- HISTORIAL ACADÉMICO COMPLETO ---");
        printGradesTable(history);
        InputReader.pause();
    }

    private void calculateSubjectAverage() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);

        BigDecimal avg = controller.calculateStudentSubjectAverage(studentId, subjectId, year);
        System.out.println("\n>> Promedio acumulado en la materia para el año " + year + ": " + avg);
        InputReader.pause();
    }

    private void calculatePeriodGpa() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período:", Validator.GRADE_PERIODS);

        BigDecimal gpa = controller.calculateStudentPeriodGpa(studentId, year, period);
        System.out.println("\n>> Promedio general del estudiante (GPA Período " + period + "): " + gpa);
        InputReader.pause();
    }

    private void calculateCourseAverage() throws SQLException {
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período:", Validator.GRADE_PERIODS);

        BigDecimal avg = controller.calculateCourseSubjectAverage(subjectId, year, period);
        System.out.println("\n>> Promedio general del grupo en la materia: " + avg);
        InputReader.pause();
    }

    private void markAttendance() throws SQLException {
        System.out.println("\n--- CONTROL DE ASISTENCIA DIARIA ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        LocalDate date = InputReader.readDate("Fecha de la sesión");
        String status = InputReader.readChoice("Estado de asistencia:", Validator.ATTENDANCE_STATUSES);
        String remarks = InputReader.readOptional("Justificación / Observaciones");
        int teacherId = InputReader.readInt("ID del profesor que registra: ", 1, Integer.MAX_VALUE);

        controller.markAttendance(studentId, subjectId, date, status, remarks, teacherId);
        System.out.println("✔ Asistencia marcada con éxito.");
    }

    private void viewAttendanceByDate() throws SQLException {
        int subjectId = InputReader.readInt("ID de la materia: ", 1, Integer.MAX_VALUE);
        LocalDate date = InputReader.readDate("Fecha de la clase");

        List<Attendance> list = controller.getSubjectAttendanceByDate(subjectId, date);
        System.out.println("\nRegistro de Asistencia (" + date + "):");
        if (list.isEmpty()) {
            System.out.println("  (No hay registros para esa fecha)");
        } else {
            for (Attendance a : list) {
                System.out.println("  • " + a.getStudentName() + " : " + a.getStatus() +
                        (a.getRemarks() != null ? " (" + a.getRemarks() + ")" : ""));
            }
        }
        InputReader.pause();
    }

    private void highAbsencesReport() throws SQLException {
        System.out.println("\n--- REPORTE DE ESTUDIANTES CON INASISTENCIAS PREOCUPANTES ---");
        LocalDate start = InputReader.readDate("Fecha de inicio del rango");
        LocalDate end = InputReader.readDate("Fecha de fin del rango");
        int min = InputReader.readInt("Número mínimo de ausencias para alertar (ej: 3): ", 1, 50);

        Map<String, Integer> alerts = controller.getHighAbsenceReport(start, end, min);
        if (alerts.isEmpty()) {
            System.out.println("✔ No se encontraron estudiantes con " + min + " o más inasistencias en el período.");
        } else {
            System.out.println("\n¡ALERTA DE INASISTENCIAS ENCONTRADAS!");
            alerts.forEach((info, count) -> System.out.println("  ⚠ " + info + " -> " + count + " ausencias"));
        }
        InputReader.pause();
    }

    private void printGradesTable(List<AcademicRecord> records) {
        System.out.println("\n" + String.format("%-25s | %-8s | %-6s | %-6s | %-20s", "MATERIA / ESTUDIANTE", "PERÍODO", "NOTA", "ESTADO", "OBSERVACIONES"));
        System.out.println("---------------------------------------------------------------------------------");
        for (AcademicRecord r : records) {
            String name = r.getSubjectName() != null ? r.getSubjectName() : r.getStudentName();
            System.out.println(String.format("%-25s | %-8s | %-6s | %-6s | %-20s",
                    name,
                    r.getAcademicPeriod(),
                    r.getScore(),
                    r.isApproved() ? "APROB" : "REP",
                    r.getRemarks() != null ? r.getRemarks() : "-"));
        }
    }
}
