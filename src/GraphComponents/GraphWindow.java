package GraphComponents;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphWindow extends JFrame {

    static Font titleFont = new Font(Font.DIALOG, Font.BOLD, 40);
    static Font dataFont = new Font(Font.DIALOG, Font.PLAIN, 30);
    static Font markerFont = new Font(Font.DIALOG, Font.BOLD, 20);
    Boolean showingData = false;

    GraphDisplay leftDisplay;
    GraphDisplay rightDisplay;

    JLabel updateLabel, fastestLabel, slowestLabel;

    public GraphWindow(String title) {
        super(title);

        // Make window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new Label("Loading..."));
    }

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");

    List<GraphComponent> cList = new ArrayList<>(Arrays.asList(
            new GraphComponent("City1"),
            new GraphComponent("City2"),
            new GraphComponent("City3"),
            new GraphComponent("City4"),
            new GraphComponent("City5"),
            new GraphComponent("City6"),
            new GraphComponent("City7"),
            new GraphComponent("City8"),
            new GraphComponent("City9"),
            new GraphComponent("City10")
    ));

    public static void Load() {
        try {
            Font base = Font.createFont(Font.TRUETYPE_FONT, GraphWindow.class.getClassLoader().getResourceAsStream("ValoaCircularWeb-Regular.ttf"));
            Font bold = Font.createFont(Font.TRUETYPE_FONT, GraphWindow.class.getClassLoader().getResourceAsStream("ValoaCircularWeb-Bold.ttf"));

            titleFont = bold.deriveFont(40f);
            dataFont = base.deriveFont(30f);
            markerFont = base.deriveFont(20f);

            System.out.println("Successfully overrode fonts.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO remove
    public void SetData() {
        for (int i = 0; i < cList.size(); i++) {
            if (i % 2 == 0)
                leftDisplay.AddComponent(cList.get(i));
            else
                rightDisplay.AddComponent(cList.get(i));
        }
    }

    public void Update() {
        if (!showingData)
            return;

        // Get and set highest values
        int highest = leftDisplay.GetHighestValue();
        if (rightDisplay.GetHighestValue() > highest)
            highest = rightDisplay.GetHighestValue();

        // Get and set slowest labels
        GraphComponent slowest = leftDisplay.GetSlowest();
        if (rightDisplay.GetSlowest().GetCurrentValue() > slowest.GetCurrentValue())
            slowest = rightDisplay.GetSlowest();
        slowestLabel.setText("Slowest: " + slowest.GetName() + " @ " + slowest.GetCurrentValue() + " days.");

        // Get and set fastest labels
        GraphComponent fastest = leftDisplay.GetSlowest();
        if (rightDisplay.GetFastest().GetCurrentValue() < fastest.GetCurrentValue())
            fastest = rightDisplay.GetFastest();
        fastestLabel.setText("Fastest: " + fastest.GetName() + " @ " + fastest.GetCurrentValue() + " days.");

        leftDisplay.SetHighestValue(highest);
        rightDisplay.SetHighestValue(highest);

        leftDisplay.Update();
        rightDisplay.Update();

        // TODO best and worst times

        // Refresh update label
        updateLabel.setText("Last Update: " + LocalDateTime.now().format(timeFormat));
    }

    public void ShowDataScreen() throws URISyntaxException{
        clearFrame();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0f;
        c.weighty = 1.0f;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3, 3, 3, 3);

        // Add left data box
        leftDisplay = new GraphDisplay();
        leftDisplay.setBorder(BorderFactory.createLineBorder(Color.black));
        c.gridx = 0;
        c.gridy = 0;
        panel.add(leftDisplay, c);

        // Add right data box
        rightDisplay = new GraphDisplay();
        rightDisplay.setBorder(BorderFactory.createLineBorder(Color.black));
        c.gridx = 3;
        c.gridy = 0;
        panel.add(rightDisplay, c);

        // Reset weights
        c.weightx = 0f;
        c.weighty = 0f;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Add logo label
        ImageIcon ico = sizedIcon(getClass().getClassLoader().getResourceAsStream("ValoaSolarLogo.png"), 0.05f);
        JLabel logoLabel = new JLabel(ico, SwingConstants.LEFT);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(logoLabel, c);

        // Add leaderboard label
        fastestLabel = new JLabel("Fastest:", SwingConstants.RIGHT);
        fastestLabel.setForeground(new Color(0x00aa00));
        fastestLabel.setFont(dataFont);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(fastestLabel, c);

        // Add leaderboard label
        slowestLabel = new JLabel("Slowest:", SwingConstants.LEFT);
        slowestLabel.setForeground(new Color(0xaa0000));
        slowestLabel.setFont(dataFont);
        c.gridx = 3;
        c.gridy = 1;
        panel.add(slowestLabel, c);

        // Add update label
        updateLabel = new JLabel("Last Update: Never", SwingConstants.RIGHT);
        updateLabel.setFont(dataFont);
        c.gridx = 3;
        c.gridy = 1;
        panel.add(updateLabel, c);

        add(panel);
        revalidate();
        showingData = true;
    }

    public void ShowIntroScreen(String authMessage) throws URISyntaxException {
        showingData = false;
        clearFrame();

        Box box = new Box(BoxLayout.Y_AXIS);
        add(box);

        JLabel titleLabel = new JLabel("Productivity Chart for Valoa Solar");
        titleLabel.setFont(titleFont);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        box.add(titleLabel);

        box.add(Box.createVerticalGlue());

        // Authentication Label
        JLabel authLabel = new JLabel(authMessage);
        authLabel.setFont(titleFont);
        authLabel.setAlignmentX(CENTER_ALIGNMENT);

        box.add(authLabel, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        // QR Code
        JLabel qrCode = new JLabel(sizedIcon(getClass().getClassLoader().getResourceAsStream("ms-devicelogin-code.png"), 0.75f));
        qrCode.setAlignmentX(CENTER_ALIGNMENT);
        box.add(qrCode, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        revalidate();
    }

    void clearFrame() {
        getContentPane().removeAll();
        //repaint();
    }

    ImageIcon sizedIcon(InputStream path, float heightPct) {
        // Get bufferedImage
        BufferedImage img = null;
        try {
            img = ImageIO.read(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calc new size
        float aspectRatio = (float)img.getWidth() / (float)img.getHeight();
        int newHeight = (int)(getHeight() * heightPct);
        int newWidth = (int)(newHeight * aspectRatio);

        // Resize and return
        Image dimg = img.getScaledInstance(newWidth, newHeight,
                Image.SCALE_REPLICATE);
        return new ImageIcon(dimg);
    }
}
