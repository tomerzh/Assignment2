package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class TestModelEvent implements Event<Integer> {
    String name = "TestModelEvent";

    public String getName(){
        return name;
    }

}
