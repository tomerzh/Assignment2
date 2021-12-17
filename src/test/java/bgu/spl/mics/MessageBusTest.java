package bgu.spl.mics;

import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.GPUService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusTest {
    public static MessageBusImpl messageBus;
    public static GPUService service;
    public static TestModelEvent event;
    public static TickBroadcast broadcast;
    public static Student student;
    public static Model model;

    @Before
    public void setUp() throws Exception {
        messageBus = MessageBusImpl.getInstance();
        GPU gpu = new GPU(GPU.Type.RTX2080);
        event = new TestModelEvent(student, model);
        broadcast = new TickBroadcast();
        service = new GPUService("Test", gpu);
        model = new Model( "Test", Data.Type.Images, 3000);
        model.setStudent(student);
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
        Future future = messageBus.sendEvent(event);
        assertFalse("future should not be resolved yet", future.isDone());
        messageBus.complete(event, model);
        assertTrue("event should be resolved", future.isDone());
        assertSame(future.get(), model);
    }

    @Test
    public void sendBroadcast() {
        messageBus.register(service);
        messageBus.subscribeBroadcast(broadcast.getClass(), service);
        messageBus.sendBroadcast(broadcast);
        assertTrue(messageBus.isInServiceQueue(service, broadcast));
    }

    @Test
    public void sendEvent() {
        messageBus.register(service);
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
        messageBus.register(service);
        messageBus.subscribeEvent(event.getClass(), service);
        messageBus.subscribeBroadcast(broadcast.getClass(), service);
        assertTrue(messageBus.isInAllMicroServices(service));
        messageBus.unregister(service);
        assertFalse(messageBus.isInAllMicroServices(service));
        assertFalse(messageBus.isSubscribeToBroadCast(broadcast.getClass(), service));
        assertFalse(messageBus.isSubscribeToEvent(event.getClass(), service));
    }

    @Test
    public void awaitMessage() throws InterruptedException{
        messageBus.register(service);
        messageBus.subscribeEvent(event.getClass(), service);
        messageBus.sendEvent(event);
        assertTrue(messageBus.isInServiceQueue(service,event));
        messageBus.awaitMessage(service);
        assertFalse(messageBus.isInServiceQueue(service, event));
    }
}