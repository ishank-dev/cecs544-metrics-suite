import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

// Tab panel for the Software Maturity Index.
// Each row represents one release cycle. The user enters Added, Changed, Deleted;
// Total and SMI are computed when "Compute Index" is pressed.
//
// Total_n  = Total_(n-1) + Added_n - Deleted_n   (Total_0 = 0)
// SMI_n    = (Total_n - (Added_n + Changed_n + Deleted_n)) / Total_n
public class SMIPane extends JPanel {

    private static final String[] COL_NAMES =
        {"Version", "Added", "Changed", "Deleted", "Total", "SMI"};

    private final String         tabName;
    private final DefaultTableModel model;
    private final JTable         table;

    public SMIPane(String tabName) {
        this.tabName = tabName;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(COL_NAMES, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col >= 1 && col <= 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);

        styleColumns();

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Software Maturity Index", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        content.add(header, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void styleColumns() {
        Color readOnlyBg = new Color(232, 232, 232);
        int[] colWidths = {60, 90, 90, 90, 90, 90};

        for (int c = 0; c < COL_NAMES.length; c++) {
            TableColumn col = table.getColumnModel().getColumn(c);
            col.setPreferredWidth(colWidths[c]);

            if (c == 0 || c == 4 || c == 5) {
                col.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean foc, int r, int cc) {
                        Component comp =
                            super.getTableCellRendererComponent(t, v, sel, foc, r, cc);
                        if (!sel) comp.setBackground(readOnlyBg);
                        setHorizontalAlignment(SwingConstants.CENTER);
                        return comp;
                    }
                });
            } else {
                DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
                cr.setHorizontalAlignment(SwingConstants.CENTER);
                col.setCellRenderer(cr);
            }
        }
    }

    private JPanel buildButtons() {
        JButton addRowBtn  = new JButton("Add Row");
        JButton computeBtn = new JButton("Compute Index");
        addRowBtn.addActionListener(e  -> onAddRow());
        computeBtn.addActionListener(e -> onComputeIndex());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.add(addRowBtn);
        p.add(computeBtn);
        return p;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void onAddRow() {
        int version = model.getRowCount() + 1;
        model.addRow(new Object[]{version, "0", "0", "0", "", ""});
    }

    private void onComputeIndex() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int prevTotal = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            int added   = parseCell(i, 1);
            int changed = parseCell(i, 2);
            int deleted = parseCell(i, 3);

            int total = prevTotal + added - deleted;
            if (total < 0) total = 0;

            double smi = (total == 0) ? 0.0
                       : (double)(total - (added + changed + deleted)) / total;

            model.setValueAt(String.valueOf(total), i, 4);
            model.setValueAt(formatSMI(smi), i, 5);
            prevTotal = total;
        }
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    public SMIPaneData getData() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        SMIPaneData d = new SMIPaneData();
        d.tabName = tabName;
        for (int i = 0; i < model.getRowCount(); i++) {
            d.rows.add(new int[]{parseCell(i, 1), parseCell(i, 2), parseCell(i, 3)});
        }
        return d;
    }

    public void loadData(SMIPaneData d) {
        model.setRowCount(0);
        for (int i = 0; i < d.rows.size(); i++) {
            int[] r = d.rows.get(i);
            model.addRow(new Object[]{
                i + 1,
                String.valueOf(r[0]),
                String.valueOf(r[1]),
                String.valueOf(r[2]),
                "", ""
            });
        }
        if (model.getRowCount() > 0) onComputeIndex();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    // Format SMI with up to 8 decimal places, stripping trailing zeros so that
    // 0.5 shows as "0.5" but 18/19 shows as "0.94736842".
    private String formatSMI(double smi) {
        String s = String.format("%.8f", smi);
        s = s.replaceAll("0+$", "");
        if (s.endsWith(".")) s += "0";
        return s;
    }

    private int parseCell(int row, int col) {
        Object val = model.getValueAt(row, col);
        if (val == null) return 0;
        try { return Math.max(0, Integer.parseInt(val.toString().trim())); }
        catch (NumberFormatException e) { return 0; }
    }
}
