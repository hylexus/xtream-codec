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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 提问下发
 *
 * @author hylexus
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Jt808ResponseBody(messageId = 0x8302)
public class BuiltinMessage8302 {
    /**
     * 事件类型
     * <li>bit[0] -- 1：紧急</li>
     * <li>bit[1] -- 保留</li>
     * <li>bit[2] -- 保留</li>
     * <li>bit[3] -- 1：终端TTS播读</li>
     * <li>bit[4] -- 1：广告屏显示</li>
     * <li>bit[5~7] -- 保留</li>
     */
    @Preset.JtStyle.Byte
    private short identifier;

    /**
     * 问题内容长度
     */
    @Preset.JtStyle.Byte
    private short questionLength;

    /**
     * 问题
     */
    @Preset.JtStyle.Str(lengthExpression = "getQuestionLength()")
    private String question;

    /**
     * 候选答案列表
     */
    @Preset.JtStyle.List
    private List<CandidateAnswer> candidateAnswerList;

    @SuppressWarnings("lombok")
    public short getQuestionLength() {
        return questionLength;
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class CandidateAnswer {
        /**
         * 答案 ID
         */
        @Preset.JtStyle.Byte
        private short answerId;

        /**
         * 答案内容长度
         */
        @Preset.JtStyle.Word
        private int answerLength;

        /**
         * 答案内容
         */
        @Preset.JtStyle.Str(lengthExpression = "getAnswerLength()")
        private String answer;

        @SuppressWarnings("lombok")
        public int getAnswerLength() {
            return answerLength;
        }
    }
}