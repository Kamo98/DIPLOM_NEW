package ru.vkr.vkr.domain;

import edu.csus.ecs.pc2.core.IInternalController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
}
