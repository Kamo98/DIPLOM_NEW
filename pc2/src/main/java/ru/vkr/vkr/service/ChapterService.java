package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.FileManager;
import ru.vkr.vkr.entity.Chapter;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.entity.Theory;
import ru.vkr.vkr.form.TheoryMaterialForm;
import ru.vkr.vkr.repository.ChapterRepository;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.TheoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private TheoryRepository theoryRepository;
    @PersistenceContext
    EntityManager entityManager;

    private final String nameOfFolder = "theory";

    private static Logger logger = LoggerFactory.getLogger(ChapterService.class);


    public Theory getTheoryById(Long id) {
        return theoryRepository.getOne(id);
    }

    public void deleteTheory(Long chapterId, Long tmId) {
        Chapter chapter = getChapterById(chapterId);
        Theory theory = theoryRepository.findById(tmId).get();
        chapter.getChapterTheories().remove(theory);
        chapterRepository.save(chapter);
    }


    public void loadTheory(Chapter chapter, TheoryMaterialForm theoryMaterialForm) {
        if (theoryMaterialForm.getLink().equals("")) {
            String sourceChapter = FileManager.loadFileNewRandomDirToServer(theoryMaterialForm.getMultipartFile(), nameOfFolder);
            if (FileManager.fileExists(sourceChapter)) {
                Theory theory = new Theory();
                theory.setName(theoryMaterialForm.getName());
                theory.setSource(sourceChapter);
                theory.setFile(true);
                chapter.getChapterTheories().add(theory);
                theoryRepository.save(theory);
            }
        } else {
            Theory theory = new Theory();
            theory.setName(theoryMaterialForm.getName());
            theory.setSource(theoryMaterialForm.getLink());
            theory.setFile(false);
            chapter.getChapterTheories().add(theory);
            theoryRepository.save(theory);
        }
    }

    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id).get();
    }

    public List<Chapter> getAll(){
        return chapterRepository.findAll();
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


    public void attachProblem(Chapter chapter, Problem problem) {
        chapter.getChapterProblems().add(problem);
        chapterRepository.save(chapter);
    }

    public void dettachProblem(Chapter chapter, Problem problem) {
        chapter.getChapterProblems().remove(problem);
        chapterRepository.save(chapter);
    }
}
