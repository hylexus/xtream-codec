/*
 * Copyright 2024 the original author or authors.
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

package io.github.hylexus.xtream.codec.ext.jt1078.codec.h264;

import java.util.StringJoiner;

public class Sps {
    public Sps() {
    }

    private byte profileIdc;
    private byte levelIdc;
    private byte profileCompat;
    private int width;
    private int height;

    public byte getProfileIdc() {
        return profileIdc;
    }

    public Sps setProfileIdc(byte profileIdc) {
        this.profileIdc = profileIdc;
        return this;
    }

    public byte getLevelIdc() {
        return levelIdc;
    }

    public Sps setLevelIdc(byte levelIdc) {
        this.levelIdc = levelIdc;
        return this;
    }

    public byte getProfileCompat() {
        return profileCompat;
    }

    public Sps setProfileCompat(byte profileCompat) {
        this.profileCompat = profileCompat;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public Sps setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Sps setHeight(int height) {
        this.height = height;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Sps.class.getSimpleName() + "[", "]")
                .add("profileIdc=" + profileIdc)
                .add("levelIdc=" + levelIdc)
                .add("profileCompat=" + profileCompat)
                .add("width=" + width)
                .add("height=" + height)
                .toString();
    }
}
