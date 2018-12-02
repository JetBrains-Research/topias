package editor;

import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.LineExtensionInfo;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import state.ChangesState;
import state.MethodInfo;

import java.util.*;

//public class EditorLP extends EditorLinePainter {
//    @Nullable
//    @Override
//    public Collection<LineExtensionInfo> getLineExtensions(@NotNull Project project, @NotNull VirtualFile file, int lineNumber) {
//        final Map<String, Set<MethodInfo>> state = Objects.requireNonNull(ChangesState.getInstance().getState()).persistentState;
//
//        final Set<MethodInfo> infos = state.get(file.getCanonicalPath());
//        final List<LineExtensionInfo> lineExtensionInfos = new LinkedList<>();
//        infos.stream().filter(x -> x.getStartOffset() == lineNumber).forEach(
//                x -> lineExtensionInfos.add(new LineExtensionInfo("    Was changed " + x.getChangesCount() + " times", new TextAttributes(new Element("cool"))))
//        );
//        return lineExtensionInfos;
//    }
//}
