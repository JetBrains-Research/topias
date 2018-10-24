package editor;

import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class EditorLP extends EditorLinePainter {
    @Nullable
    @Override
    public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
        final List<LineExtensionInfo> lineExtensionInfos = new LinkedList<>();
        if (lineNumber % 2 == 1)
            lineExtensionInfos.add(new LineExtensionInfo("Much cool text", new TextAttributes(new Element("cool"))));

        return lineExtensionInfos;
    }
}
