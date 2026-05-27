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

package io.github.hylexus.xtream.codec.ext.jt1078.codec;

import java.io.*;

public class AbstractAudioVideoCodecTest {

    protected InputStream getInputStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    protected void deleteFileIfExists(String filePath) {
        final File file = new File(filePath);
        if (file.exists()) {
            var ignored = file.delete();
        }
    }

    protected OutputStream getMockDataOutputFileStream(String path) {
        final File file = new File(path);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException("新建目录失败:" + file);
            }
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new RuntimeException("新建文件失败:" + file);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
