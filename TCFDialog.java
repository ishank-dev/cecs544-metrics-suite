import javax.swing.*;
import java.awt.*;

// Modal dialog for entering the 13 Technical Complexity Factor values (0-5).
// Computes and displays TCF = 0.6 + 0.01 * sum(value * weight) live.
public class TCFDialog extends JDialog {

    private boolean accepted = false;
    private int[]   result   = new int[13];

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] combos = new JComboBox[13];
    private final JTextField tcfField;

    private static final String[] VALS = {"0","1","2","3","4","5"};

    public TCFDialog(Frame owner, int[] initialValues) {
        super(owner, "Technical Complexity Factor", true);

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

        for (int i = 0; i < 13; i++) {
            GridBagConstraints gcLabel = new GridBagConstraints();
            gcLabel.gridx  = 0; gcLabel.gridy = i;
            gcLabel.anchor = GridBagConstraints.WEST;
            gcLabel.fill   = GridBagConstraints.HORIZONTAL;
            gcLabel.weightx = 1.0;
            gcLabel.insets  = new Insets(3, 0, 3, 12);

            double w = Constants.TCF_WEIGHTS[i];
            String wStr = (w == (int) w) ? String.valueOf((int) w) : String.valueOf(w);
            factorPanel.add(
                new JLabel(Constants.TCF_FACTORS[i] + "  (weight = " + wStr + ")"),
                gcLabel);

            GridBagConstraints gcCombo = new GridBagConstraints();
            gcCombo.gridx  = 1; gcCombo.gridy = i;
            gcCombo.anchor = GridBagConstraints.EAST;
            gcCombo.insets = new Insets(3, 0, 3, 0);

            combos[i] = new JComboBox<>(VALS);
            combos[i].setSelectedIndex(
                (initialValues != null && initialValues[i] >= 0 && initialValues[i] <= 5)
                    ? initialValues[i] : 0);
            combos[i].addActionListener(e -> refresh());
            factorPanel.add(combos[i], gcCombo);
        }

        JScrollPane scroll = new JScrollPane(factorPanel);
        scroll.setPreferredSize(new Dimension(540, 320));
        scroll.setBorder(BorderFactory.createEtchedBorder());
        root.add(scroll, BorderLayout.CENTER);

        // ── Bottom: result row + buttons ────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout(6, 6));

        JPanel resultRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        resultRow.add(new JLabel("Technical Complexity Factor (TCF):"));
        tcfField = makeReadOnly(10);
        resultRow.add(tcfField);
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
        tcfField.setText(String.format("%.4f", computeTCF(currentValues())));
    }

    private void onDone() {
        result   = currentValues();
        accepted = true;
        dispose();
    }

    private int[] currentValues() {
        int[] v = new int[13];
        for (int i = 0; i < 13; i++) v[i] = combos[i].getSelectedIndex();
        return v;
    }

    private JTextField makeReadOnly(int cols) {
        JTextField f = new JTextField(cols);
        f.setEditable(false);
        f.setHorizontalAlignment(JTextField.RIGHT);
        f.setBackground(new Color(232, 232, 232));
        return f;
    }

    // TCF = 0.6 + 0.01 * sum(value_i * weight_i)
    public static double computeTCF(int[] values) {
        double sum = 0;
        for (int i = 0; i < 13; i++) sum += values[i] * Constants.TCF_WEIGHTS[i];
        return 0.6 + 0.01 * sum;
    }

    public boolean isAccepted() { return accepted; }
    public int[]   getResult()  { return result;   }
}
