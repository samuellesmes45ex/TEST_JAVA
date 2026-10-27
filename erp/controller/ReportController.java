package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.ResourceLoan;
import com.littlehorizons.erp.model.Student;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller consolidating analytics, statistics, and reports using Java Streams & Lambdas.
 */
public class ReportController {

    private final StudentDao studentDao = new StudentDao();
    private final AcademicRecordDao academicRecordDao = new AcademicRecordDao();
    private final AttendanceDao attendanceDao = new AttendanceDao();
    private final ResourceDao resourceDao = new ResourceDao();
    private final FinancialController financialController = new FinancialController();

    /**
     * Top students by GPA in an academic period.
     */
    public List<StudentGpaRank> getTopStudents(int academicYear, String period, int limit) throws SQLException {
        List<Student> students = studentDao.findAll();

        return students.stream()
                .map(s -> {
                    try {
                        List<AcademicRecord> recs = academicRecordDao.getRecordsByStudentAndPeriod(s.getId(), academicYear, period);
                        if (recs.isEmpty()) {
                            return null;
                        }
                        double avg = recs.stream()
                                .mapToDouble(r -> r.getScore().doubleValue())
                                .average()
                                .orElse(0.0);
                        return new StudentGpaRank(s, BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP), recs.size());
                    } catch (SQLException e) {
                        return null;
                    }
                })
                .filter(rank -> rank != null && rank.getSubjectCount() > 0)
                .sorted(Comparator.comparing(StudentGpaRank::getGpa).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getAttendanceAlerts(LocalDate start, LocalDate end, int minAbsences) throws SQLException {
        return attendanceDao.getStudentsWithHighAbsences(start, end, minAbsences);
    }

    public List<ResourceLoan> getOverdueLoans() throws SQLException {
        return resourceDao.getOverdueLoans();
    }

    public List<FinancialController.DefaulterRecord> getFinancialMorosos(int days) throws SQLException {
        return financialController.getDefaultersReport(days);
    }

    public static class StudentGpaRank {
        private final Student student;
        private final BigDecimal gpa;
        private final int subjectCount;

        public StudentGpaRank(Student student, BigDecimal gpa, int subjectCount) {
            this.student = student;
            this.gpa = gpa;
            this.subjectCount = subjectCount;
        }

        public Student getStudent() { return student; }
        public BigDecimal getGpa() { return gpa; }
        public int getSubjectCount() { return subjectCount; }

        @Override
        public String toString() {
            return student.getFullName() + " | Promedio: " + gpa + " (" + subjectCount + " materias evaluadas)";
        }
    }
}
