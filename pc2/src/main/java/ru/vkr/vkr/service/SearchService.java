package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vkr.vkr.entity.Complexity;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.facade.ProblemFacade;
import ru.vkr.vkr.form.SearchProblemForm;

import java.io.*;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private ProblemService problemService;
    @Autowired
    private HashTagService hashTagService;
    @Autowired
    private ProblemFacade problemFacade;


    //Для страницы пула задач с пустым списком задач
    public void poolProblemsGet(Model model) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        List<Complexity> complexities = problemService.getAllComplexity();
        model.addAttribute("complexities", complexities);
        SearchProblemForm searchProblemForm = new SearchProblemForm(hashTags.size(), complexities.size());
        model.addAttribute("searchProblemForm", searchProblemForm);
        model.addAttribute("isFirstRunPool", true);

        List<Problem> problems = problemService.getAllPublicProblems();
        problemFacade.getStatisticForProblems(problems);
        model.addAttribute("problems", problems);
    }

    //Для страницы фильтрации задач
    public void poolSearchProblems(Model model, SearchProblemForm searchProblemForm) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        List<Complexity> complexities = problemService.getAllComplexity();
        model.addAttribute("complexities", complexities);
        filterProblems(model, searchProblemForm, hashTags, complexities, -1L);
    }

    //Для фильтрации по одному тегу
    public void poolSearchProblems(Model model, Long selectedTagId) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        List<Complexity> complexities = problemService.getAllComplexity();
        model.addAttribute("complexities", complexities);
        SearchProblemForm searchProblemForm = new SearchProblemForm(hashTags.size(), complexities.size());
        filterProblems(model, searchProblemForm, hashTags, complexities, selectedTagId);
    }


    //Непосредстванно фильтрация задач
    //Ищет как по одному тегу selectedTagId, так и по списку тегов из searchProblemForm
    private void filterProblems(Model model, SearchProblemForm searchProblemForm, List<HashTag> hashTags, List<Complexity> complexities, Long selectedTagId) {
        Set<Problem> problems_1 = new HashSet<>();
        if (searchProblemForm.getTagList().size() == 0)
            problems_1.addAll(problemService.getAllPublicProblems());
        else {
            for (int i = 0; i < searchProblemForm.getTagList().size(); i++) {
                if (selectedTagId != -1L && hashTags.get(i).getId().equals(selectedTagId))
                    searchProblemForm.getTagList().set(i, true);
                if (searchProblemForm.getTagList().get(i) != null && searchProblemForm.getTagList().get(i))
                    problems_1.addAll(hashTags.get(i).getProblems());
            }
        }



        Set<Complexity> needComplexity = new HashSet<>();       //Для быстрого поиска при фильтрации задач
        for (int i = 0; i < searchProblemForm.getComplexityList().size(); i++)
            if (searchProblemForm.getComplexityList().get(i) != null && searchProblemForm.getComplexityList().get(i))
                needComplexity.add(complexities.get(i));

        List<Problem> problems = new ArrayList<>();
        if (needComplexity.size() == 0) {
            problems.addAll(problems_1);
        } else
            for (Problem problem : problems_1)
                if (problem.getComplexity() == null || needComplexity.contains(problem.getComplexity()))
                    problems.add(problem);

        List<Problem> problemsResult = new ArrayList<>();
        for (Problem pr : problems)
            if (pr.isPubl())
                problemsResult.add(pr);

        problemFacade.getStatisticForProblems(problemsResult);
        model.addAttribute("problems", problemsResult);
        SearchProblemForm searchProblemForm_ = new SearchProblemForm(hashTags.size(), complexities.size(), searchProblemForm);
        model.addAttribute("searchProblemForm", searchProblemForm_);
    }


    //Для вставки в бд тегов
    public void genTags() {
        hashTagService.deleteAll();
        Map<Integer, HashTag> level2tag = new HashMap<>();
        try {
            File file = new File("H:\\Университет\\8 сем - FINISH!!!\\Диплом\\Теги\\теги.txt");
            //создаем объект FileReader для объекта File
            FileReader fr = new FileReader(file);
            //создаем BufferedReader с существующего FileReader для построчного считывания
            BufferedReader reader = new BufferedReader(fr);
            // считаем сначала первую строку
            String line = reader.readLine();
            int l = 0;
            while (line.charAt(l) == '\t')
                l++;

            while (line != null) {
                String nameTag = line.trim();
                HashTag tag = new HashTag();
                tag.setName(nameTag);
                if (l != 0)
                    tag.setParent(level2tag.get(l-1));
                hashTagService.save(tag);

                line = reader.readLine();
                int oldL = l;
                l = 0;
                while (line.charAt(l) == '\t')
                    l++;
                if (l > oldL) {
                    level2tag.put(oldL, tag);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
