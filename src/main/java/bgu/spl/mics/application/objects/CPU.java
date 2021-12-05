package bgu.spl.mics.application.objects;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private Collection<DataBatch> data;
    private DataBatch currDataProcessing;
    private Cluster cluster;

    /**
     * public constructor
     * @param cores number of cores in the CPU
     */
    public CPU(int cores){
        this.cores = cores;
        cluster = Cluster.getInstance();
        data = new LinkedList<DataBatch>();
    }

    /**
     *
     * @return number of cores in the CPU.
     */
    public int getCores() {
        return cores;
    }

    /**
     *
     * @return data the CPU is currently processing.
     */
    public Collection getData(){
        return data;
    }

    /**
     * @pre: isFree() == true
     * @post: data.size() = @pre data.size() + 1
     * @param dataBatch the DataBatch added to the collection for the cpu to process.
     * @return true if the data is added successfully, false otherwise.
     */
    public boolean processData(DataBatch dataBatch){
        if (isFree()){
            currDataProcessing = dataBatch;
            return data.add(dataBatch);
        }
        return false;
    }

    /**
     * @pre: isFree() == false
     * @post: data.size() = @pre data.size() - 1
     * @return true if data is not empty and removed DataBatch, false otherwise.
     */
    public boolean finishedProcessData(){
        if (!isFree()){
            return data.remove(currDataProcessing);
        }
        return false;
    }

    /**
     *
     * @return the instance of the singleton cluster.
     */
    public Cluster getCluster(){
        return cluster;
    }

    /**
     *
     * @return true if the cpu is not processing any data, false otherwise.
     */
    public boolean isFree(){
        return data.isEmpty();
    }
}
