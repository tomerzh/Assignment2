package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

import java.util.concurrent.CountDownLatch;

/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private final CPU cpu;
    private CountDownLatch initSynchronizer;
    private CountDownLatch terminateSynchronizer;
    private  boolean initialized = false;

    public CPUService(String name, CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    public CPU getCpu() {
        return cpu;
    }

    public void doneInitialize() {
        this.initialized = true;
        if (initSynchronizer != null) {
            initSynchronizer.countDown();
        }
    }

    public boolean getInitialize(){
        return initialized;
    }

    public void setInitSynchronizer(CountDownLatch initSynchronizer) {
        this.initSynchronizer = initSynchronizer;
    }

    public void setTerminateSynchronizer(CountDownLatch terminateSynchronizer) {
        this.terminateSynchronizer = terminateSynchronizer;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, tick->{
            cpu.incrementTotalTimeTicks();
            if(cpu.isAvailableToProcess()){
                cpu.fetchUnprocessedData();
            }
            else{
                if(cpu.isProcessDataDone()){
                    cpu.pushProcessedData();
                }
            }
        });

        subscribeBroadcast(TerminateBroadcast.class, terminate->{
            terminate();
            if (terminateSynchronizer != null) {
                terminateSynchronizer.countDown();
                terminateSynchronizer = null;
            }
        });
        doneInitialize();
    }
}
