package tachiyomi.core.common.util.system

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.blue
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.hippo.unifile.UniFile
import logcat.LogPriority
import okio.Buffer
import okio.BufferedSource
import java.io.InputStream
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ImageUtil {
    // ... other code ...

    fun mergeBitmaps(
        iBitmap: Bitmap,
        iBitmap2: Bitmap,
        isLTR: Boolean,
        @ColorInt background: Int = Color.WHITE,
        hingeGap: Int = 0,
        context: Context? = null,
    ): BufferedSource {
        var imageBitmap = iBitmap
        var imageBitmap2 = iBitmap2
        var height = imageBitmap.height
        var width = imageBitmap.width
        var height2 = imageBitmap2.height
        var width2 = imageBitmap2.width
        val maxHeight = max(height, height2)
        val maxWidth = max(width, width2)
        val adjustedHingeGap = context?.let {
            val resources = it.resources
            (maxHeight.toFloat() / resources.displayMetrics.heightPixels * hingeGap).toInt()
        } ?: hingeGap

        val result = createBitmap((maxWidth * 2) + adjustedHingeGap, maxHeight)
        result.applyCanvas {
            drawColor(background)
            val widthAndHinge = maxWidth + adjustedHingeGap
            if (imageBitmap.height != maxHeight && imageBitmap.width != maxWidth) {
                val minRatio = min(maxHeight / height.toFloat(), maxWidth / width.toFloat())
                imageBitmap =
                    Bitmap.createScaledBitmap(
                        imageBitmap,
                        (width * minRatio).toInt(),
                        (height * minRatio).toInt(),
                        true,
                    )
            }
            height = imageBitmap.height
            width = imageBitmap.width
            val upperPart = Rect(
                if (isLTR) max(maxWidth - width, 0) else widthAndHinge,
                (maxHeight - height) / 2,
                (if (isLTR) max(maxWidth - width, 0) else widthAndHinge) + width,
                height + (maxHeight - height) / 2,
            )
            drawBitmap(imageBitmap, null, upperPart, null)

            if (imageBitmap2.height != maxHeight && imageBitmap2.width != maxWidth) {
                val minRatio = min(maxHeight / height2.toFloat(), maxWidth / width2.toFloat())
                imageBitmap2 =
                    Bitmap.createScaledBitmap(
                        imageBitmap2,
                        (width2 * minRatio).toInt(),
                        (height2 * minRatio).toInt(),
                        true,
                    )
            }
            height2 = imageBitmap2.height
            width2 = imageBitmap2.width
            val bottomPart = Rect(
                if (!isLTR) max(maxWidth - width2, 0) else widthAndHinge,
                (maxHeight - height2) / 2,
                (if (!isLTR) max(maxWidth - width2, 0) else widthAndHinge) + width2,
                height2 + (maxHeight - height2) / 2,
            )
            drawBitmap(imageBitmap2, null, bottomPart, null)
        }

        val output = Buffer()
        result.compress(Bitmap.CompressFormat.JPEG, 100, output.outputStream())
        return output
    }

    val displayMaxHeightInPx: Int
        get() = Resources.getSystem().displayMetrics.let { max(it.heightPixels, it.widthPixels) }
}
