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

package io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.response;

import io.github.hylexus.xtream.codec.core.annotation.PrependLengthFieldType;
import io.github.hylexus.xtream.codec.core.type.Preset;
import io.github.hylexus.xtream.codec.ext.jt808.extensions.handler.Jt808ResponseBody;

import java.util.List;
import java.util.StringJoiner;

/**
 * 提问下发
 *
 * @author hylexus
 */
@Jt808ResponseBody(messageId = 0x8302, desc = "提问下发")
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
    @Preset.JtStyle.Byte(desc = "事件类型")
    private short identifier;

    // prependLengthFieldType: 前置一个 u8类型的字段 表示 问题内容长度
    @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u8, desc = "问题")
    private String question;

    @Preset.JtStyle.List(desc = "候选答案列表")
    private List<CandidateAnswer> candidateAnswerList;

    public short getIdentifier() {
        return identifier;
    }

    public BuiltinMessage8302 setIdentifier(short identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public BuiltinMessage8302 setQuestion(String question) {
        this.question = question;
        return this;
    }

    public List<CandidateAnswer> getCandidateAnswerList() {
        return candidateAnswerList;
    }

    public BuiltinMessage8302 setCandidateAnswerList(List<CandidateAnswer> candidateAnswerList) {
        this.candidateAnswerList = candidateAnswerList;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BuiltinMessage8302.class.getSimpleName() + "[", "]")
                .add("identifier=" + identifier)
                .add("question='" + question + "'")
                .add("candidateAnswerList=" + candidateAnswerList)
                .toString();
    }

    public static class CandidateAnswer {

        @Preset.JtStyle.Byte(desc = "答案 ID")
        private short answerId;

        // prependLengthFieldType: 前置一个 u16(Word)类型的字段 表示 答案内容
        @Preset.JtStyle.Str(prependLengthFieldType = PrependLengthFieldType.u16, desc = "答案内容")
        private String answer;

        public short getAnswerId() {
            return answerId;
        }

        public CandidateAnswer setAnswerId(short answerId) {
            this.answerId = answerId;
            return this;
        }

        public String getAnswer() {
            return answer;
        }

        public CandidateAnswer setAnswer(String answer) {
            this.answer = answer;
            return this;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CandidateAnswer.class.getSimpleName() + "[", "]")
                    .add("answerId=" + answerId)
                    .add("answer='" + answer + "'")
                    .toString();
        }
    }
}
