package com.developination;

import com.developination.fitnotes2fit.ActivityEncoder.ActivityEncoder;
import com.developination.fitnotes2fit.FitNotesParser.FitNotesParser;
import com.developination.fitnotes2fit.models.Activity;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequestScoped
public class ConvertService {
    Logger logger = Logger.getLogger(ConvertService.class);

    public byte[] convert(MultipartFormDataInput input) {
        Map<String, byte[]> activityFiles = new HashMap<>();
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        if (inputParts == null || inputParts.isEmpty()) {
            return "No file".getBytes(StandardCharsets.UTF_8);
        }
        String fileName = null;
        String mimeType = null;
        String message = null;
        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header =
                        inputPart.getHeaders();
                fileName = getFileName(header);
//                fileNames.add(fileName);
                mimeType = getMimeType(fileName);
//                mimeTypes.add(mimeType);
                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                message = "[uploadFiles] Filename: " + fileName + "; MimeType: " + mimeType;
                logger.debug(message);

                byte[] content = inputStream.readAllBytes();
                List<Activity> activities = FitNotesParser.parseFileNotesIntoActivities(content);
                for (Activity activity: activities) {
                    ActivityEncoder activityEncoder = new ActivityEncoder(activity, (short) 0, 0);
                    logger.debug(activity.getActivityName());
                    activityEncoder.encodeActivity("");
                    Path path = Paths.get(activity.getActivityName() + ".fit");
                    activityFiles.put(activity.getActivityName() + ".fit", Files.readAllBytes(path));
                    Files.deleteIfExists(path);
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        try {
            return zipBytes(activityFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "";
    }

    private String getMimeType(String _fileName) {
        try {
            return StringUtils.defaultIfBlank(Files.probeContentType(new File(_fileName).toPath()),
                    "application/octet-stream");
        } catch (final Exception e) {
            logger.warn("[getMimeType] failed with " + e);
            return "application/octet-stream";
        }
    }

    private byte[] zipBytes(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> file: files.entrySet()) {
            ZipEntry entry = new ZipEntry(file.getKey());
            entry.setSize(file.getValue().length);
            zos.putNextEntry(entry);
            zos.write(file.getValue());
            zos.closeEntry();
        }
        zos.close();
        return baos.toByteArray();
    }
}
