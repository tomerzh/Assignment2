package bgu.spl.mics.application.objects;


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

    /**
     * public constructor
     */
    public GPU(Type type){
        //type from json file
        this.type = type;
        workingOnModel = false;
        cluster = Cluster.getInstance();

        switch(getType()){ //capacity of the GPU depends on type.
            case RTX3090:
                numberOfBatchesAvailable = 32;
                break;
            case RTX2080:
                numberOfBatchesAvailable = 16;
                break;
            case GTX1080:
                numberOfBatchesAvailable = 8;
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
//    public void splitToDataBatches(){
//
//    }
//
//    public DataBatch pushDataToProcess(){
//        if(availableProcessedBatch()){
//
//        }
//    }
}
