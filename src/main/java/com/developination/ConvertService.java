package com.developination;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RequestScoped
public class ConvertService {
    Logger logger = Logger.getLogger(ConvertService.class);

    public String convert(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        if (inputParts == null || inputParts.isEmpty()) {
            return "No file";
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
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        return message;
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
}
