package processing;

import com.intellij.psi.*;
import gr.uom.java.xmi.UMLOperation;

public final class MethodUtils {
    private MethodUtils() {
    }

    public static String calculateSignature(PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        final String className;
        if (containingClass != null) {
            className = containingClass.getQualifiedName();
        } else {
            className = "";
        }
        final String methodName = method.getName();
        final StringBuilder out = new StringBuilder(50);
        out.append(className);
        out.append('.');
        out.append(methodName);
        out.append('(');
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                out.append(',');
            }
            final PsiType parameterType = parameters[i].getType();
            final String parameterTypeText = parameterType.getPresentableText();
            out.append(parameterTypeText);
        }
        out.append(')');
        return out.toString();
    }

    public static String calculateSignatureForEcl(UMLOperation operation) {
        StringBuilder builder = new StringBuilder();

        builder.append(operation.getClassName())
                .append(".")
                .append(operation.getName())
                .append("(");

        operation.getParameterTypeList().forEach(x -> builder.append(x).append(","));

        if (operation.getParameterTypeList().size() > 0)
            builder.deleteCharAt(builder.length() - 1);

        builder.append(")");
        return builder.toString();
    }
}
