@echo off
setlocal

REM Set the path to your JavaFX SDK (update this to your actual JavaFX SDK path)
set PATH_TO_FX="C:\path\to\javafx-sdk-21.0.2\lib"

REM Compile and run with JavaFX modules
mvn clean compile
echo.
echo If the above command succeeds, run the application with:
echo mvn exec:java -Dexec.mainClass="com.unieats.UniEatsApp" -Dexec.args="--module-path %PATH_TO_FX% --add-modules=javafx.controls,javafx.fxml,javafx.web"

endlocal
