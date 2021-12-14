package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int tickTime;
	private int duration;
	private int currTime = 1;

	public TimeService(String name, int tickTime, int duration) {
		super(name);
		this.tickTime = tickTime;
		this.duration = duration;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, tick->{
			currTime++;
			if(currTime == duration){
				terminate(); //not finished.
			}
			else{
				try{
					Thread.sleep(tickTime);
					sendBroadcast(new TickBroadcast());
				}catch (InterruptedException exception){};
			}
		});

		sendBroadcast(new TickBroadcast());
	}

}
