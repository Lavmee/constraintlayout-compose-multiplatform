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
package androidx.constraintlayout.coremp.motion

import androidx.constraintlayout.coremp.motion.key.MotionKeyAttributes
import androidx.constraintlayout.coremp.motion.parse.KeyParser
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionParsingTest {
    internal inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion
        var mPos = 0f

        init {
            mMotion = Motion(mMW1)
            mMW1.setBounds(0, 0, 30, 40)
            mMW2.setBounds(400, 400, 430, 440)
            mMotion.setPathMotionArc(ArcCurveFit.ARC_START_VERTICAL)
        }

        fun setup() {
            mMotion.setStart(mMW1)
            mMotion.setEnd(mMW2)
            mMotion.setup(1000, 1000, 1f, 1000000)
        }
    }

    var mStr = """
         {frame:22,
         target:'widget1',
         easing:'easeIn',
         curveFit:'spline',
         progress:0.3,
         alpha:0.2,
         elevation:0.7,
         rotationZ:23,
         rotationX:25.0,
         rotationY:27.0,
         pivotX:15,
         pivotY:17,
         pivotTarget:'32',
         pathRotate:23,
         scaleX:0.5,
         scaleY:0.7,
         translationX:5,
         translationY:7,
         translationZ:11,
         }
    """.trimIndent()

    @Test
    fun parseKeAttributes() {
        val mka = MotionKeyAttributes()
        KeyParser.parseAttributes(mStr).applyDelta(mka)
        assertEquals(22, mka.mFramePosition)
        val attrs: HashSet<String> = HashSet()
        mka.getAttributeNames(attrs)
        val split =
            mStr.replace("\n", "").split("[,:\\{\\}]".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val expectlist: ArrayList<String> = ArrayList<String>()
        val exclude: HashSet<String> = HashSet<String>(
            mutableListOf<String>(
                "curveFit",
                "easing",
                "frame",
                "target",
                "pivotTarget",
            ),
        )
        var i = 1
        var j = 0
        while (i < split.size) {
            println(i.toString() + " " + split[i])
            if (!exclude.contains(split[i])) {
                expectlist.add(split[i])
            }
            i += 2
            j++
        }
        val expect: Array<String> = expectlist.toTypedArray<String>()
        val result: Array<String> = attrs.toTypedArray()
        expect.sort()
        result.sort()
        assertEquals(expect.contentToString(), result.contentToString())
    }

    companion object {
        private const val DEBUG = false
    }
}
