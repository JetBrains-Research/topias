package ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import db.dao.StatisticsViewDAO;
import db.entities.StatisticsViewEntity;
import kotlin.Pair;
import navigation.wrappers.DataHolder;
import navigation.wrappers.Reference;
import processing.Utils;
import settings.TopiasSettingsState;
import settings.enums.DiscrType;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static processing.Utils.*;

public class TopChangedMethodsListPanel extends SimpleToolWindowPanel {
    private final MySideBar sideBar = new MySideBar();

    public TopChangedMethodsListPanel(boolean vertical, boolean borderless, Project project) {
        super(false, true);
        DumbService.getInstance(project).runWhenSmart(() -> {
            final StatisticsViewDAO dao = new StatisticsViewDAO(buildPathForSystem(project));
            final DiscrType period = TopiasSettingsState.getInstance().getState() != null ? TopiasSettingsState.getInstance().getState().discrType : DiscrType.MONTH;
            final List<StatisticsViewEntity> entities = dao.getMostChangedMethods(period);
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

            final List<Pair<PsiReference, Integer>> psiReferences = methodCountPairs.stream()
                    .map(x -> new Pair<>(
                            MethodReferencesSearch.search(x.getFirst(), GlobalSearchScope.projectScope(project), false).findFirst(),
                            x.getSecond()
                    ))
                    .filter(x -> x.getFirst() != null)
                    .collect(Collectors.toList());

            final DataContext context = DataManager.getInstance().getDataContext(this);
            DataHolder.getInstance().initDataHolder(DataManager.getInstance().getDataContext(this));
            final List<Reference> references = psiReferences.stream().filter(Objects::nonNull).map(x -> new Reference(x.getFirst(), x.getSecond())).collect(Collectors.toList());
            sideBar.updateListItems(references);
            setContent(sideBar.getPanel());
        });
    }


}
