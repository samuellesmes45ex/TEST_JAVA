package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a student's membership in a club (table 'membresias_club').
 */
public class ClubMembership {

    private int id;
    private int studentId;
    private int clubId;
    private LocalDate joinDate;
    private String status; // 'Activa', 'Retirada'
    private LocalDate withdrawalDate;
    private String withdrawalReason;

    // Display fields
    private String studentName;
    private String clubName;

    public ClubMembership() {
    }

    public ClubMembership(int id, int studentId, int clubId, LocalDate joinDate,
                          String status, LocalDate withdrawalDate, String withdrawalReason) {
        this.id = id;
        this.studentId = studentId;
        this.clubId = clubId;
        this.joinDate = joinDate;
        this.status = status;
        this.withdrawalDate = withdrawalDate;
        this.withdrawalReason = withdrawalReason;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getClubId() { return clubId; }
    public void setClubId(int clubId) { this.clubId = clubId; }

    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getWithdrawalDate() { return withdrawalDate; }
    public void setWithdrawalDate(LocalDate withdrawalDate) { this.withdrawalDate = withdrawalDate; }

    public String getWithdrawalReason() { return withdrawalReason; }
    public void setWithdrawalReason(String withdrawalReason) { this.withdrawalReason = withdrawalReason; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    @Override
    public String toString() {
        return "[" + id + "] " + studentName + " en " + clubName + " (" + status + " desde " + joinDate + ")";
    }
}
