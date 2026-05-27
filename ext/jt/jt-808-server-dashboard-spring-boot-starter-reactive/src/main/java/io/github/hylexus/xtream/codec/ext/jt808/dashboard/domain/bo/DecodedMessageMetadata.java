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

package io.github.hylexus.xtream.codec.ext.jt808.dashboard.domain.bo;

import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808RequestHeader;

@SuppressWarnings("NullAway")
public class DecodedMessageMetadata {
    private Jt808RequestHeader header;
    private String originalHexString;
    private String escapedHexString;
    private String escapedBody;
    private int encryptionType;
    private String decryptedHexString;
    private String decryptedBody;

    public DecodedMessageMetadata() {
    }

    public String getEscapedBody() {
        return escapedBody;
    }

    public DecodedMessageMetadata setEscapedBody(String escapedBody) {
        this.escapedBody = escapedBody;
        return this;
    }

    public String getDecryptedBody() {
        return decryptedBody;
    }

    public DecodedMessageMetadata setDecryptedBody(String decryptedBody) {
        this.decryptedBody = decryptedBody;
        return this;
    }

    public Jt808RequestHeader getHeader() {
        return header;
    }

    public DecodedMessageMetadata setHeader(Jt808RequestHeader header) {
        this.header = header;
        return this;
    }

    public String getOriginalHexString() {
        return originalHexString;
    }

    public DecodedMessageMetadata setOriginalHexString(String originalHexString) {
        this.originalHexString = originalHexString;
        return this;
    }

    public String getEscapedHexString() {
        return escapedHexString;
    }

    public DecodedMessageMetadata setEscapedHexString(String escapedHexString) {
        this.escapedHexString = escapedHexString;
        return this;
    }

    public int getEncryptionType() {
        return encryptionType;
    }

    public DecodedMessageMetadata setEncryptionType(int encryptionType) {
        this.encryptionType = encryptionType;
        return this;
    }

    public String getDecryptedHexString() {
        return decryptedHexString;
    }

    public DecodedMessageMetadata setDecryptedHexString(String decryptedHexString) {
        this.decryptedHexString = decryptedHexString;
        return this;
    }
}
