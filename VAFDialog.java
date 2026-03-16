import javax.swing.*;
import java.awt.*;

// Dialog for the 14 Value Adjustment Factor questions.
// Each question gets a combo box with values 0-5.
// Re-opening it restores whatever was set before.
public class VAFDialog extends JDialog {

    private final JComboBox<String>[] combos;
    private boolean accepted = false;
    private int[]   result   = null;

    @SuppressWarnings("unchecked")
    public VAFDialog(Frame owner, int[] currentValues) {
        super(owner, "Value Adjustment Factors", true);
        combos = new JComboBox[14];
        buildUI(currentValues);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI(int[] currentValues) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        root.add(new JLabel(
            "<html><b>Assign a value from 0 to 5 for each of the following Value Adjustment Factors:</b></html>"),
            BorderLayout.NORTH);

        JPanel qPanel = new JPanel(new GridBagLayout());
        String[] values = {"0", "1", "2", "3", "4", "5"};

        for (int i = 0; i < 14; i++) {
            GridBagConstraints gbcLabel = new GridBagConstraints();
            gbcLabel.gridx   = 0; gbcLabel.gridy = i;
            gbcLabel.fill    = GridBagConstraints.HORIZONTAL;
            gbcLabel.weightx = 1.0;
            gbcLabel.anchor  = GridBagConstraints.WEST;
            gbcLabel.insets  = new Insets(3, 0, 3, 10);
            qPanel.add(new JLabel(
                "<html><body style='width:490px'>" + Constants.VAF_QUESTIONS[i] + "</body></html>"),
                gbcLabel);

            GridBagConstraints gbcCombo = new GridBagConstraints();
            gbcCombo.gridx   = 1; gbcCombo.gridy = i;
            gbcCombo.anchor  = GridBagConstraints.EAST;
            gbcCombo.insets  = new Insets(3, 0, 3, 0);
            combos[i] = new JComboBox<>(values);
            combos[i].setSelectedIndex(currentValues != null ? currentValues[i] : 0);
            qPanel.add(combos[i], gbcCombo);
        }

        JScrollPane scroll = new JScrollPane(qPanel);
        scroll.setPreferredSize(new Dimension(620, 380));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        root.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton doneBtn   = new JButton("Done");
        JButton cancelBtn = new JButton("Cancel");
        doneBtn.addActionListener(e -> onDone());
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(doneBtn);
        btnRow.add(cancelBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        add(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void onDone() {
        result = new int[14];
        for (int i = 0; i < 14; i++)
            result[i] = combos[i].getSelectedIndex();
        accepted = true;
        dispose();
    }

    public boolean isAccepted() { return accepted; }
    public int[]   getResult()  { return result; }
}
