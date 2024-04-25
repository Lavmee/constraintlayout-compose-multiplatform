/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintlayout.coremp.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CLParserBenchmarkTest {
    private var mSimpleFromWiki2 = """{
  firstName: 'John',
  lastName: 'Smith',
  isAlive: true,
  age: 27,
  address: {
    streetAddress: '21 2nd Street',
    city: 'New York',
    state: 'NY',
    postalCode: '10021-3100'
  },
  phoneNumbers: [
    {
      type: 'home',
      number: '212 555-1234'
    },
    {
      type: 'office',
      number: '646 555-4567'
    }
  ],
  children: [],
  spouse: null
}"""

    @Test
    fun parseAndCheck1000x() {
        try {
            for (i in 0..999) {
                parseAndeCheck()
            }
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun parse1000x() {
        try {
            for (i in 0..999) {
                parseOnce()
            }
            parseAndeCheck()
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Throws(CLParsingException::class)
    private fun parseOnce() {
        val test = mSimpleFromWiki2
        val parsedContent = CLParser.parse(test)
        var o: CLObject
        assertEquals("John", parsedContent.getString("firstName"))
    }

    @Throws(CLParsingException::class)
    private fun parseAndeCheck() {
        val test = mSimpleFromWiki2
        val parsedContent = CLParser.parse(test)
        var o: CLObject
        assertEquals("John", parsedContent.getString("firstName"))
        assertEquals("Smith", parsedContent.getString("lastName"))
        assertEquals(true, parsedContent.getBoolean("isAlive"))
        assertEquals(27, parsedContent.getInt("age"))
        assertEquals(
            "{ streetAddress: '21 2nd Street', city: 'New York'" +
                ", state: 'NY', postalCode: '10021-3100' }",
            parsedContent.getObject("address").also { o = it }.toJSON(),
        )
        assertEquals("21 2nd Street", o.getString("streetAddress"))
        assertEquals("New York", o.getString("city"))
        assertEquals("NY", o.getString("state"))
        assertEquals("NY", o.getString("state"))
        assertEquals("NY", o.getString("state"))
        assertEquals("NY", o.getString("state"))
        assertEquals("10021-3100", o.getString("postalCode"))
        assertEquals(
            "{ type: 'home', number: '212 555-1234' }",
            parsedContent.getArray("phoneNumbers").getObject(0).also { o = it }.toJSON(),
        )
        assertEquals("home", o.getString("type"))
        assertEquals("212 555-1234", o.getString("number"))
        assertEquals(
            "{ type: 'office', number: '646 555-4567' }",
            parsedContent.getArray("phoneNumbers").getObject(1).also { o = it }.toJSON(),
        )
        assertEquals("office", o.getString("type"))
        assertEquals("646 555-4567", o.getString("number"))
        assertEquals(0, parsedContent.getArray("children").mElements.size)
        val element = parsedContent["spouse"]
        if (element is CLToken) {
            assertEquals(CLToken.Type.NULL, element.mType)
        }
    }
}
