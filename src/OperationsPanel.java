import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class OperationsPanel extends JPanel {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hostel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Maria@07"; // Update if changed

    // Dark Theme Colors matching the application
    private static final Color CANVAS = new Color(11, 14, 30);
    private static final Color CARD_BG = new Color(25, 32, 68);
    private static final Color CARD_BORDER = new Color(38, 48, 96);
    private static final Color INPUT_BG = new Color(36, 44, 82);
    private static final Color INPUT_BORDER = new Color(55, 68, 125);
    private static final Color TEXT_MUTED = new Color(160, 168, 204);

    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MEDIUM = new Font("Segoe UI", Font.BOLD, 13);

    private final Runnable onDataChanged;

    public OperationsPanel(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(0, 16));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        // Top Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Hostel Operations & Fast Actions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Process check-outs, mark evening attendance, and resolve tickets.");
        subtitle.setFont(BODY);
        subtitle.setForeground(TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // Grid containing the 3 operational cards
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        grid.add(buildVacateCard());
        grid.add(buildAttendanceCard());
        grid.add(buildResolveComplaintCard());

        add(grid, BorderLayout.CENTER);
    }

    // 1. Vacate & Check-Out Card
    private JPanel buildVacateCard() {
        JPanel card = createCard("Room Check-Out / Vacate", "Verify fees and free up occupied beds.");

        JTextField studentIdInput = createStyledTextField();
        JLabel label = fieldTitle("Enter Student ID:");

        JButton vacateBtn = createButton("Verify Dues & Vacate", new Color(210, 85, 95));
        vacateBtn.addActionListener(e -> {
            String sId = studentIdInput.getText().trim();
            if (sId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Student ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Check pending dues
                String feeQuery = "SELECT SUM(amount_due - amount_paid) AS balance FROM payments WHERE student_id = ? AND status != 'Paid'";
                try (PreparedStatement psFee = conn.prepareStatement(feeQuery)) {
                    psFee.setString(1, sId);
                    ResultSet rsFee = psFee.executeQuery();
                    double balance = 0.0;
                    if (rsFee.next()) {
                        balance = rsFee.getDouble("balance");
                    }

                    if (balance > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Cannot vacate Student #" + sId + "!\nOutstanding balance due: ₹" + balance,
                                "Pending Dues Found", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                int confirm = JOptionPane.showConfirmDialog(this,
                        "No pending dues for Student #" + sId + ".\nEnd active allocation and vacate bed?",
                        "Confirm Check-Out", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    String vacateQuery = "UPDATE allocations SET status = 'Ended' WHERE student_id = ? AND status = 'Active'";
                    try (PreparedStatement psVacate = conn.prepareStatement(vacateQuery)) {
                        psVacate.setString(1, sId);
                        int affected = psVacate.executeUpdate();

                        if (affected > 0) {
                            JOptionPane.showMessageDialog(this, "Student successfully checked out! Bed released.");
                            studentIdInput.setText("");
                            if (onDataChanged != null) onDataChanged.run();
                        } else {
                            JOptionPane.showMessageDialog(this, "No active allocation found for this Student ID.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(label);
        card.add(Box.createVerticalStrut(4));
        card.add(studentIdInput);
        card.add(Box.createVerticalStrut(16));
        card.add(vacateBtn);
        return card;
    }

    // 2. Curfew / Attendance Card
    private JPanel buildAttendanceCard() {
        JPanel card = createCard("Daily Curfew / Roll-Call", "Log daily student hostel presence.");

        JLabel idLabel = fieldTitle("Student ID:");
        JTextField studentIdInput = createStyledTextField();

        JLabel statusLabel = fieldTitle("Attendance Status:");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Present", "Night Out", "Leave", "Late Entry"});
        statusCombo.setFont(BODY);
        statusCombo.setBackground(INPUT_BG);
        statusCombo.setForeground(Color.WHITE);
        statusCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel remLabel = fieldTitle("Remarks (Optional):");
        JTextField remarksInput = createStyledTextField();

        JButton saveBtn = createButton("Save Entry", new Color(103, 114, 229));
        saveBtn.addActionListener(e -> {
            String sId = studentIdInput.getText().trim();
            if (sId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Student ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO attendance (student_id, date, status, remarks) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(sId));
                    ps.setString(2, LocalDate.now().toString());
                    ps.setString(3, (String) statusCombo.getSelectedItem());
                    ps.setString(4, remarksInput.getText().trim());
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Attendance recorded successfully!");
                    studentIdInput.setText("");
                    remarksInput.setText("");
                    if (onDataChanged != null) onDataChanged.run();
                }
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(idLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(studentIdInput);
        card.add(Box.createVerticalStrut(8));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(statusCombo);
        card.add(Box.createVerticalStrut(8));
        card.add(remLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(remarksInput);
        card.add(Box.createVerticalStrut(16));
        card.add(saveBtn);
        return card;
    }

    // 3. Resolve Complaint Card
    private JPanel buildResolveComplaintCard() {
        JPanel card = createCard("Complaint Desk", "Mark maintenance tickets as resolved.");

        JLabel idLabel = fieldTitle("Complaint ID:");
        JTextField complaintIdInput = createStyledTextField();

        JButton resolveBtn = createButton("✓ Mark as Resolved", new Color(58, 151, 125));
        resolveBtn.addActionListener(e -> {
            String cId = complaintIdInput.getText().trim();
            if (cId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a Complaint ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE complaints SET status = 'Resolved' WHERE complaint_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, cId);
                    int updated = ps.executeUpdate();

                    if (updated > 0) {
                        JOptionPane.showMessageDialog(this, "Complaint #" + cId + " marked as Resolved!");
                        complaintIdInput.setText("");
                        if (onDataChanged != null) onDataChanged.run();
                    } else {
                        JOptionPane.showMessageDialog(this, "Complaint ID not found.", "Not Found", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        card.add(idLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(complaintIdInput);
        card.add(Box.createVerticalStrut(16));
        card.add(resolveBtn);
        return card;
    }

    // Component Styling Helpers
    private JPanel createCard(String titleText, String subtitleText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("<html><div style='width:180px'>" + subtitleText + "</div></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(16));
        return card;
    }

    private JLabel fieldTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(BODY);
        tf.setBackground(INPUT_BG);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(INPUT_BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JButton createButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(MEDIUM);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}