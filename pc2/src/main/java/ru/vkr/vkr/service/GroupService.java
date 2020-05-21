package ru.vkr.vkr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.entity.Course;
import ru.vkr.vkr.entity.Group;
import ru.vkr.vkr.facade.AuthenticationFacade;
import ru.vkr.vkr.repository.CourseRepository;
import ru.vkr.vkr.repository.GroupRepository;

import java.util.Collection;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private AuthenticationFacade authenticationFacade;


    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    public void saveGroup(Group group) {
        logger.info("save group " + group.toString());
        groupRepository.save(group);
    }

    public void deleteGroup(Group group) {
        logger.info("delete group " + group.toString());
        groupRepository.delete(group);
    }

    //Устанавливает владельца создаваемой группы
    public void setAuthorForNewGroup(Group group){
        //Владелец группы - текущий пользователь (преподаватель)
        group.setTeacherOwner(authenticationFacade.getCurrentTeacher());
    }


    public Group getGroupById(Long idGroup) {
        return groupRepository.getOne(idGroup);
    }

    public Collection<Group> getGroupsByCurrentTeacher() {
        return groupRepository.findByTeacherOwner_id(authenticationFacade.getCurrentTeacher().getId());
    }


}
