package school

import org.springframework.boot.SpringApplication.run
import org.springframework.context.ApplicationContext
import school.Setup.InstallationType.ALL_IN_ONE
import school.Setup.InstallationType.SEPARATED_FOLDERS
import java.awt.EventQueue.invokeLater
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Level.SEVERE
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
import javax.swing.UIManager.getInstalledLookAndFeels
import javax.swing.UIManager.setLookAndFeel
import kotlin.Short.Companion.MAX_VALUE

/**
 * @author cheroliv
 */
open class Setup : JFrame("School Project Setup") {
    private lateinit var context: ApplicationContext
    private val selectedPaths: MutableMap<String, Path?> = HashMap()
    private var currentInstallationType = ALL_IN_ONE

    // Service pour gérer les opérations sur le workspace
    private val workspaceService = WorkspaceService()

    private val allInOneWorkspaceRadioButton = JRadioButton().apply {
        text = "All-in-one"
        isSelected = true
        addActionListener { handleInstallationTypeChange(SEPARATED_FOLDERS) }
    }
    private val browseCommunicationPathButton =
        JButton().apply { addActionListener { selectDirectory("communication", communicationPathTextField) } }
    private val browseConfigurationPathButton =
        JButton().apply { addActionListener { selectDirectory("configuration", configurationPathTextField) } }
    private val browseEducationPathButton = JButton().apply {
        addActionListener { selectDirectory("education", educationPathTextField) }
    }
    private val browseOfficePathButton = JButton().apply {
        addActionListener { selectDirectory("office", officePathTextField) }
    }
    private val browseWorkspacePathButton = JButton().apply {
        addActionListener { selectDirectory("workspace", workspacePathTextField) }
    }
    private val browsejobPathButton = JButton().apply { addActionListener { selectDirectory("job", jobPathTextField) } }
    private val communicationPathLabel = JLabel().apply {
        text = "Communication"
        toolTipText = ""
    }
    private val communicationPathTextField = JTextField()
    private val configurationPathLabel = JLabel().apply {
        text = "Configuration"
        toolTipText = ""
    }
    private val configurationPathTextField = JTextField()
    private val createWorkspaceButton = JButton().apply {
        text = "Create"
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) = createWorkspaceButtonMouseClicked(evt)
        })
        addActionListener { handleCreateWorkspace() }
    }
    private val educationPathLabel = JLabel().apply {
        text = "Education"
        toolTipText = ""
    }
    private val educationPathTextField = JTextField()
    private val separatedEntriesWorkspaceRadioButton = JRadioButton().apply {
        text = "Separated folders"
        isSelected = false
        addActionListener { handleInstallationTypeChange(ALL_IN_ONE) }
    }

    private val installationTypeGroup = ButtonGroup().apply {
        add(allInOneWorkspaceRadioButton)
        add(separatedEntriesWorkspaceRadioButton)
    }

    private val jobPathLabel = JLabel().apply {
        text = "Job"
        toolTipText = ""
    }

    private val jobPathTextField = JTextField()

    private val officePathLabel = JLabel().apply {
        text = "Office"
        toolTipText = ""
    }
    private val officePathTextField = JTextField()

    private val titleLabel = JLabel().apply {
        text = "School installer"
    }

    private val workspaceEntriesPanel = JPanel().apply {
        run(::GroupLayout).run {
            this@apply.layout = this
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
                                            .addComponent(jobPathLabel, TRAILING, PREFERRED_SIZE, 190, PREFERRED_SIZE)
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
                                            .addComponent(educationPathLabel, PREFERRED_SIZE, 42, PREFERRED_SIZE)
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
                                            .addComponent(communicationPathLabel, PREFERRED_SIZE, 42, PREFERRED_SIZE)
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
                                            .addComponent(configurationPathLabel, PREFERRED_SIZE, 42, PREFERRED_SIZE)
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

    private val workspacePathLabel = JLabel().apply {
        text = "Path"
    }

    private val workspacePathTextField = JTextField()

    private val workspacePathPanel = JPanel().apply {
        border = createTitledBorder("Workspace")
        run(::GroupLayout).run {
            this@apply.layout = this
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
                                    .addComponent(workspacePathTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                    .addComponent(browseWorkspacePathButton)
                                    .addComponent(workspacePathLabel)
                            )
                            .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                    )
            )
        }
    }
    private val workspaceTopPanel = JPanel().apply {
        run(::GroupLayout).run {
            this@apply.layout = this
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

    private val workspaceTypeSelectorPanel = JPanel().apply {
        run(::GroupLayout).run {
            this@apply.layout = this
            setHorizontalGroup(
                createParallelGroup(LEADING)
                    .addGroup(
                        createSequentialGroup()
                            .addContainerGap()
                            .addComponent(allInOneWorkspaceRadioButton)
                            .addGap(18, 18, 18)
                            .addComponent(separatedEntriesWorkspaceRadioButton)
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
                                    .addComponent(separatedEntriesWorkspaceRadioButton)
                                    .addComponent(allInOneWorkspaceRadioButton)
                            )
                            .addContainerGap(DEFAULT_SIZE, MAX_VALUE.toInt())
                    )
            )
        }
    }

    private val workspaceTypePanel = JPanel().apply {
        border = createTitledBorder("Installation type")
        run(::GroupLayout).run {
            this@apply.layout = this
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
                                    .addComponent(workspaceEntriesPanel, DEFAULT_SIZE, DEFAULT_SIZE, MAX_VALUE.toInt())
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
                                    .addComponent(workspaceEntriesPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                    .addContainerGap()
                            )
                    )
            )
        }
    }

    init {
        name = "setupFrame" // NOI18N
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane.apply {
            run(::GroupLayout).run {
                this@apply.layout = this
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
        setWorkspaceEntriesVisibility(false)
        mutableSetOf(
            browseEducationPathButton,
            browseOfficePathButton,
            browseCommunicationPathButton,
            browseWorkspacePathButton,
            browseConfigurationPathButton,
            browsejobPathButton,
        ).onEach { "Select directory".run(it::setText) }
        this.pack()
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


    private fun createWorkspaceButtonMouseClicked(evt: MouseEvent) {
        // TODO: add your handling code here
        Arrays.stream(context.beanDefinitionNames)
            .sequential()
            .forEach { x: String? -> println(x) }
    }

    private enum class InstallationType {
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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            setupLookAndFeel()
            /* Create and display the form */
            invokeLater {
                Setup().run {
                    isVisible = true
                    context = run(Application::class.java, *args)
                }
            }
        }

        private fun setupLookAndFeel() {
            try {
                getInstalledLookAndFeels()
                    .find { it.name == "Nimbus" }
                    ?.let { setLookAndFeel(it.className) }
            } catch (ex: ClassNotFoundException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: InstantiationException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: IllegalAccessException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            } catch (ex: UnsupportedLookAndFeelException) {
                Logger.getLogger(Setup::class.java.name).log(SEVERE, null, ex)
            }
        }
    }

}

//class Setup_ : JFrame() {
//    private enum class InstallationType {
//        ALL_IN_ONE,
//        SEPARATED_FOLDERS
//    }
//
//    private data class WorkspaceConfig(
//        val basePath: Path,
//        val type: InstallationType,
//        val subPaths: Map<String, Path> = emptyMap()
//    )
//
//    private lateinit var context: ApplicationContext
//    private val selectedPaths = mutableMapOf<String, Path?>()
//    private var currentInstallationType = ALL_IN_ONE
//
//    // Service pour gérer les opérations sur le workspace
//    private val workspaceService = WorkspaceService()
//
//    // UI Components
//    private val components = SetupComponents()
//
//    init {
//        title = "School Project Installer"
//        defaultCloseOperation = EXIT_ON_CLOSE
//        initComponents()
//        initListeners()
//    }
//
//    private fun initListeners() {
//        components.apply {
//            // Installation type listeners
//            allInOneRadioButton.addActionListener { handleInstallationTypeChange(ALL_IN_ONE) }
//            separatedFoldersRadioButton.addActionListener { handleInstallationTypeChange(SEPARATED_FOLDERS) }
//
//            // Browse button listeners
//            browsePaths.forEach { (key, button) ->
//                button.addActionListener { selectDirectory(key, textFields[key]!!) }
//            }
//
//            // Create workspace button listener
//            createButton.addActionListener { handleCreateWorkspace() }
//        }
//    }
//
//    private fun handleCreateWorkspace() {
//        try {
//            val config = validateAndCreateConfig()
//            workspaceService.createWorkspace(config)
//            showSuccessMessage()
//            initializeSpringComponents()
//        } catch (e: Exception) {
//            showErrorMessage(e.message ?: "Unknown error occurred")
//        }
//    }
//
//    private fun validateAndCreateConfig(): WorkspaceConfig {
//        val basePath = components.workspacePathField.text.takeIf { it.isNotEmpty() }
//            ?: throw IllegalStateException("Workspace path is required")
//
//        return WorkspaceConfig(
//            basePath = Paths.get(basePath),
//            type = currentInstallationType,
//            subPaths = when (currentInstallationType) {
//                SEPARATED_FOLDERS -> validateSeparatedPaths()
//                ALL_IN_ONE -> emptyMap()
//            }
//        )
//    }
//
//    private fun validateSeparatedPaths(): Map<String, Path> {
//        return selectedPaths.mapValues { (key, path) ->
//            path ?: throw IllegalStateException("Path for $key is required in separated folders mode")
//        }
//    }
//
//    private fun initializeSpringComponents() {
//        context.getBeansOfType(WorkspaceInitializer::class.java).values.forEach { initializer ->
//            try {
//                initializer.initialize(selectedPaths["workspace"]!!)
//            } catch (e: Exception) {
//                showErrorMessage("Failed to initialize ${initializer.javaClass.simpleName}: ${e.message}")
//            }
//        }
//    }
//
//    private inner class WorkspaceService {
//        fun createWorkspace(config: WorkspaceConfig) {
//            when (config.type) {
//                ALL_IN_ONE -> createAllInOneWorkspace(config.basePath)
//                SEPARATED_FOLDERS -> createSeparatedFoldersWorkspace(config.basePath, config.subPaths)
//            }
//        }
//
//        private fun createAllInOneWorkspace(basePath: Path) {
//            val directories = listOf("office", "education", "communication", "configuration", "job")
//            directories.forEach { dir ->
//                createDirectory(basePath.resolve(dir))
//            }
//            createConfigFiles(basePath)
//        }
//
//        private fun createSeparatedFoldersWorkspace(basePath: Path, subPaths: Map<String, Path>) {
//            subPaths.forEach { (name, path) ->
//                createDirectory(path)
//                createConfigFiles(path)
//            }
//        }
//
//        private fun createDirectory(path: Path) {
//            path.toFile().mkdirs()
//        }
//
//        private fun createConfigFiles(basePath: Path) {
//            // Création des fichiers de configuration nécessaires
//            File(basePath.toFile(), "application.properties").writeText("""
//                school.workspace.path=${basePath}
//                # Add other configuration properties here
//            """.trimIndent())
//        }
//    }
//
//
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            setupLookAndFeel()
//            invokeLater {
//                Setup().apply {
//                    isVisible = true
//                    context = run(Application::class.java, *args)
//                }
//            }
//        }
//
//        private fun setupLookAndFeel() {
//            try {
//                UIManager.getInstalledLookAndFeels()
//                    .find { it.name == "Nimbus" }
//                    ?.let { UIManager.setLookAndFeel(it.className) }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}
//
//// Interface for Spring components that need to initialize workspace
//interface WorkspaceInitializer {
//    fun initialize(workspacePath: Path)
//}
