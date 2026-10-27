package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Interactive console interface for Student, Guardian, and Enrollment Management.
 */
public class StudentView {

    private final StudentController controller = new StudentController();

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║         MÓDULO: GESTIÓN DE ESTUDIANTES       ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo estudiante");
            System.out.println("2. Listar todos los estudiantes");
            System.out.println("3. Buscar estudiante (nombre o documento)");
            System.out.println("4. Registrar nuevo acudiente");
            System.out.println("5. Asociar acudiente a estudiante");
            System.out.println("6. Consultar acudientes de un estudiante");
            System.out.println("7. Matricular estudiante en un grado escolar");
            System.out.println("8. Inscribir estudiante en una materia");
            System.out.println("9. Consultar materias inscritas de un estudiante");
            System.out.println("0. Volver al Menú Principal");

            int option = InputReader.readInt("Seleccione una opción: ", 0, 9);
            try {
                switch (option) {
                    case 1 -> registerStudent();
                    case 2 -> listStudents();
                    case 3 -> searchStudents();
                    case 4 -> registerGuardian();
                    case 5 -> linkGuardian();
                    case 6 -> viewStudentGuardians();
                    case 7 -> enrollInGrade();
                    case 8 -> enrollInSubject();
                    case 9 -> viewEnrolledSubjects();
                    default -> back = true;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ERROR DE VALIDACIÓN] " + e.getMessage());
            } catch (SQLException e) {
                AppLogger.error("StudentView database failure", e);
                System.out.println("\n[ERROR DE BASE DE DATOS] Operación no completada. Consulte error_log.txt.");
            }
        }
    }

    private void registerStudent() throws SQLException {
        System.out.println("\n--- REGISTRO DE ESTUDIANTE ---");
        Student s = new Student();
        s.setDocumentType(InputReader.readChoice("Tipo de documento:", Validator.DOCUMENT_TYPES));
        s.setDocumentNumber(InputReader.readText("Número de documento: "));
        s.setFirstName(InputReader.readText("Primer nombre: "));
        s.setMiddleName(InputReader.readOptional("Segundo nombre"));
        s.setLastName(InputReader.readText("Primer apellido: "));
        s.setSecondLastName(InputReader.readOptional("Segundo apellido"));
        s.setBirthDate(InputReader.readDate("Fecha de nacimiento"));
        s.setGender(InputReader.readChoice("Género:", Validator.GENDERS));
        s.setAddress(InputReader.readOptional("Dirección"));
        s.setPhone(InputReader.readOptional("Teléfono"));
        s.setEmail(InputReader.readOptional("Correo electrónico"));
        s.setBloodType(InputReader.readOptional("Tipo de sangre (ej: O+, A-)"));
        s.setAllergies(InputReader.readOptional("Alergias conocidas"));
        s.setMedicalConditions(InputReader.readOptional("Condiciones médicas relevantes"));

        int id = controller.registerStudent(s);
        System.out.println("\n✔ Estudiante registrado exitosamente con ID: " + id);

        // Optional immediate guardian association
        if (InputReader.readBoolean("¿Desea asociar un acudiente ahora mismo?")) {
            associateGuardianFlow(id);
        }
    }

    private void listStudents() throws SQLException {
        List<Student> list = controller.listAllStudents();
        printStudentTable(list);
    }

    private void searchStudents() throws SQLException {
        String text = InputReader.readText("Ingrese nombre, apellido o documento a buscar: ");
        List<Student> list = controller.searchStudents(text);
        printStudentTable(list);
    }

    private void registerGuardian() throws SQLException {
        System.out.println("\n--- REGISTRO DE ACUDIENTE / TUTOR LEGAL ---");
        Guardian g = new Guardian();
        g.setDocumentType(InputReader.readChoice("Tipo de documento:", Validator.GUARDIAN_DOCUMENT_TYPES));
        g.setDocumentNumber(InputReader.readText("Número de documento: "));
        g.setFirstName(InputReader.readText("Primer nombre: "));
        g.setMiddleName(InputReader.readOptional("Segundo nombre"));
        g.setLastName(InputReader.readText("Primer apellido: "));
        g.setSecondLastName(InputReader.readOptional("Segundo apellido"));
        g.setRelationship(InputReader.readText("Parentesco (ej: Padre, Madre, Tutor Legal, Abuelo): "));
        g.setPrimaryPhone(InputReader.readText("Teléfono principal de contacto: "));
        g.setSecondaryPhone(InputReader.readOptional("Teléfono secundario"));
        g.setEmail(InputReader.readOptional("Correo electrónico"));
        g.setAddress(InputReader.readOptional("Dirección"));
        g.setOccupation(InputReader.readOptional("Ocupación"));
        g.setFinancialResponsible(InputReader.readBoolean("¿Es el responsable financiero de los pagos?"));

        int id = controller.registerGuardian(g);
        System.out.println("\n✔ Acudiente registrado exitosamente con ID: " + id);
    }

    private void linkGuardian() throws SQLException {
        int studentId = InputReader.readInt("Ingrese el ID del estudiante: ", 1, Integer.MAX_VALUE);
        associateGuardianFlow(studentId);
    }

    private void associateGuardianFlow(int studentId) throws SQLException {
        int guardianId = InputReader.readInt("Ingrese el ID del acudiente: ", 1, Integer.MAX_VALUE);
        boolean isPrimary = InputReader.readBoolean("¿Es el acudiente principal para comunicaciones?");
        boolean isEmergency = InputReader.readBoolean("¿Es contacto de emergencia?");

        controller.linkStudentWithGuardian(studentId, guardianId, isPrimary, isEmergency);
        System.out.println("✔ Acudiente vinculado exitosamente al estudiante.");
    }

    private void viewStudentGuardians() throws SQLException {
        int studentId = InputReader.readInt("Ingrese el ID del estudiante: ", 1, Integer.MAX_VALUE);
        Student s = controller.findStudentById(studentId);
        if (s == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        List<Guardian> guardians = controller.getStudentGuardians(studentId);
        System.out.println("\nAcudientes de " + s.getFullName() + ":");
        if (guardians.isEmpty()) {
            System.out.println("  (No tiene acudientes asociados)");
        } else {
            for (Guardian g : guardians) {
                System.out.println("  • " + g.getFullName() + " (" + g.getRelationship() + ") - Tel: " + g.getPrimaryPhone() + " - Resp. Financiero: " + (g.isFinancialResponsible() ? "Sí" : "No"));
            }
        }
        InputReader.pause();
    }

    private void enrollInGrade() throws SQLException {
        System.out.println("\n--- MATRÍCULA POR GRADO ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        List<GradeLevel> grades = controller.listAllGrades();
        System.out.println("\nGrados disponibles:");
        for (GradeLevel g : grades) {
            System.out.println("  " + g.getId() + ". " + g.getName());
        }
        int gradeId = InputReader.readInt("Seleccione el ID del grado: ", 1, grades.size());
        int year = InputReader.readInt("Año académico (ej: 2026): ", 2020, 2035);
        String remarks = InputReader.readOptional("Observaciones de matrícula");

        int enrollmentId = controller.enrollStudentInGrade(studentId, gradeId, year, remarks);
        System.out.println("✔ Matrícula formalizada con éxito (Registro #" + enrollmentId + ").");
    }

    private void enrollInSubject() throws SQLException {
        System.out.println("\n--- INSCRIPCIÓN EN MATERIA ---");
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int gradeId = InputReader.readInt("ID del grado para ver materias (1-12): ", 1, 12);
        List<Subject> subjects = controller.listSubjectsByGrade(gradeId);
        if (subjects.isEmpty()) {
            System.out.println("No hay materias activas registradas para ese grado.");
            return;
        }
        System.out.println("\nMaterias del grado:");
        for (Subject sub : subjects) {
            System.out.println("  " + sub.getId() + ". " + sub.getCode() + " - " + sub.getName());
        }
        int subjectId = InputReader.readInt("ID de la materia a inscribir: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período académico:", Validator.ACADEMIC_PERIODS);
        String section = InputReader.readOptional("Sección / Grupo (ej: A, B)");

        controller.enrollStudentInSubject(studentId, subjectId, year, period, section);
        System.out.println("✔ Estudiante inscrito en la materia correctamente.");
    }

    private void viewEnrolledSubjects() throws SQLException {
        int studentId = InputReader.readInt("ID del estudiante: ", 1, Integer.MAX_VALUE);
        int year = InputReader.readInt("Año académico: ", 2020, 2035);
        String period = InputReader.readChoice("Período académico:", Validator.ACADEMIC_PERIODS);

        List<SubjectEnrollment> list = controller.getStudentSubjects(studentId, year, period);
        System.out.println("\nMaterias inscritas para el estudiante (Año: " + year + ", Período: " + period + "):");
        if (list.isEmpty()) {
            System.out.println("  (No hay materias inscritas)");
        } else {
            for (SubjectEnrollment se : list) {
                System.out.println("  • " + se.getSubjectName() + " [Sección: " + se.getSection() + "] - Estado: " + se.getStatus());
            }
        }
        InputReader.pause();
    }

    private void printStudentTable(List<Student> students) {
        System.out.println("\n" + String.format("%-5s | %-12s | %-32s | %-10s | %-10s", "ID", "DOCUMENTO", "NOMBRE COMPLETO", "ESTADO", "TELÉFONO"));
        System.out.println("--------------------------------------------------------------------------------");
        for (Student s : students) {
            System.out.println(String.format("%-5d | %-12s | %-32s | %-10s | %-10s",
                    s.getId(),
                    s.getDocumentType() + " " + s.getDocumentNumber(),
                    s.getFullName(),
                    s.getStatus(),
                    s.getPhone() != null ? s.getPhone() : "-"));
        }
        System.out.println("(" + students.size() + " estudiantes encontrados)");
        InputReader.pause();
    }
}
