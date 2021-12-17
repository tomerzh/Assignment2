package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private LinkedList<Model> models;
    private int publications;
    private int papersRead;
    private int nextModelInd;

    public Student(String name, String department, Degree status){
        this.name = name;
        this.department = department;
        this.status = status;
        this.models = new LinkedList<>();
        this.publications = 0;
        this.papersRead = 0;
        nextModelInd = 0;
    }

    public Model nextModelToTrain(){
        if(nextModelInd < models.size() - 1){
            nextModelInd = nextModelInd + 1;
            return models.get(nextModelInd);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Degree getStatus() {
        return status;
    }

    public LinkedList<Model> getModels() {
        return models;
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public int getPublications() {
        return publications;
    }

    public void setPublications(int publications) {
        this.publications = this.publications + publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public void setPapersRead(int papersRead) {
        this.papersRead = this.papersRead + papersRead;
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
