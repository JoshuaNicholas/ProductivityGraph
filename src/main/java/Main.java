import ApiAccessors.DriveReader;
import GraphComponents.GraphWindow;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Graph Display Test-Run");
        GraphWindow.Load();

        GraphWindow window = new GraphWindow("Graph Display");
        DriveReader reader = new DriveReader(challenge -> {
            try {
                window.ShowIntroScreen(challenge);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        while (!reader.Init()) {
            try {
                window.ShowIntroScreen("Failed to log in!");
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        window.ShowItemsScreen(reader);
        // Initial update
        window.Update(reader);

        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                window.Update(reader);
            }
        };

        Timer refreshTimer = new Timer();
        // Update every 30 seconds
        refreshTimer.scheduleAtFixedRate(refreshTask, 0, 30000);
    }
}