package bgu.spl.mics.application.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private HashMap<Student, HashSet<Model>> studentToPublishedModels;

    public ConfrenceInformation(String name, int date){
        this.name = name;
        this.date = date;
        studentToPublishedModels = new HashMap<>();
    }

    public void addToPublishHash(Student student, Model modelToAdd){
        if(!studentToPublishedModels.containsKey(student)){
            studentToPublishedModels.put(student, new HashSet<>());
        }

        studentToPublishedModels.get(student).add(modelToAdd);
    }

    public HashMap<Student, HashSet<Model>> getStudentToPublishedModels(){
        return studentToPublishedModels;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void conferenceOutput(StringBuilder builder){
        builder.append(System.lineSeparator());
        builder.append("Conference: ").append("name='").append(name).append("',");
        builder.append("date= ").append(date).append(System.lineSeparator());
        HashSet<Model> publications = new HashSet<>();
        studentToPublishedModels.values().forEach(publications::addAll);
        builder.append("publications: ").append(System.lineSeparator());
        if (!publications.isEmpty()) {
            for (Model model : publications) {
                builder.append("\t\t");
                model.modelOutput(builder);
                builder.append(System.lineSeparator());
            }
        }
    }

    @Override
    public String toString() {
        return "ConfrenceInformation{" +
                "name='" + name + '\'' +
                ", date=" + date +
                '}';
    }
}
