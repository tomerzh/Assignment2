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
    private int totalTimeTicks = 0;
    private int currDataProcessingTime;
    private int currDataStartTime;
    private int totalDataProcessed = 0;
    private int timeUnitUsed = 0;
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
     * @return number of cores in the CPU
     */
    public int getCores() {
        return cores;
    }

    /**
     * @return the instance of the singleton cluster
     */
    public Cluster getCluster(){
        return cluster;
    }

    /**
     * @inv: @pre(isAvailableToProcess) == @post(isAvailableToProcess)
     * @return true if cpu can process a new data batch, false otherwise
     */
    public boolean isAvailableToProcess(){
        return availableToProcess;
    }

    /**
     * everytime cpu gets TickBroadcast, the time increments by 1
     * if the cpu is currently processing data batch, the cpu time used increments by 1
     * @post: totalTimeTicks = @pre(totalTimeTicks) + 1
     */
    public void incrementTotalTimeTicks() {
        totalTimeTicks++;
        if(!isAvailableToProcess()){ //cpu is processing data batch.
            timeUnitUsed++;
        }
    }

    /**
     * pulling new unprocessed data batch from the cluster to the cpu to process
     */
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

    /**
     * number of data batches the cpu processed
     * @return number of data batches the cpu processed so far
     */
    public int getTotalDataProcessed() {
        return totalDataProcessed;
    }

    /**
     * @return total time (ticks) the cpu was processing data batch
     */
    public int getTimeUnitUsed(){
        return timeUnitUsed;
    }

    /**
     * cpu pushing processed data batch to the cluster
     */
    public void pushProcessedData(){
        cluster.sendDataFromCpu(currDataProcessing);
        totalDataProcessed++;
        availableToProcess = true;
    }

    /**
     * @return true if current data batch is finished processing, false otherwise
     */
    public boolean isProcessDataDone(){
        return (totalTimeTicks - currDataStartTime) == currDataProcessingTime;
    }
}
