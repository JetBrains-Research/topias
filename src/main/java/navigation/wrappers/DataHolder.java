package navigation.wrappers;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataContextWrapper;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class DataHolder {
    public Project PROJECT;
    public Editor EDITOR;
    public Module MODULE;
    public PsiElement PSI_ELEMENT;
    public Navigatable NAVIGATABLE;
    public VirtualFile VIRTUAL_FILE;
    public PsiFile PSI_FILE;

    private DataHolder() {
    }

    private static DataHolder _instance = null;

    public static DataHolder getInstance() {
        return _instance = (_instance == null ? new DataHolder() : _instance);
    }

    public void initDataHolder(DataContext dataContext) {
        PROJECT = dataContext.getData(DataKeys.PROJECT);
        EDITOR = dataContext.getData(DataKeys.EDITOR);
        MODULE = dataContext.getData(DataKeys.MODULE);
        PSI_ELEMENT = dataContext.getData(DataKeys.PSI_ELEMENT);
        NAVIGATABLE = dataContext.getData(DataKeys.NAVIGATABLE);
        VIRTUAL_FILE = dataContext.getData(DataKeys.VIRTUAL_FILE);
        PSI_FILE = dataContext.getData(DataKeys.PSI_FILE);
    }
}
