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

package io.github.hylexus.xtream.codec.base.web.domain.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageableDtoTest {

    @Test
    void testDefaults() {
        PageableDto dto = new PageableDto();
        assertEquals(1, dto.getPageNumber());
        assertEquals(10, dto.getPageSize());
    }

    @Test
    void testGetOffsetFirstPage() {
        PageableDto dto = new PageableDto();
        // (1 - 1) * 10 = 0
        assertEquals(0, dto.getOffset());
    }

    @Test
    void testGetOffsetSecondPage() {
        PageableDto dto = new PageableDto().setPageNumber(2).setPageSize(20);
        // (2 - 1) * 20 = 20
        assertEquals(20, dto.getOffset());
    }

    @Test
    void testGetOffsetLargePage() {
        PageableDto dto = new PageableDto().setPageNumber(5).setPageSize(50);
        // (5 - 1) * 50 = 200
        assertEquals(200, dto.getOffset());
    }

    @Test
    void testFluentSetters() {
        PageableDto dto = new PageableDto();
        PageableDto returned = dto.setPageNumber(3);
        assertSame(dto, returned);
        returned = dto.setPageSize(25);
        assertSame(dto, returned);
        assertEquals(3, dto.getPageNumber());
        assertEquals(25, dto.getPageSize());
    }

    @Test
    void testToString() {
        PageableDto dto = new PageableDto().setPageNumber(2).setPageSize(15);
        String str = dto.toString();
        assertTrue(str.contains("2"));
        assertTrue(str.contains("15"));
    }
}
