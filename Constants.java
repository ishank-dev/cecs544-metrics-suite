import java.util.LinkedHashMap;
import java.util.Map;

// Holds all the static data the app needs - weights, languages, VAF questions, etc.
// Nothing here should ever change at runtime.
public class Constants {

    public static final String APP_TITLE = "CECS 544 Metrics Suite";
    public static final String FILE_EXT  = ".ms";

    public static final String[] LANGUAGES = {
        "Assembler", "Ada 95", "C", "C++", "C#",
        "COBOL", "FORTRAN", "HTML", "Java", "JavaScript",
        "VBScript", "Visual Basic"
    };

    // How many lines of code one Function Point translates to, per language.
    // COBOL = 80 is confirmed by Figure 7 in the vision doc (45899.7 * 80 ≈ 3,671,920)
    public static final Map<String, Integer> LOC_PER_FP;
    static {
        LOC_PER_FP = new LinkedHashMap<>();
        LOC_PER_FP.put("Assembler",    320);
        LOC_PER_FP.put("Ada 95",        71);
        LOC_PER_FP.put("C",            128);
        LOC_PER_FP.put("C++",           53);
        LOC_PER_FP.put("C#",            54);
        LOC_PER_FP.put("COBOL",         80);
        LOC_PER_FP.put("FORTRAN",      105);
        LOC_PER_FP.put("HTML",          34);
        LOC_PER_FP.put("Java",          53);
        LOC_PER_FP.put("JavaScript",    47);
        LOC_PER_FP.put("VBScript",      32);
        LOC_PER_FP.put("Visual Basic",  32);
    }

    public static final String[] FP_TYPES = {
        "External Inputs",
        "External Outputs",
        "External Inquiries",
        "Internal Logical Files",
        "External Interface Files"
    };

    // [row][0=Simple, 1=Average, 2=Complex]
    // Order matches FP_TYPES above
    public static final int[][] FP_WEIGHTS = {
        { 3,  4,  6},   // External Inputs
        { 4,  5,  7},   // External Outputs
        { 3,  4,  6},   // External Inquiries
        { 7, 10, 15},   // Internal Logical Files
        { 5,  7, 10}    // External Interface Files
    };

    public static final String[] WEIGHT_NAMES = {"Simple", "Average", "Complex"};

    // The 14 standard VAF questions (each scored 0-5)
    public static final String[] VAF_QUESTIONS = {
        "Does the system require reliable backup and recovery processes?",
        "Are specialized data communications required to transfer information to or from the application?",
        "Are there distributed processing functions?",
        "Is performance critical?",
        "Will the system run in an existing, heavily utilized operational environment?",
        "Does the system require online data entry?",
        "Does the online data entry require the input transaction to be built over multiple screens or operations?",
        "Are the internal logical files updated online?",
        "Are the input, output, files or inquiries complex?",
        "Is the internal processing complex?",
        "Is the code designed to be reusable?",
        "Are conversion and installation included in the design?",
        "Is the system designed for multiple installations in different organizations?",
        "Is the application designed to facilitate change and for ease of use by the user?"
    };

    private Constants() {}
}
