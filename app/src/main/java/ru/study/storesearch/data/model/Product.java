package ru.study.storesearch.data.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Модель товара с привязкой к разделу магазина.
 */
public class Product implements Serializable {
    private long id;
    private String name;
    private String normalizedName;
    private long sectionId;
    private String sectionName;

    public Product() {
    }

    public Product(long id, String name, String normalizedName, long sectionId, String sectionName) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizedName;
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    public Product(String name, String normalizedName, long sectionId) {
        this(0, name, normalizedName, sectionId, null);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public long getSectionId() {
        return sectionId;
    }

    public void setSectionId(long sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id &&
                sectionId == product.sectionId &&
                Objects.equals(name, product.name) &&
                Objects.equals(normalizedName, product.normalizedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, normalizedName, sectionId);
    }

    @Override
    public String toString() {
        return name + " -> " + (sectionName != null ? sectionName : ("#" + sectionId));
    }
}
