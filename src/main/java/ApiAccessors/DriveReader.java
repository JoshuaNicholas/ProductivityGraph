package ApiAccessors;

import com.azure.identity.*;
import com.microsoft.graph.authentication.*;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

public class DriveReader {
    private Settings settings;
    private GraphServiceClient<Request> graphClient;
    public Consumer<String> AuthChallenge;

    public WorksheetReader worksheetReader;

    private Drive Drive;

    public DriveReader(Consumer<String> authChallenge) {
        settings = new Settings("appsettings.json");
        AuthChallenge = authChallenge;
    }

    public boolean SetSheet(String sheetId) {
        System.out.println(sheetId);
        worksheetReader = new WorksheetReader(graphClient, graphClient.drives(Drive.id).items(sheetId));
        try {
            worksheetReader.ReadSheet();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Map<String, String> GetSheets() {
        Map<String, String> sheetItems = new HashMap<>();
        System.out.println("Sheets");

        var driveItems = graphClient.drives(Drive.id)
                .items("root")
                .children()
                .buildRequest()
                .select("name,id")
                .get();
        System.out.println("Sheets2");

        driveItems.getCurrentPage().forEach(item -> {
            if (!item.name.endsWith("xlsx"))
                return;
            sheetItems.put(item.id, item.name);
        });

        return sheetItems;
    }

    public void Init() {
        IAuthenticationProvider authenticationProvider;
        try {
            authenticationProvider = createProvider();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authenticationProvider)
                .buildClient();

        Drive = graphClient
                .me()
                .drive()
                .buildRequest()
                .get();
        System.out.println("Found Drive " + Drive.name);
    }

    private IAuthenticationProvider createProvider() throws Exception {
        final DeviceCodeCredential credential = new DeviceCodeCredentialBuilder()
                .clientId(settings.getClientId()).tenantId(settings.getTenantId()).challengeConsumer(challenge -> {
                    // Display challenge to the user
                    System.out.println(challenge.getMessage());
                    AuthChallenge.accept(challenge.getMessage());
                }).build();

        if (null == settings.getUserScopes() || null == credential) {
            throw new Exception("Unexpected error");
        }
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
                settings.getUserScopes(), credential);

        return authProvider;
    }
}
