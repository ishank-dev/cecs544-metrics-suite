import javax.swing.*;
import java.awt.*;

// Pops up when the user hits File > New.
// Collects the project name, product name, creator, and any comments.
public class NewProjectDialog extends JDialog {

    private JTextField projectNameField;
    private JTextField productNameField;
    private JTextField creatorField;
    private JTextArea  commentsArea;
    private boolean    accepted = false;

    public NewProjectDialog(Frame owner) {
        super(owner, "New Project", true);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("CECS 544 Metrics Suite New Project");
        title.setFont(new Font("Arial", Font.BOLD, 12));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        projectNameField = addRow(form, "Project Name:", 0);
        productNameField = addRow(form, "Product Name:", 1);
        creatorField     = addRow(form, "Creator:",      2);

        // Comments gets a text area instead of a single-line field
        GridBagConstraints gbcL = makeGbc(0, 3);
        gbcL.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Comments:"), gbcL);

        GridBagConstraints gbcF = makeGbc(1, 3);
        gbcF.fill    = GridBagConstraints.BOTH;
        gbcF.weighty = 1.0;
        commentsArea = new JTextArea(4, 28);
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        form.add(new JScrollPane(commentsArea), gbcF);

        root.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JButton okBtn     = new JButton("Ok");
        JButton cancelBtn = new JButton("Cancel");
        okBtn.addActionListener(e -> {
            if (projectNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(NewProjectDialog.this,
                    "Project Name is required.",
                    "Missing Required Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            accepted = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());
        btnRow.add(okBtn);
        btnRow.add(cancelBtn);
        root.add(btnRow, BorderLayout.SOUTH);

        add(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private JTextField addRow(JPanel panel, String labelText, int row) {
        panel.add(new JLabel(labelText), makeGbc(0, row));
        GridBagConstraints gbc = makeGbc(1, row);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JTextField field = new JTextField(28);
        panel.add(field, gbc);
        return field;
    }

    private GridBagConstraints makeGbc(int col, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx  = col;
        gbc.gridy  = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, (col == 0 ? 0 : 10), 5, 0);
        return gbc;
    }

    public boolean isAccepted()    { return accepted; }
    public String getProjectName() { return projectNameField.getText().trim(); }
    public String getProductName() { return productNameField.getText().trim(); }
    public String getCreator()     { return creatorField.getText().trim(); }
    public String getComments()    { return commentsArea.getText(); }
}
