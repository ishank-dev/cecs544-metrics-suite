#!/bin/bash
# CECS 544 Metrics Suite — compile and run

cd "$(dirname "$0")"
echo "Compiling..."
javac Constants.java FPPaneData.java UCPPaneData.java ProjectData.java \
      LanguageDialog.java VAFDialog.java NewProjectDialog.java \
      TCFDialog.java ECFDialog.java \
      FunctionPointPane.java UCPPane.java MetricsSuiteApp.java

if [ $? -eq 0 ]; then
    echo "Running..."
    java MetricsSuiteApp
else
    echo "Compilation failed."
fi
