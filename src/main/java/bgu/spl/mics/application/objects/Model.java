package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {
        PreTrained, Training, Trained, Tested
    }

    public enum Results {
        None, Good, Bad
    }

    private String name;
    private Data data; //Model should open a new Data object for himself.
    private Student student;
    private Status status;
    private Results results;
    private Boolean publish;

    public Model(Student student, String name, Data.Type type, int size){
        this.student = student;
        this.name = name;
        data = new Data(type, size);
        status = Status.PreTrained;
        results = Results.None;
        publish = false;
    }

    public String getName() {
        return name;
    }
    public Data getData(){
        return data;
    }
    public Status getStatus() {
        return status;
    }
    public Results getResults() {
        return results;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public void setPublish(){
        this.publish = true;
    }


}
