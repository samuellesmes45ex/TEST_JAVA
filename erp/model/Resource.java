package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain model representing a physical borrowable asset (table 'recursos').
 */
public class Resource {

    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private String status; // 'Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja'

    // Display fields
    private String categoryName;

    public Resource() {
    }

    public Resource(int id, String code, String name, int categoryId, String description,
                    LocalDate acquisitionDate, String physicalLocation, String status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.physicalLocation = physicalLocation;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public String getPhysicalLocation() { return physicalLocation; }
    public void setPhysicalLocation(String physicalLocation) { this.physicalLocation = physicalLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public boolean isAvailable() {
        return "Disponible".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + code + " - " + name + " (" + (categoryName != null ? categoryName : "Cat " + categoryId) +
                ") | Ubicación: " + physicalLocation + " | Estado: " + status;
    }
}
