package bgu.spl.mics.application.objects;

import java.awt.*;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;

    /**
     * public constructor
     */
    public GPU(){
        //type from json file
        //model from json file
        cluster = Cluster.getInstance();
    }

    /**
     *
     * @return the type of the GPU.
     */
    public Type getType() {
        return type;
    }

    /**
     *
     * @return the model that the GPU is working on.
     */
    public Model getModel(){
        return model;
    }

    /**
     *
     * @return the instance of the singleton cluster.
     */
    public Cluster getCluster(){
        return cluster;
    }
}
