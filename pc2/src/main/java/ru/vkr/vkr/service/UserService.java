package ru.vkr.vkr.service;


import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vkr.vkr.domain.ROLE;
import ru.vkr.vkr.domain.RandomString;
import ru.vkr.vkr.domain.Translit;
import ru.vkr.vkr.domain.exception.Pc2Exception;
import ru.vkr.vkr.entity.Student;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.entity.User;
import ru.vkr.vkr.form.UserForm;
import ru.vkr.vkr.repository.RoleRepository;
import ru.vkr.vkr.repository.StudentRepository;
import ru.vkr.vkr.repository.TeacherRepository;
import ru.vkr.vkr.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserService implements UserDetailsService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @PersistenceContext
    private EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private BridgePc2Service bridgePc2Service;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }

    public User findUserById(Long userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }

    public boolean saveUser(User user) {
        User userFromDB = userRepository.findByUsername(user.getUsername());
        if (userFromDB != null) {
            return false;
        }
        user.setPassword(user.getPassword());
        userRepository.save(user);
        return true;
    }

    public boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public List<User> usergtList(Long idMin) {
        return em.createQuery("SELECT u FROM User u WHERE u.id > :paramId", User.class)
                .setParameter("paramId", idMin).getResultList();
    }

    //todo: когда будет интерфейс, нужно будет возвращать его, а не идентификаторы teacher или student
    public List<Long> addUsers(UserForm userForm, ROLE role) throws Pc2Exception {
        int countUser = getCountUser(role);
        if (countUser == -1) return null;

        List<Long> usersId = new ArrayList<>();
        String fios = userForm.getFios().trim();
        List<String> surname = new ArrayList<>();
        List<String> name = new ArrayList<>();
        List<String> middleName = new ArrayList<>();

        //todo: я поппытался написать регулярное выражения для split, но оно всё равно неадекватно работает, поэтому просто прверяю, что фио непустое
        String[] fiosArr = fios.split("\n+|\r+");
        for (String fio : fiosArr) {
            if (!fio.trim().equals("")) {
                logger.info("AdminFacade.addUsers: fio = " + fio);
                String[] curFIO = fio.trim().split(" +");
                if (curFIO.length < 3) {
                    return null;
                } else {
                    logger.info("UserService.addUsers: surname = " + curFIO[0]);
                    logger.info("UserService.addUsers: name = " + curFIO[1]);
                    logger.info("UserService.addUsers: middleName = " + curFIO[2]);
                    surname.add(curFIO[0].trim());
                    name.add(curFIO[1].trim());
                    middleName.add(curFIO[2].trim());
                }
            }
        }
        logger.info("UserService.addUsers: count surnames = " + surname.size());
        for (int i = 0; i < surname.size(); ++i) {
            User user = new User();
            //todo: логин сделал как транслит фио, по идее это норм, но длину при этом мы не контролируем
            String login = Translit.fio2login(surname.get(i), name.get(i), middleName.get(i));
            String password = RandomString.getAlphaNumericString(5);
            user.setUsername(login);
            user.setPassword(password);
            user.setRole(roleRepository.findById(role.getId()).get());
            user.setLoginPC2(generateNewAccount(role, countUser++));
            saveUser(user);
            //todo: добавляю суффикс к логину (id), но приходится обновлять запись
            user.setUsername(login + "_" + user.getId());
            saveUser(user);

            logger.info("UserService.addUsers: fio = " + fiosArr[i] + "  login = " + login + "  pass = " + password +
                    "loginPC2 = ");

            switch (role) {
                case ROLE_STUDENT: {
                    Student student = new Student();
                    student.setMiddleName(middleName.get(i));
                    student.setName(name.get(i));
                    student.setSurname(surname.get(i));
                    student.setUser(user);
                    studentRepository.save(student);
                    usersId.add(student.getId());
                    break;
                }
                case ROLE_TEACHER: {
                    Teacher teacher = new Teacher();
                    teacher.setMiddleName(middleName.get(i));
                    teacher.setName(name.get(i));
                    teacher.setSurname(surname.get(i));
                    teacher.setUser(user);
                    teacherRepository.save(teacher);
                    usersId.add(teacher.getId());
                    break;
                }
                default:
                    return null;
            }
        }
        return usersId;
    }

    /**
     * генерация соответствующего пользователя в pc2
     *
     * @return логин и пароль сгенерированного пользователя
     */
    private String generateNewAccount(ROLE role, int countUser) throws Pc2Exception {
        String auth = role.getRolePc2().toLowerCase() + countUser;
        bridgePc2Service.addAccount(role.getRolePc2(), auth, auth);
        return auth;
    }

    // передача accountNumber of new account
    private int getCountUser(ROLE role) throws Pc2Exception {
        if (role == ROLE.ROLE_STUDENT) {
            return bridgePc2Service.getContestTeams().length + 1;
        } else {
            IClient iClients[] = bridgePc2Service.getContestClients();
            int count = 0;
            for (IClient iClient : iClients) {
                if (iClient.getType() == IClient.ClientType.ADMIN_CLIENT) {
                    count++;
                }
            }
            return count + 1;
        }
    }
}
