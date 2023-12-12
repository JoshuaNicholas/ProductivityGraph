package org.example;

import org.example.ApiAccessors.*;
import org.example.GraphComponents.*;

import java.awt.*;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        new Main().Start(args);
    }

    public static final int UpdateIntervalMs = 30000;
    public static final int RetryIntervalMs = 1000;

    GraphWindow window;
    DriveReader reader;
    String desiredSheetName = "";

    public void Start(String[] args) {
        System.out.println("Graph Display Test-Run");
        // Add xlsx to file name if it is missing
        if (args.length > 0)
            desiredSheetName = args[0] + (args[0].endsWith(".xlsx") ? "" : ".xlsx");

        GraphWindow.Initialize();

        window = new GraphWindow("Graph Display");
        reader = new DriveReader(challenge -> {
            // Replace: "To sign in, use a web browser to open the page https://microsoft.com/devicelogin and enter the code _________ to authenticate."
            // With: "Open https://microsoft.com/devicelogin and enter the code _________ to authenticate."
            String eChallenge = challenge.replace("To sign in, use a web browser to open the page", "Open");

            if (desiredSheetName.equals(""))
                window.ShowIntroScreen(eChallenge);
            else
                window.ShowIntroScreen(eChallenge, "Will attempt to load file \"" + desiredSheetName + "\"");
        });

        reader.Init((b) -> {
            if (b)
                AfterReaderInit();
            else {
                if (desiredSheetName.equals(""))
                    window.ShowIntroScreen("Failed to log in!\nRetrying...");
                else
                    window.ShowIntroScreen("Failed to log in!\nRetrying...", "Will attempt to load file \"" + desiredSheetName + "\"");
            }
        });

        String loadIndicator = "";
        long startTime = System.currentTimeMillis();
        while (!reader.HasInitialized) {
            window.UpdateBasicText("Loading" + loadIndicator + " (" + (System.currentTimeMillis() - startTime)/1000 + "s)");
            switch (loadIndicator) {
                case "":
                    loadIndicator = ".";
                    break;
                case ".":
                    loadIndicator = "..";
                    break;
                case "..":
                    loadIndicator = "...";
                    break;
                case "...":
                    loadIndicator = "";
                    break;
            }
            try {
                Thread.sleep(RetryIntervalMs);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void AfterReaderInit() {
        // Try load preset item
        if (Objects.equals(desiredSheetName, ""))
            window.ShowItemsScreen(reader);
        else {
            window.ShowBasicText("Loading preset item [" + desiredSheetName + "]...");
            if (!reader.SetSheetFromName(desiredSheetName))
                window.ShowItemsScreen(reader);
            window.ShowDataScreen();
        }

        // Initial update
        window.Update(reader);

        Robot hal = null;
        try {
            hal = new Robot();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Robot finalHal = hal;
        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                if (reader.worksheetReader == null)
                    return;
                try {
                    window.Update(reader);

                    // Move mouse so that the screen doesn't fall asleep
                    Point pObj = MouseInfo.getPointerInfo().getLocation();
                    assert finalHal != null;
                    finalHal.mouseMove(pObj.x + 1, pObj.y + 1);
                    finalHal.mouseMove(pObj.x - 1, pObj.y - 1);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Timer refreshTimer = new Timer();
        // Update every 30 seconds
        refreshTimer.scheduleAtFixedRate(refreshTask, 0, UpdateIntervalMs);
    }
}