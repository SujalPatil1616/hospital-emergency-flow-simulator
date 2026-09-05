import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.*;

public class HospitalEmergencySimulator extends JFrame {

    // ===================== COLORS =====================

    private static final Color BG = new Color(15, 20, 27);
    private static final Color SIDEBAR = new Color(11, 16, 22);
    private static final Color PANEL = new Color(27, 34, 44);
    private static final Color CARD = new Color(34, 42, 54);

    private static final Color TEXT = new Color(240, 243, 247);
    private static final Color MUTED = new Color(160, 170, 184);

    private static final Color GREEN = new Color(42, 165, 128);
    private static final Color BLUE = new Color(55, 125, 200);
    private static final Color DISABLED = new Color(75, 82, 94);

    private static final Color TABLE_HEADER =
            new Color(55, 66, 82);

    private static final Color TABLE_BG =
            new Color(22, 28, 37);

    private static final Color BORDER =
            new Color(58, 68, 82);

    private static final Font NORMAL =
            new Font("Segoe UI", Font.PLAIN, 14);

    private static final Font BOLD =
            new Font("Segoe UI", Font.BOLD, 14);

    private static final Font TITLE =
            new Font("Segoe UI", Font.BOLD, 26);


    // ===================== PATIENT =====================

    static class Patient {

        String id;
        String name;
        int age;
        String condition;
        String emergency;
        int priority;

        String doctor = "-";
        String status = "Waiting";

        long arrivalTime;

        ArrayList<String> history =
                new ArrayList<>();

        Patient(
                String id,
                String name,
                int age,
                String condition,
                String emergency,
                int priority) {

            this.id = id;
            this.name = name;
            this.age = age;
            this.condition = condition;
            this.emergency = emergency;
            this.priority = priority;

            arrivalTime =
                    System.currentTimeMillis();

            history.add("Patient registered");
        }
    }


    // ===================== PATIENT LOOKUP =====================

    static class PatientHashTable {

        static class Node {

            Patient patient;
            Node next;

            Node(Patient patient) {
                this.patient = patient;
            }
        }

        Node[] table = new Node[31];

        int hash(String id) {

            return Math.abs(
                    id.toUpperCase().hashCode()
            ) % table.length;
        }

        void put(Patient patient) {

            int index = hash(patient.id);

            Node current = table[index];

            while (current != null) {

                if (current.patient.id
                        .equalsIgnoreCase(patient.id)) {

                    current.patient = patient;
                    return;
                }

                current = current.next;
            }

            Node node = new Node(patient);

            node.next = table[index];

            table[index] = node;
        }

        Patient get(String id) {

            int index = hash(id);

            Node current = table[index];

            while (current != null) {

                if (current.patient.id
                        .equalsIgnoreCase(id)) {

                    return current.patient;
                }

                current = current.next;
            }

            return null;
        }

        ArrayList<Patient> getAll() {

            ArrayList<Patient> result =
                    new ArrayList<>();

            for (Node node : table) {

                Node current = node;

                while (current != null) {

                    result.add(current.patient);

                    current = current.next;
                }
            }

            return result;
        }
    }


    // ===================== EMERGENCY PRIORITY QUEUE =====================

    static class EmergencyHeap {

        ArrayList<Patient> heap =
                new ArrayList<>();

        boolean higher(Patient a, Patient b) {

            if (a.priority != b.priority) {

                return a.priority > b.priority;
            }

            return a.arrivalTime < b.arrivalTime;
        }

        void add(Patient patient) {

            heap.add(patient);

            int index = heap.size() - 1;

            while (index > 0) {

                int parent = (index - 1) / 2;

                if (!higher(
                        heap.get(index),
                        heap.get(parent))) {

                    break;
                }

                swap(index, parent);

                index = parent;
            }
        }

        Patient remove() {

            if (heap.isEmpty()) {
                return null;
            }

            Patient result = heap.get(0);

            Patient last =
                    heap.remove(heap.size() - 1);

            if (!heap.isEmpty()) {

                heap.set(0, last);

                int index = 0;

                while (true) {

                    int left =
                            index * 2 + 1;

                    int right =
                            index * 2 + 2;

                    int best = index;

                    if (left < heap.size()
                            && higher(
                            heap.get(left),
                            heap.get(best))) {

                        best = left;
                    }

                    if (right < heap.size()
                            && higher(
                            heap.get(right),
                            heap.get(best))) {

                        best = right;
                    }

                    if (best == index) {
                        break;
                    }

                    swap(index, best);

                    index = best;
                }
            }

            return result;
        }

        void swap(int a, int b) {

            Patient temp = heap.get(a);

            heap.set(a, heap.get(b));

            heap.set(b, temp);
        }

        int size() {
            return heap.size();
        }
    }


    // ===================== NORMAL QUEUE =====================

    static class WaitingQueue {

        LinkedList<Patient> queue =
                new LinkedList<>();

        void add(Patient patient) {
            queue.addLast(patient);
        }

        Patient remove() {

            if (queue.isEmpty()) {
                return null;
            }

            return queue.removeFirst();
        }

        int size() {
            return queue.size();
        }
    }


    // ===================== ACTIVITY HISTORY =====================

    static class ActivityStack {

        Stack<String> stack =
                new Stack<>();

        void push(String action) {
            stack.push(action);
        }

        ArrayList<String> getAll() {

            ArrayList<String> result =
                    new ArrayList<>();

            for (int i = stack.size() - 1;
                 i >= 0;
                 i--) {

                result.add(stack.get(i));
            }

            return result;
        }
    }


    // ===================== HOSPITAL ROUTE =====================

    static class HospitalGraph {

        static class Edge {

            String to;
            int distance;

            Edge(String to, int distance) {

                this.to = to;
                this.distance = distance;
            }
        }

        HashMap<String, ArrayList<Edge>> graph =
                new HashMap<>();

        void addDepartment(String name) {

            graph.putIfAbsent(
                    name,
                    new ArrayList<>()
            );
        }

        void addEdge(
                String a,
                String b,
                int distance) {

            graph.get(a).add(
                    new Edge(b, distance)
            );

            graph.get(b).add(
                    new Edge(a, distance)
            );
        }

        ArrayList<String> shortestPath(
                String start,
                String end) {

            HashMap<String, Integer> distance =
                    new HashMap<>();

            HashMap<String, String> previous =
                    new HashMap<>();

            for (String node : graph.keySet()) {

                distance.put(
                        node,
                        Integer.MAX_VALUE
                );
            }

            distance.put(start, 0);

            PriorityQueue<String> queue =
                    new PriorityQueue<>(
                            Comparator.comparingInt(
                                    distance::get
                            )
                    );

            queue.add(start);

            while (!queue.isEmpty()) {

                String current =
                        queue.poll();

                for (Edge edge :
                        graph.get(current)) {

                    int newDistance =
                            distance.get(current)
                                    + edge.distance;

                    if (newDistance <
                            distance.get(edge.to)) {

                        distance.put(
                                edge.to,
                                newDistance
                        );

                        previous.put(
                                edge.to,
                                current
                        );

                        queue.remove(edge.to);

                        queue.add(edge.to);
                    }
                }
            }

            ArrayList<String> path =
                    new ArrayList<>();

            if (distance.get(end)
                    == Integer.MAX_VALUE) {

                return path;
            }

            String current = end;

            while (current != null) {

                path.add(current);

                current = previous.get(current);
            }

            Collections.reverse(path);

            return path;
        }
    }


    // ===================== DATA =====================

    private final PatientHashTable patients =
            new PatientHashTable();

    private final EmergencyHeap emergencyQueue =
            new EmergencyHeap();

    private final WaitingQueue normalQueue =
            new WaitingQueue();

    private final ActivityStack activity =
            new ActivityStack();

    private final HospitalGraph hospital =
            new HospitalGraph();

    private final ArrayList<Patient> treated =
            new ArrayList<>();

    private final ArrayList<Patient> discharged =
            new ArrayList<>();

    private Patient currentPatient;

    private int totalPatients = 0;
    private int totalTreated = 0;
    private int totalDischarged = 0;


    // ===================== GUI =====================

    private JPanel contentPanel;

    private JLabel totalLabel;
    private JLabel waitingLabel;
    private JLabel emergencyLabel;
    private JLabel treatedLabel;

    private JLabel currentName;
    private JLabel currentCondition;
    private JLabel currentEmergency;
    private JLabel currentDoctor;
    private JLabel currentStatus;

    private JButton dischargeButton;

    private DefaultTableModel patientModel;
    private JTable patientTable;


    // ===================== CONSTRUCTOR =====================

    public HospitalEmergencySimulator() {

        setTitle(
                "Hospital Emergency Flow Simulator"
        );

        setSize(
                1200,
                720
        );

        setMinimumSize(
                new Dimension(
                        1050,
                        650
                )
        );

        setLocationRelativeTo(null);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        buildHospital();

        loadSamplePatients();

        buildApplication();
    }


    // ===================== HOSPITAL =====================

    private void buildHospital() {

        String[] departments = {

                "Emergency",
                "Triage",
                "Radiology",
                "Laboratory",
                "ICU",
                "Pharmacy",
                "Recovery"
        };

        for (String department : departments) {

            hospital.addDepartment(department);
        }

        hospital.addEdge(
                "Emergency",
                "Triage",
                2
        );

        hospital.addEdge(
                "Triage",
                "Radiology",
                4
        );

        hospital.addEdge(
                "Triage",
                "Laboratory",
                3
        );

        hospital.addEdge(
                "Triage",
                "ICU",
                6
        );

        hospital.addEdge(
                "Radiology",
                "Recovery",
                5
        );

        hospital.addEdge(
                "Laboratory",
                "Pharmacy",
                3
        );

        hospital.addEdge(
                "Pharmacy",
                "Recovery",
                2
        );

        hospital.addEdge(
                "ICU",
                "Recovery",
                4
        );
    }


    // ===================== SAMPLE PATIENTS =====================

    private void loadSamplePatients() {

        addPatient(
                "P101",
                "Aarav Sharma",
                45,
                "Fever",
                "LOW"
        );

        addPatient(
                "P102",
                "Neha Patil",
                31,
                "Fracture",
                "HIGH"
        );

        addPatient(
                "P103",
                "Rahul Deshmukh",
                58,
                "Chest Pain",
                "CRITICAL"
        );

        addPatient(
                "P104",
                "Priya Joshi",
                27,
                "Headache",
                "LOW"
        );

        addPatient(
                "P105",
                "Vikram Singh",
                64,
                "Breathing Difficulty",
                "CRITICAL"
        );

        addPatient(
                "P106",
                "Ananya Kulkarni",
                39,
                "Severe Injury",
                "HIGH"
        );
    }


    // ===================== ADD PATIENT =====================

    private void addPatient(
            String id,
            String name,
            int age,
            String condition,
            String emergency) {

        int priority =
                getPriority(emergency);

        Patient patient =
                new Patient(
                        id,
                        name,
                        age,
                        condition,
                        emergency,
                        priority
                );

        patients.put(patient);

        if (priority >= 2) {

            emergencyQueue.add(patient);

        } else {

            normalQueue.add(patient);
        }

        totalPatients++;

        activity.push(
                "Registered "
                        + id
                        + " - "
                        + emergency
        );
    }


    private int getPriority(String emergency) {

        if (emergency.equals("CRITICAL")) {
            return 3;
        }

        if (emergency.equals("HIGH")) {
            return 2;
        }

        return 1;
    }


    // ===================== APPLICATION =====================

    private void buildApplication() {

        JPanel root =
                new JPanel(
                        new BorderLayout()
                );

        root.setBackground(BG);

        root.add(
                createSidebar(),
                BorderLayout.WEST
        );

        contentPanel =
                new JPanel(
                        new BorderLayout()
                );

        contentPanel.setBackground(BG);

        root.add(
                contentPanel,
                BorderLayout.CENTER
        );

        setContentPane(root);

        showOverview();
    }


    // ===================== SIDEBAR =====================

    private JPanel createSidebar() {

        JPanel sidebar =
                new JPanel();

        sidebar.setPreferredSize(
                new Dimension(
                        205,
                        0
                )
        );

        sidebar.setBackground(SIDEBAR);

        sidebar.setLayout(
                new BoxLayout(
                        sidebar,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel logo =
                new JLabel(
                        "<html><b>✚ CAREPOINT</b>"
                                + "<br>"
                                + "<span style='font-size:10px;'>"
                                + "Emergency Services"
                                + "</span></html>"
                );

        logo.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        logo.setForeground(TEXT);

        logo.setBorder(
                new EmptyBorder(
                        28,
                        22,
                        28,
                        10
                )
        );

        sidebar.add(logo);

        sidebar.add(
                menuButton(
                        "Overview",
                        e -> showOverview()
                )
        );

        sidebar.add(
                menuButton(
                        "Patients",
                        e -> showPatients()
                )
        );

        sidebar.add(
                menuButton(
                        "Triage",
                        e -> showRegister()
                )
        );

        sidebar.add(
                menuButton(
                        "Treatment",
                        e -> showTreatment()
                )
        );

        sidebar.add(
                menuButton(
                        "Departments",
                        e -> showDepartments()
                )
        );

        sidebar.add(
                menuButton(
                        "Reports",
                        e -> showReports()
                )
        );

        sidebar.add(
                menuButton(
                        "Find Patient",
                        e -> searchPatient()
                )
        );

        sidebar.add(
                Box.createVerticalGlue()
        );

        JLabel online =
                new JLabel(
                        "<html><b>● SYSTEM ONLINE</b>"
                                + "<br><br>"
                                + "Emergency Department"
                                + "</html>"
                );

        online.setForeground(
                new Color(
                        110,
                        190,
                        160
                )
        );

        online.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        online.setBorder(
                new EmptyBorder(
                        15,
                        22,
                        22,
                        10
                )
        );

        sidebar.add(online);

        return sidebar;
    }


    private JButton menuButton(
            String text,
            java.awt.event.ActionListener listener) {

        JButton button =
                new JButton(text);

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setForeground(
                new Color(
                        205,
                        213,
                        223
                )
        );

        button.setBackground(SIDEBAR);

        button.setFont(NORMAL);

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setOpaque(true);

        button.setBorder(
                new EmptyBorder(
                        10,
                        24,
                        10,
                        10
                )
        );

        button.addActionListener(listener);

        return button;
    }


    // ===================== OVERVIEW =====================

    private void showOverview() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Emergency Department",
                        "Patient flow, treatment status and department activity"
                );

        JPanel cards =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                12,
                                0
                        )
                );

        cards.setOpaque(false);

        totalLabel = valueLabel();
        waitingLabel = valueLabel();
        emergencyLabel = valueLabel();
        treatedLabel = valueLabel();

        cards.add(
                statCard(
                        "Registered Patients",
                        totalLabel
                )
        );

        cards.add(
                statCard(
                        "Currently Waiting",
                        waitingLabel
                )
        );

        cards.add(
                statCard(
                        "Urgent Cases",
                        emergencyLabel
                )
        );

        cards.add(
                statCard(
                        "Treated Today",
                        treatedLabel
                )
        );

        JPanel body =
                new JPanel(
                        new BorderLayout(
                                14,
                                0
                        )
                );

        body.setOpaque(false);

        body.add(
                createPatientTablePanel(),
                BorderLayout.CENTER
        );

        body.add(
                createTreatmentPanel(),
                BorderLayout.EAST
        );

        JPanel center =
                new JPanel(
                        new BorderLayout(
                                0,
                                14
                        )
                );

        center.setOpaque(false);

        center.add(
                cards,
                BorderLayout.NORTH
        );

        center.add(
                body,
                BorderLayout.CENTER
        );

        page.add(
                center,
                BorderLayout.CENTER
        );

        contentPanel.add(page);

        refreshAll();

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    // ===================== PAGE =====================

    private JPanel createPage(
            String title,
            String subtitle) {

        JPanel page =
                new JPanel(
                        new BorderLayout(
                                0,
                                18
                        )
                );

        page.setBackground(BG);

        page.setBorder(
                new EmptyBorder(
                        25,
                        25,
                        25,
                        25
                )
        );

        JPanel header =
                new JPanel(
                        new GridLayout(
                                2,
                                1
                        )
                );

        header.setOpaque(false);

        JLabel titleLabel =
                new JLabel(title);

        titleLabel.setFont(TITLE);

        titleLabel.setForeground(TEXT);

        JLabel subtitleLabel =
                new JLabel(subtitle);

        subtitleLabel.setForeground(MUTED);

        subtitleLabel.setFont(NORMAL);

        header.add(titleLabel);
        header.add(subtitleLabel);

        page.add(
                header,
                BorderLayout.NORTH
        );

        return page;
    }


    // ===================== STAT CARD =====================

    private JPanel statCard(
            String title,
            JLabel value) {

        JPanel card =
                new JPanel(
                        new BorderLayout(
                                0,
                                7
                        )
                );

        card.setBackground(CARD);

        card.setBorder(
                new EmptyBorder(
                        15,
                        18,
                        15,
                        18
                )
        );

        JLabel label =
                new JLabel(title);

        label.setForeground(MUTED);

        label.setFont(NORMAL);

        card.add(
                label,
                BorderLayout.NORTH
        );

        card.add(
                value,
                BorderLayout.CENTER
        );

        return card;
    }


    private JLabel valueLabel() {

        JLabel label =
                new JLabel("0");

        label.setForeground(TEXT);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        27
                )
        );

        return label;
    }


    // ===================== PATIENT TABLE =====================

    private JPanel createPatientTablePanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        panel.setBackground(PANEL);

        panel.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JLabel title =
                new JLabel(
                        "Current Patient Flow"
                );

        title.setFont(BOLD);

        title.setForeground(TEXT);

        panel.add(
                title,
                BorderLayout.NORTH
        );

        String[] columns = {

                "ID",
                "Patient",
                "Age",
                "Condition",
                "Emergency",
                "Doctor",
                "Status"
        };

        patientModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        patientTable =
                new JTable(patientModel);

        patientTable.setRowHeight(30);

        patientTable.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        patientTable.setBackground(TABLE_BG);

        patientTable.setForeground(TEXT);

        patientTable.setGridColor(BORDER);

        patientTable.setSelectionBackground(
                new Color(
                        47,
                        105,
                        128
                )
        );

        patientTable.setSelectionForeground(
                Color.WHITE
        );

        styleTableHeader(patientTable);

        int[] widths = {

                55,
                125,
                45,
                135,
                90,
                110,
                85
        };

        for (int i = 0;
             i < widths.length;
             i++) {

            patientTable
                    .getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(
                            widths[i]
                    );
        }

        JScrollPane scroll =
                new JScrollPane(
                        patientTable
                );

        scroll.getViewport()
                .setBackground(TABLE_BG);

        panel.add(
                scroll,
                BorderLayout.CENTER
        );

        return panel;
    }


    // ===================== TABLE HEADER =====================

    private void styleTableHeader(
            JTable table) {

        JTableHeader header =
                table.getTableHeader();

        header.setOpaque(true);

        header.setBackground(
                TABLE_HEADER
        );

        header.setForeground(
                Color.WHITE
        );

        header.setFont(BOLD);

        header.setPreferredSize(
                new Dimension(
                        0,
                        36
                )
        );

        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setOpaque(true);

        renderer.setBackground(
                TABLE_HEADER
        );

        renderer.setForeground(
                Color.WHITE
        );

        renderer.setFont(BOLD);

        renderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        for (int i = 0;
             i < table.getColumnCount();
             i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setHeaderRenderer(
                            renderer
                    );
        }
    }


    // ===================== CURRENT TREATMENT =====================

    private JPanel createTreatmentPanel() {

        JPanel panel =
                new JPanel();

        panel.setPreferredSize(
                new Dimension(
                        255,
                        0
                )
        );

        panel.setBackground(PANEL);

        panel.setBorder(
                new EmptyBorder(
                        17,
                        17,
                        17,
                        17
                )
        );

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title =
                new JLabel(
                        "Current Treatment"
                );

        title.setFont(BOLD);

        title.setForeground(TEXT);

        title.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(title);

        panel.add(
                Box.createVerticalStrut(18)
        );

        currentName =
                detailLabel(
                        "No patient selected",
                        20
                );

        currentCondition =
                detailLabel(
                        "No active treatment",
                        13
                );

        currentEmergency =
                detailLabel(
                        "",
                        13
                );

        currentDoctor =
                detailLabel(
                        "",
                        13
                );

        currentStatus =
                detailLabel(
                        "",
                        13
                );

        panel.add(currentName);

        panel.add(
                Box.createVerticalStrut(10)
        );

        panel.add(currentCondition);
        panel.add(currentEmergency);
        panel.add(currentDoctor);
        panel.add(currentStatus);

        panel.add(
                Box.createVerticalGlue()
        );

        dischargeButton =
                new JButton(
                        "Complete Treatment"
                );

        dischargeButton.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        dischargeButton.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        44
                )
        );

        dischargeButton.setPreferredSize(
                new Dimension(
                        220,
                        44
                )
        );

        dischargeButton.setFont(BOLD);

        dischargeButton.setFocusPainted(false);

        dischargeButton.setBorderPainted(false);

        dischargeButton.setOpaque(true);

        dischargeButton.addActionListener(
                e -> dischargePatient()
        );

        panel.add(dischargeButton);

        updateTreatmentPanel();

        return panel;
    }


    private JLabel detailLabel(
            String text,
            int size) {

        JLabel label =
                new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        size
                )
        );

        label.setForeground(TEXT);

        label.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        label.setBorder(
                new EmptyBorder(
                        5,
                        0,
                        5,
                        0
                )
        );

        return label;
    }


    // ===================== UPDATE TREATMENT PANEL =====================

    private void updateTreatmentPanel() {

        if (currentName == null) {
            return;
        }

        if (currentPatient == null) {

            currentName.setText(
                    "No patient selected"
            );

            currentCondition.setText(
                    "No active treatment"
            );

            currentEmergency.setText("");

            currentDoctor.setText("");

            currentStatus.setText("");

            dischargeButton.setEnabled(false);

            dischargeButton.setBackground(
                    DISABLED
            );

            dischargeButton.setForeground(
                    new Color(
                            175,
                            180,
                            188
                    )
            );

        } else {

            currentName.setText(
                    currentPatient.name
            );

            currentCondition.setText(
                    "Condition: "
                            + currentPatient.condition
            );

            currentEmergency.setText(
                    "Emergency: "
                            + currentPatient.emergency
            );

            currentDoctor.setText(
                    "Doctor: "
                            + currentPatient.doctor
            );

            currentStatus.setText(
                    "Status: "
                            + currentPatient.status
            );

            dischargeButton.setEnabled(true);

            dischargeButton.setBackground(
                    GREEN
            );

            dischargeButton.setForeground(
                    Color.WHITE
            );
        }
    }


    // ===================== TREAT NEXT PATIENT =====================

    private void treatNextPatient() {

        if (currentPatient != null
                && currentPatient.status
                .equals("In Treatment")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete the current treatment before treating another patient.",
                    "Treatment in Progress",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Patient next =
                emergencyQueue.remove();

        if (next == null) {

            next =
                    normalQueue.remove();
        }

        if (next == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "There are no patients waiting.",
                    "No Waiting Patients",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        currentPatient = next;

        currentPatient.status =
                "In Treatment";

        currentPatient.doctor =
                assignDoctor(next);

        currentPatient.history.add(
                "Treatment started"
        );

        currentPatient.history.add(
                "Assigned to "
                        + currentPatient.doctor
        );

        treated.add(currentPatient);

        totalTreated++;

        activity.push(
                "Treatment started: "
                        + currentPatient.id
        );

        refreshAll();

        JOptionPane.showMessageDialog(
                this,
                currentPatient.name
                        + " is now receiving treatment from "
                        + currentPatient.doctor
                        + ".",
                "Treatment Started",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    private String assignDoctor(
            Patient patient) {

        if (patient.priority == 3) {
            return "Dr. Sharma";
        }

        if (patient.priority == 2) {
            return "Dr. Mehta";
        }

        return "Dr. Joshi";
    }


    // ===================== COMPLETE TREATMENT =====================

    private void dischargePatient() {

        if (currentPatient == null) {
            return;
        }

        String patientName =
                currentPatient.name;

        currentPatient.status =
                "Discharged";

        currentPatient.history.add(
                "Treatment completed"
        );

        currentPatient.history.add(
                "Patient discharged"
        );

        discharged.add(currentPatient);

        totalDischarged++;

        activity.push(
                "Discharged: "
                        + currentPatient.id
        );

        currentPatient = null;

        refreshAll();

        JOptionPane.showMessageDialog(
                this,
                patientName
                        + " has completed treatment and was discharged.",
                "Treatment Completed",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // ===================== PATIENTS SCREEN =====================

    private void showPatients() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Patients",
                        "View patients registered in the emergency department"
                );

        JPanel body =
                createPatientTablePanel();

        page.add(
                body,
                BorderLayout.CENTER
        );

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                0
                        )
                );

        buttons.setOpaque(false);

        JButton register =
                actionButton(
                        "Register Patient",
                        GREEN
                );

        register.addActionListener(
                e -> showRegister()
        );

        JButton treat =
                actionButton(
                        "Treat Next Patient",
                        BLUE
                );

        treat.addActionListener(
                e -> {

                    treatNextPatient();

                    showPatients();
                }
        );

        JButton search =
                actionButton(
                        "Find Patient",
                        new Color(
                                85,
                                94,
                                108
                        )
                );

        search.addActionListener(
                e -> searchPatient()
        );

        buttons.add(register);
        buttons.add(treat);
        buttons.add(search);

        page.add(
                buttons,
                BorderLayout.SOUTH
        );

        contentPanel.add(page);

        refreshAll();

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    // ===================== TRIAGE =====================

    private void showRegister() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Triage",
                        "Register a patient and assign an emergency level"
                );

        JPanel form =
                new JPanel(
                        new GridBagLayout()
                );

        form.setBackground(PANEL);

        form.setBorder(
                new EmptyBorder(
                        25,
                        35,
                        25,
                        35
                )
        );

        GridBagConstraints c =
                new GridBagConstraints();

        c.insets =
                new Insets(
                        9,
                        9,
                        9,
                        9
                );

        c.fill =
                GridBagConstraints.HORIZONTAL;

        c.weightx = 1;

        JTextField id =
                new JTextField();

        JTextField name =
                new JTextField();

        JTextField age =
                new JTextField();

        JTextField condition =
                new JTextField();

        JComboBox<String> emergency =
                new JComboBox<>(
                        new String[]{
                                "LOW",
                                "HIGH",
                                "CRITICAL"
                        }
                );

        addFormRow(
                form,
                c,
                0,
                "Patient ID",
                id
        );

        addFormRow(
                form,
                c,
                1,
                "Patient Name",
                name
        );

        addFormRow(
                form,
                c,
                2,
                "Age",
                age
        );

        addFormRow(
                form,
                c,
                3,
                "Condition",
                condition
        );

        addFormRow(
                form,
                c,
                4,
                "Emergency Level",
                emergency
        );

        JButton register =
                actionButton(
                        "Register Patient",
                        GREEN
                );

        c.gridx = 1;
        c.gridy = 5;

        form.add(
                register,
                c
        );

        register.addActionListener(
                e -> {

                    String patientId =
                            id.getText().trim();

                    String patientName =
                            name.getText().trim();

                    String ageText =
                            age.getText().trim();

                    String patientCondition =
                            condition.getText().trim();

                    String level =
                            emergency
                                    .getSelectedItem()
                                    .toString();

                    if (patientId.isEmpty()
                            || patientName.isEmpty()
                            || ageText.isEmpty()
                            || patientCondition.isEmpty()) {

                        showError(
                                "Please fill all fields."
                        );

                        return;
                    }

                    if (patients.get(
                            patientId
                    ) != null) {

                        showError(
                                "Patient ID already exists."
                        );

                        return;
                    }

                    int patientAge;

                    try {

                        patientAge =
                                Integer.parseInt(
                                        ageText
                                );

                    } catch (
                            NumberFormatException ex) {

                        showError(
                                "Age must be a number."
                        );

                        return;
                    }

                    if (patientAge <= 0
                            || patientAge > 120) {

                        showError(
                                "Enter a valid age."
                        );

                        return;
                    }

                    addPatient(
                            patientId,
                            patientName,
                            patientAge,
                            patientCondition,
                            level
                    );

                    id.setText("");
                    name.setText("");
                    age.setText("");
                    condition.setText("");

                    refreshAll();

                    JOptionPane.showMessageDialog(
                            this,
                            "Patient registered successfully.",
                            "Registration Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
        );

        page.add(
                form,
                BorderLayout.CENTER
        );

        contentPanel.add(page);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void addFormRow(
            JPanel panel,
            GridBagConstraints c,
            int row,
            String label,
            JComponent field) {

        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0.25;

        JLabel text =
                new JLabel(label);

        text.setForeground(TEXT);

        text.setFont(BOLD);

        panel.add(text, c);

        c.gridx = 1;
        c.weightx = 1;

        field.setPreferredSize(
                new Dimension(
                        300,
                        38
                )
        );

        panel.add(field, c);
    }


    // ===================== TREATMENT SCREEN =====================

    private void showTreatment() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Treatment Center",
                        "Manage emergency cases and complete patient treatment"
                );

        JPanel main =
                new JPanel(
                        new BorderLayout(
                                14,
                                14
                        )
                );

        main.setOpaque(false);

        main.add(
                createTreatmentQueue(),
                BorderLayout.CENTER
        );

        JPanel side =
                new JPanel(
                        new BorderLayout(
                                0,
                                15
                        )
                );

        side.setPreferredSize(
                new Dimension(
                        270,
                        0
                )
        );

        side.setBackground(PANEL);

        side.setBorder(
                new EmptyBorder(
                        18,
                        18,
                        18,
                        18
                )
        );

        JLabel title =
                new JLabel(
                        "Active Treatment"
                );

        title.setFont(BOLD);

        title.setForeground(TEXT);

        side.add(
                title,
                BorderLayout.NORTH
        );

        JPanel info =
                new JPanel();

        info.setOpaque(false);

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        if (currentPatient == null) {

            JLabel none =
                    new JLabel(
                            "No patient currently in treatment"
                    );

            none.setForeground(MUTED);

            none.setFont(NORMAL);

            info.add(none);

        } else {

            JLabel name =
                    new JLabel(
                            currentPatient.name
                    );

            name.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            20
                    )
            );

            name.setForeground(TEXT);

            info.add(name);

            info.add(
                    Box.createVerticalStrut(12)
            );

            info.add(
                    infoText(
                            "ID: "
                                    + currentPatient.id
                    )
            );

            info.add(
                    infoText(
                            "Condition: "
                                    + currentPatient.condition
                    )
            );

            info.add(
                    infoText(
                            "Emergency: "
                                    + currentPatient.emergency
                    )
            );

            info.add(
                    infoText(
                            "Doctor: "
                                    + currentPatient.doctor
                    )
            );

            info.add(
                    infoText(
                            "Status: "
                                    + currentPatient.status
                    )
            );
        }

        side.add(
                info,
                BorderLayout.CENTER
        );

        JPanel actions =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                8
                        )
                );

        actions.setOpaque(false);

        JButton treat =
                actionButton(
                        "Treat Next Patient",
                        BLUE
                );

        treat.addActionListener(
                e -> {

                    treatNextPatient();

                    showTreatment();
                }
        );

        JButton complete =
                actionButton(
                        "Complete Treatment",
                        GREEN
                );

        if (currentPatient == null) {

            complete.setEnabled(false);

            complete.setBackground(DISABLED);

            complete.setForeground(
                    new Color(
                            175,
                            180,
                            188
                    )
            );
        }

        complete.addActionListener(
                e -> {

                    dischargePatient();

                    showTreatment();
                }
        );

        actions.add(treat);
        actions.add(complete);

        side.add(
                actions,
                BorderLayout.SOUTH
        );

        main.add(
                side,
                BorderLayout.EAST
        );

        page.add(
                main,
                BorderLayout.CENTER
        );

        contentPanel.add(page);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private JPanel createTreatmentQueue() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                10
                        )
                );

        panel.setBackground(PANEL);

        panel.setBorder(
                new EmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        JLabel title =
                new JLabel(
                        "Patients Waiting for Treatment"
                );

        title.setFont(BOLD);

        title.setForeground(TEXT);

        panel.add(
                title,
                BorderLayout.NORTH
        );

        String[] columns = {

                "Priority",
                "ID",
                "Patient",
                "Condition",
                "Emergency"
        };

        DefaultTableModel model =
                new DefaultTableModel(
                        columns,
                        0
                );

        ArrayList<Patient> urgent =
                new ArrayList<>(
                        emergencyQueue.heap
                );

        urgent.sort(
                (a, b) -> {

                    if (a.priority != b.priority) {

                        return Integer.compare(
                                b.priority,
                                a.priority
                        );
                    }

                    return Long.compare(
                            a.arrivalTime,
                            b.arrivalTime
                    );
                }
        );

        for (Patient p : urgent) {

            model.addRow(
                    new Object[]{
                            p.emergency,
                            p.id,
                            p.name,
                            p.condition,
                            p.emergency
                    }
            );
        }

        for (Patient p :
                normalQueue.queue) {

            model.addRow(
                    new Object[]{
                            "ROUTINE",
                            p.id,
                            p.name,
                            p.condition,
                            p.emergency
                    }
            );
        }

        JTable table =
                new JTable(model);

        table.setRowHeight(31);

        table.setBackground(TABLE_BG);

        table.setForeground(TEXT);

        table.setGridColor(BORDER);

        styleTableHeader(table);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        return panel;
    }


    // ===================== DEPARTMENTS =====================

    private void showDepartments() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Hospital Departments",
                        "Find the recommended route between hospital departments"
                );

        JPanel main =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                18,
                                0
                        )
                );

        main.setOpaque(false);

        JPanel departments =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                10,
                                10
                        )
                );

        departments.setOpaque(false);

        String[] names = {

                "Emergency",
                "Triage",
                "Radiology",
                "Laboratory",
                "ICU",
                "Pharmacy",
                "Recovery"
        };

        for (String name : names) {

            JPanel card =
                    new JPanel(
                            new BorderLayout()
                    );

            card.setBackground(CARD);

            card.setBorder(
                    new EmptyBorder(
                            18,
                            18,
                            18,
                            18
                    )
            );

            JLabel label =
                    new JLabel(name);

            label.setFont(BOLD);

            label.setForeground(TEXT);

            card.add(label);

            departments.add(card);
        }

        main.add(departments);

        JPanel route =
                new JPanel();

        route.setBackground(PANEL);

        route.setBorder(
                new EmptyBorder(
                        22,
                        22,
                        22,
                        22
                )
        );

        route.setLayout(
                new BoxLayout(
                        route,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel routeTitle =
                new JLabel(
                        "Find Internal Route"
                );

        routeTitle.setFont(BOLD);

        routeTitle.setForeground(TEXT);

        route.add(routeTitle);

        route.add(
                Box.createVerticalStrut(18)
        );

        JComboBox<String> from =
                new JComboBox<>(names);

        JComboBox<String> to =
                new JComboBox<>(names);

        route.add(
                labelWithField(
                        "From",
                        from
                )
        );

        route.add(
                Box.createVerticalStrut(10)
        );

        route.add(
                labelWithField(
                        "To",
                        to
                )
        );

        route.add(
                Box.createVerticalStrut(18)
        );

        JButton find =
                actionButton(
                        "Find Route",
                        BLUE
                );

        route.add(find);

        find.addActionListener(
                e -> {

                    String start =
                            from.getSelectedItem()
                                    .toString();

                    String end =
                            to.getSelectedItem()
                                    .toString();

                    ArrayList<String> path =
                            hospital.shortestPath(
                                    start,
                                    end
                            );

                    JOptionPane.showMessageDialog(
                            this,
                            "Recommended route:\n\n"
                                    + String.join(
                                    "  →  ",
                                    path
                            ),
                            "Hospital Route",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
        );

        main.add(route);

        page.add(
                main,
                BorderLayout.CENTER
        );

        contentPanel.add(page);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private JPanel labelWithField(
            String text,
            JComponent field) {

        JPanel panel =
                new JPanel(
                        new BorderLayout(
                                0,
                                5
                        )
                );

        panel.setOpaque(false);

        JLabel label =
                new JLabel(text);

        label.setForeground(MUTED);

        label.setFont(BOLD);

        panel.add(
                label,
                BorderLayout.NORTH
        );

        panel.add(
                field,
                BorderLayout.CENTER
        );

        return panel;
    }


    // ===================== REPORTS =====================

    private void showReports() {

        contentPanel.removeAll();

        JPanel page =
                createPage(
                        "Reports",
                        "Emergency department activity and patient statistics"
                );

        JPanel cards =
                new JPanel(
                        new GridLayout(
                                2,
                                2,
                                15,
                                15
                        )
                );

        cards.setOpaque(false);

        cards.add(
                reportCard(
                        "Registered Patients",
                        String.valueOf(
                                totalPatients
                        )
                )
        );

        cards.add(
                reportCard(
                        "Patients Treated",
                        String.valueOf(
                                totalTreated
                        )
                )
        );

        cards.add(
                reportCard(
                        "Patients Discharged",
                        String.valueOf(
                                totalDischarged
                        )
                )
        );

        cards.add(
                reportCard(
                        "Patients Waiting",
                        String.valueOf(
                                emergencyQueue.size()
                                        + normalQueue.size()
                        )
                )
        );

        page.add(
                cards,
                BorderLayout.CENTER
        );

        contentPanel.add(page);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private JPanel reportCard(
            String title,
            String value) {

        JPanel card =
                new JPanel(
                        new BorderLayout()
                );

        card.setBackground(CARD);

        card.setBorder(
                new EmptyBorder(
                        22,
                        22,
                        22,
                        22
                )
        );

        JLabel t =
                new JLabel(title);

        t.setForeground(MUTED);

        t.setFont(BOLD);

        JLabel v =
                new JLabel(value);

        v.setForeground(TEXT);

        v.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        34
                )
        );

        card.add(
                t,
                BorderLayout.NORTH
        );

        card.add(
                v,
                BorderLayout.CENTER
        );

        return card;
    }


    // ===================== SEARCH =====================

    private void searchPatient() {

        String id =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Patient ID:",
                        "Find Patient",
                        JOptionPane.QUESTION_MESSAGE
                );

        if (id == null ||
                id.trim().isEmpty()) {

            return;
        }

        Patient patient =
                patients.get(id.trim());

        if (patient == null) {

            showError(
                    "Patient not found."
            );

            return;
        }

        StringBuilder result =
                new StringBuilder();

        result.append(
                "PATIENT INFORMATION\n\n"
        );

        result.append(
                "ID: "
                        + patient.id
                        + "\n"
        );

        result.append(
                "Name: "
                        + patient.name
                        + "\n"
        );

        result.append(
                "Age: "
                        + patient.age
                        + "\n"
        );

        result.append(
                "Condition: "
                        + patient.condition
                        + "\n"
        );

        result.append(
                "Emergency: "
                        + patient.emergency
                        + "\n"
        );

        result.append(
                "Doctor: "
                        + patient.doctor
                        + "\n"
        );

        result.append(
                "Status: "
                        + patient.status
                        + "\n\n"
        );

        result.append(
                "CARE HISTORY\n"
        );

        for (String item :
                patient.history) {

            result.append(
                    "• "
                            + item
                            + "\n"
            );
        }

        JTextArea area =
                new JTextArea(
                        result.toString()
                );

        area.setEditable(false);

        area.setFont(NORMAL);

        area.setRows(16);

        area.setColumns(40);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Patient Details",
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // ===================== REFRESH =====================

    private void refreshAll() {

        if (totalLabel != null) {

            totalLabel.setText(
                    String.valueOf(
                            totalPatients
                    )
            );

            waitingLabel.setText(
                    String.valueOf(
                            emergencyQueue.size()
                                    + normalQueue.size()
                    )
            );

            emergencyLabel.setText(
                    String.valueOf(
                            emergencyQueue.size()
                    )
            );

            treatedLabel.setText(
                    String.valueOf(
                            totalTreated
                    )
            );
        }

        refreshPatientTable();

        updateTreatmentPanel();

        contentPanel.revalidate();

        contentPanel.repaint();
    }


    private void refreshPatientTable() {

        if (patientModel == null) {
            return;
        }

        patientModel.setRowCount(0);

        ArrayList<Patient> list =
                patients.getAll();

        list.sort(
                Comparator.comparing(
                        p -> p.id
                )
        );

        for (Patient p : list) {

            patientModel.addRow(
                    new Object[]{
                            p.id,
                            p.name,
                            p.age,
                            p.condition,
                            p.emergency,
                            p.doctor,
                            p.status
                    }
            );
        }
    }


    // ===================== BUTTON =====================

    private JButton actionButton(
            String text,
            Color color) {

        JButton button =
                new JButton(text);

        button.setBackground(color);

        button.setForeground(
                Color.WHITE
        );

        button.setFont(BOLD);

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setOpaque(true);

        button.setPreferredSize(
                new Dimension(
                        175,
                        42
                )
        );

        return button;
    }


    // ===================== INFO TEXT =====================

    private JLabel infoText(String text) {

        JLabel label =
                new JLabel(text);

        label.setForeground(MUTED);

        label.setFont(NORMAL);

        label.setBorder(
                new EmptyBorder(
                        6,
                        0,
                        6,
                        0
                )
        );

        return label;
    }


    // ===================== ERROR =====================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Attention",
                JOptionPane.WARNING_MESSAGE
        );
    }


    // ===================== MAIN =====================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                () -> {

                    try {

                        UIManager.setLookAndFeel(
                                UIManager
                                        .getSystemLookAndFeelClassName()
                        );

                    } catch (Exception ignored) {
                    }

                    HospitalEmergencySimulator app =
                            new HospitalEmergencySimulator();

                    app.setVisible(true);
                }
        );
    }
}