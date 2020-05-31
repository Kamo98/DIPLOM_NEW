package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.entity.Chapter;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.entity.Theory;
import ru.vkr.vkr.form.TheoryMaterialForm;
import ru.vkr.vkr.repository.ChapterRepository;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.TheoryRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private static Logger logger = LoggerFactory.getLogger(ChapterService.class);
    @Value("${spring.servlet.multipart.location}")
    private String location;

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    public Theory getTheoryById(Long id) {
        return theoryRepository.getOne(id);
    }

    public void deleteTheory(Long chapterId, Long tmId) {
        Chapter chapter = getChapterById(chapterId);
        Theory theory = theoryRepository.findById(tmId).get();
        chapter.getChapterTheories().remove(theory);
        chapterRepository.save(chapter);
    }

    private String convertPathToString(String path) {
        StringBuilder resultBuild = new StringBuilder();
        for (int i = 0; i < path.length(); ++i) {
            resultBuild.append(path.charAt(i));
            if (path.charAt(i) == '\\') {
                resultBuild.append(path.charAt(i));
            }
        }
        return resultBuild.toString();
    }

    private String convertFileToString(MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String newLocation = location + "theory" + "\\" + UUID.randomUUID().toString();
            File uploadDir = new File(newLocation);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String resultFilename = convertPathToString(newLocation + "\\" + file.getOriginalFilename());
            try {
                file.transferTo(new File(resultFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultFilename;
        }
        return "";
    }

    public void loadTheory(Chapter chapter, TheoryMaterialForm theoryMaterialForm) {
        if (theoryMaterialForm.getLink().equals("")) {
            String sourceChapter = convertFileToString(theoryMaterialForm.getMultipartFile());
            if (fileExists(sourceChapter)) {
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
