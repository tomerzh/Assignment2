package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TestModelEvent implements Event<Model> {
    String name = "TestModelEvent";

    public String getName(){
        return name;
    }

}
