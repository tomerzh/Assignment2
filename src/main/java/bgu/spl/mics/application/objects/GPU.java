package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private final Type type;
    private Model model;
    private Data data;
    private final Cluster cluster;
    private boolean trainingModel;
    private Queue<DataBatch> unProcessedData;
    private BlockingQueue<DataBatch> processedData;

    private LinkedList<? extends Event> trainModelEvents;
    private LinkedList<? extends Event> testModelEvents;

    private int numberOfBatchesAvailable; //place available for processed data.
    private DataBatch currDataProcessing;
    private int dataProcessingTime;
    private int totalTimeTicks;
    private int currDataStartTime;

    /**
     * public constructor
     */
    public GPU(Type type){
        //type from json file
        this.type = type;
        trainingModel = false;
        unProcessedData = new LinkedList<>();
        processedData = new LinkedBlockingQueue<>();
        trainModelEvents = new LinkedList<>();
        testModelEvents = new LinkedList<>();
        cluster = Cluster.getInstance();

        switch(getType()){ //capacity of the GPU depends on type.
            case RTX3090:
                numberOfBatchesAvailable = 32;
                dataProcessingTime = 1;
                break;
            case RTX2080:
                numberOfBatchesAvailable = 16;
                dataProcessingTime = 2;
                break;
            case GTX1080:
                numberOfBatchesAvailable = 8;
                dataProcessingTime = 4;
                break;
        }
    }

    /**
     *
     * @return the type of the GPU.
     */
    public Type getType() {
        return type;
    }

    /**
     *
     * @return the model that the GPU is working on.
     */
    public Model getModel(){
        return model;
    }

    public Data getData(){
        return data;
    }

    /**
     *
     * @return the instance of the singleton cluster.
     */
    public Cluster getCluster(){
        return cluster;
    }

    public boolean isTrainingModel() {
        return trainingModel;
    }

    public boolean availableTrainModel(){
        return !(trainModelEvents.isEmpty());
    }

    public boolean availableTestModel(){
        return !(testModelEvents.isEmpty());
    }

    /**
     * @inv: numberOfBatchesAvailable > -1
     * @post: numberOfBatchesAvailable == @pre numberOfBatchesAvailable
     * @return the number of processed batches available in the gpu vmemory.
     */
    public int getNumberOfBatchesAvailable(){
        return numberOfBatchesAvailable;
    }

    /**
     * @post: availableProcessedBatch == @pre availableProcessedBatch
     * @return true if there is an available place for a processed batch, false otherwise.
     */
    public boolean availableProcessedBatch(){
        return numberOfBatchesAvailable > 0;
    }

    public boolean isUnProcessedDataEmpty(){
        return unProcessedData.isEmpty();
    }

    public boolean isProcessedDataEmpty(){
        return processedData.isEmpty();
    }

    /**
     * @pre: workingOnModel == false
     * @param model the new model the GPU is working on.
     */
    public void insertModel(Model model){
        if(!trainingModel){
            this.model = model;
            data = model.getData();
            trainingModel = true;
        }
    }

    /**
     * this method takes the data and splits it into data batches of 1000 samples each.
     */
    public void splitToDataBatches(){
        for(int i = 1; i < data.getSize(); i = i + 1000){
            DataBatch dataBatch = new DataBatch(data, i, this);
            unProcessedData.add(dataBatch);
        }
    }

    public void incrementTotalTimeTicks() {
        totalTimeTicks++;
    }

    public void pushDataToProcess(){
        if(!isUnProcessedDataEmpty()){
            cluster.sendDataFromGpu(unProcessedData.remove());
        }
    }

    public boolean isCpuProcessedBatchReady(){
        return !(cluster.getGpuQueue(this).isEmpty());
    }

    public void fetchProcessedData(){
        DataBatch dataBatch = cluster.getGpuQueue(this).remove();
        processedData.add(dataBatch);
        numberOfBatchesAvailable--;
    }


    public void finishProcessingDataBatch(){
        numberOfBatchesAvailable++;
        data.incrementProcessedData();
        if(!isProcessedDataEmpty()){ //processing a new data batch.
            currDataProcessing = processedData.remove();
            currDataStartTime = totalTimeTicks;
        }
    }

    public void finishTrainModelEvent(){
        unProcessedData.clear();
        processedData.clear();
        currDataProcessing = null;
        trainingModel = false;
    }

    public boolean isProcessDataDone(){
        return (totalTimeTicks - currDataStartTime) == dataProcessingTime;
    }

    public void runTrainModel(){
        trainModelEvents.remove();
    }

    public void runTestModel(){
        testModelEvents.remove();
    }
}
