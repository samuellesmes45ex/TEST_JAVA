package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business controller for Library & Laboratory Resources, Loans (Students & Teachers), and Overdue tracking.
 */
public class ResourceController {

    private final ResourceDao resourceDao = new ResourceDao();
    private final StudentDao studentDao = new StudentDao();
    private final TeacherDao teacherDao = new TeacherDao();

    public List<ResourceCategory> listCategories() throws SQLException {
        return resourceDao.getAllCategories();
    }

    public List<Resource> listAllResources() throws SQLException {
        return resourceDao.getAllResources();
    }

    public Resource findResourceByCode(String code) throws SQLException {
        return resourceDao.findByCode(code);
    }

    public Resource findResourceById(int id) throws SQLException {
        return resourceDao.findById(id);
    }

    public int registerResource(Resource r) throws SQLException {
        if (r.getCode() == null || r.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de inventario es obligatorio.");
        }
        if (resourceDao.findByCode(r.getCode()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con el código " + r.getCode());
        }
        if (r.getName() == null || r.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio.");
        }
        if (r.getStatus() == null) {
            r.setStatus("Disponible");
        }
        return resourceDao.insertResource(r);
    }

    public int loanToStudent(int resourceId, int studentId, LocalDate returnDate, String remarks) throws SQLException {
        Resource r = resourceDao.findById(resourceId);
        if (r == null) {
            throw new IllegalArgumentException("El recurso con ID " + resourceId + " no existe.");
        }
        if (!r.isAvailable()) {
            throw new IllegalArgumentException("El recurso '" + r.getName() + "' no está disponible (Estado actual: " + r.getStatus() + ").");
        }
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        if (returnDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución pactada no puede ser anterior a hoy.");
        }

        ResourceLoan loan = new ResourceLoan();
        loan.setResourceId(resourceId);
        loan.setBorrowerType("Estudiante");
        loan.setBorrowerId(studentId);
        loan.setLoanDate(LocalDate.now());
        loan.setExpectedReturnDate(returnDate);
        loan.setRemarks(remarks);

        return resourceDao.loanToStudent(loan);
    }

    public int loanToTeacher(int resourceId, int teacherId, LocalDate returnDate, String remarks) throws SQLException {
        Resource r = resourceDao.findById(resourceId);
        if (r == null) {
            throw new IllegalArgumentException("El recurso con ID " + resourceId + " no existe.");
        }
        if (!r.isAvailable()) {
            throw new IllegalArgumentException("El recurso '" + r.getName() + "' no está disponible (Estado actual: " + r.getStatus() + ").");
        }
        Teacher t = teacherDao.findById(teacherId);
        if (t == null) {
            throw new IllegalArgumentException("El profesor no existe.");
        }
        if (returnDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de devolución pactada no puede ser anterior a hoy.");
        }

        ResourceLoan loan = new ResourceLoan();
        loan.setResourceId(resourceId);
        loan.setBorrowerType("Profesor");
        loan.setBorrowerId(teacherId);
        loan.setLoanDate(LocalDate.now());
        loan.setExpectedReturnDate(returnDate);
        loan.setRemarks(remarks);

        return resourceDao.loanToTeacher(loan);
    }

    public boolean returnLoan(String borrowerType, int loanId) throws SQLException {
        if ("Estudiante".equalsIgnoreCase(borrowerType)) {
            return resourceDao.returnStudentLoan(loanId);
        } else if ("Profesor".equalsIgnoreCase(borrowerType)) {
            return resourceDao.returnTeacherLoan(loanId);
        } else {
            throw new IllegalArgumentException("Tipo de prestatario inválido ('Estudiante' o 'Profesor').");
        }
    }

    public List<ResourceLoan> getActiveLoans() throws SQLException {
        return resourceDao.getActiveLoans();
    }

    public List<ResourceLoan> getOverdueLoans() throws SQLException {
        return resourceDao.getOverdueLoans();
    }
}
