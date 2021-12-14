package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {
    private CPU cpu;
    private GPU gpu;
    private Cluster cluster;
    private Student student;
    private Model model;
    private DataBatch dataBatch;
    int cores = 16;

    @Before
    public void setUp() throws Exception {
        cpu = new CPU(cores);
        gpu = new GPU(GPU.Type.RTX3090);
        cluster = Cluster.getInstance();
        cluster.addCpu(cpu);
        cluster.addGpu(gpu);
        model = new Model(student, "Tomer", Data.Type.Images, 3000);
        dataBatch = new DataBatch(model.getData(),0,gpu);
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
    public void isAvailableToProcess() {
        assertTrue(cpu.isAvailableToProcess());
        gpu.insertModel(model);
        gpu.splitToDataBatches();
        gpu.pushDataToProcess();
        cpu.fetchUnprocessedData();
        assertFalse(cpu.isAvailableToProcess());
    }

    @Test
    public void getTotalDataProcessed() {
        assertEquals(cpu.getTotalDataProcessed(),0);
        gpu.insertModel(model);
        gpu.splitToDataBatches();
        gpu.pushDataToProcess();
        cpu.fetchUnprocessedData();
        cpu.pushProcessedData();
        assertEquals(cpu.getTotalDataProcessed(),1);
    }
}