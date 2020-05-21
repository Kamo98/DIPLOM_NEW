package ru.vkr.vkr.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.vkr.vkr.entity.Teacher;
import ru.vkr.vkr.facade.AdminFacade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/download")
public class DownloadController {
    @Autowired
    private AdminFacade adminFacade;

    private static final String FONT = "static\\fonts\\DejaVuSans.ttf";
    private static final String FILE_PATH = "src\\main\\resources\\tmp\\report.pdf";
    private static final String APPLICATION_PDF = "application/pdf";

    // скачивание pdf документа с логинами и паролями пользоваетелй
    @RequestMapping(value = "/b", method = RequestMethod.GET, produces = APPLICATION_PDF)
    public @ResponseBody
    HttpEntity<byte[]> downloadB() throws Exception {
        createLoginPasswordDocument();
        File file = getFile();
        byte[] document = FileCopyUtils.copyToByteArray(file);

        HttpHeaders header = new HttpHeaders();
        header.setContentType(new MediaType("application", "pdf"));
        header.set("Content-Disposition", "inline; filename=" + file.getName());
        header.setContentLength(document.length);

        return new HttpEntity(document, header);
    }

    private File getFile() throws FileNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            throw new FileNotFoundException("file with path: " + FILE_PATH + " was not found.");
        }
        return file;
    }


    public void createLoginPasswordDocument() throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(FILE_PATH));
        document.open();
        document.add(new Paragraph("table"));
        document.add(new Paragraph(new Date().toString()));
        PdfPTable table = new PdfPTable(3);

        PdfPCell cell = new PdfPCell(new Paragraph("users"));

        cell.setColspan(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10.0f);
        cell.setBackgroundColor(new BaseColor(140, 221, 8));

        table.addCell(cell);
        List<Teacher> teacherArrayList = adminFacade.getTeachers();



        String[] fio = new String[teacherArrayList.size()];
        String[] login = new String[teacherArrayList.size()];
        String[] password = new String[teacherArrayList.size()];

        int jj = 0;
        for (Teacher teacher : teacherArrayList) {
            fio[jj] = teacher.getSurname() + " " + teacher.getName() + " " + teacher.getMiddleName();
            login[jj] = teacher.getUser().getUsername();
            password[jj] = teacher.getUser().getPassword();
            ++jj;
        }
        Font mainFont = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10.0f);

        table.addCell(new Phrase("ФИО", mainFont));
        table.addCell("login");
        table.addCell("password");

        for (int i = 0; i < teacherArrayList.size(); i++) {
            table.addCell(new Phrase(fio[i], mainFont));
            table.addCell(login[i]);
            table.addCell(password[i]);
        }

        document.add(table);
        document.close();
    }

}
