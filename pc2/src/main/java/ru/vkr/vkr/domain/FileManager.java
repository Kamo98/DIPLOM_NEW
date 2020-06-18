package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.api.ILanguage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vkr.vkr.domain.run.RunStatistic;

import java.io.*;
import java.util.UUID;

@Component
public class FileManager {

    private static String location;

    @Value("${spring.servlet.multipart.location}")
    public void setLocationStatic(String location){
        FileManager.location = location;
    }

    public static boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    private static String convertPathToString(String path) {
        StringBuilder resultBuild = new StringBuilder();
        for (int i = 0; i < path.length(); ++i) {
            resultBuild.append(path.charAt(i));
            if (path.charAt(i) == '\\') {
                resultBuild.append(path.charAt(i));
            }
        }
        return resultBuild.toString();
    }

    public static String loadFileToServer(MultipartFile file, String nameOfFolder) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String newLocation = location + nameOfFolder + "\\" + UUID.randomUUID().toString();
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

    public static String loadSourceToServer(String source, String nameOfFolder, ILanguage iLanguage) {
        if (iLanguage != null && !source.isEmpty()) {
            String newLocation = location + nameOfFolder + "\\" + UUID.randomUUID().toString();
            File uploadDir = new File(newLocation);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String resultFilename = convertPathToString(newLocation + "\\" + "source" + Format.getFormat(iLanguage.getName()));
            try (OutputStream outputStream = new FileOutputStream(resultFilename);
                 BufferedOutputStream fileOutputStream = new BufferedOutputStream(outputStream)) {
                fileOutputStream.write(source.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultFilename;
        }
        return "";
    }

    public static void saveRunStatistic(RunStatistic runStatistic) {
        //создаем 2 потока для сериализации объекта и сохранения его в файл
        try(FileOutputStream outputStream = new FileOutputStream("C:\\diplom\\save.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            // сохраняем статистические данные в файл
            objectOutputStream.writeObject(runStatistic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
