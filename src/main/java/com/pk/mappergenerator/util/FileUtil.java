package com.pk.mappergenerator.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {

    public static String readAsString(String path) {
        return readAsString(new File(path));
    }

    public static String readAsString(InputStream in) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(Const.ENDL);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            log.error("文件字符集非UTF-8", e);
        } catch (IOException e) {
            log.error("文件读取出错！", e);
        }
        return null;
    }

    public static String readAsString(File file) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.forName("utf-8"));
            if (lines != null && lines.size() > 0) {
                String content = lines.stream().collect(Collectors.joining(Const.ENDL));
                return content;
            }
        } catch (IOException e) {
            log.error("文件读取出错！", e);
        }
        return null;
    }

    public static void writeString(String filePath, String content) throws IOException {
        File file = new File(filePath);
        writeString(file, content);
    }

    public static void writeString(File file, String content) throws IOException {
        // 如果文件不存在就创建
        FileUtil.createIfNotExist(file);
        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            writer.write(content);
        }
    }

    public static boolean createIfNotExist(File file) throws IOException {
        File parentDir = file.getParentFile();
        boolean parentCreated = false;
        if (!parentDir.exists()) {
            parentCreated = parentDir.mkdirs();
        }
        if (parentCreated) {
            return file.createNewFile();
        }
        return false;
    }
}
