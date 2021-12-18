package bgu.spl.mics;

import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.StudentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    Future testFuture;


    @Before
    public void setUp() throws Exception {
        testFuture = new Future();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() {
        assertFalse(testFuture.isDone());
        Thread service = new Thread(()->{
            try{
                Thread.currentThread().sleep(9000);
            }catch (InterruptedException ex){
                System.out.println(ex.getMessage());
            }
            testFuture.resolve("isDone");
        });

        service.start();
        long currTime = System.currentTimeMillis();
        Object resultInFuture = testFuture.get();
        long timePassed = System.currentTimeMillis()-currTime;
        assertTrue(timePassed>=9000);
        assertEquals("isDone",resultInFuture);
    }

    @Test
    public void resolve() {
        testFuture.resolve("isDone");
        assertTrue(testFuture.isDone());
        assertEquals("isDone", testFuture.get());
    }

    @Test
    public void isDone() {
        assertFalse(testFuture.isDone());
        testFuture.resolve("isDone");
        assertTrue(testFuture.isDone());
    }

    @Test
    public void testGet() {
        assertFalse(testFuture.isDone());
        Boolean changed = false;
//        assertNull(testFuture.get(1,TimeUnit.MILLISECONDS));

        Thread service = new Thread(()->{
           try{
               Thread.sleep(3000);
           }catch(InterruptedException ex){
               System.out.println(ex.getMessage());
           }
           testFuture.resolve("isDone");
        });
        service.start();
//        assertNull(testFuture.get(1,TimeUnit.MILLISECONDS));
        assertEquals("isDone",testFuture.get(1, TimeUnit.MILLISECONDS));
    }
}