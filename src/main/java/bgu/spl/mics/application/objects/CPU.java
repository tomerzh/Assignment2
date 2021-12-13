package bgu.spl.mics.application.objects;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private final int cores;
    private DataBatch currDataProcessing;
    private int currDataProcessingTime;
    private int totalTimeTicks;
    private int currDataStartTime;
    private boolean availableToProcess = true;
    private final Cluster cluster;

    /**
     * public constructor
     * @param cores number of cores in the CPU
     */
    public CPU(int cores){
        this.cores = cores;
        cluster = Cluster.getInstance();
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
     * @return the instance of the singleton cluster.
     */
    public Cluster getCluster(){
        return cluster;
    }

    public void fetchUnprocessedData(){
        currDataProcessing = cluster.dataBatchToCpu();
        currDataStartTime = totalTimeTicks;
        availableToProcess = false;
        switch (currDataProcessing.getType()){
            case Images:
                currDataProcessingTime = (32/cores) * 4;
                break;
            case Text:
                currDataProcessingTime = (32/cores) * 2;
                break;
            case Tabular:
                currDataProcessingTime = (32/cores);
                break;
        }
    }

    public void pushProcessedData(){
        cluster.sendDataFromCpu(currDataProcessing);
        availableToProcess = true;
    }

    public boolean isProcessDataDone(){
        return (totalTimeTicks - currDataStartTime) == currDataProcessingTime;
    }
}
