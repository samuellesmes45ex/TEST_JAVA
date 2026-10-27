package com.littlehorizons.erp;

import com.littlehorizons.erp.config.DatabaseConnection;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.AcademicView;
import com.littlehorizons.erp.view.ExtracurricularView;
import com.littlehorizons.erp.view.FinancialView;
import com.littlehorizons.erp.view.ReportView;
import com.littlehorizons.erp.view.ResourceView;
import com.littlehorizons.erp.view.StudentView;
import com.littlehorizons.erp.view.TeacherView;

import java.sql.Connection;

/**
 * Main entry point for the Little Horizons Educational ERP.
 * Implements the master console navigation strictly adhering to the MVC architecture.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando ERP Colegio Little Horizons...");

        // Verify database connectivity
        try (Connection con = DatabaseConnection.getInstance().getConnection()) {
            System.out.println("✔ Conectado exitosamente a la base de datos: " + con.getCatalog());
        } catch (Exception e) {
            AppLogger.error("Initial database connection failed", e);
            System.out.println("\n[AVISO] No se pudo conectar automáticamente a MySQL.");
            System.out.println("Asegúrese de haber creado 'config.properties' (basado en config.properties.example)");
            System.out.println("y de tener MySQL 8.0 en ejecución con la base de datos 'erp_little_horizons'.");
            System.out.println("Detalles técnicos registrados en error_log.txt.\n");
        }

        runMainMenu();
    }

    private static void runMainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║   ERP Colegio Little Horizons                ║");
            System.out.println("║   Sistema de Gestión Educativa Integral      ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Gestión de Estudiantes");
            System.out.println("2. Gestión Académica");
            System.out.println("3. Gestión de Profesores");
            System.out.println("4. Actividades Extracurriculares");
            System.out.println("5. Gestión Financiera");
            System.out.println("6. Gestión de Recursos");
            System.out.println("7. Reportes y Estadísticas");
            System.out.println("8. Salir");

            int option = InputReader.readInt("Seleccione una opción: ", 1, 8);
            try {
                switch (option) {
                    case 1 -> new StudentView().show();
                    case 2 -> new AcademicView().show();
                    case 3 -> new TeacherView().show();
                    case 4 -> new ExtracurricularView().show();
                    case 5 -> new FinancialView().show();
                    case 6 -> new ResourceView().show();
                    case 7 -> new ReportView().show();
                    case 8 -> {
                        running = false;
                        System.out.println("\nGracias por utilizar el ERP Colegio Little Horizons. ¡Hasta pronto!");
                    }
                }
            } catch (Exception e) {
                AppLogger.error("Unhandled error in main menu execution", e);
                System.out.println("\n[ERROR GENERAL] Ha ocurrido un error inesperado. Consulte error_log.txt.");
            }
        }
    }
}
