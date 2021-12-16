package bgu.spl.mics;

import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

	private HashMap<Class<? extends Event>, List<MicroService>> eventToServices;
	private HashMap<Class<? extends Broadcast>, Set<MicroService>> broadcastToService;
	private HashMap<MicroService, Queue<Message>> serviceToWorkQueue;
	private HashMap<Event<?>, Future<?>> eventToFuture;
	private HashMap<Class<? extends Event>, MicroService> eventToNextMs;

	private MessageBusImpl(){
		eventToServices = new HashMap<Class<? extends Event>, List<MicroService>>();
		broadcastToService = new HashMap<Class<? extends Broadcast>, Set<MicroService>>();
		serviceToWorkQueue = new HashMap<MicroService, Queue<Message>>();
		eventToFuture = new HashMap<Event<?>, Future<?>>();
		eventToNextMs = new HashMap<Class<? extends Event>, MicroService>();
		eventToNextMs.put(TrainModelEvent.class, null);
		eventToNextMs.put(TestModelEvent.class, null);
		eventToNextMs.put(PublishResultsEvent.class, null);
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
					List<MicroService> eventList = Collections.synchronizedList(new LinkedList<MicroService>());
					eventToServices.put(type, eventList);
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
					HashSet<MicroService> bcSet = new HashSet<MicroService>();
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
		synchronized (eventToFuture){
			Future future = eventToFuture.get(e);
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (broadcastToService.get(b.getClass())) {
			if(!broadcastToService.get(b.getClass()).isEmpty()){
				for (MicroService mc : broadcastToService.get(b.getClass())) {
					synchronized (serviceToWorkQueue.get(mc)){
						serviceToWorkQueue.get(mc).add(b);
						serviceToWorkQueue.get(mc).notifyAll();
					}
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (eventToNextMs){
			synchronized (eventToServices.get(e.getClass())){
				if(!eventToServices.get(e.getClass()).isEmpty()){
					MicroService nextMs;
					if(eventToNextMs.get(e.getClass()) == null){
						nextMs = eventToServices.get(e.getClass()).get(0);
						eventToNextMs.replace(e.getClass(), nextMs);
					}

					else{
						MicroService lastMs = eventToNextMs.get(e.getClass());
						Integer currInd = eventToServices.get(e.getClass()).indexOf(lastMs);
						Integer nextInd = this.nextMsInd(eventToServices.get(e.getClass()).size(), currInd);
						nextMs = eventToServices.get(e.getClass()).get(nextInd);
						eventToNextMs.replace(e.getClass(), lastMs, nextMs);
					}

					synchronized (serviceToWorkQueue.get(nextMs)){
						serviceToWorkQueue.get(nextMs).add(e);
						System.out.println("An event added.");
						serviceToWorkQueue.get(nextMs).notifyAll();
					}
				}

				else{
					return null;
				}
			}
		}

		Future<T> future = new Future<T>();
		eventToFuture.put(e, future);
		return future;
	}

	@Override
	public void register(MicroService m) {
		ConcurrentLinkedQueue<Message> mWorkQueue = new ConcurrentLinkedQueue<Message>();
		synchronized (serviceToWorkQueue){
			serviceToWorkQueue.put(m, mWorkQueue);
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (serviceToWorkQueue.get(m)){
			Queue<Message> myQueue = serviceToWorkQueue.get(m);
			//remove ms from event lists
			synchronized (eventToServices){
				for(List<MicroService> eventList : eventToServices.values()){
					if(eventList.contains(m)){
						eventList.remove(m);
					}
				}
			}
			//remove ms from broadcast sets
			synchronized (broadcastToService){
				for(Set<MicroService> bcSet : broadcastToService.values()){
					if(bcSet.contains(m)){
						bcSet.remove(m);
					}
				}
			}

			//sends back to messageBus remain messages
			for(Message ms : serviceToWorkQueue.get(m)){
				if(ms.getClass() == TrainModelEvent.class || ms.getClass() == TestModelEvent.class
						|| ms.getClass() == PublishResultsEvent.class){
					m.sendEvent((Event<? extends Object>) ms);
				}
			}
			//remove ms queue
			synchronized (serviceToWorkQueue){
				serviceToWorkQueue.remove(m, myQueue);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message newMs = null;
		synchronized (serviceToWorkQueue.get(m)){
			while (serviceToWorkQueue.get(m).isEmpty()){
				serviceToWorkQueue.get(m).wait();
			}
			newMs = serviceToWorkQueue.get(m).remove();
		}
		return newMs;
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
