package com.blooddonation.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing a Donor's response to an Emergency Blood Request.
 */
public class DonorResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long requestId;
    private Long donorId;
    private String status; // ACCEPTED, REJECTED, PENDING, COMPLETED
    private String message;
    private Timestamp respondedAt;

    // Optional display helper fields
    private String donorName;
    private String donorPhone;
    private String donorBloodGroup;
    private String patientName;
    private String hospitalName;

    public DonorResponse() {
    }

    public DonorResponse(Long requestId, Long donorId, String status, String message) {
        this.requestId = requestId;
        this.donorId = donorId;
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
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

    public String getDonorBloodGroup() {
        return donorBloodGroup;
    }

    public void setDonorBloodGroup(String donorBloodGroup) {
        this.donorBloodGroup = donorBloodGroup;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}
