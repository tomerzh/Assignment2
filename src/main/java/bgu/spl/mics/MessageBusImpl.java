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
	private HashMap<MicroService, BlockingQueue<Message>> serviceToWorkQueue;
	private HashMap<Event<?>, Future<?>> eventToFuture;
	private HashMap<Class<? extends Event>, MicroService> eventToNextMs;

	private MessageBusImpl(){
		eventToServices = new HashMap<Class<? extends Event>, LinkedList<MicroService>>();
		broadcastToService = new HashMap<Class<? extends Broadcast>, HashSet<MicroService>>();
		serviceToWorkQueue = new HashMap<MicroService, BlockingQueue<Message>>();
		eventToFuture = new HashMap<Event<?>, Future<?>>();
		eventToNextMs = new HashMap<Class<? extends Event>, MicroService>();
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
					broadcastToService.notifyAll();
				}
			}
		}

		synchronized (eventToServices.get(type)){
			eventToServices.get(type).add(m);
			eventToServices.get(type).notifyAll();
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
			broadcastToService.get(type).notifyAll();
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		synchronized (eventToFuture){
			Future future = eventToFuture.get(e);
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (!broadcastToService.containsKey(b.getClass())) {
			synchronized (broadcastToService) {
				while (!broadcastToService.containsKey(b.getClass())) {
					try {
						broadcastToService.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
		}

		synchronized (broadcastToService.get(b.getClass())) {
			while (broadcastToService.get(b.getClass()).isEmpty()) {
				try {
					broadcastToService.get(b.getClass()).wait();
				} catch (InterruptedException ex) {}
			}
			for (MicroService mc : broadcastToService.get(b.getClass())) {
				serviceToWorkQueue.get(mc).add(b);
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if (!eventToServices.containsKey(e.getClass())) {
			synchronized (eventToServices) {
				while (!eventToServices.containsKey(e.getClass())) {
					try {
						broadcastToService.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
		}

		synchronized (eventToNextMs.get(e.getClass())){
			synchronized (eventToServices.get(e.getClass())){
				if(!eventToServices.get(e.getClass()).isEmpty()){
					MicroService lastMs = eventToNextMs.get(e.getClass());
					Integer currInd = eventToServices.get(e.getClass()).indexOf(lastMs);
					Integer nextInd = this.nextMsInd(eventToServices.get(e.getClass()).size(), currInd);
					MicroService nextMs = eventToServices.get(e.getClass()).get(nextInd);
					eventToNextMs.replace(e.getClass(), lastMs, nextMs);
					synchronized (serviceToWorkQueue.get(nextMs)){
						try{
							serviceToWorkQueue.get(nextMs).put(e);
						}catch(InterruptedException ex){}
					}
				}

				else{
					return null;
				}
			}
		}

		Future<T> future = new Future<T>();
		return future;
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
		BlockingQueue<Message> mQueue = serviceToWorkQueue.get(m);
		return mQueue.take();
	}

	public Integer nextMsInd (int listSize, Integer currInd){
		Integer nextInd;
		if(listSize == currInd + 1){
			nextInd = 0;
			return nextInd;
		}
		nextInd = currInd + 1;
		return nextInd;
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
