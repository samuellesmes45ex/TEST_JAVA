package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * Domain model representing a billing payment concept (table 'conceptos_pago').
 */
public class PaymentConcept {

    private int id;
    private String code;
    private String name;
    private String description;
    private BigDecimal baseAmount;
    private boolean recurrent;
    private boolean active;

    public PaymentConcept() {
    }

    public PaymentConcept(int id, String code, String name, String description,
                          BigDecimal baseAmount, boolean recurrent, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.baseAmount = baseAmount;
        this.recurrent = recurrent;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public boolean isRecurrent() { return recurrent; }
    public void setRecurrent(boolean recurrent) { this.recurrent = recurrent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "[" + id + "] " + code + " - " + name + " : $" + baseAmount +
                (recurrent ? " (Mensual/Recurrente)" : "");
    }
}
