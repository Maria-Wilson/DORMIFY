import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("DORMIFY | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);

        // Custom layered background containing the background spheres and the glass card
        BackgroundPanel root = new BackgroundPanel();
        root.setLayout(new GridBagLayout()); // Centers the glass card
        setContentPane(root);

        // Glassmorphic Login Card
        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(440, 390));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 45, 30, 45));

        // 1. Heading
        JLabel title = new JLabel("LOGIN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Email Section
        JLabel emailLabel = fieldLabel("Email address");
        emailField = new RoundedTextField(15);
        emailField.setText("admin@dormify.com");

        // 3. Password Section
        JLabel passLabel = fieldLabel("Password");
        passField = new RoundedPasswordField(15);
        passField.setText("password123");

        // 4. Remember me & Forgot Password
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JCheckBox rememberMe = new JCheckBox("Remember me");
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rememberMe.setForeground(new Color(210, 205, 230));
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);

        JLabel forgotPass = new JLabel("Forgot password?");
        forgotPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        forgotPass.setForeground(new Color(210, 205, 230));
        forgotPass.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        optionsPanel.add(rememberMe, BorderLayout.WEST);
        optionsPanel.add(forgotPass, BorderLayout.EAST);

        // 5. Sign In Button
        JButton signInBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vivid violet-magenta button gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(130, 50, 240), getWidth(), 0, new Color(165, 45, 230));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 24, 24));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        signInBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setContentAreaFilled(false);
        signInBtn.setMaximumSize(new Dimension(180, 42));
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Click action: launch your Main dashboard
        signInBtn.addActionListener(e -> {
            new Main().setVisible(true);
            dispose(); // Closes the login window
        });

        // Assemble into the glass card
        card.add(title);
        card.add(Box.createVerticalStrut(22));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(passField);
        card.add(Box.createVerticalStrut(12));
        card.add(optionsPanel);
        card.add(Box.createVerticalStrut(24));
        card.add(signInBtn);

        root.add(card);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(205, 195, 230));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // ------------------- CUSTOM GRAPHICS COMPONENTS -------------------

    // Background Canvas with Deep Violet Tint & Glowing 3D Spheres
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Midnight purple backdrop
            g2.setColor(new Color(19, 13, 38));
            g2.fillRect(0, 0, w, h);

            // Draw glowing 3D spheres positioned behind & around the card
            drawSphere(g2, (int) (w * 0.38), (int) (h * 0.28), 170); // Upper center purple sphere
            drawSphere(g2, (int) (w * 0.28), (int) (h * 0.68), 160); // Bottom-left foreground sphere
            drawSphere(g2, (int) (w * 0.65), (int) (h * 0.66), 190); // Bottom-right sphere
            drawSphere(g2, (int) (w * 0.62), (int) (h * 0.45), 110); // Center-right ambient orb

            g2.dispose();
        }

        private void drawSphere(Graphics2D g2, int centerX, int centerY, int radius) {
            // Radial highlight producing a lit 3D ball effect
            Point center = new Point(centerX - radius / 3, centerY - radius / 3);
            float[] dist = {0.0f, 0.45f, 1.0f};
            Color[] colors = {
                    new Color(230, 80, 255), // Top specular highlight
                    new Color(135, 30, 220), // Rich magenta-purple body
                    new Color(35, 10, 80)    // Deep ambient shadow
            };
            RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
            g2.setPaint(p);
            g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        }
    }

    // Frosted Glass Effect Card
    private static class GlassCard extends JPanel {
        public GlassCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 32;

            // Semi-transparent frosted glass body
            g2.setColor(new Color(255, 255, 255, 28));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

            // Soft white glossy rim
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, w - 1.2f, h - 1.2f, arc, arc));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded translucent text field
    private static class RoundedTextField extends JTextField {
        RoundedTextField(int cols) {
            super(cols);
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Pill translucent container
            g2.setColor(new Color(125, 90, 190, 85));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded translucent password field
    private static class RoundedPasswordField extends JPasswordField {
        RoundedPasswordField(int cols) {
            super(cols);
            setOpaque(false);
            setForeground(Color.WHITE);
            setCaretColor(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(125, 90, 190, 85));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
