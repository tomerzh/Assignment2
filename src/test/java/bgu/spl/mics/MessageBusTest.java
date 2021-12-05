package bgu.spl.mics;

import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusTest {
    public static MessageBusImpl messageBus;
    public static CPUService service;
    public static TestModelEvent event;
    public static PublishConferenceBroadcast broadcast;

    @Before
    public void setUp() throws Exception {
        messageBus = new MessageBusImpl();
        service = new CPUService("test");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void subscribeEvent() {
        messageBus.subscribeEvent(event.getClass(), service);
        assertTrue(messageBus.isSubscribeToEvent(event.getClass(), service));
    }

    @Test
    public void subscribeBroadcast() {
        messageBus.subscribeBroadcast(broadcast.getClass(), service);
        assertTrue(messageBus.isSubscribeToBroadCast(broadcast.getClass(), service));
    }

    @Test
    public void complete() {
        messageBus.register(service);
        messageBus.subscribeEvent(event.getClass(), service);
        Future<Integer> future = messageBus.sendEvent(event);
        assertFalse("future should not be resolved yet", future.isDone());
        messageBus.complete(event, 123);
        assertTrue("event should be resolved", future.isDone());
        assertTrue(future.get() == 123);
    }

    @Test
    public void sendBroadcast() {
        messageBus.subscribeBroadcast(broadcast.getClass(), service);
        messageBus.sendBroadcast(broadcast);
        assertTrue(messageBus.isInServiceQueue(service, broadcast));
    }

    @Test
    public void sendEvent() {
        messageBus.subscribeEvent(event.getClass(), service);
        messageBus.sendEvent(event);
        assertTrue(messageBus.isInServiceQueue(service, event));
    }

    @Test
    public void register() {
        assertFalse("service is already registered",messageBus.isInAllMicroServices(service));
        messageBus.register(service);
        assertTrue(messageBus.isInAllMicroServices(service));
    }

    @Test
    public void unregister() {
        assertTrue(messageBus.isInAllMicroServices(service));
        messageBus.unregister(service);
        assertTrue(messageBus.isInAllMicroServices(service));
    }

    @Test
    public void awaitMessage() throws InterruptedException{
        messageBus.register(service);
        messageBus.sendEvent(event);
        assertTrue(messageBus.isInServiceQueue(service,event));
        messageBus.awaitMessage(service);
        assertFalse(messageBus.isInServiceQueue(service, event));
    }
}