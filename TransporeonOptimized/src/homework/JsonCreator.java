package homework;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonCreator {
	public static void createJsonObject(String variableName, String variableValue, String fileName) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(variableName, variableValue);
		createPrettyJson(jsonObject, fileName);
	}

	public static void createPrettyJson(JSONObject jsonObject, String fileName) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonStringPretty = jsonObject.toString(); 
		writingToJson(jsonStringPretty, fileName);
	}

	public static void writingToJson(String jsonStringPretty, String fileName) {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName, true))) {
			bufferedWriter.write(jsonStringPretty); //adding data beautifully
			bufferedWriter.newLine();
			System.out.println("Data is appended to " + fileName + " successfully!");
		} catch (IOException e) {
			System.out.println("Error while appending data to " + fileName + ": " + e.getMessage());
		}
	}
}