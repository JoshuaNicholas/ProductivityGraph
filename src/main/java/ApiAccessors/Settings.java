package ApiAccessors;

import com.sun.tools.javac.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private String clientId = "", tenantId = "";
    private List<String> userScopes = new ArrayList<>();

    public String getClientId() {
        return clientId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public List<String> getUserScopes() {
        return userScopes;
    }

    public Settings(String settingsName) {
        try {
            ReadSettings(settingsName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ReadSettings(String settingsName) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (byte b : Main.class.getClassLoader().getResourceAsStream(settingsName).readAllBytes()) {
            builder.append((char)b);
        }
        String jsonString = builder.toString();
        JSONObject json = new JSONObject(jsonString);

        clientId = json.getJSONObject("settings").getString("clientId");
        tenantId = json.getJSONObject("settings").getString("tenantId");

        System.out.println("User Scopes:");
        List<String> userScopesList = new ArrayList<String>();
        JSONArray jsonScopes = json.getJSONObject("settings").getJSONArray("graphUserScopes");
        jsonScopes.forEach(scope -> {
            userScopesList.add(scope.toString());
            System.out.println("    " + scope);
        });
        userScopes = userScopesList;
    }
}
