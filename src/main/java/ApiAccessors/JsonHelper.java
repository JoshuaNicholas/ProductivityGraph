package ApiAccessors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class JsonHelper {
    static void ReadJsonObject(JsonElement element) {
        ReadJsonObject(element, 0);
    }

    static void ReadJsonObject(JsonElement element, int level) {
        String s = "";
        for (int i = 0; i < level; i++)
            s += "  ";
        if (element.isJsonArray()) {
            System.out.println(element.toString());
            element.getAsJsonArray().forEach(e -> ReadJsonObject(e, level + 1));
        }
        if (element.isJsonObject() || element.isJsonPrimitive())
            System.out.println(s + element.getAsString());
        else
            System.err.println(s + "Err");
    }

    public static String[][] ReadJsonSheet(JsonElement element) {
        List<String[]> sheet = new ArrayList<>();

        if (element.isJsonArray()) {
            System.out.println("Sheet");
            element.getAsJsonArray().forEach(e -> sheet.add(ReadJsonArray(e.getAsJsonArray())));
        }
        //if (element.isJsonObject() || element.isJsonPrimitive())
        //    System.out.println(element.getAsString());
        //else
        //    System.err.println("Err");

        return sheet.toArray(new String[0][]);
    }
    public static String[] ReadJsonArray(JsonArray element) {
        List<String> array = new ArrayList<>();

        if (element.isJsonArray()) {
            System.out.println("Row");
            element.getAsJsonArray().forEach(e -> array.add(ReadJsonString(e)));
        }

        return array.toArray(new String[0]);
    }

    private static String ReadJsonString(JsonElement element) {
        if (element.isJsonObject() || element.isJsonPrimitive()) {
            System.out.println("  " + element.getAsString());
            return element.getAsString();
        }
        return null;
    }
}
