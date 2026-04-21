import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

// One tab in the main window for Use Case Point estimation.
// Layout:
//   Input grid  – actor counts and use-case counts with auto-calculated row totals
//   Complexity  – buttons to open TCF and ECF dialogs; displays their results
//   Parameters  – editable Productivity Factor, LOC/PM, LOC/UCP
//   Results     – Compute UCP button; read-only Total UCP, Hours, LOC, PM fields
public class UCPPane extends JPanel {

    private final String tabName;
    private boolean loading = false;

    // ── Input grid fields ────────────────────────────────────────────────────
    private final JTextField[] actorCountFields    = new JTextField[3];
    private final JTextField[] actorTotalFields    = new JTextField[3];
    private final JTextField[] ucCountFields       = new JTextField[3];
    private final JTextField[] ucTotalFields       = new JTextField[3];

    private JTextField uawTotalField;
    private JTextField ucwTotalField;
    private JTextField uucpField;

    // ── Complexity factor state & display ────────────────────────────────────
    private int[] tcfValues = new int[13];  // all 0 by default
    private int[] ecfValues = new int[8];

    private JTextField tcfDisplayField;
    private JTextField ecfDisplayField;

    // ── Editable parameters ──────────────────────────────────────────────────
    private JTextField prodFactorField;
    private JTextField locPerPMField;
    private JTextField locPerUCPField;

    // ── Result fields ────────────────────────────────────────────────────────
    private JTextField ucpResultField;
    private JTextField estHoursField;
    private JTextField estLOCField;
    private JTextField estPMField;

    public UCPPane(String tabName) {
        this.tabName = tabName;
        setLayout(new BorderLayout());
        buildUI();
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(8));
        content.add(buildInputGrid());
        content.add(Box.createVerticalStrut(10));
        content.add(buildComplexitySection());
        content.add(Box.createVerticalStrut(8));
        content.add(buildParametersSection());
        content.add(Box.createVerticalStrut(8));
        content.add(buildResultsSection());

        JScrollPane scroll = new JScrollPane(content,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Use Case Points Estimation");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        return p;
    }

    private JPanel buildInputGrid() {
        JPanel grid = new JPanel(new GridBagLayout());

        int row = 0;

        // ── Unadjusted Actor Weight section ───────────────────────────
        addSectionLabel(grid, "Unadjusted Actor Weight (UAW)", row++);
        addColumnHeaders(grid, row++);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addInputRow(grid, row++,
                Constants.ACTOR_LABELS[i],
                String.valueOf(Constants.ACTOR_WEIGHTS[i]),
                actorCountFields,
                actorTotalFields,
                i,
                () -> updateActorRow(idx));
        }

        // UAW total
        uawTotalField = makeReadOnly(9);
        addTotalRow(grid, row++, "  Total UAW", uawTotalField);

        addBlankRow(grid, row++);

        // ── Unadjusted Use Case Weight section ────────────────────────
        addSectionLabel(grid, "Unadjusted Use Case Weight (UUCW)", row++);
        addColumnHeaders(grid, row++);

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addInputRow(grid, row++,
                Constants.USE_CASE_LABELS[i],
                String.valueOf(Constants.USE_CASE_WEIGHTS[i]),
                ucCountFields,
                ucTotalFields,
                i,
                () -> updateUCRow(idx));
        }

        // UUCW total
        ucwTotalField = makeReadOnly(9);
        addTotalRow(grid, row++, "  Total UUCW", ucwTotalField);

        addBlankRow(grid, row++);

        // ── UUCP ──────────────────────────────────────────────────────
        uucpField = makeReadOnly(9);
        addTotalRow(grid, row, "Unadjusted Use Case Points (UUCP = UAW + UUCW)", uucpField);

        recalculateAll();
        return grid;
    }

    // Adds a bold section header spanning all columns
    private void addSectionLabel(JPanel grid, String text, int row) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 6, 2, 6);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        grid.add(label, gc);
    }

    // "Count   Weight   Row Total" header row
    private void addColumnHeaders(JPanel grid, int row) {
        String[] headers = {"", "Count", "Weight", "Row Total"};
        int[] cols = {0, 1, 2, 3};
        for (int i = 0; i < headers.length; i++) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = cols[i]; gc.gridy = row;
            gc.anchor = GridBagConstraints.CENTER;
            gc.insets = new Insets(0, 6, 2, 6);
            JLabel h = new JLabel(headers[i]);
            h.setFont(new Font("Arial", Font.ITALIC, 10));
            grid.add(h, gc);
        }
    }

    // One actor or use-case row with a count field, static weight label, and read-only total
    private void addInputRow(JPanel grid, int row, String label,
                             String weightStr,
                             JTextField[] countArr, JTextField[] totalArr,
                             int arrIdx, Runnable onChange) {
        // Label
        GridBagConstraints gcL = new GridBagConstraints();
        gcL.gridx = 0; gcL.gridy = row; gcL.anchor = GridBagConstraints.WEST;
        gcL.fill = GridBagConstraints.HORIZONTAL; gcL.weightx = 1.0;
        gcL.insets = new Insets(3, 6, 3, 8);
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(270, 20));
        grid.add(lbl, gcL);

        // Count entry
        JTextField countField = new JTextField("0", 6);
        countField.setHorizontalAlignment(JTextField.RIGHT);
        countField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { if (!loading) onChange.run(); }
            public void removeUpdate(DocumentEvent e)  { if (!loading) onChange.run(); }
            public void changedUpdate(DocumentEvent e) { if (!loading) onChange.run(); }
        });
        countArr[arrIdx] = countField;
        GridBagConstraints gcC = new GridBagConstraints();
        gcC.gridx = 1; gcC.gridy = row; gcC.anchor = GridBagConstraints.CENTER;
        gcC.insets = new Insets(3, 4, 3, 4);
        grid.add(countField, gcC);

        // Weight label (static)
        GridBagConstraints gcW = new GridBagConstraints();
        gcW.gridx = 2; gcW.gridy = row; gcW.anchor = GridBagConstraints.CENTER;
        gcW.insets = new Insets(3, 4, 3, 4);
        grid.add(new JLabel("× " + weightStr), gcW);

        // Row total (read-only)
        JTextField totalField = makeReadOnly(9);
        totalArr[arrIdx] = totalField;
        GridBagConstraints gcT = new GridBagConstraints();
        gcT.gridx = 3; gcT.gridy = row; gcT.anchor = GridBagConstraints.EAST;
        gcT.insets = new Insets(3, 4, 3, 10);
        grid.add(totalField, gcT);
    }

    // A label + read-only total field spanning the right column
    private void addTotalRow(JPanel grid, int row, String labelText, JTextField field) {
        GridBagConstraints gcL = new GridBagConstraints();
        gcL.gridx = 0; gcL.gridy = row; gcL.gridwidth = 3;
        gcL.anchor = GridBagConstraints.WEST;
        gcL.insets = new Insets(4, 6, 4, 8);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        grid.add(lbl, gcL);

        GridBagConstraints gcF = new GridBagConstraints();
        gcF.gridx = 3; gcF.gridy = row; gcF.anchor = GridBagConstraints.EAST;
        gcF.insets = new Insets(4, 4, 4, 10);
        grid.add(field, gcF);
    }

    private void addBlankRow(JPanel grid, int row) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.insets = new Insets(4, 0, 0, 0);
        grid.add(new JLabel(""), gc);
    }

    // ── Complexity Factor section ────────────────────────────────────────────
    private JPanel buildComplexitySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Complexity Factors"));

        tcfDisplayField = makeReadOnly(10);
        tcfDisplayField.setText(String.format("%.4f", TCFDialog.computeTCF(tcfValues)));
        JButton tcfBtn = new JButton("Technical Complexity Factor");
        tcfBtn.addActionListener(e -> onOpenTCF());
        section.add(buttonRow(tcfBtn, tcfDisplayField));

        ecfDisplayField = makeReadOnly(10);
        ecfDisplayField.setText(String.format("%.4f", ECFDialog.computeECF(ecfValues)));
        JButton ecfBtn = new JButton("Environmental Complexity Factor");
        ecfBtn.addActionListener(e -> onOpenECF());
        section.add(buttonRow(ecfBtn, ecfDisplayField));

        return section;
    }

    // ── Parameters section ───────────────────────────────────────────────────
    private JPanel buildParametersSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Parameters"));

        prodFactorField = new JTextField("20", 8);
        locPerPMField   = new JTextField("700", 8);
        locPerUCPField  = new JTextField("120", 8);

        section.add(paramRow("Productivity Factor (hrs / UCP):", prodFactorField));
        section.add(paramRow("LOC per Person-Month (LOC/PM):",   locPerPMField));
        section.add(paramRow("LOC per UCP:",                      locPerUCPField));

        return section;
    }

    // ── Results section ──────────────────────────────────────────────────────
    private JPanel buildResultsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(BorderFactory.createTitledBorder("Results"));

        JButton computeBtn = new JButton("Compute UCP");
        computeBtn.addActionListener(e -> onComputeUCP());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnRow.add(computeBtn);
        section.add(btnRow);

        ucpResultField = makeReadOnly(12);
        estHoursField  = makeReadOnly(12);
        estLOCField    = makeReadOnly(12);
        estPMField     = makeReadOnly(12);

        section.add(resultRow("Total UCP:",          ucpResultField));
        section.add(resultRow("Estimated Hours:",    estHoursField));
        section.add(resultRow("Estimated LOC:",      estLOCField));
        section.add(resultRow("Estimated PM:",       estPMField));

        return section;
    }

    // ── Row helpers ──────────────────────────────────────────────────────────

    private JPanel buttonRow(JButton btn, JTextField field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.add(btn);
        p.add(field);
        return p;
    }

    private JPanel paramRow(String labelText, JTextField field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(240, 20));
        p.add(lbl);
        p.add(field);
        return p;
    }

    private JPanel resultRow(String labelText, JTextField field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(160, 20));
        p.add(lbl);
        p.add(field);
        return p;
    }

    // ── Calculation ──────────────────────────────────────────────────────────

    private void updateActorRow(int idx) {
        int count  = parseField(actorCountFields[idx]);
        int weight = Constants.ACTOR_WEIGHTS[idx];
        actorTotalFields[idx].setText(String.valueOf(count * weight));
        updateUAWTotal();
        updateUUCP();
    }

    private void updateUCRow(int idx) {
        int count  = parseField(ucCountFields[idx]);
        int weight = Constants.USE_CASE_WEIGHTS[idx];
        ucTotalFields[idx].setText(String.valueOf(count * weight));
        updateUCWTotal();
        updateUUCP();
    }

    private void updateUAWTotal() {
        int sum = 0;
        for (JTextField f : actorTotalFields) sum += parseField(f);
        uawTotalField.setText(String.valueOf(sum));
    }

    private void updateUCWTotal() {
        int sum = 0;
        for (JTextField f : ucTotalFields) sum += parseField(f);
        ucwTotalField.setText(String.valueOf(sum));
    }

    private void updateUUCP() {
        int uaw  = parseField(uawTotalField);
        int uucw = parseField(ucwTotalField);
        uucpField.setText(String.valueOf(uaw + uucw));
    }

    private void recalculateAll() {
        for (int i = 0; i < 3; i++) updateActorRow(i);
        for (int i = 0; i < 3; i++) updateUCRow(i);
        updateUAWTotal();
        updateUCWTotal();
        updateUUCP();
    }

    // ── Button actions ───────────────────────────────────────────────────────

    private void onOpenTCF() {
        TCFDialog dlg = new TCFDialog(getOwnerFrame(), tcfValues);
        if (dlg.isAccepted()) {
            tcfValues = dlg.getResult();
            tcfDisplayField.setText(
                String.format("%.4f", TCFDialog.computeTCF(tcfValues)));
        }
    }

    private void onOpenECF() {
        ECFDialog dlg = new ECFDialog(getOwnerFrame(), ecfValues);
        if (dlg.isAccepted()) {
            ecfValues = dlg.getResult();
            ecfDisplayField.setText(
                String.format("%.4f", ECFDialog.computeECF(ecfValues)));
        }
    }

    private void onComputeUCP() {
        // Validate parameter fields
        double prodFactor, locPM, locUCP;
        try {
            prodFactor = Double.parseDouble(prodFactorField.getText().trim());
            locPM      = Double.parseDouble(locPerPMField.getText().trim());
            locUCP     = Double.parseDouble(locPerUCPField.getText().trim());
            if (prodFactor <= 0 || locPM <= 0 || locUCP <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Productivity Factor, LOC/PM, and LOC/UCP must be positive numbers.",
                "Invalid Parameters", JOptionPane.ERROR_MESSAGE);
            return;
        }

        recalculateAll();

        int    uucp = parseField(uucpField);
        double tcf  = TCFDialog.computeTCF(tcfValues);
        double ecf  = ECFDialog.computeECF(ecfValues);
        double ucp  = uucp * tcf * ecf;

        double estHours = ucp * prodFactor;
        double estLOC   = ucp * locUCP;
        double estPM    = estLOC / locPM;

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        ucpResultField.setText(nf.format(ucp));
        estHoursField.setText(nf.format(estHours));
        estLOCField.setText(nf.format(estLOC));
        estPMField.setText(nf.format(estPM));
    }

    // ── Serialization ────────────────────────────────────────────────────────

    public UCPPaneData getData() {
        UCPPaneData d = new UCPPaneData();
        d.tabName = tabName;
        for (int i = 0; i < 3; i++) d.actorCounts[i]   = parseField(actorCountFields[i]);
        for (int i = 0; i < 3; i++) d.useCaseCounts[i] = parseField(ucCountFields[i]);
        d.tcfValues         = tcfValues.clone();
        d.ecfValues         = ecfValues.clone();
        d.productivityFactor = prodFactorField.getText();
        d.locPerPM           = locPerPMField.getText();
        d.locPerUCP          = locPerUCPField.getText();
        d.ucpResult          = ucpResultField.getText();
        d.estHours           = estHoursField.getText();
        d.estLOC             = estLOCField.getText();
        d.estPM              = estPMField.getText();
        return d;
    }

    public void loadData(UCPPaneData d) {
        loading = true;
        try {
            for (int i = 0; i < 3; i++) actorCountFields[i].setText(String.valueOf(d.actorCounts[i]));
            for (int i = 0; i < 3; i++) ucCountFields[i].setText(String.valueOf(d.useCaseCounts[i]));
            tcfValues = d.tcfValues.clone();
            ecfValues = d.ecfValues.clone();
            tcfDisplayField.setText(String.format("%.4f", TCFDialog.computeTCF(tcfValues)));
            ecfDisplayField.setText(String.format("%.4f", ECFDialog.computeECF(ecfValues)));
            prodFactorField.setText(d.productivityFactor);
            locPerPMField.setText(d.locPerPM);
            locPerUCPField.setText(d.locPerUCP);
            ucpResultField.setText(d.ucpResult);
            estHoursField.setText(d.estHours);
            estLOCField.setText(d.estLOC);
            estPMField.setText(d.estPM);
        } finally {
            loading = false;
        }
        recalculateAll();
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private int parseField(JTextField f) {
        try { return Math.max(0, Integer.parseInt(f.getText().trim())); }
        catch (NumberFormatException e) { return 0; }
    }

    private JTextField makeReadOnly(int cols) {
        JTextField f = new JTextField(cols);
        f.setEditable(false);
        f.setHorizontalAlignment(JTextField.RIGHT);
        f.setBackground(new Color(232, 232, 232));
        return f;
    }

    private Frame getOwnerFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }
}
