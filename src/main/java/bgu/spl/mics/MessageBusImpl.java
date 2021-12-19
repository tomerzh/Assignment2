package bgu.spl.mics;

import bgu.spl.mics.application.messages.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ConcurrentHashMap<Class<? extends Event>, AtomicInteger> eventToNextMs;

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
    }


    public static MessageBusImpl getInstance() {
        return MessageBusHolder.MessageBusInstance;
    }

    // MessageBus methods

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        List<MicroService> microServices = eventToServices.get(type);
        if (microServices != null) {
            microServices.add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        Set<MicroService> microServices = broadcastToService.get(type);
        if (microServices != null) {
            microServices.add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future future = eventToFuture.get(e);
        future.resolve(result);
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        Set<MicroService> microServices = broadcastToService.get(b.getClass());
        if (microServices == null || microServices.isEmpty()) {
            return;
        }
        microServices.forEach(mc -> {
            try {
                serviceToWorkQueue.get(mc).put(b);
            } catch (InterruptedException e) {
            }
        });
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        List<MicroService> microServices = eventToServices.get(e.getClass());
        if (microServices == null || microServices.isEmpty()) {
            return null;
        }
        eventToNextMs.putIfAbsent(e.getClass(), new AtomicInteger(-1));
        int serviceIndex = eventToNextMs.get(e.getClass()).updateAndGet(i ->
                ++i % microServices.size()
        );
        MicroService nextMs = microServices.get(serviceIndex);
        try {
            serviceToWorkQueue.get(nextMs).put(e);
        } catch (InterruptedException ex) {}

        Future<T> future = new Future<T>();
        eventToFuture.put(e, future);
        return future;
    }

    @Override
    public void register(MicroService m) {
        LinkedBlockingQueue<Message> mWorkQueue = new LinkedBlockingQueue<>();
        serviceToWorkQueue.put(m, mWorkQueue);
    }

    @Override
    public void unregister(MicroService m) {
        eventToServices.values().forEach(list -> list.remove(m));
        broadcastToService.values().forEach(list -> list.remove(m));
        Queue<Message> mQueue = serviceToWorkQueue.remove(m);
        for (Message ms : mQueue) {
            if (ms.getClass() == TrainModelEvent.class || ms.getClass() == TestModelEvent.class
                    || ms.getClass() == PublishResultsEvent.class) {
                eventToFuture.remove(ms);
                m.sendEvent((Event<? extends Object>) ms);
            }
        }
        mQueue.clear();
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        Message newMs;
        newMs = serviceToWorkQueue.get(m).take();
        return newMs;
    }

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
