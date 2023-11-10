package GraphComponents;

import ApiAccessors.DriveReader;
import ApiAccessors.WorksheetReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Map;

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

    //List<GraphComponent> cList = new ArrayList<>(Arrays.asList(
    //        new GraphComponent("City1"),
    //        new GraphComponent("City2"),
    //        new GraphComponent("City3"),
    //        new GraphComponent("City4"),
    //        new GraphComponent("City5"),
    //        new GraphComponent("City6"),
    //        new GraphComponent("City7"),
    //        new GraphComponent("City8"),
    //        new GraphComponent("City9"),
    //        new GraphComponent("City10")
    //));
    GraphComponent[] cList = new GraphComponent[0];

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

    public void SetData(WorksheetReader reader) {
        cList = reader.GetData();
        leftDisplay.components.clear();
        rightDisplay.components.clear();
        for (int i = 0; i < cList.length; i++) {
            if (i % 2 == 0)
                leftDisplay.AddComponent(cList[i]);
            else
                rightDisplay.AddComponent(cList[i]);
        }
    }

    public void Update(DriveReader reader) {
        if (!showingData)
            return;

        try {
            reader.worksheetReader.ReadSheet();
        }
        catch (Exception e) {
            ShowItemsScreen(reader);
        }

        SetData(reader.worksheetReader);

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

        // Refresh update label
        updateLabel.setText("Last Update: " + LocalDateTime.now().format(timeFormat));
    }

    public void ShowDataScreen(DriveReader reader) throws URISyntaxException{
        showingData = false;
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
        JLabel authLabel = new JLabel("<html><p style=\"text-align:center\">" + authMessage + "</p></html>", SwingConstants.CENTER);
        authLabel.setFont(dataFont);
        authLabel.setAlignmentX(CENTER_ALIGNMENT);
        //authLabel.setPreferredSize(new Dimension((int)(getWidth()*0.9), 0));

        box.add(authLabel, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        // QR Code
        JLabel qrCode = new JLabel(sizedIcon(getClass().getClassLoader().getResourceAsStream("ms-devicelogin-code.png"), 0.75f));
        qrCode.setAlignmentX(CENTER_ALIGNMENT);
        box.add(qrCode, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        revalidate();
    }

    public void ShowItemsScreen(DriveReader reader) {
        Map<String, String> sheets = reader.GetSheets();
        clearFrame();

        Box box = new Box(BoxLayout.Y_AXIS);
        add(box);

        JLabel titleLabel = new JLabel("Select spreadsheet to display.");
        titleLabel.setFont(titleFont);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        box.add(titleLabel);

        box.add(Box.createVerticalGlue());

        for (var sheet : sheets.keySet()) {
            JButton button = new JButton(sheets.get(sheet));
            button.addActionListener(e -> {
                if (!reader.SetSheet(sheet))
                    ShowItemsScreen(reader);
                try {
                    ShowDataScreen(reader);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            });
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            box.add(button);
            box.add(Box.createVerticalGlue());
        }
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
