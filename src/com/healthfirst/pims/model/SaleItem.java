package com.healthfirst.pims.model;

import java.math.BigDecimal;

public class SaleItem {

    private int saleItemId;
    private int saleId;
    private int medicineId;
    private String medicineName;
    private int quantitySold;
    private BigDecimal priceAtSale;

    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
    }

    public BigDecimal getLineTotal() {
        if (priceAtSale == null) {
            return BigDecimal.ZERO;
        }
        return priceAtSale.multiply(BigDecimal.valueOf(quantitySold));
    }
}
