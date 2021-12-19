package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args) {
        JsonRead data = null;
        Path path = Paths.get("example_input.json");
        try {
            String jsonStr = new String(
                    Files.readAllBytes(path), StandardCharsets.UTF_8);
            data = JsonRead.fromJsonStr(jsonStr);
        } catch (Exception ex) {
            System.out.println("Exception during read or parse json file: " + path.toAbsolutePath());
            ex.printStackTrace();
            System.exit(-1);
        }
        runProgram(data);
    }

    private static void runProgram(JsonRead data) {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int numberOfMicroservices = data.Students.size() + data.CPUS.length +
                data.GPUS.length + data.Conferences.size() + 1;
        CountDownLatch initSynchronizer = new CountDownLatch(numberOfMicroservices);
        CountDownLatch terminateSynchronizer = new CountDownLatch(numberOfMicroservices);

        ArrayList<GPUService> gpus = new ArrayList<>();
        for (String gpuType : data.GPUS) {
            GPU gpu = new GPU(GPU.Type.valueOf(gpuType));
            Cluster.getInstance().addGpu(gpu);
            GPUService gpuService = new GPUService("GPUService", gpu);
            gpuService.setInitSynchronizer(initSynchronizer);
            gpuService.setTerminateSynchronizer(terminateSynchronizer);
            gpus.add(gpuService);
            threadPool.submit(gpuService);
        }

        ArrayList<CPUService> cpus = new ArrayList<>();
        for (int cpuCores : data.CPUS) {
            CPU cpu = new CPU(cpuCores);
            Cluster.getInstance().addCpu(cpu);
            CPUService cpuService = new CPUService("CPUService", cpu);
            cpuService.setInitSynchronizer(initSynchronizer);
            cpuService.setTerminateSynchronizer(terminateSynchronizer);
            cpus.add(cpuService);
            threadPool.submit(cpuService);
        }

        ArrayList<StudentService> students = new ArrayList<>();
        for (Student student : data.Students) {
            StudentService studentService =
                    new StudentService("StudentService", student);
            studentService.setInitSynchronizer(initSynchronizer);
            studentService.setTerminateSynchronizer(terminateSynchronizer);
            students.add(studentService);
            threadPool.submit(studentService);
        }

        ArrayList<ConferenceService> conferences = new ArrayList<>();
        for (ConfrenceInformation conference : data.Conferences) {
            ConferenceService conferenceService =
                    new ConferenceService("ConferenceService", conference);
            conferenceService.setInitSynchronizer(initSynchronizer);
            conferenceService.setTerminateSynchronizer(terminateSynchronizer);
            conferences.add(conferenceService);
            threadPool.submit(conferenceService);
        }

        TimeService timeService = new TimeService("TimeService", data.TickTime, data.Duration);
        timeService.setInitSynchronizer(initSynchronizer);
        timeService.setTerminateSynchronizer(terminateSynchronizer);
        threadPool.submit(timeService);

        try {
            terminateSynchronizer.await();
        } catch (InterruptedException e) {}

        threadPool.shutdown();

        StringBuilder builder = new StringBuilder();
        students.forEach(s -> s.getStudent().studentOutput(builder));
        conferences.forEach(s -> s.getConference().conferenceOutput(builder));
        Cluster.getInstance().sumAllDataProcessedAndTimeUnits(builder);
        writeResults(builder);
    }

    private static void writeResults(StringBuilder results) {
        try {
            Files.write(Paths.get("output.txt"),
                    results.toString().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
