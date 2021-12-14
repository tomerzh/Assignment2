package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    enum Status {
        PreTrained, Training, Trained, Tested
    }

    enum Results {
        None, Good, Bad
    }

    private String name;
    private Data data; //Model should open a new Data object for himself.
    private Student student;
    private Status status;
    private Results results;

    public Model(Student student, String name, Data.Type type, int size){
        this.student = student;
        this.name = name;
        data = new Data(type, size);
        status = Status.PreTrained;
        results = Results.None;
    }

    public Data getData(){
        return data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatusToTraining() {
        status = Status.Training;
    }

    public void setStatusToTrained() {
        status = Status.Trained;
    }
    public void setStatusToTested() {
        status = Status.Tested;
    }

    public Results getResults() {
        return results;
    }

    public void setResultsToGood() {
        results = Results.Good;
    }

    public void setResultsToBad() {
        results = Results.Bad;
    }
}
