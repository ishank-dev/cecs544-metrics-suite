import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Stores all rows of the SMI table so they survive save/load
public class SMIPaneData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String     tabName = "SMI";
    public List<int[]> rows   = new ArrayList<>(); // each element: [added, changed, deleted]
}
