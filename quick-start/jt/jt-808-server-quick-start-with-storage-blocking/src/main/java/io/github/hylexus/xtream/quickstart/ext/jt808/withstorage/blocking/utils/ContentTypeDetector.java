/*
 * Copyright 2024-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hylexus
 */
public final class ContentTypeDetector {
    private ContentTypeDetector() {
        throw new UnsupportedOperationException();
    }

    private static final Map<String, String> CONTENT_TYPE_MAPPING = new HashMap<>();

    static {
        CONTENT_TYPE_MAPPING.put("jpg", "image/jpeg");
        CONTENT_TYPE_MAPPING.put("jpeg", "image/jpeg");
        CONTENT_TYPE_MAPPING.put("png", "image/png");
        CONTENT_TYPE_MAPPING.put("gif", "image/gif");
        CONTENT_TYPE_MAPPING.put("bmp", "image/bmp");
        CONTENT_TYPE_MAPPING.put("tiff", "image/tiff");
        CONTENT_TYPE_MAPPING.put("pdf", "application/pdf");
        CONTENT_TYPE_MAPPING.put("doc", "application/msword");
        CONTENT_TYPE_MAPPING.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        CONTENT_TYPE_MAPPING.put("xls", "application/vnd.ms-excel");
        CONTENT_TYPE_MAPPING.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        CONTENT_TYPE_MAPPING.put("ppt", "application/vnd.ms-powerpoint");
        CONTENT_TYPE_MAPPING.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        CONTENT_TYPE_MAPPING.put("txt", "text/plain");
        CONTENT_TYPE_MAPPING.put("html", "text/html");
        CONTENT_TYPE_MAPPING.put("css", "text/css");
        CONTENT_TYPE_MAPPING.put("js", "application/javascript");
        CONTENT_TYPE_MAPPING.put("json", "application/json");
        CONTENT_TYPE_MAPPING.put("xml", "application/xml");
        CONTENT_TYPE_MAPPING.put("zip", "application/zip");
        CONTENT_TYPE_MAPPING.put("gz", "application/gzip");
        CONTENT_TYPE_MAPPING.put("mp4", "video/mp4");
    }

    public static String detectContentType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "application/octet-stream";
        }

        final int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            return CONTENT_TYPE_MAPPING.getOrDefault(extension, "application/octet-stream");
        }

        return "application/octet-stream";
    }

}
