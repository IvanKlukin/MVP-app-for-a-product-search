package ru.study.storesearch.data.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Модель раздела магазина с координатами метки на схеме.
 * Координаты хранятся в относительном формате (от 0.0 до 1.0).
 */
public class Section implements Serializable {
    private long id;
    private String name;
    private float xPercent;
    private float yPercent;

    public Section() {
    }

    public Section(long id, String name, float xPercent, float yPercent) {
        this.id = id;
        this.name = name;
        this.xPercent = xPercent;
        this.yPercent = yPercent;
    }

    public Section(String name, float xPercent, float yPercent) {
        this(0, name, xPercent, yPercent);
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

    public float getXPercent() {
        return xPercent;
    }

    public void setXPercent(float xPercent) {
        this.xPercent = xPercent;
    }

    public float getYPercent() {
        return yPercent;
    }

    public void setYPercent(float yPercent) {
        this.yPercent = yPercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return id == section.id &&
                Float.compare(section.xPercent, xPercent) == 0 &&
                Float.compare(section.yPercent, yPercent) == 0 &&
                Objects.equals(name, section.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, xPercent, yPercent);
    }

    @Override
    public String toString() {
        return name + " (" + Math.round(xPercent * 100) + "%, " + Math.round(yPercent * 100) + "%)";
    }
}
