package androidx.constraintlayout.coremp.ext

/**
 * Copyright 2023, @lavmee and the project contributors
 **/

class Rectangle(
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
) {
    constructor(rectangle: Rectangle) : this(rectangle.x, rectangle.y, rectangle.width, rectangle.height)

    fun union(rectangle: Rectangle): Rectangle {
        var tx2 = width.toLong()
        var ty2 = height.toLong()
        if (tx2 or ty2 < 0) {
            return Rectangle(rectangle)
        }
        var rx2: Long = rectangle.width.toLong()
        var ry2: Long = rectangle.height.toLong()
        if (rx2 or ry2 < 0) {
            return Rectangle(rectangle = this)
        }
        var tx1 = x
        var ty1 = y
        tx2 += tx1.toLong()
        ty2 += ty1.toLong()
        val rx1: Int = rectangle.x
        val ry1: Int = rectangle.y
        rx2 += rx1.toLong()
        ry2 += ry1.toLong()
        if (tx1 > rx1) tx1 = rx1
        if (ty1 > ry1) ty1 = ry1
        if (tx2 < rx2) tx2 = rx2
        if (ty2 < ry2) ty2 = ry2
        tx2 -= tx1.toLong()
        ty2 -= ty1.toLong()
        if (tx2 > Int.MAX_VALUE) tx2 = Int.MAX_VALUE.toLong()
        if (ty2 > Int.MAX_VALUE) ty2 = Int.MAX_VALUE.toLong()
        return Rectangle(tx1, ty1, tx2.toInt(), ty2.toInt())
    }

    fun intersects(rectangle: Rectangle): Boolean {
        var tw = width
        var th = height
        var rw: Int = rectangle.width
        var rh: Int = rectangle.height
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false
        }
        val tx = x
        val ty = y
        val rx: Int = rectangle.x
        val ry: Int = rectangle.y
        rw += rx
        rh += ry
        tw += tx
        th += ty
        return (rw < rx || rw > tx) &&
            (rh < ry || rh > ty) &&
            (tw < tx || tw > rx) &&
            (th < ty || th > ry)
    }
}
