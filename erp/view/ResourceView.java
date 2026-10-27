package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Interactive console interface for Library and Laboratory Resource Management, Loans, and Returns.
 */
public class ResourceView {

    private final ResourceController controller = new ResourceController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║   MÓDULO: GESTIÓN DE RECURSOS (BIBLIOTECA)   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Consultar catálogo general de recursos");
            System.out.println("2. Registrar nuevo recurso o equipo");
            System.out.println("3. Registrar préstamo a ESTUDIANTE");
            System.out.println("4. Registrar préstamo a PROFESOR");
            System.out.println("5. Registrar devolución de recurso");
            System.out.println("6. Listar préstamos activos");
            System.out.println("7. Reporte de préstamos VENCIDOS");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 7);
            try {
                switch (option) {
                    case 1 -> listResources();
                    case 2 -> registerResource();
                    case 3 -> loanToStudent();
                    case 4 -> loanToTeacher();
                    case 5 -> returnLoan();
                    case 6 -> listActiveLoans();
                    case 7 -> listOverdueLoans();
                    default -> back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("ResourceView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void listResources() throws SQLException {
        List<Resource> list = controller.listAllResources();
        System.out.println("\n" + String.format("%-5s | %-12s | %-28s | %-20s | %-15s | %-12s",
                "ID", "CÓDIGO", "NOMBRE", "CATEGORÍA", "UBICACIÓN", "ESTADO"));
        System.out.println("------------------------------------------------------------------------------------------------------");
        for (Resource r : list) {
            System.out.println(String.format("%-5d | %-12s | %-28s | %-20s | %-15s | %-12s",
                    r.getId(),
                    r.getCode(),
                    r.getName(),
                    r.getCategoryName() != null ? r.getCategoryName() : "-",
                    r.getPhysicalLocation() != null ? r.getPhysicalLocation() : "-",
                    r.getStatus()));
        }
        System.out.println("(" + list.size() + " recursos en inventario)");
        InputReader.pause();
    }

    private void registerResource() throws SQLException {
        System.out.println("\n--- REGISTRO DE RECURSO EN INVENTARIO ---");
        List<ResourceCategory> categories = controller.listCategories();
        System.out.println("Categorías disponibles:");
        for (ResourceCategory cat : categories) {
            System.out.println("  " + cat.getId() + ". " + cat.getName());
        }
        int catId = InputReader.readInt("Seleccione ID de categoría: ", 1, categories.size());

        Resource r = new Resource();
        r.setCode(InputReader.readText("Código de inventario único (ej: LIB-005, MIC-010): "));
        r.setName(InputReader.readText("Nombre del recurso: "));
        r.setCategoryId(catId);
        r.setDescription(InputReader.readOptional("Descripción técnica"));
        r.setPhysicalLocation(InputReader.readOptional("Ubicación física (ej: Estante 4B, Lab 1)"));
        r.setAcquisitionDate(LocalDate.now());
        r.setStatus("Disponible");

        int id = controller.registerResource(r);
        System.out.println("\n✔ Recurso registrado en inventario con ID: " + id);
    }

    private void loanToStudent() throws SQLException {
        System.out.println("\n--- PRÉSTAMO DE RECURSO A ESTUDIANTE ---");
        int resourceId = InputReader.readInt("ID del recurso a prestar: ", 1, Integer.MAX_VALUE);
        int studentId = InputReader.readInt("ID del estudiante solicitante: ", 1, Integer.MAX_VALUE);
        LocalDate returnDate = InputReader.readDate("Fecha límite programada para la devolución");
        String remarks = InputReader.readOptional("Observaciones del estado inicial");

        int loanId = controller.loanToStudent(resourceId, studentId, returnDate, remarks);
        System.out.println("\n✔ Préstamo a estudiante registrado con éxito (Préstamo #" + loanId + ").");
    }

    private void loanToTeacher() throws SQLException {
        System.out.println("\n--- PRÉSTAMO DE RECURSO A PROFESOR ---");
        int resourceId = InputReader.readInt("ID del recurso a prestar: ", 1, Integer.MAX_VALUE);
        int teacherId = InputReader.readInt("ID del profesor solicitante: ", 1, Integer.MAX_VALUE);
        LocalDate returnDate = InputReader.readDate("Fecha límite programada para la devolución");
        String remarks = InputReader.readOptional("Observaciones de la entrega");

        int loanId = controller.loanToTeacher(resourceId, teacherId, returnDate, remarks);
        System.out.println("\n✔ Préstamo a profesor registrado con éxito (Préstamo #" + loanId + ").");
    }

    private void returnLoan() throws SQLException {
        System.out.println("\n--- REGISTRO DE DEVOLUCIÓN DE RECURSO ---");
        String type = InputReader.readChoice("¿A quién se realizó el préstamo?", List.of("Estudiante", "Profesor"));
        int loanId = InputReader.readInt("ID del préstamo a devolver: ", 1, Integer.MAX_VALUE);

        boolean success = controller.returnLoan(type, loanId);
        if (success) {
            System.out.println("\n✔ Devolución procesada exitosamente. El recurso ha retornado a estado 'Disponible'.");
        } else {
            System.out.println("\nNo se encontró un préstamo activo con ese ID para procesar la devolución.");
        }
    }

    private void listActiveLoans() throws SQLException {
        List<ResourceLoan> list = controller.getActiveLoans();
        System.out.println("\n--- PRÉSTAMOS ACTIVOS EN CIRCULACIÓN ---");
        printLoanTable(list);
    }

    private void listOverdueLoans() throws SQLException {
        List<ResourceLoan> list = controller.getOverdueLoans();
        System.out.println("\n--- ALERTA: PRÉSTAMOS VENCIDOS PENDIENTES DE ENTREGA ---");
        if (list.isEmpty()) {
            System.out.println("✔ No hay préstamos vencidos en este momento.");
        } else {
            printLoanTable(list);
        }
    }

    private void printLoanTable(List<ResourceLoan> loans) {
        System.out.println("\n" + String.format("%-5s | %-12s | %-24s | %-10s | %-24s | %-12s | %-10s",
                "ID", "CÓDIGO", "RECURSO", "TIPO", "PRESTATARIO", "ENTREGA LIM.", "ESTADO"));
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
        for (ResourceLoan l : loans) {
            String status = l.isOverdue() ? "¡VENCIDO!" : l.getStatus();
            System.out.println(String.format("%-5d | %-12s | %-24s | %-10s | %-24s | %-12s | %-10s",
                    l.getId(),
                    l.getResourceCode() != null ? l.getResourceCode() : "-",
                    l.getResourceName() != null ? l.getResourceName() : "-",
                    l.getBorrowerType(),
                    l.getBorrowerName() != null ? l.getBorrowerName() : "-",
                    l.getExpectedReturnDate(),
                    status));
        }
        System.out.println("(" + loans.size() + " registros)");
        InputReader.pause();
    }
}
