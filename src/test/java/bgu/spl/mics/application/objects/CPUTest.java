package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {
    private CPU cpu;
    private GPU gpu;
    private Cluster cluster;
    private Data data;
    private DataBatch dataBatch;
    int cores = 16;

    @Before
    public void setUp() throws Exception {
        cpu = new CPU(cores);
        gpu = new GPU(GPU.Type.RTX3090);
        cluster = Cluster.getInstance();
        data = new Data(Data.Type.Images, 3000);
        dataBatch = new DataBatch(data,0,gpu);
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
}