package navigation.finders;


import navigation.wrappers.ReferenceCollection;

public interface PsiElementUsageFinder {
    ReferenceCollection findUsages();

    PsiElementUsageFinder setNext(PsiElementUsageFinder nextFinder);
}
