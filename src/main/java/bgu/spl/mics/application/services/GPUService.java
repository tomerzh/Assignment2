package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;

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

    private final GPU gpu;
    private Event<Model> currEvent;
    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class, trainModelEvent->{
            currEvent = trainModelEvent;
            gpu.insertModel(trainModelEvent.getModel());
            gpu.getModel().setStatusToTraining();
            gpu.splitToDataBatches();
        });

        subscribeEvent(TestModelEvent.class, testModelEvent->{
            currEvent = testModelEvent;
        });

        subscribeBroadcast(TickBroadcast.class, tick->{
            gpu.incrementTotalTimeTicks();
            if(gpu.isTrainingModel()){ //if gpu is training model.
                if(gpu.isCpuProcessedBatchReady()){ //if there is new processed batch in the cluster
                    gpu.fetchProcessedData();
                }

                if(gpu.availableProcessedBatch()){ //if gpu have free space for processed batch and unprocessed isn't empty.
                    gpu.pushDataToProcess();
                }

                if(gpu.isProcessDataDone()){ //if gpu finished processing data batch.
                    gpu.finishProcessingDataBatch();
                }

                if(gpu.getData().isDataFinishedProcessing()){ //training and finished all the data.
                    gpu.getModel().setStatusToTrained();
                    complete(currEvent, gpu.getModel());
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
