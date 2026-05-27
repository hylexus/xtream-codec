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

import io.github.hylexus.xtream.codec.ext.jt808.builtin.messages.BaseCodecTest;
import io.github.hylexus.xtream.codec.ext.jt808.spec.Jt808ProtocolVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuiltinMessage8302Test extends BaseCodecTest {

    @Test
    void testEncode() {
        final BuiltinMessage8302 entity = new BuiltinMessage8302();
        entity.setIdentifier((short) 1);

        final String question = "你是谁?";
        entity.setQuestion(question);
        entity.setCandidateAnswerList(List.of(
                new BuiltinMessage8302.CandidateAnswer()
                        .setAnswerId((short) 1)
                        .setAnswer("张三"),
                new BuiltinMessage8302.CandidateAnswer()
                        .setAnswerId((short) 2)
                        .setAnswer("李四")
        ));

        final String hex = encode(entity, Jt808ProtocolVersion.VERSION_2013, terminalId2013);
        assertEquals("7e8302001701391234432300000107c4e3cac7cbad3f010004d5c5c8fd020004c0eecbc49a7e", hex);
    }

    @Test
    void testDecode() {
        final String hex = "8302001701391234432300000107c4e3cac7cbad3f010004d5c5c8fd020004c0eecbc49a";
        final BuiltinMessage8302 entity = decodeAsEntity(BuiltinMessage8302.class, hex);

        assertEquals(1, entity.getIdentifier());
        assertEquals("你是谁?", entity.getQuestion());
        assertEquals(2, entity.getCandidateAnswerList().size());
        assertEquals("张三", entity.getCandidateAnswerList().getFirst().getAnswer());
        assertEquals("李四", entity.getCandidateAnswerList().getLast().getAnswer());
    }
}
