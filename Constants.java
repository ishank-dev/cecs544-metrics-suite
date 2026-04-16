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
        LOC_PER_FP.put("Ada 95",       154);
        LOC_PER_FP.put("C",            128);
        LOC_PER_FP.put("C++",           53);
        LOC_PER_FP.put("C#",            54);
        LOC_PER_FP.put("COBOL",         80);
        LOC_PER_FP.put("FORTRAN",      105);
        LOC_PER_FP.put("HTML",          34);
        LOC_PER_FP.put("Java",          55);
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

    // ── Use Case Points ──────────────────────────────────────────────────────

    // Unadjusted Actor Weight (UAW)
    public static final String[] ACTOR_LABELS  = {
        "Simple Actor (external system via API)",
        "Average Actor (external system via protocol/messaging)",
        "Complex Actor (human user via GUI)"
    };
    public static final int[] ACTOR_WEIGHTS = {1, 2, 3};

    // Unadjusted Use Case Weight (UUCW)
    public static final String[] USE_CASE_LABELS = {
        "Simple Use Case  (1 – 3 transactions)",
        "Average Use Case (4 – 7 transactions)",
        "Complex Use Case (> 7 transactions)"
    };
    public static final int[] USE_CASE_WEIGHTS = {5, 10, 15};

    // Technical Complexity Factor – 13 factors
    public static final String[] TCF_FACTORS = {
        "T1:  Distributed System",
        "T2:  Response Time / Performance Objectives",
        "T3:  End-User Efficiency (Online)",
        "T4:  Complex Internal Processing",
        "T5:  Code Must Be Reusable",
        "T6:  Easy to Install",
        "T7:  Easy to Use",
        "T8:  Portable",
        "T9:  Easy to Change",
        "T10: Concurrent",
        "T11: Includes Special Security Features",
        "T12: Provides Direct Access for Third Parties",
        "T13: Special User Training Facilities Required"
    };
    public static final double[] TCF_WEIGHTS = {2, 1, 1, 1, 1, 0.5, 0.5, 2, 1, 1, 1, 1, 1};

    // Environmental Complexity Factor – 8 factors (E7 and E8 have negative weights)
    public static final String[] ECF_FACTORS = {
        "E1: Familiarity with Development Process Used",
        "E2: Application Experience",
        "E3: Object-Oriented Experience of Project Team",
        "E4: Lead Analyst Capability",
        "E5: Motivation of Project Team",
        "E6: Stability of Requirements",
        "E7: Part-time Workers",
        "E8: Difficulty of Programming Language"
    };
    public static final double[] ECF_WEIGHTS = {1.5, 0.5, 1.0, 0.5, 1.0, 2.0, -1.0, -1.0};

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
