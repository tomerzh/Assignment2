package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;

import java.util.Collection;
import java.util.LinkedList;

public class JsonRead {
    private Collection<Student> students;
    private Collection<GPU> gpus;
    private Collection<CPU> cpus;
    private Collection<ConfrenceInformation> confrences;
    private int tickTime;
    private int duration;

    public JsonRead(){
        students = new LinkedList<>();
        gpus = new LinkedList<>();
        cpus = new LinkedList<>();
        confrences = new LinkedList<>();
    }
}
