package school

import org.springframework.boot.SpringApplication.run
import org.springframework.context.ApplicationContext
import school.Setup.InstallationType.ALL_IN_ONE
import school.Setup.InstallationType.SEPARATED_FOLDERS
import school.Setup.SetupHelper.addListeners
import school.Setup.SetupHelper.initUI
import school.base.utils.Log.i
import java.awt.EventQueue.invokeLater
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger
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

/**
 * @author cheroliv
 */
class Setup(
    private val context: ApplicationContext,
    private val selectedPaths: MutableMap<String, Path?> = HashMap(),
    private var currentInstallationType: InstallationType = ALL_IN_ONE,
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
    internal val workspaceTypePanel: JPanel = JPanel().apply { border = createTitledBorder("Installation type") },
    internal val workspaceTypeSelectorPanel: JPanel = JPanel(),
    internal val workspaceTopPanel: JPanel = JPanel(),
    internal val workspacePathPanel: JPanel = JPanel().apply { border = createTitledBorder("Workspace") },
    internal val workspaceEntriesPanel: JPanel = JPanel(),
    internal val splitWorkspaceRadioButton: JRadioButton = JRadioButton("Separated folders").apply {
        isSelected = false
    },
    internal val allInOneWorkspaceRadioButton: JRadioButton = JRadioButton("All-in-one").apply { isSelected = true },
    internal val browseCommunicationPathButton: JButton = JButton(),
    internal val browseConfigurationPathButton: JButton = JButton(),
    internal val browseEducationPathButton: JButton = JButton(),
    internal val browseOfficePathButton: JButton = JButton(),
    internal val browseWorkspacePathButton: JButton = JButton(),
    internal val browsejobPathButton: JButton = JButton(),
    internal val createWorkspaceButton: JButton = JButton("Create"),
    private val installationTypeGroup: ButtonGroup = ButtonGroup().apply {
        add(allInOneWorkspaceRadioButton)
        add(splitWorkspaceRadioButton)
    },
) : JFrame("School Project Setup") {

    // Service pour gérer les opérations sur le workspace
    private val workspaceService = WorkspaceService()

    init {
        initUI().let { "Init, currentInstallationType : $currentInstallationType".run(::i) }
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

    object SetupHelper {
        @JvmStatic
        fun main(args: Array<String>) {
            setupLookAndFeel()
            invokeLater { Setup(context = run(Application::class.java, *args)).run { isVisible = true } }
        }

        private fun setupLookAndFeel() {
            try {
                UIManager.getInstalledLookAndFeels()
                    .find { it.name == "Nimbus" }
                    ?.let { UIManager.setLookAndFeel(it.className) }
            } catch (ex: ClassNotFoundException) {
                Logger.getLogger(Setup::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                Logger.getLogger(Setup::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                Logger.getLogger(Setup::class.java.name).log(Level.SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                Logger.getLogger(Setup::class.java.name).log(Level.SEVERE, null, ex)
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

        private fun Setup.createSeparatedFoldersWorkspace() {
            // Validate all required paths are selected
            val requiredPaths = arrayOf("office", "education", "communication", "configuration", "job")
            for (path in requiredPaths) {
                check(!(!selectedPaths.containsKey(path) || selectedPaths[path] == null)) { "All paths must be selected for separated folders installation" }
            }

            // TODO: Implement the actual creation of separated folders
            // You can access the paths using selectedPaths.get("office") etc.
        }


        private fun Setup.selectDirectory(pathKey: String, textField: JTextField) {
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

        private fun Setup.handleInstallationTypeChange(type: InstallationType) {
            "currentInstallationType : $currentInstallationType".run(::i)
            currentInstallationType = type
            "Installation type changed to $type".run(::i)
            setWorkspaceEntriesVisibility(type == SEPARATED_FOLDERS)
            if (type == ALL_IN_ONE) {
                // Clear all specific paths when switching to all-in-one
                clearSpecificPaths()
            }
        }

        internal fun Setup.createAllInOneWorkspace() {
            val workspacePath = Paths.get(workspacePathTextField.text)
            // TODO: Implement the creation of an all-in-one workspace
            // This would typically involve creating subdirectories in the main workspace
        }

        internal fun Setup.addListeners(): Setup {
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

        private fun Setup.setWorkspaceEntriesVisibility(visible: Boolean): Setup {
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

            return this
        }

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
                                    .addComponent(workspaceTopPanel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE.toInt())
                                    .addContainerGap()
                            )
                            .addComponent(workspacePathPanel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE.toInt())
                            .addComponent(workspaceTypePanel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE.toInt())
                    )
                    setVerticalGroup(
                        createParallelGroup(LEADING)
                            .addGroup(
                                createSequentialGroup()
                                    .addComponent(workspaceTopPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                    .addPreferredGap(RELATED)
                                    .addComponent(workspacePathPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                    .addPreferredGap(RELATED)
                                    .addComponent(workspaceTypePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
                                            .addComponent(titleLabel, PREFERRED_SIZE, 43, PREFERRED_SIZE)
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
                                    .addComponent(workspacePathLabel, PREFERRED_SIZE, 52, PREFERRED_SIZE)
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
                                                    .addComponent(officePathLabel, PREFERRED_SIZE, 42, PREFERRED_SIZE)
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
                                                    .addComponent(jobPathLabel, PREFERRED_SIZE, 42, PREFERRED_SIZE)
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
}


