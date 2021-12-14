package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TickBroadcast implements Broadcast {
    String name = "TickBroadcast";
    public TickBroadcast(){

    }

    public String getName(){
        return name;
    }
}
