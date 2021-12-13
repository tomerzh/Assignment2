package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;

import java.awt.*;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class, trainModelEvent->{
            gpu.insertModel(trainModelEvent.getModel());
            gpu.splitToDataBatches();
            while(!gpu.getData().isDataFinishedProcessing()){
                if(gpu.isCpuProcessedBatchReady()){
                    gpu.fetchProcessedData();
                }
                if(gpu.availableProcessedBatch()){
                    gpu.pushDataToProcess();
                }
            }
        });
        subscribeEvent(TestModelEvent.class, testModelEvent->{

        });
        subscribeBroadcast(TickBroadcast.class, tick->{
            gpu.incrementTotalTimeTicks();
            if(gpu.isTrainingModel()){ //if gpu is training model.
                if(!gpu.getData().isDataFinishedProcessing()){ //training and not finished
                    if(gpu.isCpuProcessedBatchReady()){ //if there is new processed batch in the cluster
                        gpu.fetchProcessedData();
                    }
                    if(gpu.availableProcessedBatch()){ //if gpu have free space for processed batch and unprocessed isn't empty.
                        gpu.pushDataToProcess();
                    }
                    if(gpu.isProcessDataDone()){ //if gpu finished processing data batch.
                        gpu.finishProcessingDataBatch();
                    }
                }
            }
            else{
                while(gpu.availableTestModel()){

                }
                if(gpu.availableTrainModel()){

                }
            }
        });
    }
}
