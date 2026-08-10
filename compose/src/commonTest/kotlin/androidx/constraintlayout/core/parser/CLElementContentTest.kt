// Copyright 2023, Sergei Gagarin and the project contributors
// SPDX-License-Identifier: Apache-2.0

package androidx.constraintlayout.core.parser

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `content()` and `toString()` return a slice of the element's buffer. The upstream Java caps that
 * slice at 1000 characters — a deliberate guard against out-of-memory on very large literals — and
 * returns an empty string for an element that never started. Both were lost in translation, so
 * these cases are pinned down here rather than left to the ported suite, which never exercises them.
 */
class CLElementContentTest {
    @Test
    fun contentReturnsEmptyWhenNotStarted() {
        val element = CLElement("hello".toCharArray())
        assertEquals("", element.content())
    }

    @Test
    fun contentReturnsOnlyTheSlice() {
        val element = CLElement("hello world".toCharArray())
        element.start = 6
        element.end = 10
        assertEquals("world", element.content())
    }

    @Test
    fun contentIsCappedAtOneThousandCharacters() {
        val element = CLElement("x".repeat(2000).toCharArray())
        element.start = 0
        element.end = 1999
        assertEquals(1000, element.content().length)
    }

    @Test
    fun contentReturnsSingleCharacterWhenNotDone() {
        val element = CLElement("hello".toCharArray())
        element.start = 1
        assertEquals("e", element.content())
    }

    @Test
    fun toStringIsCappedAtOneThousandCharacters() {
        val element = CLElement("x".repeat(2000).toCharArray())
        element.start = 0
        element.end = 1999
        assertEquals("CLElement (0 : 1999) <<${"x".repeat(1000)}>>", element.toString())
    }
}
