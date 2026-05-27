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

package io.github.hylexus.xtream.quickstart.ext.jt808.withstorage.blocking.service;

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1210;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage1212;
import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.ext.BuiltinMessage30316364;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestEntity;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808Session;

import java.io.IOException;

/**
 * @author hylexus
 */
public interface AttachmentFileService {

    String createFileIfNecessary(String terminalId, BuiltinMessage1210.AttachmentItem attachmentItem) throws IOException;

    int writeDataFragmentAsync(Jt808Session session, BuiltinMessage30316364 body, BuiltinMessage1210.AttachmentItem attachmentItem);

    boolean moveFileToRemoteStorage(Jt808RequestEntity<BuiltinMessage1212> requestEntity, BuiltinMessage1210.AttachmentItem attachmentItem, boolean deleteLocalFile);

}
