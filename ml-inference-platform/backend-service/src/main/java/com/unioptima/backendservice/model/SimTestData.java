package com.unioptima.backendservice.model;

import com.unioptima.backendservice.dto.SimulationRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sim_test_data")
public class SimTestData {

    @Id
    private String id;
    private String idOSYM;
    private String universityName;
    private String departmentName;
    private Integer lagBaseRanking;
    private Integer baseRanking;
    private Integer lagOccupiedSlot;
    private Integer occupiedSlots;
    private Integer lagQuota;
    private Integer quota;
    private Double tuitionFee;
    private Integer scholarshipRate;
    private Double P;
    private Double U;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdOSYM() {
        return idOSYM;
    }

    public void setIdOSYM(String idOSYM) {
        this.idOSYM = idOSYM;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getLagBaseRanking() {
        return lagBaseRanking;
    }

    public void setLagBaseRanking(Integer lagBaseRanking) {
        this.lagBaseRanking = lagBaseRanking;
    }

    public Integer getBaseRanking() {
        return baseRanking;
    }

    public void setBaseRanking(Integer baseRanking) {
        this.baseRanking = baseRanking;
    }

    public Integer getLagOccupiedSlot() {
        return lagOccupiedSlot;
    }

    public void setLagOccupiedSlot(Integer lagOccupiedSlot) {
        this.lagOccupiedSlot = lagOccupiedSlot;
    }

    public Integer getOccupiedSlots() {
        return occupiedSlots;
    }

    public void setOccupiedSlots(Integer occupiedSlots) {
        this.occupiedSlots = occupiedSlots;
    }

    public Integer getLagQuota() {
        return lagQuota;
    }

    public void setLagQuota(Integer lagQuota) {
        this.lagQuota = lagQuota;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public Double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(Double tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public Integer getScholarshipRate() {
        return scholarshipRate;
    }

    public void setScholarshipRate(Integer scholarshipRate) {
        this.scholarshipRate = scholarshipRate;
    }

    public Double getP() {
        return P;
    }

    public void setP(Double P) {
        this.P = P;
    }

    public Double getU() {
        return U;
    }

    public void setU(Double U) {
        this.U = U;
    }

    public SimulationRequest toSimulationRequest() {
        return new SimulationRequest(
                this.idOSYM,
                this.lagQuota < this.quota ? this.lagQuota.doubleValue() : this.quota.doubleValue(),
                this.lagQuota > this.quota ? this.lagQuota.doubleValue() : this.quota.doubleValue(),
                Double.MAX_VALUE
        );
    }
}
