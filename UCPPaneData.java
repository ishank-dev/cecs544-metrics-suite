import java.io.Serializable;
import java.util.Arrays;

// Stores everything on a single UCP tab so it can be saved to a .ms file
public class UCPPaneData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String tabName = "UCP1";

    // Actor counts [0=Simple, 1=Average, 2=Complex]
    public int[] actorCounts    = new int[3];

    // Use case counts [0=Simple, 1=Average, 2=Complex]
    public int[] useCaseCounts  = new int[3];

    // TCF values for 13 factors (0-5 each); default 0
    public int[] tcfValues      = new int[13];

    // ECF values for 8 factors (0-5 each); default 0
    public int[] ecfValues      = new int[8];

    // Editable parameters
    public String productivityFactor = "20";
    public String locPerPM           = "700";
    public String locPerUCP          = "120";

    // Computed result strings (preserved across save/load so they re-display)
    public String ucpResult      = "";
    public String estHours       = "";
    public String estLOC         = "";
    public String estPM          = "";

    public UCPPaneData() {
        Arrays.fill(actorCounts,   0);
        Arrays.fill(useCaseCounts, 0);
        Arrays.fill(tcfValues,     0);
        Arrays.fill(ecfValues,     0);
    }
}
