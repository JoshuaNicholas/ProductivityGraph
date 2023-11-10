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
        reader.Init();

        window.ShowItemsScreen(reader);

        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                window.Update(reader);
            }
        };

        //window.SetData();

        Timer refreshTimer = new Timer();
        // Update every second
        refreshTimer.scheduleAtFixedRate(refreshTask, 0, 10000);
    }
}