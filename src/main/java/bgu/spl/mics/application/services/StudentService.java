package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
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
        subscribeBroadcast(TerminateBroadcast.class, terminate->{
            this.terminate();
            System.out.println("Student terminated!");
        });

        //PublishConferenceBroadcast callback
        subscribeBroadcast(PublishConferenceBroadcast.class, publishConference->{
            HashMap<Student, HashSet<Model>> publications = publishConference.getConference().getStudentToPublishedModels();
            for(Student publishStudent : publications.keySet()){
                if(publishStudent == student){
                    int publishedModels = publications.get(publishStudent).size();
                    student.setPublications(publishedModels);
                    for(Model model : publications.get(publishStudent)){
                        model.setPublish();
                    }
                }

                else{
                    int paperRead = publications.get(publishStudent).size();
                    student.setPapersRead(paperRead);
                }
            }
        });

        //TickBroadcast callback
        subscribeBroadcast(TickBroadcast.class, tick->{
            if(currEvent == null){
                Model newModelToTrain = student.nextModelToTrain();
                if(newModelToTrain != null){
                    System.out.println("training started");
                    currEvent = new TrainModelEvent(newModelToTrain);
                    currFuture = this.sendEvent(currEvent);
                }
            }

            else if(currEvent.getClass() == TrainModelEvent.class){
                if(currFuture.isDone()){
                    System.out.println("testing started");
                    Model trainedModel = currFuture.get();
                    Event<Model> newTestModelEvent = new TestModelEvent(student,trainedModel);
                    currEvent = newTestModelEvent;
                    currFuture = sendEvent(newTestModelEvent);
                }
            }

            else if(currEvent.getClass() == TestModelEvent.class){
                if(currFuture.isDone()){
                    System.out.println("testing finished");
                    Model testedModel = currFuture.get();
                    if(testedModel.getResults() == Model.Results.Good){
                        System.out.println("model is good, send to publish");
                        Event<Model> publishResult = new PublishResultsEvent(student, testedModel);
                        currFuture = this.sendEvent(publishResult);
                    }
                    currEvent = null;
                    currFuture = null;
                }
            }
        });
    }
}
