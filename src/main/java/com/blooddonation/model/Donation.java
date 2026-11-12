package com.blooddonation.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Model class representing a completed, scheduled, or cancelled Blood Donation record.
 */
public class Donation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long donorId;
    private Long requestId;
    private int unitsDonated;
    private Date donationDate;
    private String hospitalOrCamp;
    private String status; // COMPLETED, SCHEDULED, CANCELLED
    private String remarks;
    private Timestamp createdAt;

    // Display helper fields
    private String donorName;
    private String donorPhone;
    private String bloodGroup;
    private String patientName;

    public Donation() {
    }

    public Donation(Long donorId, Long requestId, int unitsDonated, Date donationDate, String hospitalOrCamp, String status, String remarks) {
        this.donorId = donorId;
        this.requestId = requestId;
        this.unitsDonated = unitsDonated;
        this.donationDate = donationDate;
        this.hospitalOrCamp = hospitalOrCamp;
        this.status = status;
        this.remarks = remarks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public int getUnitsDonated() {
        return unitsDonated;
    }

    public void setUnitsDonated(int unitsDonated) {
        this.unitsDonated = unitsDonated;
    }

    public Date getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(Date donationDate) {
        this.donationDate = donationDate;
    }

    public String getHospitalOrCamp() {
        return hospitalOrCamp;
    }

    public void setHospitalOrCamp(String hospitalOrCamp) {
        this.hospitalOrCamp = hospitalOrCamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorPhone() {
        return donorPhone;
    }

    public void setDonorPhone(String donorPhone) {
        this.donorPhone = donorPhone;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
