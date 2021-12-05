package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {
    private CPU cpu;
    private Cluster cluster;
    private Data data;
    private DataBatch dataBatch;
    int cores = 16;

    @Before
    public void setUp() throws Exception {
        cpu = new CPU(cores);
        cluster = Cluster.getInstance();
        data = new Data(Data.Type.Images, 3000);
        dataBatch = new DataBatch(data,0);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCores() {
        assertEquals(cores, cpu.getCores());
    }

    @Test
    public void getCluster() {
        assertEquals(cluster, cpu.getCluster());
    }

    @Test
    public void isFree() {
        assertTrue(cpu.isFree());
        cpu.processData(dataBatch);
        assertFalse(cpu.isFree());
    }

    @Test
    public void processData(){
        int preSize = cpu.getData().size();
        cpu.processData(dataBatch);
        assertEquals(preSize, cpu.getData().size()-1);
    }

    @Test
    public void finishedProcessData(){
        boolean finish = cpu.finishedProcessData();
        assertFalse(finish);
        cpu.processData(dataBatch);
        int preSize = cpu.getData().size();
        cpu.finishedProcessData();
        assertEquals(preSize, cpu.getData().size()+1);
    }
}