package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private LinkedList<Model> models;
    private int publications;
    private int papersRead;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Degree getStatus() {
        return status;
    }

    public void setStatus(Degree status) {
        this.status = status;
    }

    public LinkedList<Model> getModels() {
        return models;
    }

    public void setModels(LinkedList<Model> models) {
        this.models = models;
    }

    public int getPublications() {
        return publications;
    }

    public void setPublications(int publications) {
        this.publications = publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void setPapersRead(int papersRead) {
        this.papersRead = papersRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return name == student.name;
    }


    @Override
    public String toString() {
        return "Student{" +
                "name=" + name +
                ", department='" + department + '\'' +
                ", status=" + status +
                ", models=" + models +
                ", publications=" + publications +
                ", papersRead=" + papersRead +
                '}';
    }
}
