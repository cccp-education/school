package workspace

//import school.base.utils.Log
//import school.base.utils.Log.i
import workspace.Log.i
import workspace.Workspace.*
import workspace.Workspace.InstallationType.ALL_IN_ONE
import workspace.Workspace.InstallationType.SEPARATED_FOLDERS
import java.nio.file.Path
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
    private val selectedPaths: MutableMap<String, Path?> = HashMap(),
    private var currentInstallationType: InstallationType = ALL_IN_ONE,
    private val communicationPathLabel: JLabel = JLabel("Communication").apply { toolTipText = "" },
    private val communicationPathTextField: JTextField = JTextField(),
    private val configurationPathLabel: JLabel = JLabel("Configuration").apply { toolTipText = "" },
    private val configurationPathTextField: JTextField = JTextField(),
    private val educationPathLabel: JLabel = JLabel("Education").apply { toolTipText = "" },
    private val educationPathTextField: JTextField = JTextField(),
    private val jobPathLabel: JLabel = JLabel("Job").apply { toolTipText = "" },
    private val jobPathTextField: JTextField = JTextField(),
    private val officePathLabel: JLabel = JLabel("Office").apply { toolTipText = "" },
    private val officePathTextField: JTextField = JTextField(),
    private val titleLabel: JLabel = JLabel("School installer"),
    private val workspacePathLabel: JLabel = JLabel("Path"),
    private val workspacePathTextField: JTextField = JTextField(),
    private val workspaceTypePanel: JPanel = JPanel().apply {
        border = createTitledBorder("Installation type")
    },
    private val workspaceTypeSelectorPanel: JPanel = JPanel(),
    private val workspaceTopPanel: JPanel = JPanel(),
    private val workspacePathPanel: JPanel = JPanel().apply { border = createTitledBorder("Workspace") },
    private val workspaceEntriesPanel: JPanel = JPanel(),
    private val splitWorkspaceRadioButton: JRadioButton = JRadioButton("Separated folders")
        .apply { isSelected = false },
    private val allInOneWorkspaceRadioButton: JRadioButton = JRadioButton("All-in-one").apply { isSelected = true },
    private val browseCommunicationPathButton: JButton = JButton(),
    private val browseConfigurationPathButton: JButton = JButton(),
    private val browseEducationPathButton: JButton = JButton(),
    private val browseOfficePathButton: JButton = JButton(),
    private val browseWorkspacePathButton: JButton = JButton(),
    private val browseJobPathButton: JButton = JButton(),
    private val createWorkspaceButton: JButton = JButton("Create"),
    private val installationTypeGroup: ButtonGroup = ButtonGroup().apply {
        add(allInOneWorkspaceRadioButton)
        add(splitWorkspaceRadioButton)
    },
) : JFrame("School Project SetupSwingFrame") {
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
                "Creating workspace... : $currentInstallationType".run(::i)
                if (currentInstallationType == SEPARATED_FOLDERS) arrayOf(
                    "office",
                    "education",
                    "communication",
                    "configuration",
                    "job"
                ).forEach {
                    check(selectedPaths.containsKey(it) && selectedPaths[it] != null) {
                        "All paths must be selected for separated folders installation"
                    }
                }
                WorkspaceConfig(
                    basePath = selectedPaths["workspace"]!!,
                    type = currentInstallationType,
                    subPaths = selectedPaths.map { (key, value) -> key to value!! }.toMap()
                ).run(WorkspaceManager::createWorkspace)
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
        "Installation type changed to $type".run(::i)
        setWorkspaceEntriesVisibility(type == SEPARATED_FOLDERS)
        if (type == ALL_IN_ONE) clearSpecificPaths()
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
        browseJobPathButton.addActionListener { selectDirectory("job", jobPathTextField) }
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
        browseJobPathButton
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
            browseJobPathButton,
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
                                                .addComponent(browseJobPathButton)
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
                                                    browseJobPathButton,
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