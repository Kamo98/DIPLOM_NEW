package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.form.SearchProblemForm;

import java.util.*;

@Service
public class SearchService {

    @Autowired
    private ProblemService problemService;
    @Autowired
    private HashTagService hashTagService;


    //Для страницы пула задач с пустым списком задач
    public void poolProblemsGet(Model model) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        SearchProblemForm searchProblemForm = new SearchProblemForm(hashTags.size());
        model.addAttribute("searchProblemForm", searchProblemForm);
        model.addAttribute("isFirstRunPool", true);
    }

    //Для страницы фильтрации задач
    public void poolSearchProblems(Model model, SearchProblemForm searchProblemForm) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        filterProblemsByTags(model, searchProblemForm, hashTags, -1L);
    }

    //Для фильтрации по одному тегу
    public void poolSearchProblems(Model model, Long selectedTagId) {
        List<HashTag> hashTags = hashTagService.getAllTags();
        model.addAttribute("hashTags", hashTags);
        SearchProblemForm searchProblemForm = new SearchProblemForm(hashTags.size());
        filterProblemsByTags(model, searchProblemForm, hashTags, selectedTagId);
    }


    //Непосредстванно фильтрация задач
    //Ищет как по одному тегу selectedTagId, так и по списку тегов из searchProblemForm
    private void filterProblemsByTags(Model model, SearchProblemForm searchProblemForm, List<HashTag> hashTags, Long selectedTagId) {
        Set<Problem> problems = new HashSet<>();
        for (int i = 0; i < searchProblemForm.getTagList().size(); i++) {
            if (selectedTagId != -1L && hashTags.get(i).getId().equals(selectedTagId))
                searchProblemForm.getTagList().set(i, true);
            if (searchProblemForm.getTagList().get(i) != null && searchProblemForm.getTagList().get(i))
                problems.addAll(hashTags.get(i).getProblems());
        }

        model.addAttribute("problems", problems);
        SearchProblemForm searchProblemForm_ = new SearchProblemForm(hashTags.size(), searchProblemForm);
        model.addAttribute("searchProblemForm", searchProblemForm_);
    }


    //Для вставки в бд тегов
    @GetMapping("/teacher/gen-tags")
    public void genTags() {
        HashMap<String, Collection<String>> tag2child = new HashMap<>();

        final String structData = "Структуры данных";
        final String grafs = "Графы";

        tag2child.put(structData, new ArrayList<>());
        tag2child.put(grafs, new ArrayList<>());

        tag2child.get(structData).add("Элементарные структуры данных");
        tag2child.get(structData).add("Хеш-таблицы");
        tag2child.get(structData).add("Бинарные деревья поиска");
        tag2child.get(structData).add("Красно-чёрные деревья");
        tag2child.get(structData).add("Красно-чёрные деревья");

        tag2child.get(grafs).add("Минимальный остов");
        tag2child.get(grafs).add("Кратчайшие пути");
        tag2child.get(grafs).add("Обходы графов");
        tag2child.get(grafs).add("Максимальный поток");

        for (Map.Entry<String, Collection<String>> unit : tag2child.entrySet()) {
            HashTag parent = new HashTag();
            parent.setName(unit.getKey());
            hashTagService.save(parent);

            for(String item : unit.getValue()) {
                HashTag tag = new HashTag();
                tag.setName(item);
                tag.setParent(parent);
                hashTagService.save(tag);
            }

        }
    }

}
