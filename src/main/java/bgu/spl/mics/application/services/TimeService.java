package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;

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
	private Timer timer = new Timer();
	private TimerTask timerTask;

	public TimeService(String name, int tickTime, int duration) {
		super(name);
		this.tickTime = tickTime;
		this.duration = duration;
		timerTask = new TimerTask() {
			@Override
			public void run() {
				sendBroadcast(new TickBroadcast());
				System.out.println("tick Sent");
			}
		};

		timer.scheduleAtFixedRate(timerTask, 0,tickTime);
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminate ->{
			timer.cancel();
			terminate();
		});

		subscribeBroadcast(TickBroadcast.class, tick->{
			currTime = currTime + 1;
			if(currTime == duration){
				sendBroadcast(new TerminateBroadcast());
			}
		});


//		subscribeBroadcast(TickBroadcast.class, tick->{
//			currTime++;
//			if(currTime == duration){
//				sendBroadcast(new TerminateBroadcast());
//			}
//			else{
//				try{
//					Thread.sleep(tickTime);
//					sendBroadcast(new TickBroadcast());
//					System.out.println("tick was sent");
//				}catch (InterruptedException exception){};
//			}
//		});
//
//		sendBroadcast(new TickBroadcast());

	}
}
