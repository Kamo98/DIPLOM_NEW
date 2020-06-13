package ru.vkr.vkr.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vkr.vkr.service.SubmitRunService;

@Controller
public class TestController {
    @Autowired
    private SubmitRunService submitRunService;

    //Загрузка монитора
    //@ResponseBody
    @PostMapping("/user/submitions")
    public String getSubmitions(Model model) {
        model.addAttribute("runs", submitRunService.getRunSummit());
        return "tableSubmitions";
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
