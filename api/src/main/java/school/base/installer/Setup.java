/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package school.base.installer;

import static java.util.Arrays.stream;

import javax.swing.BorderFactory;

import org.springframework.context.ApplicationContext;
import school.Application;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.awt.EventQueue.invokeLater;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static org.springframework.boot.SpringApplication.run;

/**
 * @author cheroliv
 */
public class Setup extends javax.swing.JFrame {

    /**
     * Creates new form Setup
     */
    public Setup() {
        initComponents();
        initializeCustomComponents();
    }

    protected ApplicationContext context;

    private enum InstallationType {
        ALL_IN_ONE,
        SEPARATED_FOLDERS
    }

    private final Map<String, Path> selectedPaths = new HashMap<>();
    private InstallationType currentInstallationType = InstallationType.ALL_IN_ONE;


    private void initializeCustomComponents() {
        // Group the radio buttons
        installationTypeGroup.add(allInOneWorkspaceRadioButton); // Separated folders
        installationTypeGroup.add(separatedEntriesWorkspaceRadioButton); // All-in-one
        separatedEntriesWorkspaceRadioButton.setSelected(false);
        allInOneWorkspaceRadioButton.setSelected(true);

        // Add action listeners to radio buttons
        separatedEntriesWorkspaceRadioButton.addActionListener(e -> handleInstallationTypeChange(InstallationType.ALL_IN_ONE));
        allInOneWorkspaceRadioButton.addActionListener(e -> handleInstallationTypeChange(InstallationType.SEPARATED_FOLDERS));

        // Add action listeners to browse buttons
        browseWorkspacePathButton.addActionListener(e -> selectDirectory("workspace", workspacePathTextField));
        browseOfficePathButton.addActionListener(e -> selectDirectory("office", officePathTextField));
        browseEducationPathButton.addActionListener(e -> selectDirectory("education", educationPathTextField));
        browseCommunicationPathButton.addActionListener(e -> selectDirectory("communication", communicationPathTextField));
        browseConfigurationPathButton.addActionListener(e -> selectDirectory("configuration", configurationPathTextField));
        browsejobPathButton.addActionListener(e -> selectDirectory("job", jobPathTextField));

        // Initialize workspace entries panel visibility
        setWorkspaceEntriesVisibility(false);

        // Create workspace button action
        createWorkspaceButton.addActionListener(e -> handleCreateWorkspace());
    }

    private void handleInstallationTypeChange(InstallationType type) {
        currentInstallationType = type;
        setWorkspaceEntriesVisibility(type == InstallationType.ALL_IN_ONE);

        if (type == InstallationType.ALL_IN_ONE) {
            // Clear all specific paths when switching to all-in-one
            clearSpecificPaths();
        }
    }

    private void setWorkspaceEntriesVisibility(boolean visible) {
        officePathLabel.setVisible(visible);
        officePathTextField.setVisible(visible);
        browseOfficePathButton.setVisible(visible);

        educationPathLabel.setVisible(visible);
        educationPathTextField.setVisible(visible);
        browseEducationPathButton.setVisible(visible);

        communicationPathLabel.setVisible(visible);
        communicationPathTextField.setVisible(visible);
        browseCommunicationPathButton.setVisible(visible);

        configurationPathLabel.setVisible(visible);
        configurationPathTextField.setVisible(visible);
        browseConfigurationPathButton.setVisible(visible);

        jobPathLabel.setVisible(visible);
        jobPathTextField.setVisible(visible);
        browsejobPathButton.setVisible(visible);
    }

    private void selectDirectory(String pathKey, JTextField textField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Directory");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            Path selectedPath = selectedFile.toPath();
            selectedPaths.put(pathKey, selectedPath);
            textField.setText(selectedPath.toString());
        }
    }

    private void clearSpecificPaths() {
        officePathTextField.setText("");
        educationPathTextField.setText("");
        communicationPathTextField.setText("");
        configurationPathTextField.setText("");
        jobPathTextField.setText("");

        selectedPaths.remove("office");
        selectedPaths.remove("education");
        selectedPaths.remove("communication");
        selectedPaths.remove("configuration");
        selectedPaths.remove("job");
    }

    private void handleCreateWorkspace() {
        String workspacePath = workspacePathTextField.getText();
        if (workspacePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a workspace directory",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (currentInstallationType == InstallationType.SEPARATED_FOLDERS) {
                createSeparatedFoldersWorkspace();
            } else {
                createAllInOneWorkspace();
            }

            JOptionPane.showMessageDialog(this,
                    "Workspace created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating workspace: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createSeparatedFoldersWorkspace() {
        // Validate all required paths are selected
        String[] requiredPaths = {"office", "education", "communication", "configuration", "job"};
        for (String path : requiredPaths) {
            if (!selectedPaths.containsKey(path) || selectedPaths.get(path) == null) {
                throw new IllegalStateException("All paths must be selected for separated folders installation");
            }
        }

        // TODO: Implement the actual creation of separated folders
        // You can access the paths using selectedPaths.get("office") etc.
    }

    private void createAllInOneWorkspace() {
        Path workspacePath = Paths.get(workspacePathTextField.getText());
        // TODO: Implement the creation of an all-in-one workspace
        // This would typically involve creating subdirectories in the main workspace
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        installationTypeGroup = new javax.swing.ButtonGroup();
        workspaceTopPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        createWorkspaceButton = new javax.swing.JButton();
        workspacePathPanel = new javax.swing.JPanel();
        workspacePathTextField = new javax.swing.JTextField();
        browseWorkspacePathButton = new javax.swing.JButton();
        workspacePathLabel = new javax.swing.JLabel();
        workspaceTypePanel = new javax.swing.JPanel();
        workspaceTypeSelectorPanel = new javax.swing.JPanel();
        allInOneWorkspaceRadioButton = new javax.swing.JRadioButton();
        separatedEntriesWorkspaceRadioButton = new javax.swing.JRadioButton();
        workspaceEntriesPanel = new javax.swing.JPanel();
        officePathLabel = new javax.swing.JLabel();
        officePathTextField = new javax.swing.JTextField();
        browseOfficePathButton = new javax.swing.JButton();
        educationPathLabel = new javax.swing.JLabel();
        educationPathTextField = new javax.swing.JTextField();
        browseEducationPathButton = new javax.swing.JButton();
        communicationPathLabel = new javax.swing.JLabel();
        communicationPathTextField = new javax.swing.JTextField();
        browseCommunicationPathButton = new javax.swing.JButton();
        configurationPathLabel = new javax.swing.JLabel();
        configurationPathTextField = new javax.swing.JTextField();
        browseConfigurationPathButton = new javax.swing.JButton();
        jobPathLabel = new javax.swing.JLabel();
        jobPathTextField = new javax.swing.JTextField();
        browsejobPathButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("setupFrame"); // NOI18N

        titleLabel.setText("School installer");

        createWorkspaceButton.setText("Create");
        createWorkspaceButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createWorkspaceButtonMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout workspaceTopPanelLayout = new javax.swing.GroupLayout(workspaceTopPanel);
        workspaceTopPanel.setLayout(workspaceTopPanelLayout);
        workspaceTopPanelLayout.setHorizontalGroup(
            workspaceTopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspaceTopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(createWorkspaceButton))
        );
        workspaceTopPanelLayout.setVerticalGroup(
            workspaceTopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspaceTopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(workspaceTopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createWorkspaceButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        workspacePathPanel.setBorder(BorderFactory.createTitledBorder("Workspace"));

        browseWorkspacePathButton.setText("Select directory");

        workspacePathLabel.setText("Path");

        javax.swing.GroupLayout workspacePathPanelLayout = new javax.swing.GroupLayout(workspacePathPanel);
        workspacePathPanel.setLayout(workspacePathPanelLayout);
        workspacePathPanelLayout.setHorizontalGroup(
            workspacePathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspacePathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(workspacePathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workspacePathTextField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseWorkspacePathButton)
                .addContainerGap())
        );
        workspacePathPanelLayout.setVerticalGroup(
            workspacePathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspacePathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(workspacePathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(workspacePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseWorkspacePathButton)
                    .addComponent(workspacePathLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        workspaceTypePanel.setBorder(BorderFactory.createTitledBorder("Installation type"));

        allInOneWorkspaceRadioButton.setText("All-in-one");

        separatedEntriesWorkspaceRadioButton.setText("Separated folders");

        javax.swing.GroupLayout workspaceTypeSelectorPanelLayout = new javax.swing.GroupLayout(workspaceTypeSelectorPanel);
        workspaceTypeSelectorPanel.setLayout(workspaceTypeSelectorPanelLayout);
        workspaceTypeSelectorPanelLayout.setHorizontalGroup(
            workspaceTypeSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspaceTypeSelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(allInOneWorkspaceRadioButton)
                .addGap(18, 18, 18)
                .addComponent(separatedEntriesWorkspaceRadioButton)
                .addContainerGap(508, Short.MAX_VALUE))
        );
        workspaceTypeSelectorPanelLayout.setVerticalGroup(
            workspaceTypeSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(workspaceTypeSelectorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(workspaceTypeSelectorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(separatedEntriesWorkspaceRadioButton)
                    .addComponent(allInOneWorkspaceRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        officePathLabel.setText("Office");
        officePathLabel.setToolTipText("");

        browseOfficePathButton.setText("Select directory");

        educationPathLabel.setText("Education");
        educationPathLabel.setToolTipText("");

        browseEducationPathButton.setText("Select directory");

        communicationPathLabel.setText("Communication");
        communicationPathLabel.setToolTipText("");

        browseCommunicationPathButton.setText("Select directory");

        configurationPathLabel.setText("Configuration");
        configurationPathLabel.setToolTipText("");

        browseConfigurationPathButton.setText("Select directory");

        jobPathLabel.setText("Job");
        jobPathLabel.setToolTipText("");

        browsejobPathButton.setText("Select directory");

        javax.swing.GroupLayout workspaceEntriesPanelLayout = new javax.swing.GroupLayout(workspaceEntriesPanel);
        workspaceEntriesPanel.setLayout(workspaceEntriesPanelLayout);
        workspaceEntriesPanelLayout.setHorizontalGroup(
            workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 912, Short.MAX_VALUE)
            .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(workspaceEntriesPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(officePathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(educationPathLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(communicationPathLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(configurationPathLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jobPathLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(officePathTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                        .addComponent(educationPathTextField)
                        .addComponent(communicationPathTextField)
                        .addComponent(configurationPathTextField)
                        .addComponent(jobPathTextField))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(browseEducationPathButton)
                        .addComponent(browseOfficePathButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(browseCommunicationPathButton)
                        .addComponent(browseConfigurationPathButton)
                        .addComponent(browsejobPathButton))
                    .addContainerGap()))
        );
        workspaceEntriesPanelLayout.setVerticalGroup(
            workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 269, Short.MAX_VALUE)
            .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(workspaceEntriesPanelLayout.createSequentialGroup()
                    .addGap(3, 3, 3)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(officePathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(browseOfficePathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(officePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(browseEducationPathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(educationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(educationPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(browseCommunicationPathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(communicationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(communicationPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(browseConfigurationPathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(configurationPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(configurationPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(workspaceEntriesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(browsejobPathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jobPathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jobPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(3, 3, 3)))
        );

        javax.swing.GroupLayout workspaceTypePanelLayout = new javax.swing.GroupLayout(workspaceTypePanel);
        workspaceTypePanel.setLayout(workspaceTypePanelLayout);
        workspaceTypePanelLayout.setHorizontalGroup(
            workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 924, Short.MAX_VALUE)
            .addGroup(workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, workspaceTypePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(workspaceTypeSelectorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(workspaceTypePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(workspaceEntriesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        workspaceTypePanelLayout.setVerticalGroup(
            workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 344, Short.MAX_VALUE)
            .addGroup(workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(workspaceTypePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(workspaceTypeSelectorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(263, Short.MAX_VALUE)))
            .addGroup(workspaceTypePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, workspaceTypePanelLayout.createSequentialGroup()
                    .addContainerGap(69, Short.MAX_VALUE)
                    .addComponent(workspaceEntriesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(workspaceTopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(workspacePathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(workspaceTypePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(workspaceTopPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workspacePathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workspaceTypePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createWorkspaceButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_createWorkspaceButtonMouseClicked
        // TODO add your handling code here:
        stream(context.getBeanDefinitionNames())
                .sequential()
                .forEach(System.out::println);
    }//GEN-LAST:event_createWorkspaceButtonMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Setup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Setup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Setup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Setup.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        invokeLater(() -> {
            var f = new Setup();
            f.setVisible(true);
            f.context = run(Application.class, args);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allInOneWorkspaceRadioButton;
    private javax.swing.JButton browseCommunicationPathButton;
    private javax.swing.JButton browseConfigurationPathButton;
    private javax.swing.JButton browseEducationPathButton;
    private javax.swing.JButton browseOfficePathButton;
    private javax.swing.JButton browseWorkspacePathButton;
    private javax.swing.JButton browsejobPathButton;
    private javax.swing.JLabel communicationPathLabel;
    private javax.swing.JTextField communicationPathTextField;
    private javax.swing.JLabel configurationPathLabel;
    private javax.swing.JTextField configurationPathTextField;
    private javax.swing.JButton createWorkspaceButton;
    private javax.swing.JLabel educationPathLabel;
    private javax.swing.JTextField educationPathTextField;
    private javax.swing.ButtonGroup installationTypeGroup;
    private javax.swing.JLabel jobPathLabel;
    private javax.swing.JTextField jobPathTextField;
    private javax.swing.JLabel officePathLabel;
    private javax.swing.JTextField officePathTextField;
    private javax.swing.JRadioButton separatedEntriesWorkspaceRadioButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel workspaceEntriesPanel;
    private javax.swing.JLabel workspacePathLabel;
    private javax.swing.JPanel workspacePathPanel;
    private javax.swing.JTextField workspacePathTextField;
    private javax.swing.JPanel workspaceTopPanel;
    private javax.swing.JPanel workspaceTypePanel;
    private javax.swing.JPanel workspaceTypeSelectorPanel;
    // End of variables declaration//GEN-END:variables

}
