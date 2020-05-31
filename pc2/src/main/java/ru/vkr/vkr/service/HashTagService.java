package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.repository.HashTagRepository;

import java.util.*;

@Service
public class HashTagService {
    @Autowired
    private HashTagRepository hashTagRepository;

    public void save(HashTag hashTag) {
        hashTagRepository.save(hashTag);
    }

    public void deleteAll() {
        hashTagRepository.deleteAll();
    }


    //Проверяет в списке тегов, отмеченных пользователем, наличие предков и при необходимости добавляет их с меткой visible = false
    public Map<HashTag, Boolean> checkAndAddParents(Set<HashTag> tagsFromUser) {
        Map<HashTag, Boolean> resultTagList = new HashMap<>();
        for (HashTag tagProblem : tagsFromUser) {
            goToParents(tagProblem, resultTagList);
        }
        return resultTagList;
    }

    //Для рекусривного подъёма по дереву тегов и отметки о том, что родительские теги не должны выводиться
    private void goToParents(HashTag cur, Map<HashTag, Boolean> resultTagList) {
        if (!resultTagList.containsKey(cur))        //Тега ещё нет в списке, начит пока не встречали его детей
            resultTagList.put(cur, true);
        if (cur.getParent() != null) {
            if (!resultTagList.containsKey(cur.getParent()))      //Родитель тега ещё не добавлен в рез список
                resultTagList.put(cur.getParent(), false);      //родитель должен быть не видимым
            else        //Родитель уже есть в результате
                resultTagList.replace(cur.getParent(), false);
            goToParents(cur.getParent(), resultTagList);
        }
    }

    //Возвращает список тегов, отсортированных для корректного вывода в виде дерева
    public List<HashTag> getAllTags(){
        List<HashTag> sortedTags = new ArrayList<>();

        List<HashTag> hashTagList = hashTagRepository.findAll();

        //Построим список смежности дерева
        HashMap<Long, Collection<HashTag>> tag2child = new HashMap<>();     //Для хранения детей каждого тега
        for(HashTag tag : hashTagList) {
            Long parentId = -1L;
            if (tag.getParent() != null)
                parentId = tag.getParent().getId();

            if (!tag2child.containsKey(parentId))
                tag2child.put(parentId, new ArrayList<>());

            tag2child.get(parentId).add(tag);
        }

        dfs(-1L, sortedTags, tag2child, 0);
        return sortedTags;
    }


    private void dfs(Long cur, Collection<HashTag> sortedTags, HashMap<Long, Collection<HashTag>> tag2child, int level) {
        if (tag2child.containsKey(cur))
            for (HashTag to : tag2child.get(cur)) {
                to.setLevel(level);
                sortedTags.add(to);
                dfs(to.getId(), sortedTags, tag2child, level + 1);
            }
    }
}
