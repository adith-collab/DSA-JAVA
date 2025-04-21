import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class HospitalQueueManagementSystem {
    static class Doctor {
        private String name, occupation;
        private boolean available = true;
        private Queue<Patient> patientQueue = new LinkedList<>();

        public Doctor(String name, String occupation) {
            this.name = name;
            this.occupation = occupation;
        }

        public String getName() { return name; }
        public String getOccupation() { return occupation; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public Queue<Patient> getPatientQueue() { return patientQueue; }

        public void addPatient(Patient p) {
            patientQueue.add(p);
            if (!p.getStatus().equals("Served")) available = false;
        }

        public String toString() {
            return name + " (" + occupation + ") - " + (available ? "Available" : "Busy");
        }
    }

    static class Patient {
        private String name;
        private int severity;
        private long arrivalTime;
        private String status;
        private Doctor assignedDoctor;

        public Patient(String name, int severity, Doctor doctor) {
            this.name = name;
            this.severity = severity;
            this.arrivalTime = System.currentTimeMillis();
            this.status = "Waiting";
            this.assignedDoctor = doctor;
        }

        public String getName() { return name; }
        public int getSeverity() { return severity; }
        public long getArrivalTime() { return arrivalTime; }
        public String getStatus() { return status; }
        public Doctor getAssignedDoctor() { return assignedDoctor; }

        public void setStatus(String status) { this.status = status; }
        public long getWaitingTime() {
            return (System.currentTimeMillis() - arrivalTime) / 1000;
        }

        public String toString() {
            return name + " (Severity: " + severity + ", Status: " + status +
                    ", Doctor: " + assignedDoctor.getName() + ", Wait Time: " + getWaitingTime() + " sec)";
        }
    }

    private JFrame frame;
    private JTextArea displayArea;
    private JComboBox<String> doctorSelector, themeSelector;
    private JTextField nameField, severityField;

    private ArrayList<Doctor> doctors = new ArrayList<>();
    private Set<String> patientNames = new HashSet<>();

    public HospitalQueueManagementSystem() {
        initializeDoctors();
        initializeGUI();
    }

    private void initializeDoctors() {
        String[][] doctorDetails = {
            {"Dr. Smith", "Cardiologist"}, {"Dr. Patel", "Neurologist"}, {"Dr. Wang", "Pediatrician"},
            {"Dr. Brown", "Dermatologist"}, {"Dr. Singh", "Orthopedic"}, {"Dr. Khan", "Gynecologist"},
            {"Dr. Lee", "Psychiatrist"}, {"Dr. Adams", "Surgeon"}, {"Dr. Clark", "ENT"},
            {"Dr. Das", "General Physician"}
        };
        for (String[] details : doctorDetails) {
            doctors.add(new Doctor(details[0], details[1]));
        }
    }

    private void initializeGUI() {
        frame = new JFrame("🏥 Hospital Queue Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout(10, 10));

        Font mainFont = new Font("Segoe UI", Font.BOLD, 18);
        Font monoFont = new Font("Consolas", Font.PLAIN, 14);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(monoFont);
        displayArea.setBorder(BorderFactory.createTitledBorder("Queue Output"));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        nameField = new JTextField();
        nameField.setFont(mainFont);
        severityField = new JTextField();
        severityField.setFont(mainFont);

        doctorSelector = new JComboBox<>();
        for (Doctor d : doctors) doctorSelector.addItem(d.getName() + " (" + d.getOccupation() + ")");
        doctorSelector.setFont(mainFont);

        themeSelector = new JComboBox<>(new String[]{"Light", "Dark", "Blue"});
        themeSelector.setFont(mainFont);

        JButton addBtn = new JButton(" Add Patient");
        JButton serveBtn = new JButton(" Attend Patient");
        JButton saveBtn = new JButton(" Save");
        JButton loadBtn = new JButton(" Load");
        JButton reportBtn = new JButton(" Generate Report");
        JButton displayBtn = new JButton(" Display Queue");
        JButton assignedBtn = new JButton(" Assigned Patients");

        JButton[] buttons = {addBtn, serveBtn, saveBtn, loadBtn, reportBtn, displayBtn, assignedBtn};

        Color buttonColor = new Color(224, 224, 224);       
        Color textColor = Color.BLACK;
        Color borderColor = new Color(96, 125, 139);        

        for (JButton btn : buttons) {
            btn.setFont(mainFont);
            btn.setBackground(buttonColor);
            btn.setForeground(textColor);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(200, 40));
            btn.setBorder(BorderFactory.createLineBorder(borderColor, 2));
        }

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "👤 Patient Info", 0, 0, mainFont));
        inputPanel.add(new JLabel("Patient Name:", SwingConstants.RIGHT)).setFont(mainFont);
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Severity (1-Critical to 4-Low):", SwingConstants.RIGHT)).setFont(mainFont);
        inputPanel.add(severityField);
        inputPanel.add(new JLabel("Select Doctor:", SwingConstants.RIGHT)).setFont(mainFont);
        inputPanel.add(doctorSelector);
        inputPanel.add(addBtn);
        inputPanel.add(serveBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(assignedBtn, BorderLayout.EAST);

        JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
        sidePanel.add(reportBtn, BorderLayout.NORTH);
        sidePanel.add(themeSelector, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveBtn);
        bottomPanel.add(loadBtn);
        bottomPanel.add(displayBtn);

        frame.add(topPanel, BorderLayout.NORTH); 
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        themeSelector.addActionListener(e -> {
            String t = (String) themeSelector.getSelectedItem();
            switch (t) {
                case "Dark":
                    displayArea.setBackground(Color.BLACK);
                    displayArea.setForeground(Color.GREEN);
                    break;
                case "Blue":
                    displayArea.setBackground(new Color(220, 240, 255));
                    displayArea.setForeground(Color.BLUE);
                    break;
                default:
                    displayArea.setBackground(Color.WHITE);
                    displayArea.setForeground(Color.BLACK);
            }
        });

        addBtn.addActionListener(e -> addPatient());
        serveBtn.addActionListener(e -> serveNextPatient());
        saveBtn.addActionListener(e -> saveToFile());
        loadBtn.addActionListener(e -> {
            loadFromFile();
            displayQueue();
        });
        reportBtn.addActionListener(e -> showStatistics());
        displayBtn.addActionListener(e -> displayQueue());
        assignedBtn.addActionListener(e -> showAssignedPatients());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addPatient() {
        String name = nameField.getText().trim();
        String sevText = severityField.getText().trim();

        if (name.isEmpty() || !name.matches("[a-zA-Z ]+")) {
            showError("Enter valid name.");
            return;
        }
        if (patientNames.contains(name)) {
            showError("Patient with this name already exists.");
            return;
        }
        if (!sevText.matches("[1-4]")) {
            showError("Severity must be 1 to 4.");
            return;
        }

        Doctor selectedDoctor = doctors.get(doctorSelector.getSelectedIndex());
        Patient patient = new Patient(name, Integer.parseInt(sevText), selectedDoctor);
        selectedDoctor.addPatient(patient);
        patientNames.add(name);

        nameField.setText("");
        severityField.setText("");
        displayQueue();
    }

    private void serveNextPatient() {
        for (Doctor d : doctors) {
            Queue<Patient> q = d.getPatientQueue();
            while (!q.isEmpty()) {
                Patient p = q.peek();
                if ("Waiting".equals(p.getStatus())) {
                    p.setStatus("Served");
                    d.setAvailable(q.stream().noneMatch(pt -> pt.getStatus().equals("Waiting")));
                    displayQueue();
                    return;
                } else {
                    q.poll();
                }
            }
        }
        showMessage("No waiting patients.");
    }

    private void displayQueue() {
        StringBuilder sb = new StringBuilder();
        sb.append("===== Doctor Status =====\n");
        for (Doctor d : doctors) sb.append(d).append("\n");

        boolean hasPatients = false;
        sb.append("\n===== All Patients =====\n");
        for (Doctor d : doctors) {
            for (Patient p : d.getPatientQueue()) {
                sb.append(p).append("\n");
                hasPatients = true;
            }
        }

        if (!hasPatients) sb.append("No patients in queue.\n");

        displayArea.setText(sb.toString());
    }

    private void showAssignedPatients() {
        StringBuilder sb = new StringBuilder();
        for (Doctor d : doctors) {
            sb.append("\n--- ").append(d.getName()).append(" (").append(d.getOccupation()).append(") ---\n");
            Queue<Patient> q = d.getPatientQueue();
            if (q.isEmpty()) {
                sb.append("No patients assigned.\n");
            } else {
                for (Patient p : q) sb.append(p.getName()).append(" (").append(p.getStatus()).append(")\n");
            }
        }
        displayArea.setText(sb.toString());
    }

    private void showStatistics() {
        int served = 0;
        long totalTime = 0;
        for (Doctor d : doctors) {
            for (Patient p : d.getPatientQueue()) {
                if ("Served".equals(p.getStatus())) {
                    totalTime += p.getWaitingTime();
                    served++;
                }
            }
        }
        String result = (served == 0) ? "No patients served yet."
                : "Patients served: " + served + "\nAvg wait time: " + (totalTime / served) + " sec";
        showMessage(result);
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(file)) {
                for (Doctor d : doctors) {
                    for (Patient p : d.getPatientQueue()) {
                        pw.println(p.getName() + "," + p.getSeverity() + "," + p.getArrivalTime() + "," +
                                p.getStatus() + "," + d.getName());
                    }
                }
                showMessage("Data saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Save failed.");
            }
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            patientNames.clear();
            for (Doctor d : doctors) {
                d.getPatientQueue().clear();
                d.setAvailable(true);
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", 5);
                    String pname = parts[0];
                    int sev = Integer.parseInt(parts[1]);
                    long arr = Long.parseLong(parts[2]);
                    String status = parts[3];
                    String dname = parts[4];

                    Doctor doc = doctors.stream().filter(d -> d.getName().equals(dname)).findFirst().orElse(null);
                    if (doc != null) {
                        Patient p = new Patient(pname, sev, doc);
                        p.arrivalTime = arr;
                        p.setStatus(status);
                        doc.addPatient(p);
                        if ("Waiting".equals(status)) doc.setAvailable(false);
                        patientNames.add(pname);
                    }
                }
                showMessage("Data loaded from " + file.getAbsolutePath());
            } catch (IOException e) {
                showError("Load failed.");
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HospitalQueueManagementSystem::new);
    }
}