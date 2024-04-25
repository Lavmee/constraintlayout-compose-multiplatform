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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CLParserTest {
    private fun testBasicFormat(content: String) {
        try {
            val parsedContent = CLParser.parse(content)
            assertEquals(parsedContent.toJSON(), content)
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testParsing() {
        testBasicFormat(
            "{ a: { start: ['parent', 'start', 20], " +
                "top: ['parent', 'top', 30] } }",
        )
        testBasicFormat("{ test: 'hello, the', key: 'world' }")
        testBasicFormat("{ test: [1, 2, 3] }")
        testBasicFormat("{ test: ['hello', 'world', { value: 42 }] }")
        testBasicFormat("{ test: [null] }")
        testBasicFormat("{ test: [null, false, true] }")
        testBasicFormat(
            "{ test: ['hello', 'world', { value: 42 }], value: false, " +
                "plop: 23, hello: { key: 42, text: 'bonjour' } }",
        )
    }

    @Test
    fun testValue() {
        try {
            val test = (
                "{ test: ['hello', 'world', { value: 42 }], value: false, plop: 23, " +
                    "hello: { key: 49, text: 'bonjour' } }"
                )
            val parsedContent = CLParser.parse(test)
            assertTrue(parsedContent.toJSON() == test)
            assertEquals("hello", parsedContent.getArray("test").getString(0))
            assertEquals("world", parsedContent.getArray("test").getString(1))
            assertEquals(42, parsedContent.getArray("test").getObject(2)["value"].getInt())
            assertEquals(false, parsedContent.getBoolean("value"))
            assertEquals(23, parsedContent.getInt("plop"))
            assertEquals(49, parsedContent.getObject("hello").getInt("key"))
            assertEquals("bonjour", parsedContent.getObject("hello").getString("text"))
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testException() {
        try {
            val test = (
                "{ test: ['hello', 'world', { value: 42 }], value: false, " +
                    "plop: 23, hello: { key: 49, text: 'bonjour' } }"
                )
            val parsedContent = CLParser.parse(test)
            parsedContent.getObject("test").getString(0)
        } catch (e: CLParsingException) {
            assertEquals(
                "no object found for key <test>, found [CLArray] : " +
                    "CLArray (9 : 39) <<'hello', 'world', { value: 42 }>> = <CLString (10 : 14) " +
                    "<<hello>>; CLString (19 : 23) <<world>>; CLObject (28 : 39) " +
                    "<< value: 42 }>> = <CLKey (29 : 33) <<value>> = <CLNumber (36 : 37) " +
                    "<<42>> > > > (CLObject at line 1)",
                e.reason(),
            )
            e.printStackTrace()
        }
    }

    @Test
    fun testTrailingCommas() {
        try {
            val test = "{ test: ['hello', 'world'],,,,,,, }"
            val parsedContent = CLParser.parse(test)
            assertEquals("hello", parsedContent.getArray("test").getString(0))
            assertEquals("world", parsedContent.getArray("test").getString(1))
            assertEquals("{ test: ['hello', 'world'] }", parsedContent.toJSON())
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testIncompleteObject() {
        try {
            val test = "{ test: ['hello', 'world"
            val parsedContent = CLParser.parse(test)
            assertEquals("hello", parsedContent.getArray("test").getString(0))
            assertEquals("world", parsedContent.getArray("test").getString(1))
            assertEquals("{ test: ['hello', 'world'] }", parsedContent.toJSON())
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testDoubleQuotes() {
        try {
            val test = "{ test: [\"hello\", \"world\"] }"
            val parsedContent = CLParser.parse(test)
            assertEquals("hello", parsedContent.getArray("test").getString(0))
            assertEquals("world", parsedContent.getArray("test").getString(1))
            assertEquals("{ test: ['hello', 'world'] }", parsedContent.toJSON())
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testDoubleQuotesKey() {
        try {
            val test = "{ \"test\": [\"hello\", \"world\"] }"
            val parsedContent = CLParser.parse(test)
            assertEquals("{ test: ['hello', 'world'] }", parsedContent.toJSON())
            assertEquals("hello", parsedContent.getArray("test").getString(0))
            assertEquals("world", parsedContent.getArray("test").getString(1))
            assertEquals("{ test: ['hello', 'world'] }", parsedContent.toJSON())
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testMultilines() {
        val test = """{
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
}          """
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals("John", parsedContent.getString("firstName"))
            assertEquals(
                "{ firstName: 'John', lastName: 'Smith', isAlive: true, " +
                    "age: 27, address: { streetAddress: '21 2nd Street', city: 'New " +
                    "York', " +
                    "state: 'NY', postalCode: '10021-3100' }, " +
                    "phoneNumbers: [{ type: 'home', number: '212 555-1234' }, " +
                    "{ type: 'office', number: '646 555-4567' }], " +
                    "children: [], spouse: null }",
                parsedContent.toJSON(),
            )
            assertEquals(2, parsedContent.getArray("phoneNumbers").size())
            val element = parsedContent["spouse"]
            if (element is CLToken) {
                assertEquals(CLToken.Type.NULL, element.mType)
            }
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testDoubleQuotesMultilines() {
        val test = """{
  "firstName": "John",
  "lastName": "Smith",
  "isAlive": true,
  "age": 27,
  "address": {
    "streetAddress": "21 2nd Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10021-3100"
  },
  "phoneNumbers": [
    {
      "type": "home",
      "number": "212 555-1234"
    },
    {
      "type": "office",
      "number": "646 555-4567"
    }
  ],
  "children": [],
  "spouse": null
}          """
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals("John", parsedContent.getString("firstName"))
            assertEquals(
                "{ firstName: 'John', lastName: 'Smith', isAlive: true, " +
                    "age: 27, address: { streetAddress: '21 2nd Street', city: 'New York', " +
                    "state: 'NY', postalCode: '10021-3100' }, " +
                    "phoneNumbers: [{ type: 'home', number: '212 555-1234' }, " +
                    "{ type: 'office', number: '646 555-4567' }], " +
                    "children: [], spouse: null }",
                parsedContent.toJSON(),
            )
            assertEquals(2, parsedContent.getArray("phoneNumbers").size())
            val element = parsedContent["spouse"]
            if (element is CLToken) {
                assertEquals(CLToken.Type.NULL, element.mType)
            }
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testJSON5() {
        val test = """{
  // comments
  unquoted: 'and you can quote me on that',
  singleQuotes: 'I can use "double quotes" here',
  leadingDecimalPoint: .8675309, andTrailing: 8675309.,
  positiveSign: +1,
  trailingComma: 'in objects', andIn: ['arrays',],
  "backwardsCompatible": "with JSON",
}"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                "{ unquoted: 'and you can quote me on that', " +
                    "singleQuotes: 'I can use \"double quotes\" here', " +
                    "leadingDecimalPoint: 0.8675309, andTrailing: 8675309, " +
                    "positiveSign: 1, trailingComma: 'in objects', " +
                    "andIn: ['arrays'], backwardsCompatible: 'with JSON' }",
                parsedContent.toJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testConstraints() {
        val test = """{
  g1 : { type: 'vGuideline', start: 44 },
  g2 : { type: 'vGuideline', end: 44 },
  image: {
    width: 201, height: 179,
    top: ['parent','top', 32],
    start: 'g1'
  },
"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                "{ g1: { type: 'vGuideline', start: 44 }, " +
                    "g2: { type: 'vGuideline', end: 44 }, " +
                    "image: { width: 201, height: 179, top: ['parent', 'top', 32], " +
                    "start: 'g1' } }",
                parsedContent.toJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testConstraints2() {
        val test = """            {
              Variables: {
                bottom: 20
              },
              Helpers: [
                ['hChain', ['a','b','c'], {
                  start: ['leftGuideline1', 'start'],
                  style: 'packed'
                }],
                ['hChain', ['d','e','f']],
                ['vChain', ['d','e','f'], {
                  bottom: ['topGuideline1', 'top']
                }],
                ['vGuideline', {
                  id: 'leftGuideline1', start: 100
                }],
                ['hGuideline', {
                  id: 'topGuideline1', percent: 0.5
                }]
              ],
              a: {
                bottom: ['b', 'top', 'bottom']
              },
              b: {
                width: '30%',
                height: '1:1',
                centerVertically: 'parent'
              },
              c: {
                top: ['b', 'bottom']
              }
            }"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                "{ " +
                    "Variables: { bottom: 20 }, " +
                    "Helpers: [" +
                    "['hChain', ['a', 'b', 'c'], { start: ['leftGuideline1', 'start'], " +
                    "style: 'packed' }], " +
                    "['hChain', ['d', 'e', 'f']], " +
                    "['vChain', ['d', 'e', 'f'], { bottom: ['topGuideline1', 'top'] }], " +
                    "['vGuideline', { id: 'leftGuideline1', start: 100 }], " +
                    "['hGuideline', { id: 'topGuideline1', percent: 0.5 }]" +
                    "], " +
                    "a: { bottom: ['b', 'top', 'bottom'] }, " +
                    "b: { width: '30%', height: '1:1', centerVertically: 'parent' }, " +
                    "c: { top: ['b', 'bottom'] } }",
                parsedContent.toJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testConstraints3() {
        val test = """{
                ConstraintSets: {
                  start: {
                    a: {
                      width: 40,
                      height: 40,
                      start: ['parent', 'start', 16],
                      bottom: ['parent', 'bottom', 16]
                    }
                  },
                  end: {
                    a: {
                      width: 40,
                      height: 40,
                      //rotationZ: 390,
                      end: ['parent', 'end', 16],
                      top: ['parent', 'top', 16]
                    }
                  }
                },
                Transitions: {
                  default: {
                    from: 'start',
                    to: 'end',
                    pathMotionArc: 'startHorizontal',
                    KeyFrames: {
//                      KeyPositions: [
//                        {
//                          target: ['a'],
//                          frames: [25, 50, 75],
////                          percentX: [0.4, 0.8, 0.1],
////                          percentY: [0.4, 0.8, 0.3]
//                        }
//                      ],
                      KeyAttributes: [
                        {
                          target: ['a'],
                          frames: [25, 50],
                          scaleX: 3,
                          scaleY: .3
                        }
                      ]
                    }
                  }
                }
            }"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                "{ ConstraintSets: { start: { a: { width: 40, height: 40, " +
                    "start: ['parent', 'start', 16], bottom: ['parent', 'bottom', 16] }" +
                    " }, " +
                    "end: { a: { width: 40, height: 40, end: ['parent', 'end', 16]," +
                    " top: ['parent', 'top', 16] } } }, " +
                    "Transitions: { default: { from: 'start', to: 'end', " +
                    "pathMotionArc: 'startHorizontal', " +
                    "KeyFrames: { KeyAttributes: [{ target: ['a'], frames: [25, 50], " +
                    "scaleX: 3, scaleY: 0.3 }] } } } }",
                parsedContent.toJSON(),
            )
            val transitions = parsedContent.getObject("Transitions")
            val transition = transitions.getObject("default")
            val keyframes = transition.getObjectOrNull("KeyFrames")
            val keyattributes = keyframes!!.getArrayOrNull("KeyAttributes")
            assertNotNull(keyattributes)
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
            assertTrue(false)
        }
    }

    @Test
    fun testFormatting() {
        val test = (
            "{ firstName: 'John', lastName: 'Smith', isAlive: true, " +
                "age: 27, address: { streetAddress: '21 2nd Street', city: 'New York', " +
                "state: 'NY', postalCode: '10021-3100' }, " +
                "phoneNumbers: [{ type: 'home', number: '212 555-1234' }, " +
                "{ type: 'office', number: '646 555-4567' }], " +
                "children: [], spouse: null }"
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
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
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting2() {
        val test = (
            "{ ConstraintSets: { start: { a: { width: 40, height: 40, " +
                "start: ['parent', 'start', 16], bottom: ['parent', 'bottom', 16] } }, end: " +
                "{ a: { width: 40, height: 40, end: ['parent', 'end', 16]," +
                " top: ['parent', 'top', 16]" +
                " } } }, Transitions: { default: { from: 'start', to: 'end', " +
                "pathMotionArc: 'startHorizontal', KeyFrames: { KeyAttributes: [{ target: ['a'], " +
                "frames: [25, 50], scaleX: 3, scaleY: 0.3 }] } } } }"
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  ConstraintSets: {
    start: {
      a: {
        width: 40,
        height: 40,
        start: ['parent', 'start', 16],
        bottom: ['parent', 'bottom', 16]
      }
    },
    end: {
      a: {
        width: 40,
        height: 40,
        end: ['parent', 'end', 16],
        top: ['parent', 'top', 16]
      }
    }
  },
  Transitions: {
    default: {
      from: 'start',
      to: 'end',
      pathMotionArc: 'startHorizontal',
      KeyFrames: {
        KeyAttributes: [
          {
            target: ['a'],
            frames: [25, 50],
            scaleX: 3,
            scaleY: 0.3
          }
        ]
      }
    }
  }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting3() {
        val test = """{ ConstraintSets: {
      Generate: { texts: { top: ['parent', 'top', 'margin'], start: ['parent', 'end', 16] } } } }
"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  ConstraintSets: {
    Generate: {
      texts: {
        top: ['parent', 'top', 'margin'],
        start: ['parent', 'end', 16]
      }
    }
  }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting4() {
        val test = (
            "{ Transitions: { default: { from: 'start', to: 'end', " +
                "pathMotionArc: 'startHorizontal', KeyFrames: { KeyAttributes: [{ target: ['a'], " +
                "frames: [25, 50], scaleX: 3, scaleY: 0.3 }] } } } }"
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  Transitions: {
    default: {
      from: 'start',
      to: 'end',
      pathMotionArc: 'startHorizontal',
      KeyFrames: {
        KeyAttributes: [
          {
            target: ['a'],
            frames: [25, 50],
            scaleX: 3,
            scaleY: 0.3
          }
        ]
      }
    }
  }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting5() {
        val test = """{ Debug: { name: 'motion6' }, ConstraintSets: {
    start: { Variables: { texts: { tag: 'text' }, margin: { from: 0, step: 50 }
      }, Generate: { texts: { top: ['parent', 'top', 'margin'], start: ['parent', 'end', 16] }
      }, box: { width: 'spread', height: 64, centerHorizontally: 'parent',
        bottom: ['parent', 'bottom'] }, content: { width: 'spread',
        height: '400', centerHorizontally: 'parent', top: ['box', 'bottom', 32]
      }, name: { centerVertically: 'box', start: ['parent', 'start', 16] }
    }, end: { Variables: { texts: { tag: 'text' },
        margin: { from: 0, step: 50 } }, Generate: {
        texts: { start: ['parent', 'start', 32], top: ['content', 'top', 'margin'] }
      }, box: { width: 'spread', height: 200, centerHorizontally: 'parent',
        top: ['parent', 'top'] }, content: {
        width: 'spread', height: 'spread', centerHorizontally: 'parent',
        top: ['box', 'bottom'], bottom: ['parent', 'bottom']
      }, name: { rotationZ: 90, scaleX: 2, scaleY: 2,
        end: ['parent', 'end', 16], top: ['parent', 'top', 90]
      } } }, Transitions: { default: { from: 'start', to: 'end',
      pathMotionArc: 'startHorizontal', KeyFrames: {
        KeyAttributes: [ { target: ['box', 'content'],
            frames: [50], rotationZ: [25], rotationY: [25]
          } ] } } } }"""
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  Debug: { name: 'motion6' },
  ConstraintSets: {
    start: {
      Variables: {
        texts: {
          tag: 'text'
        },
        margin: {
          from: 0,
          step: 50
        }
      },
      Generate: {
        texts: {
          top: ['parent', 'top', 'margin'],
          start: ['parent', 'end', 16]
        }
      },
      box: {
        width: 'spread',
        height: 64,
        centerHorizontally: 'parent',
        bottom: ['parent', 'bottom']
      },
      content: {
        width: 'spread',
        height: '400',
        centerHorizontally: 'parent',
        top: ['box', 'bottom', 32]
      },
      name: { centerVertically: 'box', start: ['parent', 'start', 16] }
    },
    end: {
      Variables: {
        texts: {
          tag: 'text'
        },
        margin: {
          from: 0,
          step: 50
        }
      },
      Generate: {
        texts: {
          start: ['parent', 'start', 32],
          top: ['content', 'top', 'margin']
        }
      },
      box: {
        width: 'spread',
        height: 200,
        centerHorizontally: 'parent',
        top: ['parent', 'top']
      },
      content: {
        width: 'spread',
        height: 'spread',
        centerHorizontally: 'parent',
        top: ['box', 'bottom'],
        bottom: ['parent', 'bottom']
      },
      name: {
        rotationZ: 90,
        scaleX: 2,
        scaleY: 2,
        end: ['parent', 'end', 16],
        top: ['parent', 'top', 90]
      }
    }
  },
  Transitions: {
    default: {
      from: 'start',
      to: 'end',
      pathMotionArc: 'startHorizontal',
      KeyFrames: {
        KeyAttributes: [
          {
            target: ['box', 'content'],
            frames: [50],
            rotationZ: [25],
            rotationY: [25]
          }
        ]
      }
    }
  }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting6() {
        val test = (
            "{ root: {interpolated: {left: 0, top: 0, right: 800, bottom: 772}}, " +
                "button: {interpolated: {left: 0, top: 372, right: 800, bottom: 401}}, " +
                "text1: {interpolated: {left: 100, top: 285, right: 208, bottom: 301}}, " +
                "text2: {interpolated: {left: 723, top: 736, right: 780, bottom: 752}}, " +
                "g1: {type: 'vGuideline',interpolated: {left: 100, top: 0," +
                " right: 100, bottom: 772}}, }"
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  root: { interpolated: { left: 0, top: 0, right: 800, bottom: 772 } },
  button: { interpolated: { left: 0, top: 372, right: 800, bottom: 401 } },
  text1: { interpolated: { left: 100, top: 285, right: 208, bottom: 301 } },
  text2: { interpolated: { left: 723, top: 736, right: 780, bottom: 752 } },
  g1: {
    type: 'vGuideline',
    interpolated: { left: 100, top: 0, right: 100, bottom: 772 }
  }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting7() {
        val test = (
            "{ root: {left: 0, top: 0, right: 800, bottom: 772}, " +
                "button: {left: 0, top: 372, right: 800, bottom: 401}, "
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  root: { left: 0, top: 0, right: 800, bottom: 772 },
  button: { left: 0, top: 372, right: 800, bottom: 401 }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }

    @Test
    fun testFormatting8() {
        val test = (
            "{ root: { bottom: 772}, " +
                "button: { bottom: 401 }, "
            )
        try {
            val parsedContent = CLParser.parse(test)
            assertEquals(
                """{
  root: { bottom: 772 },
  button: { bottom: 401 }
}""",
                parsedContent.toFormattedJSON(),
            )
        } catch (e: CLParsingException) {
            println("Exception " + e.reason())
            e.printStackTrace()
        }
    }
}
