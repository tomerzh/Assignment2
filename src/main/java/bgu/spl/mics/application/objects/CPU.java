package bgu.spl.mics.application.objects;

import java.awt.*;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private Container data;
    private Cluster cluster;

    /**
     * public constructor
     */
    public CPU(){
        //type from json file
        //model from json file
        cluster = Cluster.getInstance();
    }

    /**
     * @inv: cores >= 0
     * @return number of cores in the CPU.
     */
    public int getCores() {
        return cores;
    }

    /**
     *
     * @return data the CPU is currently processing.
     */
    public Container getData(){
        return data;
    }

    /**
     *
     * @return the instance of the singleton cluster.
     */
    public Cluster getCluster(){
        return cluster;
    }
}
