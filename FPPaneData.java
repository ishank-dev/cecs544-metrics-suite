import java.io.Serializable;
import java.util.Arrays;

// Stores everything on a single FP tab so it can be saved to a .ms file
public class FPPaneData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String tabName      = "FP1";
    public String language     = null;        // null means "None" in the UI
    public int[]  vafValues    = new int[14]; // all 0 by default
    public int[]  counts       = new int[5];  // one per FP type
    public int[]  weightIndices = {1, 1, 1, 1, 1}; // 0=Simple, 1=Average, 2=Complex
    public String fpResult     = "";

    public FPPaneData() {
        Arrays.fill(vafValues, 0);
        Arrays.fill(counts, 0);
    }

    public int vafSum() {
        int s = 0;
        for (int v : vafValues) s += v;
        return s;
    }

    public String weightName(int fpTypeIdx) {
        return Constants.WEIGHT_NAMES[weightIndices[fpTypeIdx]];
    }
}
