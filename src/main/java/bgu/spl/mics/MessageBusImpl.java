package bgu.spl.mics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private HashMap<Class<? extends Event>, HashSet<MicroService>> eventToServices;
	private HashMap<Class<? extends Broadcast>, HashSet<MicroService>> broadcastToService;
	private HashMap<MicroService, Queue<Message>> serviceToWorkQueue;
	private HashMap<MicroService, HashSet<Future>> serviceToFutures;
	private HashSet<MicroService> allMicroServices; //add mc here, only after mc got queue

	//"is it there?" added functions

	public boolean isSubscribeToEvent(Class<? extends Event> event, MicroService ms){
		return this.eventToServices.get(event).contains(ms);
	}

	public boolean isSubscribeToBroadCast(Class<? extends Broadcast> broadcast, MicroService ms){
		return this.broadcastToService.get(broadcast).contains(ms);
	}

	public boolean isInServiceQueue(MicroService ms, Message msg){
		return this.serviceToWorkQueue.get(ms).contains(msg);
	}

	public boolean isInServiceFutures(MicroService ms, Message msg){
		return this.serviceToFutures.get(ms).contains(msg);
	}

	public boolean isInAllMicroServices(MicroService ms){
		return this.allMicroServices.contains(ms);
	}



	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void register(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	

}
