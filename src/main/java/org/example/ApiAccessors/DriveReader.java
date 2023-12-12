package org.example.ApiAccessors;

import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.requests.GraphServiceClient;
import kotlin.Pair;
import okhttp3.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DriveReader {

    Thread ownThread = new Thread();

    private final Settings settings;
    private GraphServiceClient<Request> graphClient;
    public Consumer<String> AuthChallenge;

    public WorksheetReader worksheetReader;

    private Drive Drive;

    public DriveReader(Consumer<String> authChallenge) {
        settings = new Settings("appsettings.json");
        AuthChallenge = authChallenge;
    }

    public boolean SetSheet(String sheetId, boolean isShared) {
        System.out.println("Try set sheet [" + sheetId + "]");
        try {
            assert Drive.id != null;
            worksheetReader = new WorksheetReader(graphClient, graphClient.drives(Drive.id), sheetId, isShared);
            worksheetReader.ReadSheet();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean SetSheetFromName(String sheetName) {
        System.out.println("Try set sheet \"" + sheetName + "\"");
        String sheetId = "";
        boolean isShared = false;
        Map<String, Pair<String, Boolean>> allSheets = GetSheets();
        for (var sheet : allSheets.keySet()) {
            if (Objects.equals(allSheets.get(sheet).component1(), sheetName)) {
                System.out.println("Found sheet!");
                sheetId = sheet;
                isShared = allSheets.get(sheet).component2();
                break;
            }
        }
        if (Objects.equals(sheetId, ""))
            return false;

        try {
            assert Drive.id != null;
            worksheetReader = new WorksheetReader(graphClient, graphClient.drives(Drive.id), sheetId, isShared);
            worksheetReader.ReadSheet();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // SheetId, (SheetName, IsShared)
    public Map<String, Pair<String, Boolean>> GetSheets() {
        Map<String, Pair<String, Boolean>> sheetItems = new HashMap<>();

        try {
            // Get items in own drive
            assert Drive.id != null;
            var driveItems = graphClient.drives(Drive.id)
                    .items("root")
                    .children()
                    .buildRequest()
                    .select("name,id")
                    .get();

            assert driveItems != null;
            driveItems.getCurrentPage().forEach(item -> {
                assert item.name != null;
                if (!item.name.endsWith("xlsx"))
                    return;
                sheetItems.put(item.id, new Pair<>(item.name, false));
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Get items in shared drives
            var sharedDriveItems = graphClient.drives(Drive.id)
                    .sharedWithMe()
                    .buildRequest()
                    .select("name,id")
                    .get();

            assert sharedDriveItems != null;
            sharedDriveItems.getCurrentPage().forEach(item -> {
                assert item.name != null;
                if (!item.name.endsWith("xlsx"))
                    return;
                sheetItems.put(item.id, new Pair<>(item.name, true));
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return sheetItems;
    }

    private Consumer<Boolean> _onComplete = (b) -> {};

    public boolean HasInitialized = false;
    public void Init(Consumer<Boolean> onComplete) {
        ownThread = new Thread(this::InitTask);
        ownThread.start();

        // Automatically retry on fail.
        _onComplete = onComplete.andThen((b) -> {
            HasInitialized = b;
            if (!b) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                InitTask();
            }
        });
    }

    int initAttempts = 0;
    private void InitTask() {
        initAttempts++;
        System.out.println("Try DriveReader init " + initAttempts);

        IAuthenticationProvider authenticationProvider;
        try {
            authenticationProvider = createProvider();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            _onComplete.accept(false);
            return;
        }
        graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authenticationProvider)
                .buildClient();
        try {
            Drive = graphClient
                    .me()
                    .drive()
                    .buildRequest()
                    .get();
        }
        catch (Exception e) {
            _onComplete.accept(false);
            return;
        }
        assert Drive != null;
        System.out.println("Found Drive " + Drive.name);
        _onComplete.accept(true);
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

        return new TokenCredentialAuthProvider(
                settings.getUserScopes(), credential);
    }
}
