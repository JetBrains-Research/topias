### Build and install plugin
Supported IntelliJ IDEA versions: 2018.3 - 2019.1

In order to build this plugin please do following:
1. (Optional for Linux/MacOS) Make `gradlew` script executable by running `chmod +x gradlew`
2. Run `./gradlew jar` (on Linux/MacOS) or `gradlew.bat` (on Windows) to build plugin
3. Go to IntelliJ IDEA Settings and follow `Settings -> Plugins -> Install plugin from disk` 
and select plugin's jar which is located at: `%project_root/build/libs/plugin.jar`, then press `OK`
4. Restart IDE