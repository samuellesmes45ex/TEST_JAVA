package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business controller for Grades, Averages (calculated with Java Streams & Lambdas), and Daily Attendance.
 */
public class AcademicController {

    private final AcademicRecordDao recordDao = new AcademicRecordDao();
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final StudentDao studentDao = new StudentDao();
    private final SubjectDao subjectDao = new SubjectDao();

    // === GRADES & ACADEMIC RECORDS ===

    public int recordGrade(int studentId, int subjectId, int teacherId, int academicYear,
                           String period, BigDecimal score, String remarks) throws SQLException {
        if (!Validator.GRADE_PERIODS.contains(period)) {
            throw new IllegalArgumentException("Período académico no válido. Valores permitidos: " + Validator.GRADE_PERIODS);
        }
        if (!Validator.isValidScore(score)) {
            throw new IllegalArgumentException("La calificación debe estar en la escala de 0.0 a 5.0 (según el estándar educativo colombiano).");
        }
        Student s = studentDao.findById(studentId);
        if (s == null) {
            throw new IllegalArgumentException("El estudiante no existe.");
        }
        Subject sub = subjectDao.findById(subjectId);
        if (sub == null) {
            throw new IllegalArgumentException("La materia no existe.");
        }

        AcademicRecord record = new AcademicRecord();
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(teacherId);
        record.setAcademicYear(academicYear);
        record.setAcademicPeriod(period);
        record.setScore(score);
        record.setRemarks(remarks);
        record.setRecordDate(LocalDate.now());

        return recordDao.insertOrUpdate(record);
    }

    public List<AcademicRecord> getStudentHistory(int studentId) throws SQLException {
        return recordDao.getRecordsByStudent(studentId);
    }

    public List<AcademicRecord> getStudentGradesByPeriod(int studentId, int year, String period) throws SQLException {
        return recordDao.getRecordsByStudentAndPeriod(studentId, year, period);
    }

    public List<AcademicRecord> getCourseGrades(int subjectId, int year, String period) throws SQLException {
        return recordDao.getRecordsBySubjectAndPeriod(subjectId, year, period);
    }

    /**
     * Calculates the student's average in a subject using Java Stream API.
     */
    public BigDecimal calculateStudentSubjectAverage(int studentId, int subjectId, int year) throws SQLException {
        List<AcademicRecord> records = recordDao.getRecordsByStudent(studentId).stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == year)
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double avg = records.stream()
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the student's general GPA for an academic period using Java Stream API.
     */
    public BigDecimal calculateStudentPeriodGpa(int studentId, int year, String period) throws SQLException {
        List<AcademicRecord> records = recordDao.getRecordsByStudentAndPeriod(studentId, year, period);
        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double avg = records.stream()
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the course general average for a subject in a period using Java Stream API.
     */
    public BigDecimal calculateCourseSubjectAverage(int subjectId, int year, String period) throws SQLException {
        List<AcademicRecord> records = recordDao.getRecordsBySubjectAndPeriod(subjectId, year, period);
        if (records.isEmpty()) {
            return BigDecimal.ZERO;
        }

        double avg = records.stream()
                .mapToDouble(r -> r.getScore().doubleValue())
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    // === ATTENDANCE ===

    public int markAttendance(int studentId, int subjectId, LocalDate date, String status, String remarks, Integer teacherId) throws SQLException {
        if (!Validator.ATTENDANCE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Estado de asistencia inválido. Valores permitidos: " + Validator.ATTENDANCE_STATUSES);
        }
        Attendance a = new Attendance();
        a.setStudentId(studentId);
        a.setSubjectId(subjectId);
        a.setAttendanceDate(date);
        a.setStatus(status);
        a.setRemarks(remarks);
        a.setRecordedBy(teacherId);
        return attendanceDao.recordAttendance(a);
    }

    public List<Attendance> getAttendanceHistory(int studentId, int subjectId) throws SQLException {
        return attendanceDao.getAttendanceByStudentAndSubject(studentId, subjectId);
    }

    public List<Attendance> getSubjectAttendanceByDate(int subjectId, LocalDate date) throws SQLException {
        return attendanceDao.getAttendanceBySubjectAndDate(subjectId, date);
    }

    public Map<String, Integer> getHighAbsenceReport(LocalDate startDate, LocalDate endDate, int minAbsences) throws SQLException {
        return attendanceDao.getStudentsWithHighAbsences(startDate, endDate, minAbsences);
    }
}
