package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class PublishConferenceBroadcast implements Broadcast {
    String name = "PublishConferenceBroadcast";

    public String getName(){
        return name;
    }
}
