package processing;

import com.intellij.openapi.project.Project;

import java.util.Locale;

public class SystemUtils {
    public static String buildPathForSystem(Project project) {
        final StringBuilder pathBuilder = new StringBuilder().append(project.getBasePath());
        final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin") || os.contains("nux")) {
            pathBuilder.append("/.idea/state.db");
        } else if (os.contains("win")) {
            pathBuilder.append("\\.idea\\state.db");
        }
        return pathBuilder.toString();
    }
}
