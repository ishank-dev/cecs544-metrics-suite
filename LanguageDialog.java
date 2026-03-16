import javax.swing.*;
import java.awt.*;

// Simple dialog that lets the user pick one language from the list.
// Pre-selects whatever was chosen last time.
public class LanguageDialog extends JDialog {

    private final JRadioButton[] radioButtons;
    private final ButtonGroup    buttonGroup;
    private String selectedLanguage;

    public LanguageDialog(Frame owner, String currentLanguage) {
        super(owner, "", true);
        this.selectedLanguage = currentLanguage;
        radioButtons = new JRadioButton[Constants.LANGUAGES.length];
        buttonGroup  = new ButtonGroup();
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

        JPanel rbPanel = new JPanel(new GridLayout(Constants.LANGUAGES.length, 1, 0, 2));
        for (int i = 0; i < Constants.LANGUAGES.length; i++) {
            radioButtons[i] = new JRadioButton(Constants.LANGUAGES[i]);
            radioButtons[i].setSelected(Constants.LANGUAGES[i].equals(currentLanguage));
            buttonGroup.add(radioButtons[i]);
            rbPanel.add(radioButtons[i]);
        }
        root.add(rbPanel);
        root.add(Box.createVerticalStrut(10));

        JButton doneBtn = new JButton("Done");
        doneBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneBtn.addActionListener(e -> onDone());
        root.add(doneBtn);

        add(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void onDone() {
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
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
