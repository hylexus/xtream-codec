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

package io.github.hylexus.xtream.codec.ext.jt1078.spec.impl;


import io.github.hylexus.xtream.codec.ext.jt1078.exception.Jt1078DecodeException;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078DataType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078PayloadType;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078RequestHeader;
import io.github.hylexus.xtream.codec.ext.jt1078.spec.Jt1078SubPackageIdentifier;

import java.util.Optional;

/**
 * @author hylexus
 */
@SuppressWarnings("NullAway")
public class DefaultJt1078RequestHeader implements Jt1078RequestHeader {

    private short offset4;
    @SuppressWarnings("checkstyle:MemberName")
    private byte m;
    private Jt1078PayloadType payloadType;
    private int sequenceNumber;
    private int simLength;
    private String rawSim;
    private String convertedSim;
    private short channelNumber;
    private Jt1078DataType dataType;
    private Jt1078SubPackageIdentifier subPackageIdentifier;
    private Long timestamp;
    private Integer lastIFrameInterval;
    private Integer lastFrameInterval;
    private int msgBodyLength;


    public DefaultJt1078RequestHeader() {
    }

    @Override
    public short offset4() {
        return this.offset4;
    }

    public void offset4(short value) {
        this.offset4 = (short) (value & 0xFF);
    }

    public void markerBitAndPayloadType(short value) {
        this.m((byte) ((value >> 7) & 0b01));
        this.payloadType((byte) (value & 0b0111_1111));
    }

    @Override
    @SuppressWarnings("checkstyle:methodname")
    public byte m() {
        return this.m;
    }

    @SuppressWarnings("checkstyle:methodname")
    public void m(byte value) {
        this.m = value;
    }

    @Override
    public Jt1078PayloadType payloadType() {
        return this.payloadType;
    }

    public void payloadType(byte value) {
        this.payloadType = Jt1078PayloadType.createOrError(value);
    }

    public void payloadType(Jt1078PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    public int sequenceNumber() {
        return this.sequenceNumber;
    }

    public void sequenceNumber(int value) {
        this.sequenceNumber = value;
    }

    @Override
    public int simLength() {
        return this.simLength;
    }

    public void simLength(int value) {
        this.simLength = value;
    }

    @Override
    public String rawSim() {
        return this.rawSim;
    }

    public void rawSim(String value) {
        this.rawSim = value;
    }

    @Override
    public String convertedSim() {
        return this.convertedSim;
    }

    public void convertedSim(String value) {
        this.convertedSim = value;
    }

    @Override
    public short channelNumber() {
        return this.channelNumber;
    }

    public DefaultJt1078RequestHeader channelNumber(short value) {
        this.channelNumber = value;
        return this;
    }

    public void dataTypeAndSubPackageIdentifier(short value) {
        this.dataType = Jt1078DataType.of(Jt1078RequestHeader.dataTypeValue(value))
                .orElseThrow(() -> new Jt1078DecodeException("Unknown dataType: " + value));
        this.subPackageIdentifier = Jt1078SubPackageIdentifier.of(Jt1078RequestHeader.subPackageIdentifierValue(value))
                .orElseThrow(() -> new Jt1078DecodeException("Unknown subPackageIdentifier " + value));
    }

    @Override
    public Jt1078DataType dataType() {
        return dataType;
    }

    public void dataType(Jt1078DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public Jt1078SubPackageIdentifier subPackageIdentifier() {
        return subPackageIdentifier;
    }

    public void subPackageIdentifier(Jt1078SubPackageIdentifier subPackageIdentifier) {
        this.subPackageIdentifier = subPackageIdentifier;
    }

    @Override
    public Optional<Long> timestamp() {
        return Optional.ofNullable(this.timestamp);
    }

    public DefaultJt1078RequestHeader timestamp(Long value) {
        this.timestamp = value;
        return this;
    }

    @Override
    public Optional<Integer> lastIFrameInterval() {
        return Optional.ofNullable(this.lastIFrameInterval);
    }

    public void lastIFrameInterval(Integer value) {
        this.lastIFrameInterval = value;
    }

    @Override
    public Optional<Integer> lastFrameInterval() {
        return Optional.ofNullable(this.lastFrameInterval);
    }

    public void lastFrameInterval(Integer value) {
        this.lastFrameInterval = value;
    }

    @Override
    public int msgBodyLength() {
        return this.msgBodyLength;
    }

    public void msgBodyLength(int value) {
        this.msgBodyLength = value;
    }

    @Override
    public String toString() {
        return "DefaultJt1078RequestHeader{"
               + "V=" + this.v()
               + ", P=" + this.p()
               + ", X=" + this.x()
               + ", CC=" + this.cc()
               + ", M=" + m()
               + ", PT=" + payloadType()
               + ", sequenceNumber=" + sequenceNumber
               + ", simLength=" + simLength
               + ", convertedSim='" + convertedSim + '\''
               + ", rawSim='" + rawSim + '\''
               + ", channelNumber=" + channelNumber
               + ", dataType=" + dataType
               + ", subPackageIdentifier=" + subPackageIdentifier
               + ", timestamp=" + timestamp
               + ", lastIFrameInterval=" + lastIFrameInterval
               + ", lastFrameInterval=" + lastFrameInterval
               + ", msgBodyLength=" + msgBodyLength
               + '}';
    }
}
