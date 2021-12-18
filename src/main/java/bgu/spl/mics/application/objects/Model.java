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
    private boolean publish;

    public Model(String name, Data.Type type, int size){
        this.name = name;
        data = new Data(type, size);
        status = Status.PreTrained;
        results = Results.None;
        publish = false;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public boolean isPublished() {
        return publish;
    }

    private String getPublish(){
        String str;
        if(isPublished()){
            str = "published";
        }
        else{
            str = "not published";
        }
        return str;
    }

    public void modelOutput(StringBuilder builder){
        builder.append("Model: ").append("name='").append(name).append("',");
        builder.append("data type= ").append(data.getType()).append(",");
        builder.append("data size= ").append(data.getSize()).append(",");
        builder.append("status= ").append(status).append(",");
        builder.append("results= ").append(results);
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", data=" + data +
                ", status=" + status +
                '}';
    }
}
