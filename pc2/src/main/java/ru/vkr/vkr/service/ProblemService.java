package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.entity.Chapter;
import ru.vkr.vkr.entity.Problem;
import ru.vkr.vkr.entity.Theory;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.form.TheoryMaterialForm;
import ru.vkr.vkr.repository.ProblemRepository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@Service
public class ProblemService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;
    @Value("${spring.servlet.multipart.location}")
    private String location;

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
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

    public void loadStatement(Problem problem, TheoryMaterialForm theoryMaterialForm) {
        String sourceStatement = convertFileToString(theoryMaterialForm.getMultipartFile());
        problem.setNameOfTextProblem(theoryMaterialForm.getName());
        problem.setPathToTextProblem(sourceStatement);
        problemRepository.save(problem);
    }

    public void deleteStatement(Problem problem) {
        problem.setPathToTextProblem(null);
        problem.setNameOfTextProblem(null);
        problemRepository.save(problem);
    }

    public void save(Problem problem) {
        logger.info("save problem " + problem.toString());
        problemRepository.save(problem);
    }


    public Problem getProblemById(Long problemId) {
        return problemRepository.findById(problemId).get();
    }

    //Устанавливает автора создаваемой задачи
    public void setAuthorForNewCourse(Problem problem){
        //Автор задачи - текущий пользователь (преподаватель)
        problem.setTeacherAuthor(authenticationFacade.getCurrentTeacher());
    }

    public Collection<Problem> getProblemsByCurrentTeacher() {
        return problemRepository.findByTeacherAuthor_id(authenticationFacade.getCurrentTeacher().getId());
    }
}
