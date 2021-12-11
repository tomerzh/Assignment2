package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {

    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;

    public Data(Type type, int size){
        this.type = type;
        processed = 0;
        this.size = size;
    }

    public Type getType(){
        return type;
    }

    public int getSize(){
        return size;
    }

    public void incrementProcessedData(){
        processed++;
    }

    public boolean isDataFinishedProcessing(){
        return processed==(size/1000);
    }
}
