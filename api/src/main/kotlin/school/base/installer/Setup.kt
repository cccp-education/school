package school.base.installer

import org.springframework.context.ApplicationContext
import school.base.installer.Setup.InstallationType.ALL_IN_ONE
import school.base.installer.Setup.InstallationType.SEPARATED_FOLDERS
import school.base.utils.Log
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.*
import javax.swing.BorderFactory.createTitledBorder
import javax.swing.JOptionPane.ERROR_MESSAGE
import javax.swing.JOptionPane.showMessageDialog

/**
 * @author cheroliv
 */
class Setup(
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
) : JFrame("School Project Setup") {

    // Service pour gérer les opérations sur le workspace
    private val workspaceService = WorkspaceService()

    init {
        initUI().let { "Init, currentInstallationType : $currentInstallationType".run(Log::i) }
    }

    enum class InstallationType {
        ALL_IN_ONE,
        SEPARATED_FOLDERS
    }

    private data class WorkspaceConfig(
        val basePath: Path,
        val type: InstallationType,
        val subPaths: Map<String, Path> = emptyMap()
    )

    // Service class for workspace operations
    private inner class WorkspaceService {
        fun createWorkspace(config: WorkspaceConfig) {
            when (config.type) {
                ALL_IN_ONE -> createAllInOneWorkspace(config.basePath)
                SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace(config.basePath, config.subPaths)
            }
        }

        private fun createAllInOneWorkspace(basePath: Path) {
            val directories = listOf("office", "education", "communication", "configuration", "job")
            directories.forEach { dir ->
                createDirectory(basePath.resolve(dir))
            }
            createConfigFiles(basePath)
        }

        private fun createSeparatedFoldersWorkspace(basePath: Path, subPaths: Map<String, Path>) {
            subPaths.forEach { (name, path) ->
                createDirectory(path)
                createConfigFiles(path)
            }
        }

        private fun createDirectory(path: Path) {
            path.toFile().mkdirs()
        }

        private fun createConfigFiles(basePath: Path) {
            // Création des fichiers de configuration nécessaires
            File(basePath.toFile(), "application.properties").writeText(
                """
                school.workspace.path=${basePath}
                # Add other configuration properties here
            """.trimIndent()
            )
        }
    }

    private fun Setup.clearSpecificPaths() {
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

    private fun Setup.handleCreateWorkspace() {
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
                Log.i("Creating workspace... : $currentInstallationType")
                when {
                    currentInstallationType == SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace()
                    else -> createAllInOneWorkspace()
                }

                showMessageDialog(
                    this,
                    "Workspace created successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
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

    private fun Setup.createSeparatedFoldersWorkspace() {
        // Validate all required paths are selected
        val requiredPaths = arrayOf("office", "education", "communication", "configuration", "job")
        for (path in requiredPaths) {
            check(!(!selectedPaths.containsKey(path) || selectedPaths[path] == null)) { "All paths must be selected for separated folders installation" }
        }

        // TODO: Implement the actual creation of separated folders
        // You can access the paths using selectedPaths.get("office") etc.
    }

    private fun Setup.selectDirectory(
        pathKey: String,
        textField: JTextField
    ) = JFileChooser().run {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        dialogTitle = "Select Directory"
        when (JFileChooser.APPROVE_OPTION) {
            showOpenDialog(this) -> selectedFile.toPath().run {
                selectedPaths[pathKey] = this
                textField.text = toString()
            }
        }
    }

    private fun Setup.handleInstallationTypeChange(type: Setup.InstallationType) {
        "currentInstallationType : $currentInstallationType".run(Log::i)
        currentInstallationType = type
        "Installation type changed to $type".run(Log::i)
        setWorkspaceEntriesVisibility(type == SEPARATED_FOLDERS)
        if (type == ALL_IN_ONE) {
            // Clear all specific paths when switching to all-in-one
            clearSpecificPaths()
        }
    }

    private fun Setup.createAllInOneWorkspace() {
        val workspacePath = Paths.get(workspacePathTextField.text)
        // TODO: Implement the creation of an all-in-one workspace
        // This would typically involve creating subdirectories in the main workspace
    }

    private fun Setup.addListeners(): Setup {
        splitWorkspaceRadioButton.addActionListener { handleInstallationTypeChange(SEPARATED_FOLDERS) }
        allInOneWorkspaceRadioButton.addActionListener { handleInstallationTypeChange(ALL_IN_ONE) }
        browseCommunicationPathButton.addActionListener {
            selectDirectory(
                "communication",
                communicationPathTextField
            )
        }
        browseConfigurationPathButton.addActionListener {
            selectDirectory(
                "configuration",
                configurationPathTextField
            )
        }
        browseEducationPathButton.addActionListener { selectDirectory("education", educationPathTextField) }
        browseOfficePathButton.addActionListener { selectDirectory("office", officePathTextField) }
        browseWorkspacePathButton.addActionListener { selectDirectory("workspace", workspacePathTextField) }
        browsejobPathButton.addActionListener { selectDirectory("job", jobPathTextField) }
        createWorkspaceButton.addActionListener { handleCreateWorkspace() }
        installationTypeGroup.selection.addActionListener {
        }
        return this
    }

    private fun Setup.setWorkspaceEntriesVisibility(visible: Boolean): Setup = setOf(
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

    internal fun Setup.initUI() {
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
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 924, Short.MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    GroupLayout.Alignment.TRAILING, createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(
                                            workspaceTypeSelectorPanel,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE.toInt()
                                        )
                                        .addContainerGap()
                                )
                        )
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(
                                            workspaceEntriesPanel,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            Short.MAX_VALUE.toInt()
                                        )
                                        .addContainerGap()
                                )
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 344, Short.MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(
                                            workspaceTypeSelectorPanel,
                                            GroupLayout.PREFERRED_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.PREFERRED_SIZE
                                        )
                                        .addContainerGap(263, Short.MAX_VALUE.toInt())
                                )
                        )
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    GroupLayout.Alignment.TRAILING, createSequentialGroup()
                                        .addContainerGap(69, Short.MAX_VALUE.toInt())
                                        .addComponent(
                                            workspaceEntriesPanel,
                                            GroupLayout.PREFERRED_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.PREFERRED_SIZE
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
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addComponent(
                                    workspaceTopPanel,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt()
                                )
                                .addContainerGap()
                        )
                        .addComponent(
                            workspacePathPanel,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt()
                        )
                        .addComponent(
                            workspaceTypePanel,
                            GroupLayout.DEFAULT_SIZE,
                            GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt()
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addComponent(
                                    workspaceTopPanel,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(
                                    workspacePathPanel,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(
                                    workspaceTypePanel,
                                    GroupLayout.PREFERRED_SIZE,
                                    GroupLayout.DEFAULT_SIZE,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspaceTypeSelectorPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(allInOneWorkspaceRadioButton)
                                .addGap(18, 18, 18)
                                .addComponent(splitWorkspaceRadioButton)
                                .addContainerGap(508, Short.MAX_VALUE.toInt())
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(splitWorkspaceRadioButton)
                                        .addComponent(allInOneWorkspaceRadioButton)
                                )
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspaceTopPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(titleLabel)
                                .addPreferredGap(
                                    LayoutStyle.ComponentPlacement.RELATED,
                                    GroupLayout.DEFAULT_SIZE,
                                    Short.MAX_VALUE.toInt()
                                ).addComponent(createWorkspaceButton)
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(
                                            titleLabel,
                                            GroupLayout.PREFERRED_SIZE, 43,
                                            GroupLayout.PREFERRED_SIZE
                                        )
                                        .addComponent(createWorkspaceButton)
                                )
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspacePathPanel.apply panel@{
            border = createTitledBorder("Workspace")
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addComponent(
                                    workspacePathLabel,
                                    GroupLayout.PREFERRED_SIZE, 52,
                                    GroupLayout.PREFERRED_SIZE
                                )
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(workspacePathTextField)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseWorkspacePathButton)
                                .addContainerGap()
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(
                            createSequentialGroup()
                                .addContainerGap()
                                .addGroup(
                                    createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(
                                            workspacePathTextField,
                                            GroupLayout.PREFERRED_SIZE,
                                            GroupLayout.DEFAULT_SIZE,
                                            GroupLayout.PREFERRED_SIZE
                                        )
                                        .addComponent(browseWorkspacePathButton)
                                        .addComponent(workspacePathLabel)
                                )
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        )
                )
            }
        }
        workspaceEntriesPanel.apply panel@{
            run(::GroupLayout).run {
                this@panel.layout = this
                setHorizontalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 912, Short.MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(
                                                    officePathLabel,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    educationPathLabel,
                                                    GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    communicationPathLabel,
                                                    GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    configurationPathLabel,
                                                    GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    jobPathLabel,
                                                    GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    190,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(
                                                    officePathTextField,
                                                    GroupLayout.Alignment.TRAILING,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    475,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(educationPathTextField)
                                                .addComponent(communicationPathTextField)
                                                .addComponent(configurationPathTextField)
                                                .addComponent(jobPathTextField)
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(browseEducationPathButton)
                                                .addComponent(browseOfficePathButton, GroupLayout.Alignment.TRAILING)
                                                .addComponent(browseCommunicationPathButton)
                                                .addComponent(browseConfigurationPathButton)
                                                .addComponent(browsejobPathButton)
                                        )
                                        .addContainerGap()
                                )
                        )
                )
                setVerticalGroup(
                    createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 269, Short.MAX_VALUE.toInt())
                        .addGroup(
                            createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(
                                    createSequentialGroup()
                                        .addGap(3, 3, 3)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                    officePathLabel,
                                                    GroupLayout.PREFERRED_SIZE, 42,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                                .addComponent(
                                                    browseOfficePathButton,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    officePathTextField,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                    browseEducationPathButton,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    educationPathTextField,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                                .addComponent(
                                                    educationPathLabel,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    42,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                    browseCommunicationPathButton,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    communicationPathTextField,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                                .addComponent(
                                                    communicationPathLabel,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    42,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                    browseConfigurationPathButton,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    configurationPathTextField,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                                .addComponent(
                                                    configurationPathLabel,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    42,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                        )
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(
                                            createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                    browsejobPathButton,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE.toInt()
                                                )
                                                .addComponent(
                                                    jobPathTextField,
                                                    GroupLayout.PREFERRED_SIZE,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    GroupLayout.PREFERRED_SIZE
                                                )
                                                .addComponent(
                                                    jobPathLabel,
                                                    GroupLayout.PREFERRED_SIZE, 42,
                                                    GroupLayout.PREFERRED_SIZE
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