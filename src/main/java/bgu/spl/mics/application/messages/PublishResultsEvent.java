package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.HashMap;
import java.util.HashSet;

public class PublishResultsEvent implements Event<Model> {
    private Student student;
    private Model model;

    public PublishResultsEvent(Student student, Model model){
        this.student = student;
        this.model = model;
    }

    public Student getStudent() {
        return student;
    }

    public Model getModel() {
        return model;
    }
}
