package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;

import java.sql.SQLException;

/**
 * Interactive console interface for looking up a teacher by ID.
 */
public class TeacherView {

    private final TeacherController controller = new TeacherController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN DE PROFESORES        ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Consultar información de profesor por ID");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 1);
            try {
                if (option == 1) {
                    showTeacherById();
                } else {
                    back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("TeacherView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void showTeacherById() throws SQLException {
        int teacherId = InputReader.readInt("ID del profesor: ", 1, Integer.MAX_VALUE);
        Teacher teacher = controller.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("\nNo se encontró un profesor con el ID " + teacherId + ".");
        } else {
            printTeacherInformation(teacher);
        }
        InputReader.pause();
    }

    private void printTeacherInformation(Teacher teacher) {
        System.out.println("\n--- INFORMACIÓN DEL PROFESOR ---");
        System.out.println("ID: " + teacher.getId());
        System.out.println("Nombre completo: " + teacher.getFullName());
        System.out.println("Documento: " + teacher.getDocumentType() + " " + teacher.getDocumentNumber());
        System.out.println("Correo electrónico: " + displayValue(teacher.getEmail()));
        System.out.println("Teléfono: " + displayValue(teacher.getPhone()));
        System.out.println("Dirección: " + displayValue(teacher.getAddress()));
        System.out.println("Título académico: " + displayValue(teacher.getAcademicDegree()));
        System.out.println("Especialidad: " + displayValue(teacher.getSpecialty()));
        System.out.println("Fecha de contratación: "
                + (teacher.getHireDate() != null ? teacher.getHireDate() : "-"));
        System.out.println("Estado: " + displayValue(teacher.getStatus()));
    }

    private String displayValue(String value) {
        return value != null ? value : "-";
    }
}
