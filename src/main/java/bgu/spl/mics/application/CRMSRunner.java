package bgu.spl.mics.application;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static void main(String[] args){
        try{
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get("example_input.json"));
            JsonRead data = gson.fromJson(reader,JsonRead.class);
            System.out.println("Hello");
            reader.close();
        }catch (Exception ex){}

    }
}
