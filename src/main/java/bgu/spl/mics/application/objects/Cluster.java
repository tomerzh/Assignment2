package bgu.spl.mics.application.objects;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static Cluster instance = new Cluster();
	private HashMap<GPU, BlockingQueue<DataBatch>> gpus;
	private Collection<CPU> cpus;
	private BlockingQueue<DataBatch> unprocessedData;
	private Collection<String> namesModelsTrained;
	private Object lockModelsName = new Object();

	private int totalDataProcessed = 0;
	private int cpuTimeUsed;
	private int gpuTimeUsed;

	private Cluster(){
		unprocessedData = new LinkedBlockingQueue<>();
		gpus = new HashMap<GPU, BlockingQueue<DataBatch>>();
		cpus = new LinkedList<>();
		namesModelsTrained = new LinkedList<>();
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return instance;
	}

	public void addGpu(GPU gpu){
		gpus.put(gpu, new LinkedBlockingQueue<>());
	}

	public BlockingQueue<DataBatch> getGpuQueue(GPU gpu){
		return gpus.get(gpu);
	}

	public void addCpu(CPU cpu){
		cpus.add(cpu);
	}

	public void addModelTrained(String model){
		synchronized (lockModelsName){
			namesModelsTrained.add(model);
		}
	}

	public void sumAllDataProcessed(){
		for (CPU cpu: cpus) {
			totalDataProcessed = totalDataProcessed + cpu.getTotalDataProcessed();
		}
	}

	public void sendDataFromGpu(DataBatch data){
		unprocessedData.add(data);
	}

	public void sendDataFromCpu(DataBatch data){
		try{
			getGpuQueue(data.getGpuOrigin()).put(data);
		}catch (InterruptedException exception){}
	}

	public DataBatch dataBatchToCpu(){ //not sure about this
		try{
			return unprocessedData.take();
		}catch (InterruptedException exception){return null;}
	}
}
