package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.Chapter;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.repository.ChapterRepository;
import ru.vkr.vkr.repository.CourseRepository;

@Service
public class ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseRepository courseRepository;
    private static Logger logger = LoggerFactory.getLogger(ChapterService.class);


    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id).get();
    }

    public void setCourse(Chapter chapter, Course course) {
        chapter.setCourseChapters(course);
    }

    public void saveChapter(Chapter chapter) {
        logger.info("save chapter " + chapter.getName());
        chapterRepository.save(chapter);
    }

    public void updateName(Chapter chapter, String name) {
        logger.info("set new name chapter " + chapter.getName() + "-->" + name);
        chapter.setName(name);
    }

    public void deleteChapter(Chapter chapter) {
        logger.info("delete chapter " + chapter.getName());
        chapterRepository.delete(chapter);
    }
}
