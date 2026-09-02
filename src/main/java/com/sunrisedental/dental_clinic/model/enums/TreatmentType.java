package com.sunrisedental.dental_clinic.model.enums;

import java.math.BigDecimal;

public enum TreatmentType {
    CLEANING(new BigDecimal("3000.00"), new BigDecimal("500.00")),
    FILLING(new BigDecimal("5000.00"), new BigDecimal("500.00")),
    ROOT_CANAL(new BigDecimal("15000.00"), new BigDecimal("500.00")),
    EXTRACTION(new BigDecimal("4000.00"), new BigDecimal("500.00")),
    WHITENING(new BigDecimal("8000.00"), new BigDecimal("500.00")),
    CROWN(new BigDecimal("20000.00"), new BigDecimal("500.00")),
    CONSULTATION(new BigDecimal("1500.00"), new BigDecimal("500.00"));

    private final BigDecimal baseCost;
    private final BigDecimal consultationFee;

    TreatmentType(BigDecimal baseCost, BigDecimal consultationFee) {
        this.baseCost = baseCost;
        this.consultationFee = consultationFee;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public BigDecimal calculateTotalCost() {
        return baseCost.add(consultationFee);
    }
}
