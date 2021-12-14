package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;

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
    private boolean trainingModel = false;
    private Queue<DataBatch> unProcessedData;
    private BlockingQueue<DataBatch> processedData;

    private LinkedList<TrainModelEvent> trainModelEvents;
    private LinkedList<TestModelEvent> testModelEvents;

    private int totalTimeTicks = 0;
    private int timeUnitUsed = 0;
    private int numberOfBatchesAvailable; //place available for processed data.
    private int dataProcessingTime;
    private DataBatch currDataProcessing;
    private int currDataStartTime;
    private boolean processingDataBatch = false;

    /**
     * public constructor
     */
    public GPU(Type type){
        //type from json file
        this.type = type;
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
     * @return the type of the GPU
     */
    public Type getType() {
        return type;
    }

    /**
     * @return the model that the GPU is working on
     */
    public Model getModel(){
        return model;
    }

    /**
     * @return the data that the GPU is working on
     */
    public Data getData(){
        return data;
    }

    /**
     * @return the instance of the singleton cluster
     */
    public Cluster getCluster(){
        return cluster;
    }

    /**
     * @return true if gpu is currently training a model, false otherwise
     */
    public boolean isTrainingModel() {
        return trainingModel;
    }

    /**
     * @return true if there is a TrainModelEvent in the list that can be handled, false otherwise
     */
    public boolean availableTrainModel(){
        return !(trainModelEvents.isEmpty());
    }

    /**
     * @return true if there is a TestModelEvent in the list that can be handled, false otherwise
     */
    public boolean availableTestModel(){
        return !(testModelEvents.isEmpty());
    }

    /**
     * @return true if the gpu is currently processing data batch, false otherwise
     */
    public boolean isProcessingDataBatch() {
        return processingDataBatch;
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
     * @return total time (ticks) the gpu was processing data batch
     */
    public int getTimeUnitUsed() {
        return timeUnitUsed;
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

    /**
     * everytime gpu gets TickBroadcast, the time increments by 1
     * if the gpu is currently processing data batch, the gpu time used increments by 1
     * @post: totalTimeTicks = @pre(totalTimeTicks) + 1
     */
    public void incrementTotalTimeTicks() {
        totalTimeTicks++;
        if(isProcessingDataBatch()){ //gpu is processing data batch.
            timeUnitUsed++;
        }
    }

    /**
     * gpu pushing unprocessed data batch to the cluster for the cpu to process
     */
    public void pushDataToProcess(){
        if(!isUnProcessedDataEmpty()){
            cluster.sendDataFromGpu(unProcessedData.remove());
        }
    }

    /**
     * @return true if there is processed data batch in the cluster ready for the gpu to process, false otherwise
     */
    public boolean isCpuProcessedBatchReady(){
        return !(cluster.getGpuQueue(this).isEmpty());
    }

    /**
     * gpu add to the list a new processed data batch from the cluster
     */
    public void fetchProcessedData(){
        DataBatch dataBatch = cluster.getGpuQueue(this).remove();
        processedData.add(dataBatch);
        numberOfBatchesAvailable--;
        if(!isProcessingDataBatch()){
            processDataBatch();
        }
    }

    /**
     * gpu take a new processed data batch from the queue to process
     */
    public void processDataBatch(){
        processingDataBatch = true;
        currDataProcessing = processedData.remove();
        currDataStartTime = totalTimeTicks;
    }

    /**
     * @return true if the current data batch is done processing, false otherwise or if current data batch is null
     */
    public boolean isProcessDataDone(){
        if(currDataProcessing == null){
            return false;
        }
        return (totalTimeTicks - currDataStartTime) == dataProcessingTime;
    }

    /**
     * gpu finished processing data batch, and checks if there is a new data batch to process
     */
    public void finishProcessingDataBatch(){
        processingDataBatch = false;
        numberOfBatchesAvailable++;
        data.incrementProcessedData();
        if(!isProcessedDataEmpty()){ //processing a new data batch.
            processDataBatch();
        }
    }

    /**
     * all the data is processed, so the TrainModelEvent is done
     */
    public void finishTrainModelEvent(){
        unProcessedData.clear();
        processedData.clear();
        currDataProcessing = null;
        trainingModel = false;
        cluster.addModelTrained(model.getName());
    }

    public void addTrainModel(TrainModelEvent trainModel){
        trainModelEvents.add(trainModel);
    }

    public TrainModelEvent runTrainModel(){
        return trainModelEvents.remove();
    }

    public void addTestModel(TestModelEvent testModel){
        testModelEvents.add(testModel);
    }

    public TestModelEvent runTestModel(){
        return testModelEvents.remove();
    }
}
