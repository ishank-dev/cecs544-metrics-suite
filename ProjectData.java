import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Project-level data: the name, creator, and all the FP tabs.
// Gets serialized into a .ms file when the user hits Save.
public class ProjectData implements Serializable {

    private static final long serialVersionUID = 1L;

    public String projectName = "";
    public String productName = "";
    public String creator     = "";
    public String comments    = "";
    public List<FPPaneData> panes = new ArrayList<>();

    public static ProjectData load(String filename)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filename)))) {
            return (ProjectData) ois.readObject();
        }
    }

    public void save(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filename)))) {
            oos.writeObject(this);
        }
    }
}
