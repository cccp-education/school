package school.base.installer

import org.springframework.context.ApplicationContext
import school.base.installer.WorkspaceService.InstallationType
import school.base.installer.WorkspaceService.InstallationType.ALL_IN_ONE
import school.base.installer.WorkspaceService.InstallationType.SEPARATED_FOLDERS
import school.base.installer.WorkspaceService.WorkspaceConfig
import school.base.utils.Log
import school.base.utils.Log.i
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*
import javax.swing.BorderFactory.createTitledBorder
import javax.swing.GroupLayout.Alignment.*
import javax.swing.GroupLayout.DEFAULT_SIZE
import javax.swing.GroupLayout.PREFERRED_SIZE
import javax.swing.JFileChooser.APPROVE_OPTION
import javax.swing.JFileChooser.DIRECTORIES_ONLY
import javax.swing.JOptionPane.*
import javax.swing.LayoutStyle.ComponentPlacement.RELATED
import javax.swing.LayoutStyle.ComponentPlacement.UNRELATED
import kotlin.Short.Companion.MAX_VALUE

class SetupSwingFrame(
    private val context: ApplicationContext,
    val selectedPaths: MutableMap<String, Path?> = HashMap(),
    internal var currentInstallationType: InstallationType = ALL_IN_ONE,
    internal val communicationPathLabel: JLabel = JLabel("Communication").apply { toolTipText = "" },
    internal val communicationPathTextField: JTextField = JTextField(),
    internal val configurationPathLabel: JLabel = JLabel("Configuration").apply { toolTipText = "" },
    internal val configurationPathTextField: JTextField = JTextField(),
    internal val educationPathLabel: JLabel = JLabel("Education").apply { toolTipText = "" },
    internal val educationPathTextField: JTextField = JTextField(),
    internal val jobPathLabel: JLabel = JLabel("Job").apply { toolTipText = "" },
    internal val jobPathTextField: JTextField = JTextField(),
    internal val officePathLabel: JLabel = JLabel("Office").apply { toolTipText = "" },
    internal val officePathTextField: JTextField = JTextField(),
    internal val titleLabel: JLabel = JLabel("School installer"),
    internal val workspacePathLabel: JLabel = JLabel("Path"),
    internal val workspacePathTextField: JTextField = JTextField(),
    internal val workspaceTypePanel: JPanel = JPanel().apply {
        border = createTitledBorder("Installation type")
    },
    internal val workspaceTypeSelectorPanel: JPanel = JPanel(),
    internal val workspaceTopPanel: JPanel = JPanel(),
    internal val workspacePathPanel: JPanel = JPanel().apply { border = createTitledBorder("Workspace") },
    internal val workspaceEntriesPanel: JPanel = JPanel(),
    internal val splitWorkspaceRadioButton: JRadioButton = JRadioButton("Separated folders")
        .apply { isSelected = false },
    internal val allInOneWorkspaceRadioButton: JRadioButton = JRadioButton("All-in-one").apply { isSelected = true },
    internal val browseCommunicationPathButton: JButton = JButton(),
    internal val browseConfigurationPathButton: JButton = JButton(),
    internal val browseEducationPathButton: JButton = JButton(),
    internal val browseOfficePathButton: JButton = JButton(),
    internal val browseWorkspacePathButton: JButton = JButton(),
    internal val browsejobPathButton: JButton = JButton(),
    internal val createWorkspaceButton: JButton = JButton("Create"),
    internal val installationTypeGroup: ButtonGroup = ButtonGroup().apply {
        add(allInOneWorkspaceRadioButton)
        add(splitWorkspaceRadioButton)
    },
) : JFrame("School Project SetupSwingFrame") {

    // Service pour gérer les opérations sur le workspace
    private val workspaceService by lazy { context.run(::WorkspaceService) }

    init {
        initUI().let { "Init, currentInstallationType : $currentInstallationType".run(Log::i) }
    }

    private fun SetupSwingFrame.clearSpecificPaths() {
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

    private fun SetupSwingFrame.handleCreateWorkspace() {
        when {
            workspacePathTextField.text.isEmpty() -> {
                showMessageDialog(
                    this,
                    "Please select a workspace directory",
                    "Validation Error",
                    ERROR_MESSAGE
                )
                return
            }

            else -> try {
                i("Creating workspace... : $currentInstallationType")
                when {
                    currentInstallationType == SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace()
                    else -> createAllInOneWorkspace()
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
    }

    private fun SetupSwingFrame.createSeparatedFoldersWorkspace() {
        // Validate all required paths are selected
        val requiredPaths = arrayOf("office", "education", "communication", "configuration", "job")
        for (path in requiredPaths) {
            check(!(!selectedPaths.containsKey(path) || selectedPaths[path] == null)) { "All paths must be selected for separated folders installation" }
        }
        // TODO: Implement the actual creation of separated folders
        // You can access the paths using selectedPaths.get("office") etc.
//        workspaceService.createWorkspace(
////            WorkspaceConfig(
////                basePath = "workspace",
////                type = currentInstallationType,
////            subPaths = selectedPaths)
//
//        )
    }

    private fun SetupSwingFrame.selectDirectory(
        pathKey: String,
        textField: JTextField
    ) = JFileChooser().run {
        fileSelectionMode = DIRECTORIES_ONLY
        dialogTitle = "Select Directory"
        when (APPROVE_OPTION) {
            showOpenDialog(this) -> selectedFile.toPath().run {
                selectedPaths[pathKey] = this
                textField.text = toString()
            }
        }
    }

    private fun SetupSwingFrame.handleInstallationTypeChange(type: InstallationType) {
        "currentInstallationType : $currentInstallationType".run(Log::i)
        currentInstallationType = type
        "Installation type changed to $type".run(Log::i)
        setWorkspaceEntriesVisibility(type == SEPARATED_FOLDERS)
        if (type == ALL_IN_ONE) {
            // Clear all specific paths when switching to all-in-one
            clearSpecificPaths()
        }
    }

    private fun SetupSwingFrame.createAllInOneWorkspace() {
        val workspacePath = Paths.get(workspacePathTextField.text)
        // TODO: Implement the creation of an all-in-one workspace
        // This would typically involve creating subdirectories in the main workspace
    }

    private fun SetupSwingFrame.addListeners(): SetupSwingFrame {
        splitWorkspaceRadioButton.addActionListener { handleInstallationTypeChange(SEPARATED_FOLDERS) }
        allInOneWorkspaceRadioButton.addActionListener { handleInstallationTypeChange(ALL_IN_ONE) }
        browseCommunicationPathButton.addActionListener {
            selectDirectory("communication", communicationPathTextField)
        }
        browseConfigurationPathButton.addActionListener {
            selectDirectory("configuration", configurationPathTextField)
        }
        browseEducationPathButton.addActionListener { selectDirectory("education", educationPathTextField) }
        browseOfficePathButton.addActionListener { selectDirectory("office", officePathTextField) }
        browseWorkspacePathButton.addActionListener { selectDirectory("workspace", workspacePathTextField) }
        browsejobPathButton.addActionListener { selectDirectory("job", jobPathTextField) }
        /*
            action sur bouton create workspace
         */
        createWorkspaceButton.addActionListener { handleCreateWorkspace() }
        installationTypeGroup.selection.addActionListener {
        }
        return this
    }

    private fun SetupSwingFrame.setWorkspaceEntriesVisibility(
        visible: Boolean
    ): SetupSwingFrame = setOf(
        officePathLabel,
        officePathTextField,
        browseOfficePathButton,
        educationPathLabel,
        educationPathTextField,
        browseEducationPathButton,
        communicationPathLabel,
        communicationPathTextField,
        browseCommunicationPathButton,
        configurationPathLabel,
        configurationPathTextField,
        browseConfigurationPathButton,
        jobPathLabel,
        jobPathTextField,
        browsejobPathButton
    ).map { it.isVisible = visible }
        .run { this@setWorkspaceEntriesVisibility }

    internal fun SetupSwingFrame.initUI() {
        name = "setupFrame" // NOI18N
        defaultCloseOperation = EXIT_ON_CLOSE
        setWorkspaceEntriesVisibility(false)
        mutableSetOf(
            browseEducationPathButton,
            browseOfficePathButton,
            browseCommunicationPathButton,
            browseWorkspacePathButton,
            browseConfigurationPathButton,
            browsejobPathButton,
        ).onEach { "Select directory".run(it::setText) }
        workspaceTypePanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGap(0, 924, MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(LEADING)
                                .addGroup(
                                    TRAILING, createSequentialGroup()
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
                            createParallelGroup(LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(
                                            workspaceEntriesPanel,
                                            DEFAULT_SIZE,
                                            DEFAULT_SIZE,
                                            MAX_VALUE.toInt()
                                        )
                                        .addContainerGap()
                                )
                        )
                )
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGap(0, 344, MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(LEADING)
                                .addGroup(
                                    createSequentialGroup()
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
                            createParallelGroup(LEADING)
                                .addGroup(
                                    TRAILING, createSequentialGroup()
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
            }
        }
        contentPane.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addComponent(
                                    workspaceTopPanel,
                                    DEFAULT_SIZE,
                                    DEFAULT_SIZE, MAX_VALUE.toInt()
                                )
                                .addContainerGap()
                        )
                        .addComponent(
                            workspacePathPanel,
                            DEFAULT_SIZE,
                            DEFAULT_SIZE, MAX_VALUE.toInt()
                        )
                        .addComponent(
                            workspaceTypePanel,
                            DEFAULT_SIZE,
                            DEFAULT_SIZE, MAX_VALUE.toInt()
                        )
                )
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
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
            }
        }
        workspaceTypeSelectorPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(allInOneWorkspaceRadioButton)
                                .addGap(18, 18, 18)
                                .addComponent(splitWorkspaceRadioButton)
                                .addContainerGap(508, MAX_VALUE.toInt())
                        )
                )
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(BASELINE)
                                        .addComponent(splitWorkspaceRadioButton)
                                        .addComponent(allInOneWorkspaceRadioButton)
                                )
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspaceTopPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(titleLabel)
                                .addPreferredGap(
                                    RELATED,
                                    DEFAULT_SIZE,
                                    MAX_VALUE.toInt()
                                ).addComponent(createWorkspaceButton)
                        )
                )
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(BASELINE)
                                        .addComponent(
                                            titleLabel,
                                            PREFERRED_SIZE,
                                            43,
                                            PREFERRED_SIZE
                                        )
                                        .addComponent(createWorkspaceButton)
                                )
                                .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspacePathPanel.apply panel@{
            border = createTitledBorder("Workspace")
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(
                                    workspacePathLabel,
                                    PREFERRED_SIZE, 52,
                                    PREFERRED_SIZE
                                )
                                .addPreferredGap(RELATED)
                                .addComponent(workspacePathTextField)
                                .addPreferredGap(RELATED)
                                .addComponent(browseWorkspacePathButton)
                                .addContainerGap()
                        )
                )
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(BASELINE)
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
            }
        }
        workspaceEntriesPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(LEADING)
                        .addGap(0, 912, MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                            createParallelGroup(LEADING, false)
                                                .addComponent(
                                                    officePathLabel,
                                                    DEFAULT_SIZE,
                                                    DEFAULT_SIZE,
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
                                            createParallelGroup(LEADING)
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
                                            createParallelGroup(LEADING)
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
                setVerticalGroup(
                    createParallelGroup(LEADING)
                        .addGap(0, 269, MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addGroup(
                                            createParallelGroup(BASELINE)
                                                .addComponent(
                                                    officePathLabel,
                                                    PREFERRED_SIZE, 42,
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
                                            createParallelGroup(BASELINE)
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
                                            createParallelGroup(BASELINE)
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
                                            createParallelGroup(BASELINE)
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
                                            createParallelGroup(BASELINE)
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
                                                    PREFERRED_SIZE, 42,
                                                    PREFERRED_SIZE
                                                )
                                        )
                                        .addGap(3, 3, 3)
                                )
                        )
                )
            }
        }
        addListeners().pack()
    }
}