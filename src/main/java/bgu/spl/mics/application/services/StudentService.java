package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    String name;
    Student student;
    Event currEvent;
    Future<Model> currFuture;

    public StudentService(String name, Student student) {
        super(student.getName());
        this.student = student;
        currEvent = null;
        currFuture = null;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(PublishConferenceBroadcast.class, publishConference->{
            HashMap<Student, HashSet<Model>> publications = publishConference.getConference().getStudentToPublishedModels();
            for(Student publishStudent : publications.keySet()){
                if(publishStudent == student){
                    int publishedModels = publications.get(publishStudent).size();
                    student.setPublications(publishedModels);
                }

                else{
                    int paperRead = publications.get(publishStudent).size();
                    student.setPapersRead(paperRead);
                }
            }
        });

        subscribeBroadcast(TickBroadcast.class, tick->{
            if(currEvent == null){
                Model newModelToTrain = student.nextModelToTrain();
                if(newModelToTrain != null){
                    Event<Model> newTrainModelEvent = new TrainModelEvent(newModelToTrain);
                    currEvent = newTrainModelEvent;
                    currFuture = this.sendEvent(newTrainModelEvent);
                }
            }

            else if(currEvent.getClass() == TrainModelEvent.class){
                if(currFuture.isDone()){
                    Model trainedModel = currFuture.get();
//                    Event<Model> newTestModelEvent = new TestModelEvent(trainedModel);
                }
            }

        });
    }
}
