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
import androidx.constraintlayout.coremp.motion.utils.ArcCurveFit
import androidx.constraintlayout.coremp.motion.utils.KeyCache
import androidx.constraintlayout.coremp.motion.utils.TypedValues
import androidx.constraintlayout.coremp.test.assertEquals
import kotlinx.coroutines.Runnable
import kotlin.test.Test
import kotlin.test.assertEquals

class MotionKeyAttributesTest {
    @Test
    fun basic() {
        assertEquals(2, 1 + 1)
    }

    @Test
    fun attributes() {
        val mw1 = MotionWidget()
        val mw2 = MotionWidget()
        val res = MotionWidget()
        val cache = KeyCache()
        mw1.setBounds(0, 0, 30, 40)
        mw2.setBounds(400, 400, 430, 440)
        mw1.setRotationZ(0f)
        mw2.setRotationZ(360f)
        // mw1.motion.mPathMotionArc = MotionWidget.A
        val motion = Motion(mw1)
        motion.setPathMotionArc(ArcCurveFit.ARC_START_VERTICAL)
        motion.setStart(mw1)
        motion.setEnd(mw2)
        motion.setup(1000, 1000, 1f, 1000000)
        if (DEBUG) {
            for (p in 0..10) {
                motion.interpolate(res, p * 0.1f, (1000000 + (p * 100)).toLong(), cache)
                println((p * 0.1f).toString() + " " + res.getRotationZ())
            }
        }
        motion.interpolate(res, 0.5f, (1000000 + 1000).toLong(), cache)
        assertEquals(180, res.getRotationZ(), 0.001f)
    }

    inner class Scene {
        var mMW1 = MotionWidget()
        var mMW2 = MotionWidget()
        var mRes = MotionWidget()
        var mCache = KeyCache()
        var mMotion: Motion

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

        fun sample(r: Runnable) {
            for (p in 0..10) {
                mMotion.interpolate(mRes, p * 0.1f, (1000000 + (p * 100)).toLong(), mCache)
                r.run()
            }
        }
    }

    private fun basicRange(type: Int, start: Float, end: Float): Scene {
        val s = Scene()
        s.mMW1.setValue(type, start)
        s.mMW2.setValue(type, end)
        s.setup()
        if (DEBUG) {
            s.sample(
                Runnable {
                    println(s.mRes.getRotationZ())
                },
            )
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        return s
    }

    @Test
    fun checkRotationZ() {
        val s = basicRange(TypedValues.AttributesType.TYPE_ROTATION_Z, 0f, 360f)
        assertEquals(180, s.mRes.getRotationZ(), 0.001f)
    }

    @Test
    fun checkRotationX() {
        val s = basicRange(TypedValues.AttributesType.TYPE_ROTATION_X, 0f, 100f)
        assertEquals(50, s.mRes.getRotationX(), 0.001f)
    }

    @Test
    fun checkRotationY() {
        val s = basicRange(TypedValues.AttributesType.TYPE_ROTATION_Y, 0f, 50f)
        assertEquals(25, s.mRes.getRotationY(), 0.001f)
    }

    @Test
    fun checkTranslateX() {
        val s = basicRange(TypedValues.AttributesType.TYPE_TRANSLATION_X, 0f, 30f)
        assertEquals(15, s.mRes.getTranslationX(), 0.001f)
    }

    @Test
    fun checkTranslateY() {
        val s = basicRange(TypedValues.AttributesType.TYPE_TRANSLATION_Y, 0f, 40f)
        assertEquals(20, s.mRes.getTranslationY(), 0.001f)
    }

    @Test
    fun checkTranslateZ() {
        val s = basicRange(TypedValues.AttributesType.TYPE_TRANSLATION_Z, 0f, 18f)
        assertEquals(9, s.mRes.getTranslationZ(), 0.001f)
    }

    @Test
    fun checkScaleX() {
        val s = basicRange(TypedValues.AttributesType.TYPE_SCALE_X, 1f, 19f)
        assertEquals(10, s.mRes.getScaleX(), 0.001f)
    }

    @Test
    fun checkScaleY() {
        val s = basicRange(TypedValues.AttributesType.TYPE_SCALE_Y, 1f, 3f)
        assertEquals(2, s.mRes.getScaleY(), 0.001f)
    }

    @Test
    fun attributesRotateX() {
        val s = Scene()
        s.mMW1.setRotationX(-10f)
        s.mMW2.setRotationX(10f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(0, s.mRes.getRotationX(), 0.001f)
    }

    @Test
    fun attributesRotateY() {
        val s = Scene()
        s.mMW1.setRotationY(-10f)
        s.mMW2.setRotationY(10f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(0, s.mRes.getRotationY(), 0.001f)
    }

    @Test
    fun attributesRotateZ() {
        val s = Scene()
        s.mMW1.setRotationZ(-10f)
        s.mMW2.setRotationZ(10f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(0, s.mRes.getRotationZ(), 0.001f)
    }

    @Test
    fun attributesTranslateX() {
        val s = Scene()
        s.mMW1.setTranslationX(-10f)
        s.mMW2.setTranslationX(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getTranslationX(), 0.001f)
    }

    @Test
    fun attributesTranslateY() {
        val s = Scene()
        s.mMW1.setTranslationY(-10f)
        s.mMW2.setTranslationY(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getTranslationY(), 0.001f)
    }

    @Test
    fun attributesTranslateZ() {
        val s = Scene()
        s.mMW1.setTranslationZ(-10f)
        s.mMW2.setTranslationZ(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getTranslationZ(), 0.001f)
    }

    @Test
    fun attributesScaleX() {
        val s = Scene()
        s.mMW1.setScaleX(-10f)
        s.mMW2.setScaleX(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getScaleX(), 0.001f)
    }

    @Test
    fun attributesScaleY() {
        val s = Scene()
        s.mMW1.setScaleY(-10f)
        s.mMW2.setScaleY(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getScaleY(), 0.001f)
    }

    @Test
    fun attributesPivotX() {
        val s = Scene()
        s.mMW1.setPivotX(-10f)
        s.mMW2.setPivotX(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getPivotX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getPivotX(), 0.001f)
    }

    @Test
    fun attributesPivotY() {
        val s = Scene()
        s.mMW1.setPivotY(-10f)
        s.mMW2.setPivotY(40f)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getPivotY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(15, s.mRes.getPivotY(), 0.001f)
    }

    @Test
    fun keyFrameRotateX() {
        val s = Scene()
        s.mMW1.setRotationX(-10f)
        s.mMW2.setRotationX(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationX(), 0.001f)
    }

    @Test
    fun keyFrameRotateY() {
        val s = Scene()
        s.mMW1.setRotationY(-10f)
        s.mMW2.setRotationY(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationY(), 0.001f)
    }

    @Test
    fun keyFrameRotateZ() {
        val s = Scene()
        s.mMW1.setRotationZ(-10f)
        s.mMW2.setRotationZ(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_Z, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationZ(), 0.001f)
    }

    @Test
    fun keyFrameTranslationX() {
        val s = Scene()
        s.mMW1.setTranslationX(-10f)
        s.mMW2.setTranslationX(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationX(), 0.001f)
    }

    @Test
    fun keyFrameTranslationY() {
        val s = Scene()
        s.mMW1.setTranslationY(-10f)
        s.mMW2.setTranslationY(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationY(), 0.001f)
    }

    @Test
    fun keyFrameTranslationZ() {
        val s = Scene()
        s.mMW1.setTranslationZ(-10f)
        s.mMW2.setTranslationZ(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_Z, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationZ(), 0.001f)
    }

    @Test
    fun keyFrameScaleX() {
        val s = Scene()
        s.mMW1.setScaleX(-10f)
        s.mMW2.setScaleX(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_SCALE_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getScaleX(), 0.001f)
    }

    @Test
    fun keyFrameScaleY() {
        val s = Scene()
        s.mMW1.setScaleY(-10f)
        s.mMW2.setScaleY(10f)
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_SCALE_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getScaleY(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrRotateX() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationX(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrRotateY() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationY(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrRotateZ() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_ROTATION_Z, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getRotationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getRotationZ(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrTranslationX() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationX(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrTranslationY() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationY(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrTranslationZ() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_TRANSLATION_Z, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getTranslationZ()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getTranslationZ(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrScaleX() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_SCALE_X, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleX()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getScaleX(), 0.001f)
    }

    @Test
    fun keyFrameNoAttrScaleY() {
        val s = Scene()
        val attribute = MotionKeyAttributes()
        attribute.setValue(TypedValues.AttributesType.TYPE_SCALE_Y, 23f)
        attribute.setFramePosition(50)
        s.mMotion.addKey(attribute)
        s.setup()
        if (DEBUG) {
            s.sample(Runnable { println(s.mRes.getScaleY()) })
        }
        s.mMotion.interpolate(s.mRes, 0.5f, (1000000 + 1000).toLong(), s.mCache)
        assertEquals(23, s.mRes.getScaleY(), 0.001f)
    }

    companion object {
        private const val DEBUG = false
    }
}
