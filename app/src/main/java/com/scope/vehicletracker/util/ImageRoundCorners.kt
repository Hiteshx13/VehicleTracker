package com.scope.vehicletracker.util

import android.graphics.*
import com.squareup.picasso.Transformation


class ImageRoundCorners(val mWidth:Int,val mHeight:Int): Transformation {
    override fun transform(source: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(
            mWidth,mHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, mWidth, mHeight)
        val rectF = RectF(rect)
        val roundPx = 50f
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, rect, rect, paint);
        source.recycle();
        return output;
    }

    override fun key(): String? {
        return "RoundImage"
    }
}