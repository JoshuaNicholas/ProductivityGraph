package org.example.GraphComponents;

import kotlin.Pair;
import org.example.ApiAccessors.DriveReader;
import org.example.ApiAccessors.WorksheetReader;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public class GraphWindow extends JFrame {

    static final Color backgroundColor = new Color(0x141C1C);
    static final Color textColor = new Color(0x6bb5f4);

    static Font titleFont = new Font(Font.DIALOG, Font.BOLD, 40);
    static Font dataFont = new Font(Font.DIALOG, Font.PLAIN, 30);
    static Font markerFont = new Font(Font.DIALOG, Font.BOLD, 20);
    Boolean showingData = false;

    GraphDisplay leftDisplay;
    GraphDisplay rightDisplay;

    JLabel updateLabel, fastestLabel, slowestLabel;
    GraphicsDevice device = getGraphicsConfiguration().getDevice();

    Label tl = new Label("Loading...");
    public GraphWindow(String title) {
        super(title);

        // Make window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setResizable(true);
        //setVisible(true);
        //setForeground(backgroundColor);
        getContentPane().setBackground(backgroundColor);
        getContentPane().setForeground(textColor);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(tl);
        device.setFullScreenWindow(this);
    }

    public void ShowBasicText(String text) {
        showingData = false;
        clearFrame();
        UpdateBasicText(text);
        add(tl);
        revalidate();
    }
    public void UpdateBasicText(String text) {
        tl.setText(text);
    }

    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a");

    GraphComponent[] cList = new GraphComponent[0];

    public static void Initialize() {
        try {
            Font base = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GraphWindow.class.getClassLoader().getResourceAsStream("ValoaCircularWeb-Regular.ttf")));
            Font bold = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(GraphWindow.class.getClassLoader().getResourceAsStream("ValoaCircularWeb-Bold.ttf")));

            titleFont = bold.deriveFont(40f);
            dataFont = base.deriveFont(30f);
            markerFont = base.deriveFont(20f);

            System.out.println("Successfully overrode fonts.");

            // Transparent 16 x 16 pixel cursor image.
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            // Create a new blank cursor.
            blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            System.out.println("Successfully overrode mouse cursor.");
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
        if (!showingData || reader.worksheetReader == null)
            return;

        try {
            reader.worksheetReader.ReadSheet();
        }
        catch (Exception e) {
            ShowItemsScreen(reader);
        }

        SetData(reader.worksheetReader);

        // Get and set the highest values
        int highest = leftDisplay.GetHighestValue();
        if (rightDisplay.GetHighestValue() > highest)
            highest = rightDisplay.GetHighestValue();

        // Get and set the slowest labels
        GraphComponent slowest = leftDisplay.GetSlowest();
        if (rightDisplay.GetSlowest().GetCurrentValue() > slowest.GetCurrentValue())
            slowest = rightDisplay.GetSlowest();
        slowestLabel.setText("Slowest: " + slowest.GetName() + " @ " + slowest.GetCurrentValue() + " days.");

        // Get and set the fastest labels
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

    public void ShowDataScreen() {
        HideMouseCursor();
        showingData = false;
        clearFrame();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setForeground(textColor);
        panel.setBackground(backgroundColor);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0f;
        c.weighty = 1.0f;
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3, 3, 3, 3);

        LineBorder border = new LineBorder(GraphComponent.MarkerColor, 1, true);

        // Add left data box
        leftDisplay = new GraphDisplay();
        leftDisplay.setBackground(backgroundColor);
        leftDisplay.setBorder(border);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(leftDisplay, c);

        // Add right data box
        rightDisplay = new GraphDisplay();
        rightDisplay.setBackground(backgroundColor);
        rightDisplay.setBorder(border);
        c.gridx = 3;
        c.gridy = 0;
        panel.add(rightDisplay, c);

        // Reset weights
        c.weightx = 0f;
        c.weighty = 0f;
        c.fill = GridBagConstraints.HORIZONTAL;

        // Add logo label
        JLabel logoLabel = new JLabel(sizedIcon(getClass().getClassLoader().getResourceAsStream("ValoaSolarLogo.png"), 0.05f), SwingConstants.LEFT);
        logoLabel.setForeground(textColor);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(logoLabel, c);

        // Add leaderboard label
        fastestLabel = new JLabel("Fastest:", SwingConstants.RIGHT);
        fastestLabel.setForeground(GraphComponent.AheadColor);
        fastestLabel.setFont(dataFont);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(fastestLabel, c);

        // Add leaderboard label
        slowestLabel = new JLabel("Slowest:", SwingConstants.LEFT);
        slowestLabel.setForeground(GraphComponent.BehindColor);
        slowestLabel.setFont(dataFont);
        c.gridx = 3;
        c.gridy = 1;
        panel.add(slowestLabel, c);

        // Add update label
        updateLabel = new JLabel("Awaiting first update...", SwingConstants.RIGHT);
        updateLabel.setFont(dataFont);
        updateLabel.setForeground(textColor);
        c.gridx = 3;
        c.gridy = 1;
        panel.add(updateLabel, c);

        add(panel);
        revalidate();
        showingData = true;
    }

    public void ShowIntroScreen(String authMessage) {
        ShowIntroScreen(authMessage, "All spreadsheet names visible by the active user will be displayed.");
    }

    public void ShowIntroScreen(String authMessage, String commentMessage) {
        // Bold the authentication code
        authMessage = authMessage.replace("code ", "code <b>").replace(" to au", "</b> to au");

        HideMouseCursor();

        showingData = false;
        clearFrame();

        Box box = new Box(BoxLayout.Y_AXIS);
        box.setForeground(textColor);
        add(box);

        box.add(Box.createVerticalGlue());

        JLabel titleLabel = new JLabel("Productivity Chart for Valoa Solar");
        titleLabel.setFont(titleFont);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setForeground(textColor);
        box.add(titleLabel);

        JLabel creditsLabel = new JLabel("by Joshua Nicholas - NCAPS TS 23-24");
        creditsLabel.setForeground(textColor);
        creditsLabel.setFont(markerFont);
        creditsLabel.setAlignmentX(CENTER_ALIGNMENT);
        box.add(creditsLabel);

        box.add(Box.createVerticalGlue());

        // Authentication Label
        JLabel authLabel = new JLabel("<html><p style=\"text-align:center\">" + authMessage + "</p></html>", SwingConstants.CENTER);

        authLabel.setForeground(textColor);
        authLabel.setFont(dataFont);
        authLabel.setAlignmentX(CENTER_ALIGNMENT);
        //authLabel.setPreferredSize(new Dimension((int)(getWidth()*0.9), 0));

        box.add(authLabel, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        // QR Code
        JLabel qrCode = new JLabel(sizedIcon(getClass().getClassLoader().getResourceAsStream("ms-devicelogin-code.png"), 0.7f));
        qrCode.setAlignmentX(CENTER_ALIGNMENT);
        box.add(qrCode, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        JLabel permissionsLabel = new JLabel("<html><p style=\"text-align:center\"><i>Permissions: Files.Read.All<br>" + commentMessage + "</i></p></html>", SwingConstants.CENTER);
        permissionsLabel.setFont(markerFont);
        permissionsLabel.setAlignmentX(CENTER_ALIGNMENT);
        permissionsLabel.setForeground(textColor);
        box.add(permissionsLabel, BorderLayout.PAGE_START);
        box.add(Box.createVerticalGlue());

        revalidate();
    }

    public void ShowItemsScreen(DriveReader reader) {
        ShowMouseCursor();
        Map<String, Pair<String, Boolean>> sheets = reader.GetSheets();
        clearFrame();

        Box box = new Box(BoxLayout.Y_AXIS);
        add(box);

        box.add(Box.createVerticalGlue());

        JLabel titleLabel = new JLabel("Select spreadsheet to display.");
        titleLabel.setFont(titleFont);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setForeground(textColor);
        box.add(titleLabel);

        box.add(Box.createVerticalGlue());

        for (var sheet : sheets.keySet()) {
            JButton button = new JButton(sheets.get(sheet).component1() + (sheets.get(sheet).component2() ? " (Shared)" : ""));
            button.addActionListener(e -> {
                ShowBasicText("Loading item [" + sheet + "]...");
                if (!reader.SetSheet(sheet, sheets.get(sheet).component2()))
                    ShowItemsScreen(reader);
                ShowDataScreen();
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
        return new ImageIcon(sizedImage(path, heightPct));
    }

    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImg.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g.drawImage(srcImg, 0, 0, w, h, null);
        g.dispose();

        return resizedImg;
    }

    Image sizedImage(InputStream path, float heightPct) {
        // Get bufferedImage
        BufferedImage img = null;
        try {
            img = ImageIO.read(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calc new size
        assert img != null;
        float aspectRatio = (float)img.getWidth() / (float)img.getHeight();
        int newHeight = (int)(getHeight() * heightPct);
        int newWidth = (int)(newHeight * aspectRatio);

        // Resize and return
        return getScaledImage(img, newWidth, newHeight);
    }


    private void ShowMouseCursor() {
        getContentPane().setCursor(Cursor.getDefaultCursor());
    }

    static Cursor blankCursor;
    private void HideMouseCursor() {
        // Set the blank cursor to the JFrame.
        getContentPane().setCursor(blankCursor);
    }
}
