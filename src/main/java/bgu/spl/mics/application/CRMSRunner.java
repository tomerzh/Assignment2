package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args){
//        try{
//            Gson gson = new Gson();
//            Reader reader = Files.newBufferedReader(Paths.get("example_input.json"));
//            JsonRead data = gson.fromJson(reader,JsonRead.class);
//            System.out.println("Hello");
//            reader.close();
//        }catch (Exception ex){}

        LinkedList<Model> list = new LinkedList<Model>();
        Student student = new Student("Tomer", "Computer Science", Student.Degree.PhD, list);
        Model model = new Model(student, "model1", Data.Type.Images, 200000);
        list.add(model);
        GPU gpu = new GPU(GPU.Type.RTX3090);
        CPU cpu = new CPU(32);
        ConfrenceInformation confrence = new ConfrenceInformation("ICML", 20000);


//        TimeService timeService = new TimeService("Timer", 1, 55000);
        StudentService studentService = new StudentService(student.getName(), student);
        GPUService gpuService = new GPUService("GPU", gpu);
        CPUService cpuService = new CPUService("CPU", cpu);
        ConferenceService conferenceService = new ConferenceService(confrence.getName(), confrence);

        Thread studentThread = new Thread(studentService);
        studentThread.setName("studentThread");
        Thread gpuThread = new Thread(gpuService);
        gpuThread.setName("gpuThread");
        Thread cpuThread = new Thread(cpuService);
        cpuThread.setName("cpuThread");
        Thread conferenceThread = new Thread(conferenceService);
        conferenceThread.setName("conferenceThread");
//        Thread timeThread = new Thread(timeService);

        gpuThread.start();
        cpuThread.start();
        conferenceThread.start();
        studentThread.start();

        while(!gpuService.getInitialize() || !studentService.getInitialize() || !cpuService.getInitialize() || !conferenceService.getInitialize()){
        }

        TimeService timeService = new TimeService("Timer", 1, 5500);
        Thread timeThread = new Thread(timeService);
        timeThread.setName("timeThread");
        timeThread.start();

        try{
            gpuThread.join();
            cpuThread.join();
            conferenceThread.join();
            studentThread.join();
            timeThread.join();
        } catch (InterruptedException e) {}

        System.out.println("Finished!!!");
    }
}
