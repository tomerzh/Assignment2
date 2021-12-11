package bgu.spl.mics;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	//thread safe singleton design
	private static class MessageBusHolder{
		private static MessageBusImpl MessageBusInstance = new MessageBusImpl();
	}

	private HashMap<Class<? extends Event>, LinkedList<MicroService>> eventToServices;
	private HashMap<Class<? extends Broadcast>, HashSet<MicroService>> broadcastToService;
	private HashMap<MicroService, LinkedBlockingQueue<Message>> serviceToWorkQueue;
	private HashMap<Event<?>, Future<?>> eventToFuture;
	private Iterator<MicroService> trainIterator;
	private Iterator<MicroService> testIterator;
	private Iterator<MicroService> publishIterator;

	private MessageBusImpl(){
		eventToServices = new HashMap<Class<? extends Event>, LinkedList<MicroService>>();
		broadcastToService = new HashMap<Class<? extends Broadcast>, HashSet<MicroService>>();
		serviceToWorkQueue = new HashMap<MicroService, LinkedBlockingQueue<Message>>();
		eventToFuture = new HashMap<Event<?>, Future<?>>();
		trainIterator = null;
		testIterator = null;
		publishIterator = null;
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.MessageBusInstance;
	}

	// MessageBus methods

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if(!eventToServices.containsKey(type)){
			synchronized (eventToServices){
				if(!eventToServices.containsKey(type)){
					LinkedList<MicroService> eventList = (LinkedList<MicroService>) Collections.synchronizedList(new LinkedList<MicroService>());
					eventToServices.put(type, eventList);
					// need to add iterator
				}
			}
		}

		synchronized (eventToServices.get(type)){
			eventToServices.get(type).add(m);
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(!broadcastToService.containsKey(type)){
			synchronized (broadcastToService){
				if(!broadcastToService.containsKey(type)){
					HashSet<MicroService> bcSet = (HashSet<MicroService>) Collections.synchronizedSet(new HashSet<MicroService>());
					broadcastToService.put(type, bcSet);
				}
			}
		}

		synchronized (broadcastToService.get(type)){
			broadcastToService.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
//		synchronized (eventToFuture){
//			eventToFuture.get(e).resolve(result);
		}
//	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (broadcastToService.get(b)){
			for(MicroService mc : broadcastToService.get(b)){
				serviceToWorkQueue.get(mc).add(b);
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		//to do!
		// if service not found, should send null
		//future need to be add to futures set, need to take his key
		return null;
	}

	@Override
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> mWorkQueue = new LinkedBlockingQueue<Message>();
		synchronized (serviceToWorkQueue){
			serviceToWorkQueue.put(m, mWorkQueue);
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (serviceToWorkQueue.get(m)){
			//remove ms from event lists
			synchronized (eventToServices){
				for(LinkedList<MicroService> eventList : eventToServices.values()){
					if(eventList.contains(m)){
						eventList.remove(m);
					}
				}
			}
			//remove ms from event sets
			synchronized (broadcastToService){
				for(HashSet<MicroService> bcSet : broadcastToService.values()){
					if(bcSet.contains(m)){
						bcSet.remove(m);
					}
				}
			}

			//sends back to messageBus remain messages
			for(Message ms : serviceToWorkQueue.get(m)){
				//to do - send back to bus the events
				// also - need to delete the queue
			}


		}


	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

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

	public boolean isInAllMicroServices(MicroService ms){
		return this.serviceToWorkQueue.containsKey(ms);
	}

}
