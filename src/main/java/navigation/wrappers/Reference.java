package navigation.wrappers;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import processing.Utils;

import javax.swing.*;

public class Reference {
    private PsiMethod psiElement;
    private int count;

    public Reference(PsiMethod reference, int count) {
        this.psiElement = reference;
        this.count = count;
    }

    public Navigatable location() {
        return new OpenFileDescriptor(psiElement.getProject(), getVirtualFile(), psiElement.getTextOffset());
    }

    public VirtualFile getVirtualFile() {
        return containingFile().getVirtualFile();
    }

    public PsiMethod getPsiMethod() {
        return psiElement;
    }

    public PsiClass getParentClass() {
        PsiElement parent;
        PsiElement current = psiElement;
        while (true) {
            parent = current.getParent();
            if (parent instanceof PsiFile) return null;
            if (parent instanceof PsiClass) return (PsiClass) parent;
            current = parent;
        }
    }

    public PsiFile containingFile() {
        return psiElement.getContainingFile();
    }

    public String containingPackage() {
        String fullPackageName = "default";
        try {
            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiElement.getContainingFile().getContainingDirectory());
            if (!psiPackage.getQualifiedName().trim().equals(""))
                fullPackageName = psiPackage.getQualifiedName();
        } catch (NullPointerException e) {
            fullPackageName = "default";
        }
        return fullPackageName;
    }

    public Icon icon() {
        if (getPsiMethod() != null)
            return getPsiMethod().getIcon(0);
        if (getParentClass() != null)
            return getParentClass().getIcon(0);
        if (containingPackage() != null)
            return containingFile().getIcon(0);
        return psiElement.getIcon(0);
    }

    public boolean equals(Object reference) {
        return reference != null
                && reference instanceof Reference
                && psiElement.equals(((Reference) reference).psiElement);
    }

    //MessageBuilder?!
    public String description() {
        StringBuilder description = new StringBuilder();
        PsiClass containingClass = getParentClass();
        PsiMethod containingMethod = getPsiMethod();
        PsiFile containingFile = containingFile();

        if (getPsiMethod() != null)

        description.append(Utils.calculateSignature(getPsiMethod()));
//        if (containingClass != null && !"".equals(containingClass.getName()))
//            description.append(getContainingClassName(containingClass));
//        else
//            description.append(containingFile.getName());

//        if (containingMethod != null)
//            description.append(".").append(containingMethod.getName());

        if ((containingClass != null && !"".equals(containingClass.getName())) || !"".equals(containingFile.getName()) || (containingMethod != null && !containingMethod.getName().equals("")))
            description.append(": ");

        try {
            description.append(count).append(" time(s)");
            //int columnNumber = column();
            //description.append("Column ").append(columnNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String inPackage = containingPackage();
//        if (inPackage != null && !inPackage.equals("")) {
//            description.append(" (in ").append(inPackage).append(")");
//        }

        return description.toString();
    }

    private String getContainingClassName(PsiClass containingClass) {
        String className = containingClass.getName();
        if (className == null)
            return "Anonymous class in " + getContainingClassOrFile(containingClass);
        return className;
    }

    private String getContainingClassOrFile(PsiClass theClass) {
        if (theClass.getContainingClass() != null)
            //noinspection ConstantConditions
            return theClass.getContainingClass().getName();
        else
            return theClass.getContainingFile().getName();
    }
}
