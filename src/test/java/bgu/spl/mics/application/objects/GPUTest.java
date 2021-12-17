package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class GPUTest {
    private GPU gpu;
    private Cluster cluster;
    private Model model;

    @Before
    public void setUp() throws Exception {
        gpu = new GPU(GPU.Type.RTX3090);
        cluster = Cluster.getInstance();
        Student student = new Student("Simba", "Computer Science", Student.Degree.PhD);
        model = new Model( "model1", Data.Type.Images, 1000);
        model.setStudent(student);
        student.addModel(model);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getType() {
        assertEquals(GPU.Type.RTX3090, gpu.getType());
    }

    @Test
    public void getModel() {
        gpu.insertModel(model);
        assertEquals(model, gpu.getModel());
    }

    @Test
    public void insertModel(){
        assertNull(gpu.getModel());
        gpu.insertModel(model);
        assertNotNull(gpu.getModel());
    }

    @Test
    public void getCluster() {
        assertEquals(cluster, gpu.getCluster());
    }

    @Test
    public void getNumberOfBatchesAvailable() {
        assertEquals(32, gpu.getNumberOfBatchesAvailable());
    }

}