package ru.vkr.vkr.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.service.SubmitRunService;

@Controller
public class TestController {
    @Autowired
    private SubmitRunService submitRunService;
    @Autowired
    AuthenticationFacade authenticationFacade;

    //Загрузка монитора
    //@ResponseBody
    @PostMapping("/user/submitions")
    public String getSubmitions(Model model) {
        //todo: не очень хорошо написал, но надо как-то узнать роль, что формировать сслыку на задачу
        User user = authenticationFacade.getCurrentUser();
        if (user.getOneRole().getName().equals("ROLE_TEACHER"))
            model.addAttribute("isTeacher", true);
        else if (user.getOneRole().getName().equals("ROLE_STUDENT")) {
            model.addAttribute("isStudent", true);
        }
        model.addAttribute("runs", submitRunService.getRunSummit());
        return "tablesubmitions";
    }

    @GetMapping("/user/source/{numberRun}")
    public String showSource(Model model,
                             @PathVariable int numberRun) {
        String sourceCode = submitRunService.showSourceCode(numberRun);
        model.addAttribute("source", sourceCode);
        return "source";
    }

    @GetMapping("/403")
    public String error403() {
        return "/error/403";
    }
}
