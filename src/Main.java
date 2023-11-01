import java.time.Clock;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        System.out.println("Graph Display Test-Run");

        GraphWindow window = new GraphWindow("Graph Display");
        try {
            window.ShowIntroScreen("Text Auth Message");
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println(e);
            Thread.currentThread().interrupt();
        }

        try {
            window.ShowDataScreen();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

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