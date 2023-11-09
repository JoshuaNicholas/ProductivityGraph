import GraphComponents.GraphWindow;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Graph Display Test-Run");

        GraphWindow window = new GraphWindow("Graph Display");
        window.ShowIntroScreen("Text Auth Message");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println(e);
            Thread.currentThread().interrupt();
        }

        window.ShowDataScreen();

        TimerTask refreshTask = new TimerTask() {
            @Override
            public void run() {
                window.Update();
            }
        };

        window.SetData();

        Timer refreshTimer = new Timer();
        // Update every second
        refreshTimer.scheduleAtFixedRate(refreshTask, 0, 2000);
    }
}