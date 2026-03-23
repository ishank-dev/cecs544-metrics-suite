import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

// Main window - sets up the menu bar, split pane, and tabbed area
// Left pane is left empty for now (used in a later iteration)
public class MetricsSuiteApp extends JFrame {

    private ProjectData currentProject  = null;
    private String      currentFilePath = null;
    private String      globalLanguage  = null;  // set from Preferences > Language
    private int         paneCounter     = 0;

    private JTabbedPane tabbedPane;

    public MetricsSuiteApp() {
        super(Constants.APP_TITLE);
        buildUI();
        buildMenuBar();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 640);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerSize(5);
        split.setDividerLocation(160);

        // Left side - placeholder, will be used in iteration 2/3
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(200, 200, 200));
        leftPanel.setPreferredSize(new Dimension(160, 0));
        split.setLeftComponent(leftPanel);

        tabbedPane = new JTabbedPane();
        split.setRightComponent(tabbedPane);

        add(split, BorderLayout.CENTER);
    }

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItem("New",  "control N", e -> onFileNew()));
        fileMenu.add(menuItem("Open", "control O", e -> onFileOpen()));
        fileMenu.add(menuItem("Save", "control S", e -> onFileSave()));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Exit", null, e -> System.exit(0)));
        bar.add(fileMenu);

        // Edit has nothing for now
        bar.add(new JMenu("Edit"));

        JMenu prefsMenu = new JMenu("Preferences");
        prefsMenu.add(menuItem("Language", null, e -> onPrefsLanguage()));
        bar.add(prefsMenu);

        JMenu metricsMenu = new JMenu("Metrics");
        JMenu fpMenu = new JMenu("Function Points");
        fpMenu.add(menuItem("Enter FP Data", null, e -> onEnterFPData()));
        metricsMenu.add(fpMenu);
        bar.add(metricsMenu);

        // Help has nothing for now
        bar.add(new JMenu("Help"));

        setJMenuBar(bar);
    }

    // Creates a new project - clears everything and shows the new project dialog
    private void onFileNew() {
        NewProjectDialog dlg = new NewProjectDialog(this);
        if (!dlg.isAccepted()) return;

        tabbedPane.removeAll();
        paneCounter     = 0;
        currentFilePath = null;

        currentProject = new ProjectData();
        currentProject.projectName = dlg.getProjectName();
        currentProject.productName = dlg.getProductName();
        currentProject.creator     = dlg.getCreator();
        currentProject.comments    = dlg.getComments();

        String name = currentProject.projectName.isEmpty() ? "Untitled" : currentProject.projectName;
        setTitle(Constants.APP_TITLE + " - " + name);
    }

    // Opens a .ms file and rebuilds the tabs from the saved data
    private void onFileOpen() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
            "Metrics Suite files (*" + Constants.FILE_EXT + ")", "ms"));
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        try {
            ProjectData pd = ProjectData.load(path);
            loadProject(pd, path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not open file:\n" + ex.getMessage(),
                "Open Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Rebuilds the whole UI from a loaded ProjectData object
    private void loadProject(ProjectData pd, String path) {
        tabbedPane.removeAll();
        paneCounter     = 0;
        currentProject  = pd;
        currentFilePath = path;

        String name = pd.projectName.isEmpty() ? "Untitled" : pd.projectName;
        setTitle(Constants.APP_TITLE + " - " + name);

        for (FPPaneData fpd : pd.panes) {
            paneCounter++;
            FunctionPointPane pane = new FunctionPointPane(fpd.tabName, fpd.language);
            pane.loadData(fpd);
            tabbedPane.addTab(fpd.tabName, pane);
        }

        if (tabbedPane.getTabCount() > 0)
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    // Saves to the existing file path, or asks for one if this is the first save
    private void onFileSave() {
        if (currentProject == null) {
            JOptionPane.showMessageDialog(this,
                "Please create or open a project first (File > New).",
                "No Project", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String path = currentFilePath;
        if (path == null) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                "Metrics Suite files (*" + Constants.FILE_EXT + ")", "ms"));
            fc.setSelectedFile(new File("project" + Constants.FILE_EXT));
            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(Constants.FILE_EXT)) path += Constants.FILE_EXT;
            currentFilePath = path;
        }

        // Pull current data from each open tab before saving
        currentProject.panes.clear();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            FunctionPointPane pane = (FunctionPointPane) tabbedPane.getComponentAt(i);
            currentProject.panes.add(pane.getData());
        }

        try {
            currentProject.save(path);
            JOptionPane.showMessageDialog(this, "Project saved successfully.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save:\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Sets the global language and also updates whichever tab is currently open
    private void onPrefsLanguage() {
        LanguageDialog dlg = new LanguageDialog(this, globalLanguage);
        String chosen = dlg.getSelectedLanguage();
        if (chosen == null) return;

        globalLanguage = chosen;

        int idx = tabbedPane.getSelectedIndex();
        if (idx >= 0) {
            FunctionPointPane active = (FunctionPointPane) tabbedPane.getComponentAt(idx);
            active.setLanguage(globalLanguage);
        }
    }

    // Adds a new FP tab - won't work unless a project is open first
    private void onEnterFPData() {
        if (currentProject == null) {
            JOptionPane.showMessageDialog(this,
                "Please create a new project first (File > New).",
                "No Project", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String tabName = (String) JOptionPane.showInputDialog(
            this, "Panel Name:", "Enter FP Data",
            JOptionPane.PLAIN_MESSAGE, null, null, "FP" + (paneCounter + 1));
        if (tabName == null || tabName.trim().isEmpty()) return;
        tabName = tabName.trim();
        paneCounter++;
        FunctionPointPane pane = new FunctionPointPane(tabName, globalLanguage);
        tabbedPane.addTab(tabName, pane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    // Small helper to avoid repeating the same JMenuItem setup code
    private JMenuItem menuItem(String text, String accel, java.awt.event.ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        if (accel != null)
            item.setAccelerator(KeyStroke.getKeyStroke(accel));
        item.addActionListener(al);
        return item;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MetricsSuiteApp::new);
    }
}
