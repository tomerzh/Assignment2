package bgu.spl.mics;

import bgu.spl.mics.application.messages.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

    //thread safe singleton design
    private static class MessageBusHolder {
        private static MessageBusImpl MessageBusInstance = new MessageBusImpl();
    }

    private ConcurrentHashMap<Class<? extends Event>, List<MicroService>> eventToServices;
    private ConcurrentHashMap<Class<? extends Broadcast>, Set<MicroService>> broadcastToService;
    private ConcurrentHashMap<MicroService, BlockingQueue<Message>> serviceToWorkQueue;
    private ConcurrentHashMap<Event<?>, Future<?>> eventToFuture;
    private ConcurrentHashMap<Class<? extends Event>, Integer> eventToNextMs;

    private MessageBusImpl() {
        initEventInfo();
        initBroadcastInfo();
        serviceToWorkQueue = new ConcurrentHashMap<>();
        eventToFuture = new ConcurrentHashMap<>();
        initRoundRobinInfo();
    }

    private void initEventInfo() {
        eventToServices = new ConcurrentHashMap<>();
        eventToServices.put(TrainModelEvent.class, new CopyOnWriteArrayList<>());
        eventToServices.put(TestModelEvent.class, new CopyOnWriteArrayList<>());
        eventToServices.put(PublishResultsEvent.class, new CopyOnWriteArrayList<>());
    }

    private void initBroadcastInfo() {
        broadcastToService = new ConcurrentHashMap<>();
        broadcastToService.put(TickBroadcast.class, new CopyOnWriteArraySet<>());
        broadcastToService.put(PublishConferenceBroadcast.class, new CopyOnWriteArraySet<>());
        broadcastToService.put(TerminateBroadcast.class, new CopyOnWriteArraySet<>());
    }

    private void initRoundRobinInfo() {
        eventToNextMs = new ConcurrentHashMap<>();
        eventToNextMs.put(TrainModelEvent.class, new Integer(-1));
        eventToNextMs.put(TestModelEvent.class, new Integer(-1));
        eventToNextMs.put(PublishResultsEvent.class, new Integer(-1));
    }


    public static MessageBusImpl getInstance() {
        return MessageBusHolder.MessageBusInstance;
    }

    // MessageBus methods

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
//		if(!eventToServices.containsKey(type)){
//			synchronized (eventToServices){
//				if(!eventToServices.containsKey(type)){
//					List<MicroService> eventList = Collections.synchronizedList(new LinkedList<MicroService>());
//					eventToServices.put(type, eventList);
//				}
//			}
//		}
//
//		synchronized (eventToServices.get(type)){
//			eventToServices.get(type).add(m);
//		}
        List<MicroService> microServices = eventToServices.get(type);
        if (microServices != null) {
            microServices.add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
//		if(!broadcastToService.containsKey(type)){
//			synchronized (broadcastToService){
//				if(!broadcastToService.containsKey(type)){
//					HashSet<MicroService> bcSet = new HashSet<>();
//					broadcastToService.put(type, bcSet);
//				}
//			}
//		}
//
//		synchronized (broadcastToService.get(type)){
//			broadcastToService.get(type).add(m);
//		}
        Set<MicroService> microServices = broadcastToService.get(type);
        if (microServices != null) {
            microServices.add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        synchronized (eventToFuture) {
            Future future = eventToFuture.get(e);
            future.resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        synchronized (broadcastToService.get(b.getClass())) {
            if (!broadcastToService.get(b.getClass()).isEmpty()) {
                for (MicroService mc : broadcastToService.get(b.getClass())) {
                    try {
                        serviceToWorkQueue.get(mc).put(b);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        List<MicroService> microServices = eventToServices.get(e.getClass());
        if (microServices == null || microServices.isEmpty()) {
            System.out.println("Cannot send event: unknown event type");
            return null;
        }
        Integer prevInd = eventToNextMs.get(e.getClass());
        Integer currInd = prevInd % microServices.size();

        MicroService nextMs;
                if (eventToNextMs.get(e.getClass()) == null) {
                    nextMs = microServices.get(0);
                    eventToNextMs.put(e.getClass(), nextMs);
                } else {
                    MicroService lastMs = eventToNextMs.get(e.getClass());
                    Integer currInd = microServices.indexOf(lastMs);
                    Integer nextInd = this.nextMsInd(microServices.size(), currInd);
                    nextMs = microServices.get(nextInd);
                    eventToNextMs.replace(e.getClass(), lastMs, nextMs);
                }
                try {
                    serviceToWorkQueue.get(nextMs).put(e);
                    System.out.println("An event added.");
                } catch (InterruptedException ex) {
                }




        synchronized (eventToFuture) {
            Future<T> future = new Future<T>();
            eventToFuture.put(e, future);
            return future;
        }
    }

    @Override
    public void register(MicroService m) {
        LinkedBlockingQueue<Message> mWorkQueue = new LinkedBlockingQueue<>();
        synchronized (serviceToWorkQueue) {
            serviceToWorkQueue.put(m, mWorkQueue);
        }
    }

    @Override
    public void unregister(MicroService m) {
        synchronized (serviceToWorkQueue.get(m)) {
            Queue<Message> myQueue = serviceToWorkQueue.get(m);
            //remove ms from event lists
            synchronized (eventToServices) {
                for (List<MicroService> eventList : eventToServices.values()) {
                    if (eventList.contains(m)) {
                        eventList.remove(m);
                    }
                }
            }
            //remove ms from broadcast sets
            synchronized (broadcastToService) {
                for (Set<MicroService> bcSet : broadcastToService.values()) {
                    if (bcSet.contains(m)) {
                        bcSet.remove(m);
                    }
                }
            }

            //sends back to messageBus remain messages
            for (Message ms : serviceToWorkQueue.get(m)) {
                if (ms.getClass() == TrainModelEvent.class || ms.getClass() == TestModelEvent.class
                        || ms.getClass() == PublishResultsEvent.class) {
                    m.sendEvent((Event<? extends Object>) ms);
                }
            }
            //remove ms queue
            synchronized (serviceToWorkQueue) {
                serviceToWorkQueue.remove(m, myQueue);
            }
        }
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        Message newMs;
//		synchronized (serviceToWorkQueue.get(m)){
        newMs = serviceToWorkQueue.get(m).take();
//		}
        return newMs;
    }

    public Integer nextMsInd(int listSize, Integer currInd) {
        Integer nextInd;
        if (listSize == currInd + 1) {
            nextInd = 0;
            return nextInd;
        }
        nextInd = currInd + 1;
        return nextInd;
    }

    //"is it there?" added functions

    public boolean isSubscribeToEvent(Class<? extends Event> event, MicroService ms) {
        return this.eventToServices.get(event).contains(ms);
    }

    public boolean isSubscribeToBroadCast(Class<? extends Broadcast> broadcast, MicroService ms) {
        return this.broadcastToService.get(broadcast).contains(ms);
    }

    public boolean isInServiceQueue(MicroService ms, Message msg) {
        return this.serviceToWorkQueue.get(ms).contains(msg);
    }

    public boolean isInAllMicroServices(MicroService ms) {
        return this.serviceToWorkQueue.containsKey(ms);
    }

}
