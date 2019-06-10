package ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import db.dao.StatisticsViewDAO;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import navigation.wrappers.DataHolder;
import navigation.wrappers.Reference;
import settings.TopiasSettingsState;
import settings.enums.DiscrType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static processing.Utils.*;

public class TopChangedMethodsListPanel extends SimpleToolWindowPanel {
    private final MySideBar sideBar;
    private static TopChangedMethodsListPanel instance = null;

    public TopChangedMethodsListPanel(boolean vertical, boolean borderless, Project project) {
        super(false, true);
        sideBar = new MySideBar(project);
        refresh(project);
    }

    private void refresh(Project project) {
        DumbService.getInstance(project).runWhenSmart(() -> {
            final StatisticsViewDAO dao = new StatisticsViewDAO(buildDBUrlForSystem(project));
            final TopiasSettingsState.InnerState settingsState = TopiasSettingsState.getInstance(project).getState();
            final DiscrType period = settingsState != null ? DiscrType.getById(settingsState.discrTypeId) : DiscrType.MONTH;
            String branchName;
            try {
                branchName = getCurrentBranchName(project);
            } catch (VcsException e) {
                branchName = "master";
            }
            final List<StatisticsViewEntity> entities = dao.getMostChangedMethods(period, branchName);
            final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);

            final List<Pair<PsiMethod, Integer>> methodCountPairs = entities.stream().map(x -> {
                final PsiClass psiClass = facade.findClass(trimClassName(x.getFullSignature()), GlobalSearchScope.projectScope(project));
                if (psiClass != null) {
                    final PsiMethod[] methods = psiClass.findMethodsByName(trimMethodName(x.getFullSignature()), false);

                    if (methods.length != 0 && methods[0] != null)
                        return new Pair<>(methods[0], x.getChangesCount());
                }
                return null;
            }).filter(x -> x != null && x.getFirst() != null).collect(Collectors.toList());

//            final List<Pair<PsiReference, Integer>> psiReferences = methodCountPairs.stream()
//                    .map(x -> new Pair<>(
//                            MethodReferencesSearch.search(x.getFirst(), GlobalSearchScope.allScope(project), false).findFirst(),
//                            x.getSecond()
//                    ))
//                    .filter(x -> x.getFirst() != null)
//                    .collect(Collectors.toList());

//            final List<PsiElement> elements = psiReferences.stream().map(x -> x.getFirst().resolve()).collect(Collectors.toList());

            final DataContext context = DataManager.getInstance().getDataContext(this);
            DataHolder.getInstance().initDataHolder(DataManager.getInstance().getDataContext(this));
            final List<Reference> references = methodCountPairs.stream().filter(Objects::nonNull).limit(10).map(x -> new Reference(x.getFirst(), x.getSecond())).collect(Collectors.toList());
//            final List<Reference> references = psiReferences.stream().filter(Objects::nonNull).map(x -> new Reference(x.getFirst(), x.getSecond())).collect(Collectors.toList());
            sideBar.updateListItems(references);
            setContent(sideBar.getPanel());
            instance = this;
        });
    }

    public static void refreshList(Project project) {
        if (instance == null) {
            instance = new TopChangedMethodsListPanel(false, false, project);
        } else
            instance.refresh(project);
    }

}
