package bgu.spl.mics.application.objects;

import java.util.Collection;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private int name;
    private String department;
    private Degree status;
    private Collection<Model> models;
    private int publications;
    private int papersRead;

}
