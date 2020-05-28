package ru.vkr.vkr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.HashTag;
import ru.vkr.vkr.repository.HashTagRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Service
public class HashTagService {
    @Autowired
    private HashTagRepository hashTagRepository;

    //Возвращает список тегов, отсортированных для корректного вывода в виде дерева
    public Collection<HashTag> getAllTags(){
        Collection<HashTag> sortedTags = new ArrayList<>();

        Collection<HashTag> hashTagList = hashTagRepository.findAll();

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
