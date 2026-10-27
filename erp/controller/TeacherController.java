package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.TeacherAssignmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business controller for Teacher Directory and Subject Workload Assignments with Conflict Detection.
 */
public class TeacherController {

    private final TeacherDao teacherDao = new TeacherDao();
    private final TeacherAssignmentDao assignmentDao = new TeacherAssignmentDao();
    private final SubjectDao subjectDao = new SubjectDao();

    public int registerTeacher(Teacher t) throws SQLException {
        if (!Validator.isValidDocument(t.getDocumentNumber())) {
            throw new IllegalArgumentException("Número de documento de profesor inválido (5 a 20 caracteres).");
        }
        if (teacherDao.documentExists(t.getDocumentNumber())) {
            throw new IllegalArgumentException("Ya existe un profesor registrado con el documento " + t.getDocumentNumber());
        }
        if (t.getEmail() == null || !Validator.isValidEmail(t.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio y debe tener un formato válido.");
        }
        if (t.getPhone() != null && !Validator.isValidPhone(t.getPhone())) {
            throw new IllegalArgumentException("Número de teléfono inválido (7 a 15 dígitos).");
        }
        if (t.getHireDate() == null) {
            t.setHireDate(LocalDate.now());
        }
        if (t.getStatus() == null || t.getStatus().trim().isEmpty()) {
            t.setStatus("Activo");
        }
        return teacherDao.insert(t);
    }

    public List<Teacher> listAllTeachers() throws SQLException {
        return teacherDao.findAll();
    }

    public List<Teacher> searchTeachers(String text) throws SQLException {
        return teacherDao.search(text);
    }

    public Teacher findTeacherById(int id) throws SQLException {
        return teacherDao.findById(id);
    }

    public Teacher findTeacherByDocument(String doc) throws SQLException {
        return teacherDao.findByDocument(doc);
    }

    public int assignSubject(int teacherId, int subjectId, int academicYear, String section,
                             String dayOfWeek, LocalTime startTime, LocalTime endTime) throws SQLException {
        Teacher teacher = teacherDao.findById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("El profesor seleccionado no existe.");
        }
        Subject subject = subjectDao.findById(subjectId);
        if (subject == null) {
            throw new IllegalArgumentException("La materia seleccionada no existe.");
        }
        if (dayOfWeek != null && !Validator.DAYS_OF_WEEK.contains(dayOfWeek)) {
            throw new IllegalArgumentException("Día de la semana no válido: " + dayOfWeek);
        }
        if (startTime != null && endTime != null) {
            if (!Validator.isValidTimeRange(startTime, endTime)) {
                throw new IllegalArgumentException("La hora de inicio (" + startTime + ") debe ser anterior a la hora de fin (" + endTime + ").");
            }
            // Conflict validation
            if (assignmentDao.hasScheduleConflict(teacherId, academicYear, dayOfWeek, startTime, endTime)) {
                throw new IllegalArgumentException("¡Conflicto de Horario detectado! El profesor " + teacher.getFullName() +
                        " ya tiene asignada otra clase el " + dayOfWeek + " que se cruza con el rango " + startTime + " - " + endTime);
            }
        }

        TeacherAssignment a = new TeacherAssignment();
        a.setTeacherId(teacherId);
        a.setSubjectId(subjectId);
        a.setAcademicYear(academicYear);
        a.setSection(section != null ? section : "A");
        a.setDayOfWeek(dayOfWeek);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        return assignmentDao.assign(a);
    }

    public List<TeacherAssignment> getAssignmentsByTeacher(int teacherId, int academicYear) throws SQLException {
        return assignmentDao.getAssignmentsByTeacher(teacherId, academicYear);
    }

    public List<TeacherAssignment> getAssignmentsBySubject(int subjectId, int academicYear) throws SQLException {
        return assignmentDao.getAssignmentsBySubject(subjectId, academicYear);
    }
}
