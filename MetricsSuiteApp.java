import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

// Main window - sets up the menu bar, split pane, and tabbed area
public class MetricsSuiteApp extends JFrame {

    private ProjectData currentProject  = null;
    private String      currentFilePath = null;
    private String      globalLanguage  = null;
    private int         paneCounter     = 0;
    private int         ucpCounter      = 0;
    private boolean     dirty           = false;
    private boolean     smiPaneAdded    = false;

    private JTabbedPane tabbedPane;

    // Held so they can be enabled/disabled based on project state
    private JMenuItem fpMenuItem;
    private JMenuItem ucpMenuItem;
    private JMenuItem smiMenuItem;

    public MetricsSuiteApp() {
        super(Constants.APP_TITLE);
        buildUI();
        buildMenuBar();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onAttemptExit(); }
        });
        setSize(980, 640);
        setMinimumSize(new Dimension(720, 520));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerSize(5);
        split.setDividerLocation(160);

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
        fileMenu.add(menuItem("Exit", null, e -> onAttemptExit()));
        bar.add(fileMenu);

        bar.add(new JMenu("Edit"));

        JMenu prefsMenu = new JMenu("Preferences");
        prefsMenu.add(menuItem("Language", null, e -> onPrefsLanguage()));
        bar.add(prefsMenu);

        JMenu metricsMenu = new JMenu("Metrics");

        JMenu fpMenu = new JMenu("Function Points");
        fpMenuItem = menuItem("Enter FP Data", null, e -> onEnterFPData());
        fpMenuItem.setEnabled(false);
        fpMenu.add(fpMenuItem);
        metricsMenu.add(fpMenu);

        JMenu ucpMenu = new JMenu("Use Case Points");
        ucpMenuItem = menuItem("Enter UCP Data", null, e -> onEnterUCPData());
        ucpMenuItem.setEnabled(false);
        ucpMenu.add(ucpMenuItem);
        metricsMenu.add(ucpMenu);

        JMenu smiMenu = new JMenu("Software Maturity Index");
        smiMenuItem = menuItem("Enter SMI Data", null, e -> onEnterSMIData());
        smiMenuItem.setEnabled(false);
        smiMenu.add(smiMenuItem);
        metricsMenu.add(smiMenu);

        bar.add(metricsMenu);
        bar.add(new JMenu("Help"));

        setJMenuBar(bar);
    }

    // ── File actions ──────────────────────────────────────────────────────────

    private void onFileNew() {
        if (dirty && !confirmDiscard()) return;

        NewProjectDialog dlg = new NewProjectDialog(this);
        if (!dlg.isAccepted()) return;

        tabbedPane.removeAll();
        paneCounter     = 0;
        ucpCounter      = 0;
        smiPaneAdded    = false;
        currentFilePath = null;

        currentProject = new ProjectData();
        currentProject.projectName = dlg.getProjectName();
        currentProject.productName = dlg.getProductName();
        currentProject.creator     = dlg.getCreator();
        currentProject.comments    = dlg.getComments();

        String name = currentProject.projectName.isEmpty() ? "Untitled" : currentProject.projectName;
        setTitle(Constants.APP_TITLE + " - " + name);
        setProjectMenusEnabled(true);
        markDirty();
    }

    private void onFileOpen() {
        if (dirty && !confirmDiscard()) return;

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

    private void loadProject(ProjectData pd, String path) {
        tabbedPane.removeAll();
        paneCounter     = 0;
        ucpCounter      = 0;
        smiPaneAdded    = false;
        currentProject  = pd;
        currentFilePath = path;
        dirty           = false;

        String name = pd.projectName.isEmpty() ? "Untitled" : pd.projectName;
        setTitle(Constants.APP_TITLE + " - " + name);

        for (FPPaneData fpd : pd.panes) {
            paneCounter++;
            FunctionPointPane pane = new FunctionPointPane(fpd.tabName, fpd.language);
            pane.loadData(fpd);
            tabbedPane.addTab(fpd.tabName, pane);
        }

        if (pd.ucpPanes != null) {
            for (UCPPaneData upd : pd.ucpPanes) {
                ucpCounter++;
                UCPPane pane = new UCPPane(upd.tabName);
                pane.loadData(upd);
                tabbedPane.addTab(upd.tabName, pane);
            }
        }

        // Restore SMI tab if it was open when the project was saved
        if (pd.smiPane != null && pd.smiPaneOpen) {
            SMIPane smiPane = new SMIPane(pd.smiPane.tabName);
            smiPane.loadData(pd.smiPane);
            tabbedPane.addTab(pd.smiPane.tabName, smiPane);
            smiPaneAdded = true;
        }

        setProjectMenusEnabled(true);
        updateSMIMenuItem();

        if (tabbedPane.getTabCount() > 0)
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

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

        currentProject.panes.clear();
        if (currentProject.ucpPanes == null)
            currentProject.ucpPanes = new java.util.ArrayList<>();
        currentProject.ucpPanes.clear();
        currentProject.smiPane     = null;
        currentProject.smiPaneOpen = false;

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            java.awt.Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof FunctionPointPane)
                currentProject.panes.add(((FunctionPointPane) comp).getData());
            else if (comp instanceof UCPPane)
                currentProject.ucpPanes.add(((UCPPane) comp).getData());
            else if (comp instanceof SMIPane) {
                currentProject.smiPane     = ((SMIPane) comp).getData();
                currentProject.smiPaneOpen = true;
            }
        }

        try {
            currentProject.save(path);
            dirty = false;
            JOptionPane.showMessageDialog(this, "Project saved successfully.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save:\n" + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Preferences ───────────────────────────────────────────────────────────

    private void onPrefsLanguage() {
        LanguageDialog dlg = new LanguageDialog(this, globalLanguage);
        String chosen = dlg.getSelectedLanguage();
        if (chosen == null) return;

        globalLanguage = chosen;

        int idx = tabbedPane.getSelectedIndex();
        if (idx >= 0) {
            java.awt.Component comp = tabbedPane.getComponentAt(idx);
            if (comp instanceof FunctionPointPane)
                ((FunctionPointPane) comp).setLanguage(globalLanguage);
        }
    }

    // ── Metrics panel creation ────────────────────────────────────────────────

    private void onEnterFPData() {
        String tabName = (String) JOptionPane.showInputDialog(
            this, "Panel Name:", "Enter FP Data",
            JOptionPane.PLAIN_MESSAGE, null, null, "FP" + (paneCounter + 1));
        if (tabName == null || tabName.trim().isEmpty()) return;
        tabName = tabName.trim();
        paneCounter++;
        FunctionPointPane pane = new FunctionPointPane(tabName, globalLanguage);
        tabbedPane.addTab(tabName, pane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        markDirty();
    }

    private void onEnterUCPData() {
        String tabName = (String) JOptionPane.showInputDialog(
            this, "Panel Name:", "Enter UCP Data",
            JOptionPane.PLAIN_MESSAGE, null, null, "UCP" + (ucpCounter + 1));
        if (tabName == null || tabName.trim().isEmpty()) return;
        tabName = tabName.trim();
        ucpCounter++;
        UCPPane pane = new UCPPane(tabName);
        tabbedPane.addTab(tabName, pane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        markDirty();
    }

    private void onEnterSMIData() {
        SMIPane pane = new SMIPane("SMI");
        tabbedPane.addTab("SMI", pane);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        smiPaneAdded = true;
        updateSMIMenuItem();
        markDirty();
    }

    // ── Quit / close ──────────────────────────────────────────────────────────

    private void onAttemptExit() {
        if (!dirty) { System.exit(0); return; }

        Object[] options = {"Save", "Discard Changes", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "You have unsaved changes. What would you like to do?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null, options, options[0]);

        if (choice == 0) {          // Save
            onFileSave();
            if (!dirty) System.exit(0);
        } else if (choice == 1) {   // Discard
            System.exit(0);
        }
        // choice == 2 (Cancel) or dialog closed → do nothing
    }

    // Returns true if the user chose to proceed (discard current unsaved work)
    private boolean confirmDiscard() {
        Object[] options = {"Save", "Discard Changes", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "You have unsaved changes. What would you like to do?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null, options, options[0]);

        if (choice == 0) { onFileSave(); return !dirty; }
        if (choice == 1) { dirty = false; return true;  }
        return false;
    }

    // ── State helpers ─────────────────────────────────────────────────────────

    private void markDirty() { dirty = true; }

    // Enable or disable all metrics panel creation items
    private void setProjectMenusEnabled(boolean enabled) {
        fpMenuItem.setEnabled(enabled);
        ucpMenuItem.setEnabled(enabled);
        // SMI gets its own rule (only one per project)
        updateSMIMenuItem();
    }

    // SMI item is enabled only when a project is open AND no SMI tab exists yet
    private void updateSMIMenuItem() {
        smiMenuItem.setEnabled(currentProject != null && !smiPaneAdded);
    }

    // ── Shared helper ─────────────────────────────────────────────────────────

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
