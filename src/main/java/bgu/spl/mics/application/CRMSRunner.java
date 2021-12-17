package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args){
        JsonRead data = null;
        try{
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Model.class, new JsonRead.ModelDeserializer());
            gsonBuilder.registerTypeAdapter(Student.class, new JsonRead.StudentDeserializer());
            Gson gson = gsonBuilder.create();
            Reader reader = Files.newBufferedReader(Paths.get("example_input.json"));
            data = gson.fromJson(reader,JsonRead.class);
            reader.close();
        }catch (Exception ex){
            System.out.println("Exception during parse json file");
            ex.printStackTrace();
            System.exit(-1);
        }
        runProgram(data);
        //testLogic();
        System.out.println("Finished!!!");
    }




    private static void runProgram(JsonRead data) {
        System.out.println(data);

        ArrayList<GPUService> gpus = new ArrayList<>();
        for (String gpuType : data.GPUS) {
            GPU gpu = new GPU(GPU.Type.valueOf(gpuType));
            gpus.add(new GPUService("GPUService", gpu));
        }

        ArrayList<CPUService> cpus = new ArrayList<>();
        for (int cpuCores : data.CPUS) {
            CPU cpu = new CPU(cpuCores);
            cpus.add(new CPUService("CPUService", cpu));
        }

        ArrayList<StudentService> students = new ArrayList<>();
        for (Student student : data.Students) {
            students.add(new StudentService("StudentService", student));
        }

        ArrayList<ConferenceService> conferences = new ArrayList<>();
        for (ConfrenceInformation conference : data.Conferences) {
            conferences.add(new ConferenceService("ConferenceService", conference));
        }

        TimeService timeService = new TimeService("TimeService", data.TickTime, data.Duration);

        ExecutorService threadPool = Executors.newCachedThreadPool();

    }

    private static void testLogic() {
        Student student = new Student("Tomer", "Computer Science", Student.Degree.PhD);
        Model model = new Model("model1", Data.Type.Images, 200000);
        model.setStudent(student);
        student.addModel(model);
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

        TimeService timeService = new TimeService("Timer", 1, 55000);
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
    }
}
