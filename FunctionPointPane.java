import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

// One tab in the main window. Handles all FP input, calculation, and display.
public class FunctionPointPane extends JPanel {

    private final String tabName;
    private String  currentLanguage;
    private int[]   vafValues = new int[14];  // all start at 0
    private boolean loading   = false;        // stops listeners from firing during loadData

    private final JTextField[]    countFields  = new JTextField[5];
    private final JRadioButton[][] weightRadios = new JRadioButton[5][3];
    private final ButtonGroup[]    weightGroups = new ButtonGroup[5];
    private final JTextField[]     totalFields  = new JTextField[5];

    private JTextField totalCountField;
    private JTextField fpResultField;
    private JTextField vafSumField;
    private JTextField langDisplayField;
    private JTextField codeSizeField;

    public FunctionPointPane(String tabName, String initialLanguage) {
        this.tabName         = tabName;
        this.currentLanguage = initialLanguage;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildHeaderPanel());
        add(Box.createVerticalStrut(6));
        add(buildInputGrid());
        add(Box.createVerticalStrut(10));
        add(buildButtonSection());

        recalculateAll();
    }

    // Just the two header labels at the top
    private JPanel buildHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel wf = new JLabel("Weighting Factors");
        wf.setFont(new Font("Arial", Font.BOLD, 12));
        wf.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sac = new JLabel("Simple   Average   Complex");
        sac.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(wf);
        panel.add(sac);
        return panel;
    }

    // The main input grid: 5 FP type rows + a Total Count row at the bottom
    private JPanel buildInputGrid() {
        JPanel grid = new JPanel(new GridBagLayout());

        for (int i = 0; i < 5; i++) {
            final int fpIdx = i;

            GridBagConstraints gLabel = gbc(0, i, GridBagConstraints.WEST);
            gLabel.insets = new Insets(5, 10, 5, 6);
            gLabel.ipadx  = 10;
            JLabel label = new JLabel(Constants.FP_TYPES[i]);
            label.setPreferredSize(new Dimension(185, 20));
            grid.add(label, gLabel);

            GridBagConstraints gCount = gbc(1, i, GridBagConstraints.WEST);
            gCount.insets = new Insets(5, 4, 5, 8);
            countFields[i] = new JTextField("", 6);
            countFields[i].setHorizontalAlignment(JTextField.RIGHT);
            // recalculate the row total whenever the user changes the count
            countFields[i].getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e)  { updateRow(fpIdx); }
                public void removeUpdate(DocumentEvent e)  { updateRow(fpIdx); }
                public void changedUpdate(DocumentEvent e) { updateRow(fpIdx); }
            });
            grid.add(countFields[i], gCount);

            // radio buttons show the actual weight value (3, 4, 6 etc.) as the label
            weightGroups[i] = new ButtonGroup();
            for (int j = 0; j < 3; j++) {
                GridBagConstraints gRb = gbc(2 + j, i, GridBagConstraints.CENTER);
                gRb.insets = new Insets(5, 6, 5, 6);
                int wt = Constants.FP_WEIGHTS[i][j];
                JRadioButton rb = new JRadioButton(String.valueOf(wt));
                rb.setSelected(j == 1);  // Average is the default
                rb.addActionListener(e -> updateRow(fpIdx));
                weightGroups[i].add(rb);
                weightRadios[i][j] = rb;
                grid.add(rb, gRb);
            }

            GridBagConstraints gTotal = gbc(5, i, GridBagConstraints.EAST);
            gTotal.insets = new Insets(5, 8, 5, 10);
            totalFields[i] = makeReadOnlyField(8);
            grid.add(totalFields[i], gTotal);
        }

        // Total Count row
        GridBagConstraints gTCLabel = gbc(0, 5, GridBagConstraints.WEST);
        gTCLabel.insets = new Insets(12, 10, 5, 6);
        grid.add(new JLabel("Total Count"), gTCLabel);

        GridBagConstraints gTC = gbc(5, 5, GridBagConstraints.EAST);
        gTC.insets = new Insets(12, 8, 5, 10);
        totalCountField = makeReadOnlyField(8);
        grid.add(totalCountField, gTC);

        return grid;
    }

    // Four rows of buttons below the grid
    private JPanel buildButtonSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        fpResultField = makeReadOnlyField(14);
        JButton computeFPBtn = new JButton("Compute FP");
        computeFPBtn.addActionListener(e -> onComputeFP());
        section.add(row(computeFPBtn, fpResultField));

        vafSumField = makeReadOnlyField(14);
        vafSumField.setText("0");
        JButton vafBtn = new JButton("Value Adjustments");
        vafBtn.addActionListener(e -> onOpenVAF());
        section.add(row(vafBtn, vafSumField));

        langDisplayField = makeReadOnlyField(12);
        langDisplayField.setText(currentLanguage != null ? currentLanguage : "None");
        codeSizeField = makeReadOnlyField(14);
        JButton codeSizeBtn = new JButton("Compute Code Size");
        codeSizeBtn.addActionListener(e -> onComputeCodeSize());
        section.add(row(codeSizeBtn, new JLabel("Current Language"),
                        langDisplayField, codeSizeField));

        JButton changeLangBtn = new JButton("Change Language");
        changeLangBtn.addActionListener(e -> onChangeLang());
        section.add(row(changeLangBtn));

        return section;
    }

    // Recalculate a single row's total (count × selected weight)
    private void updateRow(int fpIdx) {
        if (loading) return;
        int count  = parseCount(fpIdx);
        int weight = Constants.FP_WEIGHTS[fpIdx][selectedWeightIndex(fpIdx)];
        totalFields[fpIdx].setText(String.valueOf(count * weight));
        updateTotalCount();
    }

    private void updateTotalCount() {
        int sum = 0;
        for (JTextField tf : totalFields) {
            try { sum += Integer.parseInt(tf.getText()); }
            catch (NumberFormatException ignored) {}
        }
        totalCountField.setText(String.valueOf(sum));
    }

    private void recalculateAll() {
        for (int i = 0; i < 5; i++) updateRow(i);
        updateTotalCount();
    }

    // FP = TotalCount × (0.65 + 0.01 × VAF sum)
    private void onComputeFP() {
        if (!validateInputs()) return;
        recalculateAll();

        int totalCount = 0;
        try { totalCount = Integer.parseInt(totalCountField.getText()); }
        catch (NumberFormatException ignored) {}
        if (totalCount == 0) return;  // no data entered — silently ignore

        double fp = totalCount * (0.65 + 0.01 * sumVAF());

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        fpResultField.setText(nf.format(fp));
    }

    // Called from MetricsSuiteApp when the user picks a language from Preferences
    public void setLanguage(String language) {
        currentLanguage = language;
        langDisplayField.setText(language != null ? language : "None");
    }

    // Opens the VAF dialog and saves the values if the user hits Done
    private void onOpenVAF() {
        VAFDialog dlg = new VAFDialog(getOwnerFrame(), vafValues);
        if (dlg.isAccepted()) {
            vafValues = dlg.getResult();
            vafSumField.setText(String.valueOf(sumVAF()));
        }
    }

    // code size = FP × LOC per FP for the chosen language
    private void onComputeCodeSize() {
        if (currentLanguage == null) {
            JOptionPane.showMessageDialog(this, "Please select a language first.",
                "No Language Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String fpText = fpResultField.getText().replace(",", "").trim();
        if (fpText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please compute Function Points first.",
                "No FP Value", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double fp       = Double.parseDouble(fpText);
            int    locPerFP = Constants.LOC_PER_FP.getOrDefault(currentLanguage, 0);
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(0);
            codeSizeField.setText(nf.format((long)fp * locPerFP));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid FP value.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onChangeLang() {
        LanguageDialog dlg = new LanguageDialog(getOwnerFrame(), currentLanguage);
        String chosen = dlg.getSelectedLanguage();
        if (chosen != null) {
            currentLanguage = chosen;
            langDisplayField.setText(currentLanguage);
        }
    }

    // Checks that all five count fields are non-negative integers before computing.
    // Empty fields are treated as zero (valid); only non-numeric or negative values error.
    private boolean validateInputs() {
        for (int i = 0; i < 5; i++) {
            String val = countFields[i].getText().trim();
            if (val.isEmpty()) continue;  // blank == 0, valid
            try {
                if (Integer.parseInt(val) < 0) { showValidationError(i, val); return false; }
            } catch (NumberFormatException e) {
                showValidationError(i, val); return false;
            }
        }
        return true;
    }

    private void showValidationError(int fpIdx, String val) {
        JOptionPane.showMessageDialog(this,
            "'" + Constants.FP_TYPES[fpIdx] + "' must be a non-negative integer.\nGot: '" + val + "'",
            "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    // Snapshot of everything on this pane — used when saving the project file
    public FPPaneData getData() {
        FPPaneData d = new FPPaneData();
        d.tabName   = tabName;
        d.language  = currentLanguage;
        d.vafValues = vafValues.clone();
        d.fpResult  = fpResultField.getText();
        for (int i = 0; i < 5; i++) {
            d.counts[i]        = parseCount(i);
            d.weightIndices[i] = selectedWeightIndex(i);
        }
        return d;
    }

    // Restores the pane from a saved snapshot; loading flag stops the listeners
    // from firing half-calculated values while we're still setting fields
    public void loadData(FPPaneData d) {
        loading = true;
        try {
            currentLanguage = d.language;
            langDisplayField.setText(d.language != null ? d.language : "None");
            vafValues = d.vafValues.clone();
            vafSumField.setText(String.valueOf(sumVAF()));
            for (int i = 0; i < 5; i++) {
                countFields[i].setText(String.valueOf(d.counts[i]));
                weightRadios[i][d.weightIndices[i]].setSelected(true);
            }
            fpResultField.setText(d.fpResult);
        } finally {
            loading = false;
        }
        recalculateAll();
    }

    private int parseCount(int fpIdx) {
        try { return Math.max(0, Integer.parseInt(countFields[fpIdx].getText().trim())); }
        catch (NumberFormatException e) { return 0; }
    }

    private int selectedWeightIndex(int fpIdx) {
        for (int j = 0; j < 3; j++)
            if (weightRadios[fpIdx][j].isSelected()) return j;
        return 1;  // shouldn't happen, but fall back to Average
    }

    private int sumVAF() {
        int s = 0;
        for (int v : vafValues) s += v;
        return s;
    }

    private Frame getOwnerFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    private JTextField makeReadOnlyField(int cols) {
        JTextField f = new JTextField(cols);
        f.setEditable(false);
        f.setHorizontalAlignment(JTextField.RIGHT);
        f.setBackground(new Color(232, 232, 232));
        return f;
    }

    private JPanel row(Component... components) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        for (Component c : components) p.add(c);
        return p;
    }

    private GridBagConstraints gbc(int col, int row, int anchor) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx  = col;
        g.gridy  = row;
        g.anchor = anchor;
        return g;
    }
}
