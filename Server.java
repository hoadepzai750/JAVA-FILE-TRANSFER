package network;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Objects;

public class Server {
    private static final int PORT = 5000;
    private JTextArea logArea;
    private JTable accountsTable;
    private JTable filesTable;
    private DefaultTableModel accountsModel;
    private DefaultTableModel filesModel;
    private JLabel clientStatusLabel;

    public Server() {
        // Initialize database (create tables if they don't exist)
        DatabaseHelper.initializeDatabase();
        setupGUI();
    }

    private void setupGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Advanced File Sharing Server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);

            // Main panel with gradient background
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    // Gradient from top (light blue) to bottom (dark blue)
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(173, 216, 230), 0, getHeight(), new Color(25, 25, 112));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setLayout(new BorderLayout());

            // Header panel with modern look
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(new Color(0, 102, 204)); // Dark blue
            headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 60));
            JLabel headerLabel = new JLabel("Advanced File Sharing Server");
            headerLabel.setFont(new Font("Arial", Font.BOLD, 28));
            headerLabel.setForeground(Color.WHITE);
            headerPanel.add(headerLabel);

            mainPanel.add(headerPanel, BorderLayout.NORTH);

            // Center panel with rounded borders
            JPanel centerPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(240, 248, 255, 200)); // Slightly transparent white
                    g2d.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 30, 30);
                }
            };
            centerPanel.setOpaque(false); // Transparent background
            centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Tabbed pane for logs, accounts, and files
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Arial", Font.PLAIN, 14));
            tabbedPane.setOpaque(false);

            // Logs tab
            logArea = new JTextArea();
            logArea.setEditable(false);
            JScrollPane logScrollPane = new JScrollPane(logArea);
            tabbedPane.addTab("Logs", logScrollPane);

            // Accounts tab
            accountsModel = new DefaultTableModel(new String[]{"Name", "Password"}, 0);
            accountsTable = new JTable(accountsModel);
            accountsTable.setFillsViewportHeight(true);
            JScrollPane accountsScrollPane = new JScrollPane(accountsTable);
            tabbedPane.addTab("Accounts", accountsScrollPane);

            // Files tab
            filesModel = new DefaultTableModel(new String[]{"File Name", "Size", "Status"}, 0);
            filesTable = new JTable(filesModel);
            filesTable.setFillsViewportHeight(true);
            JScrollPane filesScrollPane = new JScrollPane(filesTable);
            tabbedPane.addTab("Files", filesScrollPane);

            centerPanel.add(tabbedPane, BorderLayout.CENTER);
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            // Footer panel with SERVER title
            JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footerPanel.setOpaque(false);
            JLabel serverLabel = new JLabel("SERVER");
            serverLabel.setFont(new Font("Arial", Font.BOLD, 20));
            serverLabel.setForeground(Color.BLACK); // Or any color you prefer
            footerPanel.add(serverLabel);

            mainPanel.add(footerPanel, BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.setVisible(true);

            // Load accounts from database after GUI setup
            loadAccountsFromDatabase();

            // Start server in a separate thread
            new Thread(this::startServer).start();
        });
    }

    private void loadAccountsFromDatabase() {
        // Load accounts from the database and display them in the accounts table
        SwingUtilities.invokeLater(() -> {
            try (Connection connection = DatabaseHelper.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT username, password FROM Users")) {

                while (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String passwordHash = resultSet.getString("password");
                    accountsModel.addRow(new Object[]{username, passwordHash});
                }
            } catch (SQLException e) {
                logArea.append("Error loading accounts: " + e.getMessage() + "\n");
            }
        });
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logArea.append("Server started on port " + PORT + "\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                SwingUtilities.invokeLater(() -> {
                    clientStatusLabel.setText("Client Status: Connected");
                    clientStatusLabel.setForeground(Color.GREEN);
                });
                logArea.append("Client connected.\n");
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            logArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            String action = dis.readUTF();

            switch (action) {
                case "CREATE_ACCOUNT":
                    handleCreateAccount(dis, dos);
                    break;
                case "LOGIN":
                    handleLogin(dis, dos);
                    break;
                case "SEND_FILE":
                    handleFileTransfer(dis, dos);
                    break;
                default:
                    dos.writeUTF("INVALID_ACTION");
                    break;
            }

        } catch (IOException e) {
            logArea.append("Error handling client: " + e.getMessage() + "\n");
        }
    }

    private void handleCreateAccount(DataInputStream dis, DataOutputStream dos) throws IOException {
        String name = dis.readUTF();
        String password = dis.readUTF();
        String hashedPassword = hashPassword(password);

        if (DatabaseHelper.saveUserAccount(name, hashedPassword)) {
            dos.writeUTF("SUCCESS");
            SwingUtilities.invokeLater(() -> accountsModel.addRow(new Object[]{name, hashedPassword}));
        } else {
            dos.writeUTF("FAILURE");
        }
    }

    private void handleLogin(DataInputStream dis, DataOutputStream dos) throws IOException {
        String name = dis.readUTF();
        String password = dis.readUTF();
        String hashedPassword = hashPassword(password);

        if (DatabaseHelper.validateUserAccount(name, hashedPassword)) {
            dos.writeUTF("SUCCESS");
        } else {
            dos.writeUTF("FAILURE");
        }
    }

    private void handleFileTransfer(DataInputStream dis, DataOutputStream dos) {
        try {
            String username = dis.readUTF();
            String fileName = dis.readUTF();
            long fileSize = dis.readLong();

            long maxFileSize = 500L * 1024 * 1024; // 500 MB in bytes
            if (fileSize > maxFileSize) {
                dos.writeUTF("FILE_TOO_LARGE");
                logArea.append("File transfer rejected: " + fileName + " (size: " + fileSize + " bytes) exceeds 500 MB limit.\n");
                return;
            }

            // Confirm file save
            int userChoice = JOptionPane.showConfirmDialog(null,
                    "Do you want to save the file '" + fileName + "' from user: " + username + "?",
                    "File Transfer",
                    JOptionPane.YES_NO_OPTION);

            if (userChoice == JOptionPane.YES_OPTION) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(fileName));
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File saveFile = fileChooser.getSelectedFile();

                    // Write file to disk
                    try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                        byte[] buffer = new byte[8192]; // Increase buffer size for efficiency
                        int bytesRead;
                        long totalBytesRead = 0;

                        while (totalBytesRead < fileSize) {
                            bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                            if (bytesRead == -1) break; // End of stream

                            fos.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }

                        // Validate file transfer completion
                        if (totalBytesRead == fileSize) {
                            dos.writeUTF("SUCCESS");
                            logArea.append("File saved: " + saveFile.getAbsolutePath() + "\n");
                            filesModel.addRow(new Object[]{fileName, fileSize, "Received"});
                        } else {
                            dos.writeUTF("FAILURE");
                            logArea.append("Error: Incomplete file transfer for '" + fileName + "'.\n");
                        }
                    }
                } else {
                    dos.writeUTF("CANCELLED");
                }
            } else {
                dos.writeUTF("CANCELLED");
            }
        } catch (IOException e) {
            logArea.append("Error during file transfer: " + e.getMessage() + "\n");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
