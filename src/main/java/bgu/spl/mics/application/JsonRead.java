package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;

public class JsonRead {
    public LinkedList<Student> Students;
    public String[] GPUS;
    public int[] CPUS;
    public LinkedList<ConfrenceInformation> Conferences;
    public int TickTime;
    public int Duration;

    public static JsonRead fromJsonStr(String jsonStr) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Model.class, new JsonRead.ModelDeserializer());
        gsonBuilder.registerTypeAdapter(Student.class, new JsonRead.StudentDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(jsonStr, JsonRead.class);
    }

    @Override
    public String toString() {
        return "JsonRead{" +
                "students=" + Students +
                ", GPUS=" + Arrays.toString(GPUS) +
                ", CPUS=" + Arrays.toString(CPUS) +
                ", Conferences=" + Conferences +
                ", tickTime=" + TickTime +
                ", duration=" + Duration +
                '}';
    }

    static class ModelDeserializer implements JsonDeserializer<Model> {

        @Override
        public Model deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String name = jsonObject.get("name").getAsString();
            String typeStr = jsonObject.get("type").getAsString();
            int size = jsonObject.get("size").getAsInt();
            Model model = new Model(name, Data.Type.valueOf(typeStr), size);
            return model;
        }
    }

    static class StudentDeserializer implements JsonDeserializer<Student> {

        @Override
        public Student deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = (JsonObject) jsonElement;
            String name = jsonObject.get("name").getAsString();
            String department = jsonObject.get("department").getAsString();
            String status = jsonObject.get("status").getAsString();
            Student student = new Student(name, department, Student.Degree.valueOf(status));
            JsonArray models = jsonObject.getAsJsonArray("models");
            for (int i = 0; i < models.size(); i++) {
                Model model =
                        jsonDeserializationContext.deserialize(models.get(i), Model.class);
                model.setStudent(student);
                student.addModel(model);
            }
            return student;
        }
    }
}