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

package io.github.hylexus.xtream.codec.base.web.domain.vo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageableVoTest {

    @Test
    void testOf() {
        PageableVo<String> vo = PageableVo.of(100, List.of("a", "b", "c"));
        assertEquals(100, vo.getTotal());
        assertEquals(3, vo.getData().size());
        assertEquals("a", vo.getData().getFirst());
    }

    @Test
    void testEmpty() {
        PageableVo<String> vo = PageableVo.empty();
        assertEquals(0, vo.getTotal());
        assertTrue(vo.getData().isEmpty());
    }

    @Test
    void testFluentSetters() {
        PageableVo<Integer> vo = new PageableVo<>();
        PageableVo<Integer> returned = vo.setTotal(50);
        assertSame(vo, returned);
        returned = vo.setData(List.of(1, 2));
        assertSame(vo, returned);
        assertEquals(50, vo.getTotal());
        assertEquals(List.of(1, 2), vo.getData());
    }

    @Test
    void testConstructor() {
        PageableVo<String> vo = new PageableVo<>(10, List.of("x"));
        assertEquals(10, vo.getTotal());
        assertEquals(List.of("x"), vo.getData());
    }

    @Test
    void testToString() {
        PageableVo<String> vo = PageableVo.of(5, List.of("a"));
        String str = vo.toString();
        assertTrue(str.contains("total=5"));
        assertTrue(str.contains("a"));
    }
}
