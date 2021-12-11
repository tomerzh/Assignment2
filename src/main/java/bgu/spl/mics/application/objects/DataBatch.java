package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    private Data data;
    private Data.Type type;
    private int start_index;
    private GPU gpuOrigin;

    public DataBatch(Data data, int start_index, GPU gpu){
        this.data = data;
        type = data.getType();
        this.start_index = start_index;
        gpuOrigin = gpu;
    }

    public GPU getGpuOrigin(){
        return gpuOrigin;
    }

    public Data.Type getType(){
        return type;
    }
    
}
