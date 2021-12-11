package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class PublishResultsEvent implements Event {
    String name = "PublishResultsEvent";

    public String getName(){
        return name;
    }
}
