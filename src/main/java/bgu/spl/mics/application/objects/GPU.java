package bgu.spl.mics.application.objects;


import java.util.Collection;
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
    private int numberOfBatchesAvailable;
    private boolean workingOnModel;
    private BlockingQueue<DataBatch> unProcessedData;
    private BlockingQueue<DataBatch> processedData;

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
        workingOnModel = false;
        unProcessedData = new LinkedBlockingQueue<>();
        processedData = new LinkedBlockingQueue<>();
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

    public int getNumberOfBatchesAvailable(){
        return numberOfBatchesAvailable;
    }

    /**
     * @post: availableProcessedBatch = @pre availableProcessedBatch
     * @return true if there is an available place for a processed batch, false otherwise.
     */
    public boolean availableProcessedBatch(){
        return numberOfBatchesAvailable > 0;
    }

    public boolean isProcessedDataEmpty(){
        return processedData.isEmpty();
    }

    /**
     * @pre: workingOnModel == false
     * @param model the new model the GPU is working on.
     */
    public void insertModel(Model model){
        if(!workingOnModel){
            this.model = model;
            data = model.getData();
            workingOnModel = true;
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

    public void pushDataToProcess(DataBatch dataBatch){
        cluster.sendDataFromGpu(dataBatch);
    }

    public void fetchProcessedData(){
        if(cluster.getGpuQueue(this).peek() != null){ //thread safe, because every gpu has his own queue.
            try{
                DataBatch dataBatch = cluster.getGpuQueue(this).take();
                processedData.add(dataBatch);
                numberOfBatchesAvailable--;
            }catch (InterruptedException exception){}
        }
    }

    public void processDataBatch(){
        if(!isProcessedDataEmpty()){
            try{
                currDataProcessing = processedData.take();
                currDataStartTime = totalTimeTicks;
            }catch (InterruptedException exception){}
        }
    }

    public void finishProcessingDataBatch(){
        numberOfBatchesAvailable++;
        data.incrementProcessedData();
    }

    public void finishTrainModelEvent(){
        workingOnModel = false;
        unProcessedData.clear();
        processedData.clear();
        currDataProcessing = null;
    }

    public boolean isProcessDataDone(){
        return (totalTimeTicks - currDataStartTime) == dataProcessingTime;
    }
}
