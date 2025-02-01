package NC12.LupusInCampus.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class JsonConfigReader {


    /**
     * Method used for reading a json file, loading every field into a JsonObject
     * @param fileName Filepath
     * @return the object filled with json content
     */
    public static JsonObject readFile(String fileName) {

        try {
            File file = Paths.get("src/main/resources/" + fileName).toFile();
            if (!file.exists()) {
                throw new RuntimeException("Config file does not exist");
            }
            JsonParser parser = new JsonParser();
            return (JsonObject) parser.parse(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file " + fileName, e);
        }

    }


}
