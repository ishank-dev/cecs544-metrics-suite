import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

// Lets the user pick one language from the list using checkboxes.
// Selecting a checkbox automatically deselects all others (single-selection).
// Pre-selects whatever was chosen last time.
public class LanguageDialog extends JDialog {

    private final JCheckBox[] checkBoxes;
    private String selectedLanguage;

    public LanguageDialog(Frame owner, String currentLanguage) {
        super(owner, "Select Language", true);
        this.selectedLanguage = currentLanguage;
        checkBoxes = new JCheckBox[Constants.LANGUAGES.length];
        buildUI(currentLanguage);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void buildUI(String currentLanguage) {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Select one language");
        title.setFont(new Font("Arial", Font.BOLD, 11));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(title);
        root.add(Box.createVerticalStrut(8));

        JPanel cbPanel = new JPanel(new GridLayout(Constants.LANGUAGES.length, 1, 0, 2));
        for (int i = 0; i < Constants.LANGUAGES.length; i++) {
            checkBoxes[i] = new JCheckBox(Constants.LANGUAGES[i]);
            checkBoxes[i].setSelected(Constants.LANGUAGES[i].equals(currentLanguage));
            final int idx = i;
            // Deselect all others when this one is selected (mutually exclusive)
            checkBoxes[i].addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (int j = 0; j < checkBoxes.length; j++) {
                        if (j != idx) checkBoxes[j].setSelected(false);
                    }
                }
            });
            cbPanel.add(checkBoxes[i]);
        }
        root.add(cbPanel);
        root.add(Box.createVerticalStrut(10));

        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneBtn.addActionListener(e -> onDone());
        root.add(doneBtn);

        add(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void onDone() {
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].isSelected()) {
                selectedLanguage = Constants.LANGUAGES[i];
                dispose();
                return;
            }
        }
        // nothing was selected, just close and keep the previous value
        dispose();
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }
}
