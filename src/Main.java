
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * DORMIFY - Hostel Management System
 * Java 17 + Swing + MySQL
 *
 * Keep this file at: D:\java\src\Main.java
 * Update DB_PASSWORD below before running.
 */
public class Main extends JFrame {
    // ---------- DATABASE SETTINGS ----------
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hostel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Maria@07"; // Replace this

    // ---------- THEME ----------
private static final Color SIDEBAR = new Color(30, 18, 52);
private static final Color SIDEBAR_HOVER = new Color(65, 43, 96);
private static final Color PURPLE = new Color(155, 110, 235);
private static final Color LAVENDER = new Color(55, 39, 78);
private static final Color CANVAS = new Color(24, 17, 37);
private static final Color INK = new Color(245, 239, 255);
private static final Color MUTED = new Color(190, 177, 211);
private static final Color WHITE = new Color(39, 28, 55);

private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
private static final Font MEDIUM = new Font("Segoe UI", Font.BOLD, 13);
private static final Font HEADING = new Font("Segoe UI", Font.BOLD, 25);

    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageContainer = new JPanel(pageLayout);
    private final JPanel sidebar = new JPanel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, ModuleConfig> modules = new LinkedHashMap<>();
    private String activePage = "Dashboard";
    private JLabel clockLabel;
    private JPanel dashboardPage;
    private DefaultTableModel currentModel;
    private JTable currentTable;
    private String currentModuleKey;

    public Main() {
        super("DORMIFY | Hostel Management System");
        configureWindow();
        configureModules();
        buildShell();
        showPage("Dashboard");
        refreshDashboard();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 820);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CANVAS);
        setLayout(new BorderLayout());
    }

    private void configureModules() {
        modules.put("Students", new ModuleConfig("students", "Student Directory",
                "Manage student profiles and contact details.",
                new String[] { "student_id", "name", "register_number", "department", "phone", "email" },
                new String[] { "ID", "Student name", "Register number", "Department", "Phone", "Email" },
                new String[] { "name", "register_number", "department", "phone", "email" },
                new String[] { "Student name", "Register number", "Department", "Phone", "Email" },
                "student_id"));

        modules.put("Rooms", new ModuleConfig("rooms", "Room Management",
                "View and maintain hostel room information.",
                new String[] { "room_id", "room_number", "capacity", "room_type" },
                new String[] { "ID", "Room number", "Capacity", "Room type" },
                new String[] { "room_number", "capacity", "room_type" },
                new String[] { "Room number", "Capacity", "Room type" },
                "room_id"));

        modules.put("Room Allocation", new ModuleConfig("allocations", "Room Allocation",
                "Assign students to rooms and track active allocations.",
                new String[] { "allocation_id", "student_id", "room_id", "allocation_date", "status" },
                new String[] { "ID", "Student ID", "Room ID", "Allocation date", "Status" },
                new String[] { "student_id", "room_id", "allocation_date", "status" },
                new String[] { "Student ID", "Room ID", "Date (YYYY-MM-DD)", "Status (Active/Ended)" },
                "allocation_id"));

        modules.put("Fee Management", new ModuleConfig("payments", "Fee Management",
                "Record hostel fees and monitor outstanding balances.",
                new String[] { "payment_id", "student_id", "fee_month", "fee_year", "amount_due", "amount_paid",
                        "payment_date", "status" },
                new String[] { "ID", "Student ID", "Month", "Year", "Amount due", "Amount paid", "Payment date",
                        "Status" },
                new String[] { "student_id", "fee_month", "fee_year", "amount_due", "amount_paid", "payment_date",
                        "status" },
                new String[] { "Student ID", "Month", "Year", "Amount due", "Amount paid",
                        "Payment date (YYYY-MM-DD or blank)", "Status" },
                "payment_id"));

        modules.put("Complaints", new ModuleConfig("complaints", "Complaints",
                "Track student complaints and update their status.",
                new String[] { "complaint_id", "student_id", "category", "description", "complaint_date", "status" },
                new String[] { "ID", "Student ID", "Category", "Description", "Date", "Status" },
                new String[] { "student_id", "category", "description", "complaint_date", "status" },
                new String[] { "Student ID", "Category", "Description", "Date (YYYY-MM-DD)", "Status" },
                "complaint_id"));
    }

    private void buildShell() {
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(new EmptyBorder(22, 15, 18, 15));
        sidebar.setPreferredSize(new Dimension(235, 0));

        JLabel brand = new JLabel("DORMIFY");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel tagline = new JLabel("HOSTEL MANAGEMENT SYSTEM");
        tagline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagline.setForeground(new Color(198, 181, 228));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(34));

        JLabel menuLabel = new JLabel("WORKSPACE");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        menuLabel.setForeground(new Color(171, 153, 202));
        menuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuLabel);
        sidebar.add(Box.createVerticalStrut(10));

        String[] pages = { "Dashboard", "Students", "Rooms", "Room Allocation", "Fee Management", "Complaints", "Operations", "Reports" };
        for (String page : pages) {
            JButton button = new JButton(navLabel(page));
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);  // Must be true so setBackground works
            button.setOpaque(true);
            button.setBorder(new EmptyBorder(10, 14, 10, 14));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Set colors
            if (page.equals(activePage)) {
                button.setBackground(new Color(103, 114, 229)); // Active violet pill
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(SIDEBAR);                  // Matches dark sidebar
                button.setForeground(new Color(215, 210, 235)); // High-contrast lavender
            }

            button.addActionListener(e -> {
                showPage(page);
                for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
                    boolean isCurrent = entry.getKey().equals(page);
                    entry.getValue().setBackground(isCurrent ? new Color(103, 114, 229) : SIDEBAR);
                    entry.getValue().setForeground(isCurrent ? Color.WHITE : new Color(215, 210, 235));
                }
            });

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!page.equals(activePage)) {
                        button.setBackground(SIDEBAR_HOVER);
                    }
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!page.equals(activePage)) {
                        button.setBackground(SIDEBAR);
                    }
                }
            });

            navButtons.put(page, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }
        sidebar.add(Box.createVerticalGlue());
        JPanel helpCard = new JPanel();
        helpCard.setLayout(new BoxLayout(helpCard, BoxLayout.Y_AXIS));
        helpCard.setBackground(new Color(55, 38, 83));
        helpCard.setBorder(new EmptyBorder(13, 12, 13, 12));
        helpCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        JLabel helpTitle = new JLabel("Need a quick check?");
        helpTitle.setForeground(WHITE);
        helpTitle.setFont(MEDIUM);
        JLabel helpText = new JLabel(
                "<html><div style='width:155px'>Refresh your records to see the latest hostel overview.</div></html>");
        helpText.setForeground(new Color(205, 194, 224));
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helpCard.add(helpTitle);
        helpCard.add(Box.createVerticalStrut(6));
        helpCard.add(helpText);
        helpCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(helpCard);
        sidebar.add(Box.createVerticalStrut(14));

        add(sidebar, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(CANVAS);
        right.add(buildTopbar(), BorderLayout.NORTH);

        pageContainer.setBackground(CANVAS);
        pageContainer.add(buildDashboardPage(), "Dashboard");
        for (String key : modules.keySet()) {
            pageContainer.add(buildModulePage(key), key);
        }
        pageContainer.add(buildReportsPage(), "Reports");
        pageContainer.add(new OperationsPanel(() -> refreshDashboard()), "Operations");
        right.add(pageContainer, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
    }

    private String navLabel(String page) {
        String icon = switch (page) {
            case "Dashboard" -> "▦";
            case "Students" -> "♙";
            case "Rooms" -> "▤";
            case "Room Allocation" -> "⇄";
            case "Fee Management" -> "＄";
            case "Complaints" -> "!";
            case "Operations" -> "⚙";
            default -> "▥";
        };
        return icon + "    " + page;
    }

    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(WHITE);
        top.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(235, 231, 242)),
                new EmptyBorder(15, 27, 15, 27)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("DORMIFY  /  WORKSPACE");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(PURPLE);
        JLabel title = new JLabel("Hostel overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 19));
        title.setForeground(INK);
        left.add(eyebrow);
        left.add(Box.createVerticalStrut(3));
        left.add(title);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 3));
        right.setOpaque(false);
        JLabel admin = new JLabel("Administrator");
        admin.setFont(MEDIUM);
        admin.setForeground(new Color(78, 112, 91));
        admin.setBorder(new EmptyBorder(0, 0, 0, 5));
        clockLabel = new JLabel(LocalDate.now().toString());
        clockLabel.setFont(BODY);
        clockLabel.setForeground(MUTED);
        JButton refresh = button("↻  Refresh", PURPLE, WHITE);
        refresh.addActionListener(e -> {
            refreshDashboard();
            if (modules.containsKey(activePage))
                loadModuleTable(activePage);
            clockLabel.setText(LocalDate.now().toString());
        });
        right.add(admin);
        right.add(clockLabel);
        right.add(refresh);
        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildDashboardPage() {
        dashboardPage = new JPanel(new BorderLayout(0, 20));
        dashboardPage.setBackground(CANVAS);
        dashboardPage.setBorder(new EmptyBorder(25, 28, 25, 28));

        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);
        JPanel introText = new JPanel();
        introText.setOpaque(false);
        introText.setLayout(new BoxLayout(introText, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel("Good to see you 👋");
        welcome.setFont(HEADING);
        welcome.setForeground(INK);
        JLabel sub = new JLabel("Here’s what’s happening across your hostel today.");
        sub.setFont(BODY);
        sub.setForeground(MUTED);
        introText.add(welcome);
        introText.add(Box.createVerticalStrut(7));
        introText.add(sub);

        JButton addStudent = button("+  Add student", PURPLE, WHITE);
        addStudent.addActionListener(e -> openAddDialog("Students"));
        intro.add(introText, BorderLayout.WEST);
        intro.add(addStudent, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("TOTAL STUDENTS", "0", "Registered residents", "♙", new Color(124, 87, 194)));
        stats.add(statCard("TOTAL ROOMS", "0", "Rooms in the system", "▤", new Color(83, 123, 194)));
        stats.add(statCard("AVAILABLE BEDS", "0", "Based on active allocations", "⌂", new Color(58, 151, 125)));
        stats.add(statCard("OPEN COMPLAINTS", "0", "Awaiting resolution", "!", new Color(211, 139, 72)));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        content.add(stats);
        content.add(Box.createVerticalStrut(20));

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.add(buildOccupancyPanel());
        lower.add(buildQuickActionsPanel());
        lower.setPreferredSize(new Dimension(900, 275));
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, 275));
        content.add(lower);
        content.add(Box.createVerticalStrut(18));

        JPanel recent = buildRecentPanel();
        recent.setPreferredSize(new Dimension(900, 245));
        recent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 245));
        content.add(recent);

        dashboardPage.add(intro, BorderLayout.NORTH);
        dashboardPage.add(content, BorderLayout.CENTER);
        return dashboardPage;
    }

    private JPanel statCard(String label, String value, String description, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(17, 17, 16, 17)));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.BOLD, 10));
        name.setForeground(MUTED);
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        iconLabel.setForeground(accent);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
        iconLabel.setBorder(new EmptyBorder(5, 9, 5, 9));
        top.add(name, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);
        JLabel number = new JLabel(value);
        number.setName("stat:" + label);
        number.setFont(new Font("Segoe UI", Font.BOLD, 29));
        number.setForeground(INK);
        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(MUTED);
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(number);
        bottom.add(Box.createVerticalStrut(3));
        bottom.add(desc);
        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOccupancyPanel() {
        JPanel panel = whitePanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        JLabel title = sectionTitle("Room occupancy", "Capacity overview");
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel occupancyNumber = new JLabel("—");
        occupancyNumber.setName("occupancyNumber");
        occupancyNumber.setFont(new Font("Segoe UI", Font.BOLD, 33));
        occupancyNumber.setForeground(INK);
        JLabel occupancyText = new JLabel("occupied beds / total capacity");
        occupancyText.setFont(BODY);
        occupancyText.setForeground(MUTED);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setName("occupancyBar");
        bar.setStringPainted(true);
        bar.setString("Waiting for data");
        bar.setForeground(PURPLE);
        bar.setBackground(LAVENDER);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(200, 14));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        JLabel hint = new JLabel("Add rooms and active allocations to calculate occupancy.");
        hint.setName("occupancyHint");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        center.add(Box.createVerticalStrut(8));
        center.add(occupancyNumber);
        center.add(Box.createVerticalStrut(2));
        center.add(occupancyText);
        center.add(Box.createVerticalStrut(19));
        center.add(bar);
        center.add(Box.createVerticalStrut(12));
        center.add(hint);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildQuickActionsPanel() {
        JPanel panel = whitePanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        panel.add(sectionTitle("Quick actions", "Common tasks"), BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(2, 2, 10, 10));
        actions.setOpaque(false);
        actions.add(actionTile("＋", "Register student", "Create a student record", () -> openAddDialog("Students")));
        actions.add(actionTile("▤", "Add a room", "Update room inventory", () -> openAddDialog("Rooms")));
        actions.add(actionTile("⇄", "Allocate room", "Assign a student", () -> openAddDialog("Room Allocation")));
        actions.add(actionTile("!", "View complaints", "Review open issues", () -> showPage("Complaints")));
        panel.add(actions, BorderLayout.CENTER);
        return panel;
    }

    private JPanel actionTile(String icon, String title, String subtitle, Runnable action) {
        JPanel tile = new JPanel(new BorderLayout(8, 3));
        tile.setBackground(new Color(39, 28, 55));
        tile.setBorder(new CompoundBorder(new LineBorder(new Color(235, 229, 246), 1, true),
                new EmptyBorder(11, 11, 11, 11)));
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel glyph = new JLabel(icon);
        glyph.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        glyph.setForeground(PURPLE);
        JLabel heading = new JLabel(title);
        heading.setFont(MEDIUM);
        heading.setForeground(INK);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(MUTED);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(heading);
        text.add(Box.createVerticalStrut(4));
        text.add(sub);
        tile.add(glyph, BorderLayout.WEST);
        tile.add(text, BorderLayout.CENTER);
        tile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                tile.setBackground(LAVENDER);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                tile.setBackground(new Color(39, 28, 55));
            }
        });
        return tile;
    }

    private JPanel buildRecentPanel() {
        JPanel panel = whitePanel();
        panel.setLayout(new BorderLayout(0, 12));
        panel.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(18, 18, 18, 18)));
        panel.add(sectionTitle("Recently registered students", "Latest records from your database"),
                BorderLayout.NORTH);
        String[] columns = { "ID", "Student name", "Register number", "Department", "Phone" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = styleTable(new JTable(model));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(239, 235, 245)));
        scroll.getViewport().setBackground(new Color(39, 28, 55));
scroll.setBackground(new Color(39, 28, 55));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildModulePage(String key) {
        ModuleConfig cfg = modules.get(key);
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(CANVAS);
        page.setBorder(new EmptyBorder(25, 28, 25, 28));

        JPanel intro = new JPanel(new BorderLayout());
        intro.setOpaque(false);
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(cfg.title);
        title.setFont(HEADING);
        title.setForeground(INK);
        JLabel desc = new JLabel(cfg.description);
        desc.setFont(BODY);
        desc.setForeground(MUTED);
        labels.add(title);
        labels.add(Box.createVerticalStrut(7));
        labels.add(desc);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        JTextField search = new JTextField(16);
        search.setFont(BODY);
        search.setPreferredSize(new Dimension(190, 36));
        search.setToolTipText("Search loaded records");
        JButton searchButton = button("⌕  Search", new Color(65, 46, 88), PURPLE);
        JButton add = button("+  Add new", PURPLE, WHITE);
        JButton edit = button("Edit", new Color(65, 46, 88), PURPLE);
        JButton delete = button("Delete", new Color(75, 38, 58), new Color(174, 61, 77));
        JButton reload = button("↻", WHITE, INK);
        controls.add(search);
        controls.add(searchButton);
        controls.add(add);
        controls.add(edit);
        controls.add(delete);
        controls.add(reload);
        intro.add(labels, BorderLayout.WEST);
        intro.add(controls, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(cfg.headers, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = styleTable(new JTable(model));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(235, 231, 243)));
        scroll.getViewport().setBackground(new Color(39, 28, 55));
scroll.setBackground(new Color(39, 28, 55));

        JPanel tableCard = whitePanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(13, 13, 13, 13)));
        tableCard.add(scroll, BorderLayout.CENTER);

        JLabel footnote = new JLabel("Tip: Select one row before choosing Edit or Delete.");
        footnote.setForeground(MUTED);
        footnote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.add(tableCard, BorderLayout.CENTER);
        wrapper.add(footnote, BorderLayout.SOUTH);

        add.addActionListener(e -> openAddDialog(key));
        edit.addActionListener(e -> editSelected(key, table));
        delete.addActionListener(e -> deleteSelected(key, table));
        reload.addActionListener(e -> loadModuleTable(key));
        searchButton.addActionListener(e -> filterTable(model, search.getText()));
        search.addActionListener(e -> filterTable(model, search.getText()));

        page.add(intro, BorderLayout.NORTH);
        page.add(wrapper, BorderLayout.CENTER);
        page.putClientProperty("model", model);
        page.putClientProperty("table", table);
        return page;
    }

    private JPanel buildReportsPage() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(CANVAS);
        page.setBorder(new EmptyBorder(25, 28, 25, 28));
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Reports & summaries");
        title.setFont(HEADING);
        title.setForeground(INK);
        JLabel desc = new JLabel("Quick database summaries for hostel administration.");
        desc.setFont(BODY);
        desc.setForeground(MUTED);
        heading.add(title);
        heading.add(Box.createVerticalStrut(7));
        heading.add(desc);

        JPanel reportCard = whitePanel();
        reportCard.setLayout(new BorderLayout(0, 14));
        reportCard.setBorder(new CompoundBorder(new LineBorder(new Color(235, 231, 243), 1, true),
                new EmptyBorder(22, 22, 22, 22)));
        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Consolas", Font.PLAIN, 13));
        output.setForeground(INK);
        output.setBackground(new Color(39, 28, 55));
        output.setBorder(new EmptyBorder(15, 15, 15, 15));
        JButton generate = button("Generate report", PURPLE, WHITE);
        JButton export = button("Export as TXT", new Color(65, 46, 88), PURPLE);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(generate);
        actions.add(export);
        reportCard.add(actions, BorderLayout.NORTH);
        reportCard.add(new JScrollPane(output), BorderLayout.CENTER);
        generate.addActionListener(e -> output.setText(generateReport()));
        export.addActionListener(e -> {
            if (output.getText().isBlank())
                output.setText(generateReport());
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("dormify-report.txt"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.nio.file.Files.writeString(chooser.getSelectedFile().toPath(), output.getText());
                    JOptionPane.showMessageDialog(this, "Report exported successfully.", "DORMIFY",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showError("Could not export report", ex);
                }
            }
        });
        page.add(heading, BorderLayout.NORTH);
        page.add(reportCard, BorderLayout.CENTER);
        return page;
    }

    private JPanel whitePanel() {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        return p;
    }

    private JLabel sectionTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel a = new JLabel(title);
        a.setFont(new Font("Segoe UI", Font.BOLD, 15));
        a.setForeground(INK);
        JLabel b = new JLabel(subtitle);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setForeground(MUTED);
        p.add(a);
        p.add(Box.createVerticalStrut(4));
        p.add(b);
        JLabel holder = new JLabel();
        holder.setLayout(new BorderLayout());
        holder.add(p, BorderLayout.CENTER);
        return holder;
    }

    private JButton button(String text, Color background, Color foreground) {
    JButton b = new JButton(text);
    b.setFont(MEDIUM);
    b.setForeground(foreground);
    b.setBackground(background);
    b.setFocusPainted(false);
    b.setOpaque(true);
    b.setContentAreaFilled(true);
    b.setBorderPainted(true);
    b.setBorder(new CompoundBorder(
        new LineBorder(background, 1, true),
        new EmptyBorder(9, 13, 9, 13)
    ));
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return b;
}

    private JTable styleTable(JTable table) {
    Color tableBg = new Color(39, 28, 55);
    Color headerBg = new Color(55, 39, 78);
    Color textColor = new Color(245, 239, 255);
    Color gridColor = new Color(76, 58, 99);

    table.setFont(BODY);
    table.setForeground(textColor);
    table.setBackground(tableBg);
    table.setOpaque(true);
    table.setRowHeight(36);
    table.setShowVerticalLines(false);
    table.setShowHorizontalLines(true);
    table.setGridColor(gridColor);
    table.setSelectionBackground(new Color(105, 72, 150));
    table.setSelectionForeground(Color.WHITE);
    table.setFillsViewportHeight(true);

    table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean selected,
                boolean focused, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                t, value, selected, focused, row, column);

            c.setBackground(selected
                ? new Color(105, 72, 150)
                : new Color(39, 28, 55));

            c.setForeground(new Color(245, 239, 255));
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    });

    JTableHeader header = table.getTableHeader();
    header.setFont(MEDIUM);
    header.setForeground(new Color(220, 202, 255));
    header.setBackground(headerBg);
    header.setOpaque(true);
    header.setReorderingAllowed(false);

    return table;
}
    private void showPage(String page) {
        activePage = page;
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean selected = entry.getKey().equals(page);
            entry.getValue().setBackground(selected ? SIDEBAR_HOVER : SIDEBAR);
            entry.getValue().setForeground(selected ? WHITE : new Color(226, 219, 239));
        }
        pageLayout.show(pageContainer, page);
        if (modules.containsKey(page))
            loadModuleTable(page);
        if (page.equals("Dashboard"))
            refreshDashboard();
    }

    private void refreshDashboard() {
        if (dashboardPage == null)
            return;
        try (Connection c = getConnection()) {
            int students = scalar(c, "SELECT COUNT(*) FROM students");
            int rooms = scalar(c, "SELECT COUNT(*) FROM rooms");
            int capacity = scalar(c, "SELECT COALESCE(SUM(capacity),0) FROM rooms");
            int occupied = scalar(c, "SELECT COUNT(DISTINCT student_id) FROM allocations WHERE status='Active'");
            int complaints = scalar(c, "SELECT COUNT(*) FROM complaints WHERE status='Pending'");
            setStat("TOTAL STUDENTS", String.valueOf(students));
            setStat("TOTAL ROOMS", String.valueOf(rooms));
            setStat("AVAILABLE BEDS", String.valueOf(Math.max(0, capacity - occupied)));
            setStat("OPEN COMPLAINTS", String.valueOf(complaints));

            JLabel occupancyNumber = findLabel(dashboardPage, "occupancyNumber");
            JProgressBar bar = findProgress(dashboardPage, "occupancyBar");
            JLabel hint = findLabel(dashboardPage, "occupancyHint");
            if (occupancyNumber != null)
                occupancyNumber.setText(occupied + " / " + capacity);
            int pct = capacity == 0 ? 0 : Math.min(100, (int) Math.round(occupied * 100.0 / capacity));
            if (bar != null) {
                bar.setValue(pct);
                bar.setString(pct + "% occupied");
            }
            if (hint != null)
                hint.setText(capacity == 0 ? "Add rooms to calculate occupancy."
                        : (capacity - occupied) + " bed(s) currently available.");

            JTable recent = findTableOnDashboard();
            if (recent != null) {
                DefaultTableModel model = (DefaultTableModel) recent.getModel();
                model.setRowCount(0);
                String sql = "SELECT student_id,name,register_number,department,phone FROM students ORDER BY student_id DESC LIMIT 5";
                try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    while (rs.next())
                        model.addRow(new Object[] { rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                rs.getString(5) });
                }
            }
        } catch (SQLException ex) {
            showError("Could not load dashboard data. Check your MySQL connection and table names.", ex);
        }
    }

    private void setStat(String label, String value) {
        JLabel target = findLabel(dashboardPage, "stat:" + label);
        if (target != null)
            target.setText(value);
    }

    private JLabel findLabel(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel l && name.equals(l.getName()))
                return l;
            if (c instanceof Container child) {
                JLabel found = findLabel(child, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private JProgressBar findProgress(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JProgressBar p && name.equals(p.getName()))
                return p;
            if (c instanceof Container child) {
                JProgressBar found = findProgress(child, name);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private JTable findTableOnDashboard() {
        return findTable(dashboardPage);
    }

    private JTable findTable(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTable t)
                return t;
            if (c instanceof Container child) {
                JTable found = findTable(child);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    private int scalar(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void loadModuleTable(String key) {
        JPanel page = findModulePage(key);
        if (page == null)
            return;
        JTable table = (JTable) page.getClientProperty("table");
        DefaultTableModel model = (DefaultTableModel) page.getClientProperty("model");
        ModuleConfig cfg = modules.get(key);
        if (table == null || model == null)
            return;
        model.setRowCount(0);
        String columns = String.join(",", cfg.columns);
        String sql = "SELECT " + columns + " FROM " + cfg.table + " ORDER BY " + cfg.idColumn + " DESC";
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = new Object[cfg.columns.length];
                for (int i = 0; i < cfg.columns.length; i++)
                    row[i] = rs.getObject(i + 1);
                model.addRow(row);
            }
        } catch (SQLException ex) {
            showError("Could not load " + key + ". Verify that the existing table columns match the project schema.",
                    ex);
        }
    }

    private JPanel findModulePage(String key) {
        for (Component c : pageContainer.getComponents()) {
            if (c instanceof JPanel p && key.equals(p.getName()))
                return p;
        }
        // Cards are named below by locating their title label isn't reliable; use card
        // order and configured mapping.
        int index = new ArrayList<>(modules.keySet()).indexOf(key);
        Component[] cards = pageContainer.getComponents();
        // Dashboard is first; module pages follow in insertion order.
        if (index >= 0 && index + 1 < cards.length && cards[index + 1] instanceof JPanel p)
            return p;
        return null;
    }

    private void filterTable(DefaultTableModel model, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            if (modules.containsKey(activePage))
                loadModuleTable(activePage);
            return;
        }
        // Keep filtering local to the currently loaded data, without changing database
        // rows.
        List<Object[]> allRows = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            Object[] row = new Object[model.getColumnCount()];
            for (int c = 0; c < model.getColumnCount(); c++)
                row[c] = model.getValueAt(r, c);
            allRows.add(row);
        }
        model.setRowCount(0);
        for (Object[] row : allRows) {
            StringBuilder text = new StringBuilder();
            for (Object value : row)
                text.append(value == null ? "" : value.toString()).append(" ");
            if (text.toString().toLowerCase(Locale.ROOT).contains(q))
                model.addRow(row);
        }
    }

    private void openAddDialog(String key) {
        ModuleConfig cfg = modules.get(key);
        if (cfg == null)
            return;
        JTextField[] fields = new JTextField[cfg.formLabels.length];
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(10, 8, 8, 8));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        for (int i = 0; i < cfg.formLabels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            form.add(new JLabel(cfg.formLabels[i] + ":"), gbc);
            fields[i] = new JTextField(22);
            fields[i].setFont(BODY);
            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(fields[i], gbc);
            gbc.weightx = 0;
        }
        if (key.equals("Room Allocation")) {
            fields[2].setText(LocalDate.now().toString());
            fields[3].setText("Active");
        } else if (key.equals("Fee Management")) {
            fields[5].setText(LocalDate.now().toString());
            fields[6].setText("Pending");
        } else if (key.equals("Complaints")) {
            fields[3].setText(LocalDate.now().toString());
            fields[4].setText("Pending");
        }
        int result = JOptionPane.showConfirmDialog(this, form, "Add " + singular(key),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION)
            return;

        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++)
            values[i] = fields[i].getText().trim();
        try {
            validateForm(key, values);
            insertRecord(cfg, values);
            loadModuleTable(key);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, singular(key) + " added successfully.", "DORMIFY",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Could not add record. Check required fields, IDs, and dates.", ex);
        }
    }

    private String singular(String key) {
        return switch (key) {
            case "Students" -> "student";
            case "Rooms" -> "room";
            case "Room Allocation" -> "room allocation";
            case "Fee Management" -> "fee record";
            default -> "complaint";
        };
    }

    private void validateForm(String key, Object[] v) {
        for (int i = 0; i < v.length; i++) {
            if (v[i] == null || v[i].toString().isBlank()) {
                if (!(key.equals("Fee Management") && i == 5)) {
                    throw new IllegalArgumentException("Please fill in: " + modules.get(key).formLabels[i]);
                }
            }
        }
        if (key.equals("Rooms")) {
            int cap = Integer.parseInt(v[1].toString());
            if (cap < 1)
                throw new IllegalArgumentException("Room capacity must be at least 1.");
        }
        if (key.equals("Room Allocation")) {
            LocalDate.parse(v[2].toString());
            if (!v[3].toString().equalsIgnoreCase("Active") && !v[3].toString().equalsIgnoreCase("Ended"))
                throw new IllegalArgumentException("Status must be Active or Ended.");
        }
        if (key.equals("Fee Management")) {
            Integer.parseInt(v[2].toString());
            double due = Double.parseDouble(v[3].toString());
            double paid = Double.parseDouble(v[4].toString());
            if (due < 0 || paid < 0 || paid > due)
                throw new IllegalArgumentException("Amounts must be valid and paid amount cannot exceed amount due.");
            if (!v[5].toString().isBlank())
                LocalDate.parse(v[5].toString());
        }
        if (key.equals("Complaints")) {
            LocalDate.parse(v[3].toString());
            if (v[4].toString().isBlank())
                v[4] = "Pending";
        }
    }

    private void insertRecord(ModuleConfig cfg, Object[] values) throws SQLException {
        String[] insertCols = cfg.formColumns;
        String placeholders = String.join(",", Collections.nCopies(insertCols.length, "?"));
        String sql = "INSERT INTO " + cfg.table + " (" + String.join(",", insertCols) + ") VALUES (" + placeholders
                + ")";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindValues(ps, cfg.table, values);
            ps.executeUpdate();
        }
    }

    private void bindValues(PreparedStatement ps, String table, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            String value = values[i] == null ? "" : values[i].toString().trim();
            if (table.equals("rooms") && i == 1)
                ps.setInt(i + 1, Integer.parseInt(value));
            else if (table.equals("allocations") && (i == 0 || i == 1))
                ps.setInt(i + 1, Integer.parseInt(value));
            else if (table.equals("payments") && (i == 0 || i == 2))
                ps.setInt(i + 1, Integer.parseInt(value));
            else if (table.equals("payments") && (i == 3 || i == 4))
                ps.setBigDecimal(i + 1, new java.math.BigDecimal(value));
            else if (table.equals("complaints") && i == 0)
                ps.setInt(i + 1, Integer.parseInt(value));
            else if ((table.equals("allocations") && i == 2) || (table.equals("payments") && i == 5)
                    || (table.equals("complaints") && i == 3)) {
                if (value.isBlank())
                    ps.setNull(i + 1, Types.DATE);
                else
                    ps.setDate(i + 1, java.sql.Date.valueOf(LocalDate.parse(value)));
            } else
                ps.setString(i + 1, value);
        }
    }

    private void editSelected(String key, JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "DORMIFY", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ModuleConfig cfg = modules.get(key);
        int row = table.convertRowIndexToModel(viewRow);
        Object id = table.getModel().getValueAt(row, 0);
        Object[] existing = new Object[cfg.formColumns.length];
        for (int i = 0; i < cfg.formColumns.length; i++)
            existing[i] = table.getModel().getValueAt(row, i + 1);
        JTextField[] fields = new JTextField[cfg.formLabels.length];
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            form.add(new JLabel(cfg.formLabels[i] + ":"), gbc);
            fields[i] = new JTextField(existing[i] == null ? "" : existing[i].toString(), 22);
            fields[i].setFont(BODY);
            gbc.gridx = 1;
            gbc.weightx = 1;
            form.add(fields[i], gbc);
            gbc.weightx = 0;
        }
        int result = JOptionPane.showConfirmDialog(this, form, "Edit " + singular(key),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION)
            return;
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++)
            values[i] = fields[i].getText().trim();
        try {
            validateForm(key, values);
            String set = String.join(",", Arrays.stream(cfg.formColumns).map(col -> col + "=?").toArray(String[]::new));
            String sql = "UPDATE " + cfg.table + " SET " + set + " WHERE " + cfg.idColumn + "=?";
            try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                bindValues(ps, cfg.table, values);
                ps.setInt(values.length + 1, Integer.parseInt(id.toString()));
                ps.executeUpdate();
            }
            loadModuleTable(key);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Record updated successfully.", "DORMIFY",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError("Could not update record.", ex);
        }
    }

    private void deleteSelected(String key, JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "DORMIFY", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        Object id = table.getModel().getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this record (ID " + id + ")? This cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        ModuleConfig cfg = modules.get(key);
        try (Connection c = getConnection();
                PreparedStatement ps = c
                        .prepareStatement("DELETE FROM " + cfg.table + " WHERE " + cfg.idColumn + "=?")) {
            ps.setInt(1, Integer.parseInt(id.toString()));
            ps.executeUpdate();
            loadModuleTable(key);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Record deleted.", "DORMIFY", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError(
                    "Could not delete this record. It may be linked to another record (for example, an allocated student). Remove dependent records first.",
                    ex);
        }
    }

    private String generateReport() {
        StringBuilder out = new StringBuilder();
        out.append("DORMIFY — HOSTEL MANAGEMENT REPORT\n");
        out.append("Generated: ").append(LocalDate.now()).append("\n");
        out.append("========================================\n\n");
        try (Connection c = getConnection()) {
            out.append("STUDENTS\n");
            out.append("Total students: ").append(scalar(c, "SELECT COUNT(*) FROM students")).append("\n");
            out.append("ROOMS\n");
            out.append("Total rooms: ").append(scalar(c, "SELECT COUNT(*) FROM rooms")).append("\n");
            out.append("Total bed capacity: ").append(scalar(c, "SELECT COALESCE(SUM(capacity),0) FROM rooms"))
                    .append("\n");
            out.append("Active allocations: ")
                    .append(scalar(c, "SELECT COUNT(*) FROM allocations WHERE status='Active'")).append("\n");
            out.append("FEES\n");
            try (Statement st = c.createStatement();
                    ResultSet rs = st.executeQuery(
                            "SELECT COALESCE(SUM(amount_due),0), COALESCE(SUM(amount_paid),0) FROM payments")) {
                if (rs.next()) {
                    out.append(String.format(Locale.US, "Total amount due: %.2f%n", rs.getDouble(1)));
                    out.append(String.format(Locale.US, "Total amount paid: %.2f%n", rs.getDouble(2)));
                    out.append(
                            String.format(Locale.US, "Outstanding balance: %.2f%n", rs.getDouble(1) - rs.getDouble(2)));
                }
            }
            out.append("\nCOMPLAINTS\n");
            out.append("Pending complaints: ")
                    .append(scalar(c, "SELECT COUNT(*) FROM complaints WHERE status='Pending'")).append("\n");
            out.append("Resolved complaints: ")
                    .append(scalar(c, "SELECT COUNT(*) FROM complaints WHERE status='Resolved'")).append("\n");
        } catch (SQLException ex) {
            out.append("\nERROR: ").append(ex.getMessage());
        }
        return out.toString();
    }

    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, message + "\n\nDetails: " + ex.getMessage(),
                "DORMIFY - Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class ModuleConfig {
        final String table, title, description, idColumn;
        final String[] columns, headers, formColumns, formLabels;

        ModuleConfig(String table, String title, String description, String[] columns, String[] headers,
                String[] formColumns, String[] formLabels, String idColumn) {
            this.table = table;
            this.title = title;
            this.description = description;
            this.columns = columns;
            this.headers = headers;
            this.formColumns = formColumns;
            this.formLabels = formLabels;
            this.idColumn = idColumn;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            if (DB_PASSWORD.equals("YOUR_MYSQL_PASSWORD")) {
                JOptionPane.showMessageDialog(null,
                        "Please open Main.java and replace YOUR_MYSQL_PASSWORD with your MySQL root password.",
                        "DORMIFY setup", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection ignored = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    // Connection check passed.
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to MySQL.\nCheck DB_URL, DB_USER, DB_PASSWORD and the JDBC connector.\n\n"
                                + ex.getMessage(),
                        "Database connection error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Main().setVisible(true);
        });
    }
}
