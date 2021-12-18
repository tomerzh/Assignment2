package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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

	private long tickTime;
	private long duration;
	private int currTime = 1;
	private CountDownLatch initSynchronizer;
	private CountDownLatch terminateSynchronizer;
	private  boolean initialized = false;

	public TimeService(String name, long tickTime, long duration) {
		super(name);
		this.tickTime = tickTime;
		this.duration = duration;
	}

	public void doneInitialize() {
		this.initialized = true;
		if (initSynchronizer != null) {
			initSynchronizer.countDown();
		}
	}

	public boolean getInitialize(){
		return initialized;
	}

	public void setInitSynchronizer(CountDownLatch initSynchronizer) {
		this.initSynchronizer = initSynchronizer;
	}

	public void setTerminateSynchronizer(CountDownLatch terminateSynchronizer) {
		this.terminateSynchronizer = terminateSynchronizer;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminate ->{
			terminate();
			if (terminateSynchronizer != null) {
				terminateSynchronizer.countDown();
				terminateSynchronizer = null;
			}
		});
		doneInitialize();

		try {
			initSynchronizer.await();
		} catch (InterruptedException e) {}

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(currTime < duration){
					sendBroadcast(new TickBroadcast());
					currTime++;
				}

				else{
					sendBroadcast(new TerminateBroadcast());
					timer.cancel();
				}
			}
		}, 0, tickTime);
	}
}
