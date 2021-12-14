package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.awt.*;
import java.util.Random;

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
            if(!gpu.isTrainingModel()){
                currEvent = trainModelEvent;
                gpu.insertModel(trainModelEvent.getModel());
                gpu.getModel().setStatus(Model.Status.Training);
                gpu.splitToDataBatches();
            }
            else{
                gpu.addTrainModel(trainModelEvent);
            }
        });

        subscribeEvent(TestModelEvent.class, testModelEvent->{
            if(!gpu.isTrainingModel()){ //test was called and the gpu is not working on a model.
                int number = new Random().nextInt(10);
                if(testModelEvent.getStudent().getStatus() == Student.Degree.MSc){
                    if(number < 6){ //Test finished successfully.
                        testModelEvent.getModel().setStatus(Model.Status.Tested);
                        testModelEvent.getModel().setResults(Model.Results.Good);
                        complete(testModelEvent, testModelEvent.getModel());
                    }
                    else{ //unsuccessful test.
                        testModelEvent.getModel().setStatus(Model.Status.Tested);
                        testModelEvent.getModel().setResults(Model.Results.Bad);
                        complete(testModelEvent, testModelEvent.getModel());
                    }
                }
                else if(testModelEvent.getStudent().getStatus() == Student.Degree.PhD){
                    if(number < 8){ //Test finished successfully.
                        testModelEvent.getModel().setStatus(Model.Status.Tested);
                        testModelEvent.getModel().setResults(Model.Results.Good);
                        complete(testModelEvent, testModelEvent.getModel());
                    }
                    else{ //unsuccessful test.
                        testModelEvent.getModel().setStatus(Model.Status.Tested);
                        testModelEvent.getModel().setResults(Model.Results.Bad);
                        complete(testModelEvent, testModelEvent.getModel());
                    }
                }
            }
            else{ //test was called but the gpu is training model.
                gpu.addTestModel(testModelEvent);
            }
        });

        subscribeBroadcast(TickBroadcast.class, tick->{
            gpu.incrementTotalTimeTicks();
            if(gpu.isTrainingModel()){ //tick was called and the gpu is training model.
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
                    gpu.getModel().setStatus(Model.Status.Trained);
                    gpu.finishTrainModelEvent();
                    complete(currEvent, gpu.getModel());
                }
            }
            else{ //tick was called and the gpu is not working on a model.
                while(gpu.availableTestModel()){
                    TestModelEvent testModel = gpu.runTestModel();
                    Callback callback = messageToCallbacks.get(TestModelEvent.class);
                    callback.call(testModel);
                }

                if(gpu.availableTrainModel()){
                    TrainModelEvent trainModel = gpu.runTrainModel();
                    Callback callback = messageToCallbacks.get(TrainModelEvent.class);
                    callback.call(trainModel);
                }
            }
        });
    }
}
