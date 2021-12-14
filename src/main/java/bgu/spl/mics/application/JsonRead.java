package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Student;

import java.util.Collection;
import java.util.LinkedList;

public class JsonRead {
    private LinkedList<Student> students;
    private LinkedList<GPU> gpus;
    private LinkedList<CPU> cpus;
    private LinkedList<ConfrenceInformation> confrences;
    private int tickTime;
    private int duration;

    public JsonRead(LinkedList<Student> students, LinkedList<GPU> gpus, LinkedList<CPU> cpus,
                    LinkedList<ConfrenceInformation> confrences, int tickTime, int duration){
        this.students = students;
        this.gpus = gpus;
        this.cpus = cpus;
        this.confrences = confrences;
        this.tickTime = tickTime;
        this.duration = duration;
    }

    public LinkedList<Student> getStudents() {
        return students;
    }

    public void setStudents(LinkedList<Student> students) {
        this.students = students;
    }

    public LinkedList<GPU> getGpus() {
        return gpus;
    }

    public void setGpus(LinkedList<GPU> gpus) {
        this.gpus = gpus;
    }

    public LinkedList<CPU> getCpus() {
        return cpus;
    }

    public void setCpus(LinkedList<CPU> cpus) {
        this.cpus = cpus;
    }

    public LinkedList<ConfrenceInformation> getConfrences() {
        return confrences;
    }

    public void setConfrences(LinkedList<ConfrenceInformation> confrences) {
        this.confrences = confrences;
    }

    public int getTickTime() {
        return tickTime;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "JsonRead{" +
                "students=" + students +
                ", gpus=" + gpus +
                ", cpus=" + cpus +
                ", confrences=" + confrences +
                ", tickTime=" + tickTime +
                ", duration=" + duration +
                '}';
    }
}
