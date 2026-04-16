import javax.swing.*;
import java.awt.*;

// Modal dialog for entering the 8 Environmental Complexity Factor values (0-5).
// Computes and displays ECF = 1.4 + (-0.03 * sum(value * weight)) live.
// Note: E7 and E8 have negative weights (-1), so high ratings for those
// factors increase ECF (more effort), which is the correct interpretation.
public class ECFDialog extends JDialog {

    private boolean accepted = false;
    private int[]   result   = new int[8];

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] combos = new JComboBox[8];
    private final JTextField ecfField;

    private static final String[] VALS = {"0","1","2","3","4","5"};

    public ECFDialog(Frame owner, int[] initialValues) {
        super(owner, "Environmental Complexity Factor", true);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // ── Header ──────────────────────────────────────────────────────
        JLabel header = new JLabel(
            "Rate each factor from 0 (Irrelevant) to 5 (Essential):");
        header.setFont(new Font("Arial", Font.BOLD, 11));
        root.add(header, BorderLayout.NORTH);

        // ── Factor rows ─────────────────────────────────────────────────
        JPanel factorPanel = new JPanel(new GridBagLayout());
        factorPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        for (int i = 0; i < 8; i++) {
            GridBagConstraints gcLabel = new GridBagConstraints();
            gcLabel.gridx  = 0; gcLabel.gridy = i;
            gcLabel.anchor = GridBagConstraints.WEST;
            gcLabel.fill   = GridBagConstraints.HORIZONTAL;
            gcLabel.weightx = 1.0;
            gcLabel.insets  = new Insets(4, 0, 4, 12);

            double w = Constants.ECF_WEIGHTS[i];
            String wStr = (w == (int) w) ? String.valueOf((int) w) : String.valueOf(w);
            factorPanel.add(
                new JLabel(Constants.ECF_FACTORS[i] + "  (weight = " + wStr + ")"),
                gcLabel);

            GridBagConstraints gcCombo = new GridBagConstraints();
            gcCombo.gridx  = 1; gcCombo.gridy = i;
            gcCombo.anchor = GridBagConstraints.EAST;
            gcCombo.insets = new Insets(4, 0, 4, 0);

            combos[i] = new JComboBox<>(VALS);
            combos[i].setSelectedIndex(
                (initialValues != null && initialValues[i] >= 0 && initialValues[i] <= 5)
                    ? initialValues[i] : 0);
            combos[i].addActionListener(e -> refresh());
            factorPanel.add(combos[i], gcCombo);
        }

        root.add(factorPanel, BorderLayout.CENTER);

        // ── Bottom: result row + buttons ────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(6, 6));

        JPanel resultRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        resultRow.add(new JLabel("Environmental Complexity Factor (ECF):"));
        ecfField = makeReadOnly(10);
        resultRow.add(ecfField);
        bottom.add(resultRow, BorderLayout.CENTER);

        JPanel btnRow = new JPanel();
        JButton done   = new JButton("Done");   done.addActionListener(e -> onDone());
        JButton cancel = new JButton("Cancel"); cancel.addActionListener(e -> dispose());
        btnRow.add(done); btnRow.add(cancel);
        bottom.add(btnRow, BorderLayout.SOUTH);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
        refresh();
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void refresh() {
        ecfField.setText(String.format("%.4f", computeECF(currentValues())));
    }

    private void onDone() {
        result   = currentValues();
        accepted = true;
        dispose();
    }

    private int[] currentValues() {
        int[] v = new int[8];
        for (int i = 0; i < 8; i++) v[i] = combos[i].getSelectedIndex();
        return v;
    }

    private JTextField makeReadOnly(int cols) {
        JTextField f = new JTextField(cols);
        f.setEditable(false);
        f.setHorizontalAlignment(JTextField.RIGHT);
        f.setBackground(new Color(232, 232, 232));
        return f;
    }

    // ECF = 1.4 + (-0.03 * sum(value_i * weight_i))
    public static double computeECF(int[] values) {
        double sum = 0;
        for (int i = 0; i < 8; i++) sum += values[i] * Constants.ECF_WEIGHTS[i];
        return 1.4 + (-0.03 * sum);
    }

    public boolean isAccepted() { return accepted; }
    public int[]   getResult()  { return result;   }
}
