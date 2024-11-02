package school.base.installer

import org.springframework.boot.SpringApplication.run
import org.springframework.context.ApplicationContext
import school.Application
import school.base.installer.Setup.InstallationType.ALL_IN_ONE
import school.base.installer.Setup.InstallationType.SEPARATED_FOLDERS
import java.awt.EventQueue.invokeLater
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Level.SEVERE
import java.util.logging.Logger
import javax.swing.*
import javax.swing.GroupLayout.Alignment.*
import javax.swing.GroupLayout.DEFAULT_SIZE
import javax.swing.GroupLayout.PREFERRED_SIZE
import javax.swing.JFileChooser.APPROVE_OPTION
import javax.swing.JFileChooser.DIRECTORIES_ONLY
import javax.swing.JOptionPane.*
import javax.swing.LayoutStyle.ComponentPlacement.RELATED
import javax.swing.LayoutStyle.ComponentPlacement.UNRELATED
import javax.swing.UIManager.getInstalledLookAndFeels
import javax.swing.UIManager.setLookAndFeel
import kotlin.Short.Companion.MAX_VALUE

/**
 * @author cheroliv
 */
open class Setup : JFrame() {
    private enum class InstallationType {
        ALL_IN_ONE,
        SEPARATED_FOLDERS
    }
    private lateinit var context: ApplicationContext
    private val selectedPaths: MutableMap<String, Path?> = HashMap()
    private var currentInstallationType = ALL_IN_ONE
    private lateinit var allInOneWorkspaceRadioButton: JRadioButton
    private lateinit var browseCommunicationPathButton: JButton
    private lateinit var browseConfigurationPathButton: JButton
    private lateinit var browseEducationPathButton: JButton
    private lateinit var browseOfficePathButton: JButton
    private lateinit var browseWorkspacePathButton: JButton
    private lateinit var browsejobPathButton: JButton
    private lateinit var communicationPathLabel: JLabel
    private lateinit var communicationPathTextField: JTextField
    private lateinit var configurationPathLabel: JLabel
    private lateinit var configurationPathTextField: JTextField
    private lateinit var createWorkspaceButton: JButton
    private lateinit var educationPathLabel: JLabel
    private lateinit var educationPathTextField: JTextField
    private lateinit var installationTypeGroup: ButtonGroup
    private lateinit var jobPathLabel: JLabel
    private lateinit var jobPathTextField: JTextField
    private lateinit var officePathLabel: JLabel
    private lateinit var officePathTextField: JTextField
    private lateinit var separatedEntriesWorkspaceRadioButton: JRadioButton
    private lateinit var titleLabel: JLabel
    private lateinit var workspaceEntriesPanel: JPanel
    private lateinit var workspacePathLabel: JLabel
    private lateinit var workspacePathPanel: JPanel
    private lateinit var workspacePathTextField: JTextField
    private lateinit var workspaceTopPanel: JPanel
    private lateinit var workspaceTypePanel: JPanel
    private lateinit var workspaceTypeSelectorPanel: JPanel

    /**
     * Creates new form Setup
     */
    init {
        initComponents()
        initializeCustomComponents()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            /* Set the Nimbus look and feel */
            try {
                for (info in getInstalledLookAndFeels()) {
                    if ("Nimbus" == info.name) {
                        setLookAndFeel(info.className)
                        break
                    }
                }
            } catch (ex: ClassNotFoundException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            }


            /* Create and display the form */
            invokeLater {
                Setup().run {
                    isVisible = true
                    context = run(Application::class.java, *args)
                }
            }
        }
    }




    private fun initializeCustomComponents() {
        // Group the radio buttons
        installationTypeGroup.add(allInOneWorkspaceRadioButton) // Separated folders
        installationTypeGroup.add(separatedEntriesWorkspaceRadioButton) // All-in-one
        separatedEntriesWorkspaceRadioButton.isSelected = false
        allInOneWorkspaceRadioButton.isSelected = true

        // Add action listeners to radio buttons
        separatedEntriesWorkspaceRadioButton.addActionListener { e: ActionEvent? ->
            handleInstallationTypeChange(ALL_IN_ONE)
        }
        allInOneWorkspaceRadioButton.addActionListener { e: ActionEvent? ->
            handleInstallationTypeChange(SEPARATED_FOLDERS)
        }

        // Add action listeners to browse buttons
        browseWorkspacePathButton.addActionListener { e: ActionEvent? ->
            selectDirectory("workspace", workspacePathTextField)
        }
        browseOfficePathButton.addActionListener { e: ActionEvent? ->
            selectDirectory("office", officePathTextField)
        }
        browseEducationPathButton.addActionListener { e: ActionEvent? ->
            selectDirectory("education", educationPathTextField)
        }
        browseCommunicationPathButton.addActionListener { e: ActionEvent? ->
            selectDirectory("communication", communicationPathTextField)
        }
        browseConfigurationPathButton.addActionListener { e: ActionEvent? ->
            selectDirectory("configuration", configurationPathTextField)
        }
        browsejobPathButton.addActionListener {
            selectDirectory("job", jobPathTextField)
        }

        // Initialize workspace entries panel visibility
        setWorkspaceEntriesVisibility(false)

        // Create workspace button action
        createWorkspaceButton.addActionListener { e: ActionEvent? -> handleCreateWorkspace() }
    }

    private fun handleInstallationTypeChange(type: InstallationType) {
        currentInstallationType = type
        setWorkspaceEntriesVisibility(type == ALL_IN_ONE)

        if (type == ALL_IN_ONE) {
            // Clear all specific paths when switching to all-in-one
            clearSpecificPaths()
        }
    }

    private fun setWorkspaceEntriesVisibility(visible: Boolean) {
        officePathLabel.isVisible = visible
        officePathTextField.isVisible = visible
        browseOfficePathButton.isVisible = visible

        educationPathLabel.isVisible = visible
        educationPathTextField.isVisible = visible
        browseEducationPathButton.isVisible = visible

        communicationPathLabel.isVisible = visible
        communicationPathTextField.isVisible = visible
        browseCommunicationPathButton.isVisible = visible

        configurationPathLabel.isVisible = visible
        configurationPathTextField.isVisible = visible
        browseConfigurationPathButton.isVisible = visible

        jobPathLabel.isVisible = visible
        jobPathTextField.isVisible = visible
        browsejobPathButton.isVisible = visible
    }

    private fun selectDirectory(pathKey: String, textField: JTextField) {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = DIRECTORIES_ONLY
        chooser.dialogTitle = "Select Directory"

        if (chooser.showOpenDialog(this) == APPROVE_OPTION) {
            val selectedFile = chooser.selectedFile
            val selectedPath = selectedFile.toPath()
            selectedPaths[pathKey] = selectedPath
            textField.text = selectedPath.toString()
        }
    }

    private fun clearSpecificPaths() {
        officePathTextField.text = ""
        educationPathTextField.text = ""
        communicationPathTextField.text = ""
        configurationPathTextField.text = ""
        jobPathTextField.text = ""

        selectedPaths.remove("office")
        selectedPaths.remove("education")
        selectedPaths.remove("communication")
        selectedPaths.remove("configuration")
        selectedPaths.remove("job")
    }

    private fun handleCreateWorkspace() {
        val workspacePath = workspacePathTextField.text
        if (workspacePath.isEmpty()) {
            showMessageDialog(
                this,
                "Please select a workspace directory",
                "Validation Error",
                ERROR_MESSAGE
            )
            return
        }

        try {
            if (currentInstallationType == SEPARATED_FOLDERS) {
                createSeparatedFoldersWorkspace()
            } else {
                createAllInOneWorkspace()
            }

            showMessageDialog(
                this,
                "Workspace created successfully!",
                "Success",
                INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            showMessageDialog(
                this,
                "Error creating workspace: " + e.message,
                "Error",
                ERROR_MESSAGE
            )
        }
    }

    private fun createSeparatedFoldersWorkspace() {
        // Validate all required paths are selected
        val requiredPaths = arrayOf("office", "education", "communication", "configuration", "job")
        for (path in requiredPaths) {
            check(!(!selectedPaths.containsKey(path) || selectedPaths[path] == null)) { "All paths must be selected for separated folders installation" }
        }

        // TODO: Implement the actual creation of separated folders
        // You can access the paths using selectedPaths.get("office") etc.
    }

    private fun createAllInOneWorkspace() {
        val workspacePath = Paths.get(workspacePathTextField.text)
        // TODO: Implement the creation of an all-in-one workspace
        // This would typically involve creating subdirectories in the main workspace
    }

    private fun initComponents() {
        installationTypeGroup = ButtonGroup()
        workspaceTopPanel = JPanel()
        titleLabel = JLabel()
        createWorkspaceButton = JButton()
        workspacePathPanel = JPanel()
        workspacePathTextField = JTextField()
        browseWorkspacePathButton = JButton()
        workspacePathLabel = JLabel()
        workspaceTypePanel = JPanel()
        workspaceTypeSelectorPanel = JPanel()
        allInOneWorkspaceRadioButton = JRadioButton()
        separatedEntriesWorkspaceRadioButton = JRadioButton()
        workspaceEntriesPanel = JPanel()
        officePathLabel = JLabel()
        officePathTextField = JTextField()
        browseOfficePathButton = JButton()
        educationPathLabel = JLabel()
        educationPathTextField = JTextField()
        browseEducationPathButton = JButton()
        communicationPathLabel = JLabel()
        communicationPathTextField = JTextField()
        browseCommunicationPathButton = JButton()
        configurationPathLabel = JLabel()
        configurationPathTextField = JTextField()
        browseConfigurationPathButton = JButton()
        jobPathLabel = JLabel()
        jobPathTextField = JTextField()
        browsejobPathButton = JButton()

        defaultCloseOperation = EXIT_ON_CLOSE
        name = "setupFrame" // NOI18N

        titleLabel.text = "School installer"

        createWorkspaceButton.text = "Create"
        createWorkspaceButton.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                createWorkspaceButtonMouseClicked(evt)
            }
        })

        val workspaceTopPanelLayout = GroupLayout(workspaceTopPanel)
        workspaceTopPanel.layout = workspaceTopPanelLayout
        workspaceTopPanelLayout.setHorizontalGroup(
            workspaceTopPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    /* group = */ workspaceTopPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(titleLabel)
                        .addPreferredGap(
                            RELATED,
                            DEFAULT_SIZE,
                            MAX_VALUE.toInt()
                        ).addComponent(createWorkspaceButton)
                )
        )
        workspaceTopPanelLayout.setVerticalGroup(
            workspaceTopPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    workspaceTopPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            workspaceTopPanelLayout.createParallelGroup(BASELINE)
                                .addComponent(titleLabel, PREFERRED_SIZE, 43, PREFERRED_SIZE)
                                .addComponent(createWorkspaceButton)
                        )
                        .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                )
        )

        workspacePathPanel.border = BorderFactory.createTitledBorder("Workspace")

        browseWorkspacePathButton.text = "Select directory"

        workspacePathLabel.text = "Path"

        val workspacePathPanelLayout = GroupLayout(workspacePathPanel)
        workspacePathPanel.layout = workspacePathPanelLayout
        workspacePathPanelLayout.setHorizontalGroup(
            workspacePathPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    workspacePathPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(workspacePathLabel, PREFERRED_SIZE, 52, PREFERRED_SIZE)
                        .addPreferredGap(RELATED)
                        .addComponent(workspacePathTextField)
                        .addPreferredGap(RELATED)
                        .addComponent(browseWorkspacePathButton)
                        .addContainerGap()
                )
        )
        workspacePathPanelLayout.setVerticalGroup(
            workspacePathPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    workspacePathPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            workspacePathPanelLayout.createParallelGroup(BASELINE)
                                .addComponent(
                                    workspacePathTextField,
                                    PREFERRED_SIZE,
                                    DEFAULT_SIZE,
                                    PREFERRED_SIZE
                                )
                                .addComponent(browseWorkspacePathButton)
                                .addComponent(workspacePathLabel)
                        )
                        .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                )
        )

        workspaceTypePanel.border = BorderFactory.createTitledBorder("Installation type")

        allInOneWorkspaceRadioButton.text = "All-in-one"

        separatedEntriesWorkspaceRadioButton.text = "Separated folders"

        val workspaceTypeSelectorPanelLayout = GroupLayout(workspaceTypeSelectorPanel)
        workspaceTypeSelectorPanel.layout = workspaceTypeSelectorPanelLayout
        workspaceTypeSelectorPanelLayout.setHorizontalGroup(
            workspaceTypeSelectorPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    workspaceTypeSelectorPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(allInOneWorkspaceRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(separatedEntriesWorkspaceRadioButton)
                        .addContainerGap(508, MAX_VALUE.toInt())
                )
        )
        workspaceTypeSelectorPanelLayout.setVerticalGroup(
            workspaceTypeSelectorPanelLayout.createParallelGroup(LEADING)
                .addGroup(
                    workspaceTypeSelectorPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            workspaceTypeSelectorPanelLayout.createParallelGroup(BASELINE)
                                .addComponent(separatedEntriesWorkspaceRadioButton)
                                .addComponent(allInOneWorkspaceRadioButton)
                        )
                        .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                )
        )

        officePathLabel.text = "Office"
        officePathLabel.toolTipText = ""

        browseOfficePathButton.text = "Select directory"

        educationPathLabel.text = "Education"
        educationPathLabel.toolTipText = ""

        browseEducationPathButton.text = "Select directory"

        communicationPathLabel.text = "Communication"
        communicationPathLabel.toolTipText = ""

        browseCommunicationPathButton.text = "Select directory"

        configurationPathLabel.text = "Configuration"
        configurationPathLabel.toolTipText = ""

        browseConfigurationPathButton.text = "Select directory"

        jobPathLabel.text = "Job"
        jobPathLabel.toolTipText = ""

        browsejobPathButton.text = "Select directory"

        val workspaceEntriesPanelLayout = GroupLayout(workspaceEntriesPanel)
        workspaceEntriesPanel.layout = workspaceEntriesPanelLayout
        workspaceEntriesPanelLayout.setHorizontalGroup(
            workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                .addGap(0, 912, MAX_VALUE.toInt())
                .addGroup(
                    workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            workspaceEntriesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(
                                        LEADING,
                                        false
                                    )
                                        .addComponent(
                                            officePathLabel, DEFAULT_SIZE, DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            educationPathLabel,
                                            TRAILING,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            communicationPathLabel,
                                            TRAILING,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            configurationPathLabel,
                                            TRAILING,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            jobPathLabel,
                                            TRAILING,
                                            PREFERRED_SIZE,
                                            190,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addPreferredGap(RELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                                        .addComponent(
                                            officePathTextField,
                                            TRAILING,
                                            DEFAULT_SIZE,
                                            475,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(educationPathTextField)
                                        .addComponent(communicationPathTextField)
                                        .addComponent(configurationPathTextField)
                                        .addComponent(jobPathTextField)
                                )
                                .addPreferredGap(RELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                                        .addComponent(browseEducationPathButton)
                                        .addComponent(browseOfficePathButton, TRAILING)
                                        .addComponent(browseCommunicationPathButton)
                                        .addComponent(browseConfigurationPathButton)
                                        .addComponent(browsejobPathButton)
                                )
                                .addContainerGap()
                        )
                )
        )
        workspaceEntriesPanelLayout.setVerticalGroup(
            workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                .addGap(0, 269, MAX_VALUE.toInt())
                .addGroup(
                    workspaceEntriesPanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            workspaceEntriesPanelLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(BASELINE)
                                        .addComponent(
                                            officePathLabel,
                                            PREFERRED_SIZE,
                                            42,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(
                                            browseOfficePathButton,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            officePathTextField,
                                            PREFERRED_SIZE,
                                            DEFAULT_SIZE,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addPreferredGap(UNRELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(BASELINE)
                                        .addComponent(
                                            browseEducationPathButton,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            educationPathTextField,
                                            PREFERRED_SIZE,
                                            DEFAULT_SIZE,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(
                                            educationPathLabel,
                                            PREFERRED_SIZE,
                                            42,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addPreferredGap(UNRELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(BASELINE)
                                        .addComponent(
                                            browseCommunicationPathButton,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            communicationPathTextField,
                                            PREFERRED_SIZE,
                                            DEFAULT_SIZE,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(
                                            communicationPathLabel,
                                            PREFERRED_SIZE,
                                            42,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addPreferredGap(UNRELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(BASELINE)
                                        .addComponent(
                                            browseConfigurationPathButton,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            configurationPathTextField,
                                            PREFERRED_SIZE,
                                            DEFAULT_SIZE,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(
                                            configurationPathLabel,
                                            PREFERRED_SIZE,
                                            42,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addPreferredGap(UNRELATED)
                                .addGroup(
                                    workspaceEntriesPanelLayout.createParallelGroup(BASELINE)
                                        .addComponent(
                                            browsejobPathButton,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addComponent(
                                            jobPathTextField,
                                            PREFERRED_SIZE,
                                            DEFAULT_SIZE,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(
                                            jobPathLabel,
                                            PREFERRED_SIZE,
                                            42,
                                            PREFERRED_SIZE
                                        )
                                )
                                .addGap(3, 3, 3)
                        )
                )
        )

        val workspaceTypePanelLayout = GroupLayout(workspaceTypePanel)
        workspaceTypePanel.layout = workspaceTypePanelLayout
        workspaceTypePanelLayout.setHorizontalGroup(
            workspaceTypePanelLayout.createParallelGroup(LEADING)
                .addGap(0, 924, MAX_VALUE.toInt())
                .addGroup(
                    workspaceTypePanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            TRAILING, workspaceTypePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(
                                    workspaceTypeSelectorPanel,
                                    DEFAULT_SIZE,
                                    DEFAULT_SIZE,
                                    MAX_VALUE.toInt()
                                )
                                .addContainerGap()
                        )
                )
                .addGroup(
                    workspaceTypePanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            workspaceTypePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(
                                    workspaceEntriesPanel,
                                    DEFAULT_SIZE,
                                    DEFAULT_SIZE,
                                    MAX_VALUE.toInt()
                                ).addContainerGap()
                        )
                )
        )
        workspaceTypePanelLayout.setVerticalGroup(
            workspaceTypePanelLayout.createParallelGroup(LEADING)
                .addGap(0, 344, MAX_VALUE.toInt())
                .addGroup(
                    workspaceTypePanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            workspaceTypePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(
                                    workspaceTypeSelectorPanel,
                                    PREFERRED_SIZE,
                                    DEFAULT_SIZE,
                                    PREFERRED_SIZE
                                )
                                .addContainerGap(263, MAX_VALUE.toInt())
                        )
                )
                .addGroup(
                    workspaceTypePanelLayout.createParallelGroup(LEADING)
                        .addGroup(
                            TRAILING, workspaceTypePanelLayout.createSequentialGroup()
                                .addContainerGap(69, MAX_VALUE.toInt())
                                .addComponent(
                                    workspaceEntriesPanel,
                                    PREFERRED_SIZE,
                                    DEFAULT_SIZE,
                                    PREFERRED_SIZE
                                )
                                .addContainerGap()
                        )
                )
        )

        val layout = GroupLayout(contentPane)
        contentPane.layout = layout
        layout.setHorizontalGroup(
            layout.createParallelGroup(LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(
                            workspaceTopPanel, DEFAULT_SIZE, DEFAULT_SIZE,
                            MAX_VALUE.toInt()
                        )
                        .addContainerGap()
                )
                .addComponent(
                    workspacePathPanel, DEFAULT_SIZE, DEFAULT_SIZE,
                    MAX_VALUE.toInt()
                )
                .addComponent(
                    workspaceTypePanel, DEFAULT_SIZE, DEFAULT_SIZE,
                    MAX_VALUE.toInt()
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(
                            workspaceTopPanel,
                            PREFERRED_SIZE,
                            DEFAULT_SIZE,
                            PREFERRED_SIZE
                        )
                        .addPreferredGap(RELATED)
                        .addComponent(
                            workspacePathPanel,
                            PREFERRED_SIZE,
                            DEFAULT_SIZE,
                            PREFERRED_SIZE
                        )
                        .addPreferredGap(RELATED)
                        .addComponent(
                            workspaceTypePanel,
                            PREFERRED_SIZE,
                            DEFAULT_SIZE,
                            PREFERRED_SIZE
                        )
                        .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                )
        )

        pack()
    }

    private fun createWorkspaceButtonMouseClicked(evt: MouseEvent) {
        // TODO add your handling code here:
        Arrays.stream(context.beanDefinitionNames)
            .sequential()
            .forEach { x: String? -> println(x) }
    }

}
