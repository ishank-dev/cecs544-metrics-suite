# CECS 544 Metrics Suite

A Java Swing desktop application for computing software lifecycle metrics. This is Iteration 1, which covers Function Point analysis.

## How to run

**Compile:**
```bash
javac *.java
```

**Run:**
```bash
java MetricsSuiteApp
```

Or just use the provided script:
```bash
./run.sh
```

Requires Java 11 or higher.

## How to use

1. **File > New** — create a project (enter name, creator, etc.)
2. **Metrics > Function Points > Enter FP Data** — opens a new FP tab
3. Enter counts for each of the 5 component types and select a weighting factor (Simple / Average / Complex)
4. Click **Value Adjustments** to set the 14 VAF scores (0–5 each)
5. Click **Compute FP** to calculate the Function Point value
6. Select a language via **Change Language** or **Preferences > Language**, then click **Compute Code Size**
7. **File > Save** saves everything to a `.ms` file — **File > Open** restores it

## Function Point formula

```
FP = Total Count × (0.65 + 0.01 × VAF sum)
```

Code size is estimated using a standard backfiring table (LOC per FP per language).

## Files

| File | Description |
|---|---|
| `MetricsSuiteApp.java` | Main window, menus, project management |
| `FunctionPointPane.java` | The FP input/calculation tab |
| `LanguageDialog.java` | Language selection dialog |
| `VAFDialog.java` | Value Adjustment Factors dialog |
| `NewProjectDialog.java` | New project creation dialog |
| `ProjectData.java` | Project data model + file save/load |
| `FPPaneData.java` | Data model for a single FP tab |
| `Constants.java` | Languages, weights, VAF questions, LOC/FP table |

## Project files

Projects are saved as `.ms` files using Java serialization. Each file stores the project name, creator, and the full state of all open FP tabs.
