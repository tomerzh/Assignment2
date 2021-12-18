package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.CountDownLatch;


/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private String name;
    private ConfrenceInformation conference;
    private int currTime;
    private CountDownLatch initSynchronizer;
    private CountDownLatch terminateSynchronizer;
    private  boolean initialized = false;

    public ConferenceService(String name, ConfrenceInformation conference) {
        super(name);
        this.conference = conference;
        currTime = 0;
    }

    public ConfrenceInformation getConference() {
        return conference;
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
        subscribeBroadcast(TerminateBroadcast.class, terminate->{
            this.terminate();
            if (terminateSynchronizer != null) {
                terminateSynchronizer.countDown();
                terminateSynchronizer = null;
            }
            System.out.println("conference terminated!");
        });

        subscribeBroadcast(TickBroadcast.class, tick->{
            currTime = currTime + 1;
            if(currTime == conference.getDate()){
                if(!conference.getStudentToPublishedModels().isEmpty()){
                    PublishConferenceBroadcast publishBroadcast = new PublishConferenceBroadcast(conference);
                    this.sendBroadcast(publishBroadcast);
                }
                this.terminate();
                if (terminateSynchronizer != null) {
                    terminateSynchronizer.countDown();
                    terminateSynchronizer = null;
                }
            }
        });

        subscribeEvent(PublishResultsEvent.class, publishResultEvent->{
            Student student = publishResultEvent.getStudent();
            Model model = publishResultEvent.getModel();
            conference.addToPublishHash(student, model);
        });

        doneInitialize();

    }
}
