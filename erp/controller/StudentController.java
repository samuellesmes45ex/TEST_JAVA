package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business controller managing Students, Guardians, Grade Enrollments, and Subject Enrollments.
 */
public class StudentController {

    private final StudentDao studentDao = new StudentDao();
    private final GuardianDao guardianDao = new GuardianDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final SubjectDao subjectDao = new SubjectDao();

    // === STUDENT OPERATIONS ===

    public int registerStudent(Student s) throws SQLException {
        if (!Validator.isValidDocument(s.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento inválido (5 a 20 caracteres alfanuméricos).");
        }
        if (studentDao.documentExists(s.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un estudiante registrado con el documento " + s.getDocumentNumber());
        }
        if (s.getBirthDate() != null && s.getBirthDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser una fecha futura.");
        }
        if (s.getPhone() != null && !Validator.isValidPhone(s.getPhone())) {
            throw new IllegalArgumentException("Teléfono inválido (debe contener entre 7 y 15 dígitos numéricos).");
        }
        if (s.getEmail() != null && !Validator.isValidEmail(s.getEmail())) {
            throw new IllegalArgumentException("Formato de correo electrónico inválido.");
        }
        if (s.getStatus() == null || s.getStatus().trim().isEmpty()) {
            s.setStatus("Activo");
        }
        if (s.getAdmissionDate() == null) {
            s.setAdmissionDate(LocalDate.now());
        }
        return studentDao.insert(s);
    }

    public List<Student> listAllStudents() throws SQLException {
        return studentDao.findAll();
    }

    public List<Student> searchStudents(String text) throws SQLException {
        return studentDao.search(text);
    }

    public Student findStudentById(int id) throws SQLException {
        return studentDao.findById(id);
    }

    public Student findStudentByDocument(String doc) throws SQLException {
        return studentDao.findByDocument(doc);
    }

    // === GUARDIAN OPERATIONS ===

    public int registerGuardian(Guardian g) throws SQLException {
        if (!Validator.isValidDocument(g.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento de acudiente inválido.");
        }
        if (guardianDao.documentExists(g.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un acudiente con el documento " + g.getDocumentNumber());
        }
        if (g.getPrimaryPhone() == null || !Validator.isValidPhone(g.getPrimaryPhone())) {
            throw new IllegalArgumentException("El teléfono principal es obligatorio y debe tener entre 7 y 15 dígitos.");
        }
        if (g.getEmail() != null && !Validator.isValidEmail(g.getEmail())) {
            throw new IllegalArgumentException("Correo electrónico de acudiente inválido.");
        }
        return guardianDao.insert(g);
    }

    public List<Guardian> listAllGuardians() throws SQLException {
        return guardianDao.findAll();
    }

    public Guardian findGuardianById(int id) throws SQLException {
        return guardianDao.findById(id);
    }

    public void linkStudentWithGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) throws SQLException {
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante con ID " + studentId + " no existe.");
        }
        Guardian g = guardianDao.findById(guardianId);
        if (g == null) {
            throw new IllegalArgumentException("El acudiente con ID " + guardianId + " no existe.");
        }
        studentDao.linkGuardian(studentId, guardianId, isPrimary, isEmergency);
    }

    public List<Guardian> getStudentGuardians(int studentId) throws SQLException {
        return studentDao.getGuardiansForStudent(studentId);
    }

    public List<Student> getStudentsForGuardian(int guardianId) throws SQLException {
        return guardianDao.getStudentsForGuardian(guardianId);
    }

    // === GRADE ENROLLMENT OPERATIONS ===

    public List<GradeLevel> listAllGrades() throws SQLException {
        return enrollmentDao.getAllGrades();
    }

    public int enrollStudentInGrade(int studentId, int gradeId, int academicYear, String remarks) throws SQLException {
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante especificado no existe.");
        }
        GradeLevel g = enrollmentDao.getGradeById(gradeId);
        if (g == null) {
            throw new IllegalArgumentException("El grado especificado no existe.");
        }
        if (enrollmentDao.isStudentEnrolled(studentId, academicYear)) {
            throw new IllegalArgumentException("El estudiante ya cuenta con matrícula para el año escolar " + academicYear);
        }

        Enrollment e = new Enrollment();
        e.setStudentId(studentId);
        e.setGradeId(gradeId);
        e.setAcademicYear(academicYear);
        e.setStatus("Activa");
        e.setEnrollmentDate(LocalDate.now());
        e.setRemarks(remarks);
        return enrollmentDao.enrollStudent(e);
    }

    public List<Enrollment> getStudentEnrollments(int studentId) throws SQLException {
        return enrollmentDao.getEnrollmentsByStudent(studentId);
    }

    public List<Enrollment> getEnrollmentsByGrade(int gradeId, int academicYear) throws SQLException {
        return enrollmentDao.getEnrollmentsByGradeAndYear(gradeId, academicYear);
    }

    // === SUBJECT ENROLLMENT OPERATIONS ===

    public List<Subject> listSubjectsByGrade(int gradeId) throws SQLException {
        return subjectDao.getSubjectsByGrade(gradeId);
    }

    public int enrollStudentInSubject(int studentId, int subjectId, int academicYear, String academicPeriod, String section) throws SQLException {
        if (subjectDao.isStudentEnrolledInSubject(studentId, subjectId, academicYear, academicPeriod)) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en esta materia para el año " + academicYear + " y período " + academicPeriod);
        }
        SubjectEnrollment se = new SubjectEnrollment();
        se.setStudentId(studentId);
        se.setSubjectId(subjectId);
        se.setAcademicYear(academicYear);
        se.setAcademicPeriod(academicPeriod);
        se.setSection(section != null ? section : "A");
        se.setEnrollmentDate(LocalDate.now());
        se.setStatus("Activa");
        return subjectDao.enrollStudentInSubject(se);
    }

    public List<SubjectEnrollment> getStudentSubjects(int studentId, int academicYear, String academicPeriod) throws SQLException {
        return subjectDao.getSubjectEnrollmentsForStudent(studentId, academicYear, academicPeriod);
    }
}
