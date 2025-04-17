package network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Client {
    private static final int PORT = 5000;
    private static final String SERVER_ADDRESS = "localhost";

    public Client() {
        showStartupGUI();
    }

    private void showStartupGUI() {
        JFrame frame = new JFrame("File Sharing Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JLabel header = new JLabel("File Sharing Client", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setBackground(new Color(0, 122, 204));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(frame.getWidth(), 60));
        frame.add(header, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        JButton createAccountButton = createButton("Create Account");
        JButton loginButton = createButton("Login");

        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(createAccountButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(loginButton, gbc);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        createAccountButton.addActionListener(e -> {
            frame.dispose();
            showAccountCreationGUI();
        });

        loginButton.addActionListener(e -> {
            frame.dispose();
            showLoginGUI();
        });
    }

    private void showAccountCreationGUI() {
        JFrame frame = new JFrame("Create Account");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel nameLabel = createLabel("Name:");
        JTextField nameField = createTextField();
        JLabel passwordLabel = createLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton submitButton = createButton("Create Account");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(submitButton, gbc);

        frame.add(panel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name and password cannot be empty.");
                return;
            }

            new Thread(() -> {
                try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                    dos.writeUTF("CREATE_ACCOUNT");
                    dos.writeUTF(name);
                    dos.writeUTF(password);

                    // The server, in handleCreateAccount, might send back "SUCCESS" or "FAILURE"
                    // If you want to read that response, you can do so:
                    // String response = dis.readUTF();

                    // Just show a success message for now.
                    JOptionPane.showMessageDialog(frame, "Account created successfully.");
                    frame.dispose();
                    showLoginGUI();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }).start();
        });
    }

    private void showLoginGUI() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 350); // Increased size to accommodate additional elements
        frame.setLocationRelativeTo(null);

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem backToCreateAccount = new JMenuItem("Back to Create Account");
        menu.add(backToCreateAccount);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        // Main Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Title Label
        JLabel titleLabel = new JLabel("WELCOME TO ENSA FILE SHARING", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Name and Password Input
        JLabel nameLabel = createLabel("Name:");
        JTextField nameField = createTextField();
        JLabel passwordLabel = createLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Login Button
        JButton loginButton = createButton("Login");

        // Note Label
        JLabel noteLabel = new JLabel("If you do not have an account, please create it.", JLabel.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setForeground(Color.GRAY);

        // Adding Components to the Panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(noteLabel, gbc);

        frame.add(panel);
        frame.setVisible(true);

        // Action Listener for Login Button
        loginButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Name and password cannot be empty.");
                return;
            }

            new Thread(() -> {
                try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dis = new DataInputStream(socket.getInputStream())) {

                    dos.writeUTF("LOGIN");
                    dos.writeUTF(name);
                    dos.writeUTF(password);

                    String response = dis.readUTF();
                    if ("SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(frame, "Login successful.");
                        frame.dispose();
                        showFileSharingGUI(name); // Pass username to the file-sharing GUI
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid credentials.");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }).start();
        });

        // Action Listener for Back to Create Account Menu Item
        backToCreateAccount.addActionListener(e -> {
            frame.dispose(); // Close the current frame
            showAccountCreationGUI(); // Navigate to the account creation screen
        });
    }


    private void showFileSharingGUI(String username) {
        // Frame setup
        JFrame frame = new JFrame("File Sharing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        // Header Label with a sleek design
        JLabel header = new JLabel("File Sharing Client", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(0, 122, 204));
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(frame.getWidth(), 70));

        // Create a panel for content with GridBagLayout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Server code note
        JLabel serverCodeNote = new JLabel("Note: The server code is '01'.");
        serverCodeNote.setFont(new Font("Arial", Font.ITALIC, 14));
        serverCodeNote.setForeground(Color.GRAY);

        // Server code input
        JLabel serverCodeLabel = createLabel("Server Code:");
        JTextField serverCodeField = createTextField();
        JButton connectButton = createButton("Connect");

        // File selection input
        JLabel fileLabel = createLabel("Choose File:");
        JButton fileButton = createButton("Browse");
        JLabel filePathLabel = new JLabel();
        filePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        JTextField commentField = createCommentField();

        JButton sendButton = createButton("Send");

        // Status bar
        JLabel statusBar = new JLabel("Client Disconnected", JLabel.CENTER);
        statusBar.setFont(new Font("Arial", Font.BOLD, 14));
        statusBar.setOpaque(true);
        statusBar.setBackground(Color.LIGHT_GRAY);
        statusBar.setPreferredSize(new Dimension(frame.getWidth(), 30));

        // Layout arrangement
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(serverCodeNote, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(serverCodeLabel, gbc);
        gbc.gridx = 1;
        panel.add(serverCodeField, gbc);
        gbc.gridx = 2;
        panel.add(connectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(fileLabel, gbc);
        gbc.gridx = 1;
        panel.add(fileButton, gbc);
        gbc.gridx = 2;
        panel.add(filePathLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(commentField, gbc);
        gbc.gridx = 2;
        panel.add(sendButton, gbc);

        // Add header + panel + status bar
        frame.add(header, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Hover effect for buttons
        addButtonHoverEffect(fileButton, connectButton, sendButton);

        // Choose file
        fileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        // Connect button: Update status bar based on server code
        connectButton.addActionListener(e -> {
            String serverCode = serverCodeField.getText().trim();
            if ("01".equals(serverCode)) {
                statusBar.setText("Client Connected");
                statusBar.setBackground(new Color(0, 200, 0)); // Green background
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid server code!");
            }
        });

        sendButton.addActionListener(e -> {
            String serverCode = serverCodeField.getText().trim();
            String filePath = filePathLabel.getText().trim();
            String comment = commentField.getText().trim();

            if (filePath.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No file selected!");
                return;
            }

            if (!"01".equals(serverCode)) {
                JOptionPane.showMessageDialog(frame, "Please connect to the server first!");
                return;
            }

            File file = new File(filePath);
            long fileSizeInBytes = file.length();
            long maxFileSizeInBytes = 500L * 1024 * 1024; // 500 MB in bytes

            if (fileSizeInBytes > maxFileSizeInBytes) {
                JOptionPane.showMessageDialog(frame, "File size exceeds the maximum limit of 500 MB!");
                return;
            }

            // Now actually send the file
            new Thread(() -> {
                try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                     DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                     FileInputStream fis = new FileInputStream(filePath)) {

                    // Let server know we are sending a file
                    dos.writeUTF("SEND_FILE");

                    // Send the username first (needed for the DB record)
                    dos.writeUTF(username);

                    // Then send the file metadata
                    dos.writeUTF(file.getName());
                    dos.writeLong(file.length());

                    // If you want to send fileType or comment, we can do something like:
                    dos.writeUTF(comment.isEmpty() ? "No comment provided" : comment);

                    // Write the file contents
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, bytesRead);
                    }

                    // Optionally, you can read the server’s response
                    try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                        String response = dis.readUTF();
                        if ("SUCCESS".equals(response)) {
                            JOptionPane.showMessageDialog(frame, "File sent successfully!");
                        } else {
                            JOptionPane.showMessageDialog(frame, "File transfer failed!");
                        }
                    }

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                }
            }).start();
        });
    }
    // Helper methods:

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(0, 122, 204));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        return textField;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private JTextField createCommentField() {
        JTextField commentField = new JTextField("Add a comment...", 20);
        commentField.setFont(new Font("Arial", Font.PLAIN, 14));
        commentField.setForeground(Color.GRAY);
        commentField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commentField.getText().equals("Add a comment...")) {
                    commentField.setText("");
                    commentField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (commentField.getText().isEmpty()) {
                    commentField.setText("Add a comment...");
                    commentField.setForeground(Color.GRAY);
                }
            }
        });
        return commentField;
    }

    private void addButtonHoverEffect(JButton... buttons) {
        for (JButton button : buttons) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(85, 153, 255)); // Lighter blue
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(0, 122, 204)); // Original blue
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}