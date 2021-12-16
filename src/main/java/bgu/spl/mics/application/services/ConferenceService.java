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

    String name;
    ConfrenceInformation conference;
    int currTime;

    public ConferenceService(String name, ConfrenceInformation conference) {
        super(name);
        this.conference = conference;
        currTime = 0;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateBroadcast.class, terminate->{
            this.terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tick->{
            currTime = currTime + 1;
            if(currTime == conference.getDate()){
                PublishConferenceBroadcast publishBroadcast = new PublishConferenceBroadcast(conference);
                this.sendBroadcast(publishBroadcast);
                this.terminate();
            }
        });

        subscribeEvent(PublishResultsEvent.class, publishResultEvent->{
            Student student = publishResultEvent.getStudent();
            Model model = publishResultEvent.getModel();
            conference.addToPublishHash(student, model);
        });


    }
}
