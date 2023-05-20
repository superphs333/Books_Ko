package com.example.books_ko

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MyCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    var m_filename: String? = null // 셋팅할 이미지 파일 주소
    var send_filepath: String? = null // 보낼 파일 주소

    private var startX = -1
    private var startY = -1
    private var stopX = -1
    private var stopY = -1

    private var mode_eraser = false // true:지우개, false:펜
    private var bitmap: Bitmap? = null // 서버에서 온 이미지 -> 비트맵화
    private val mcanvas: Canvas = Canvas()
    private val mpaint: Paint = Paint()
    private var image_Bitmap: Bitmap? = null // 이미지 셋팅용 비트맵
    private val path = Path()

    init {
        this.setLayerType(LAYER_TYPE_SOFTWARE, null) // 뷰의 레이어 유형을 소프트웨어 레이어로 설정
        Log.i("정보태그", "Context")
        Log.d("정보태그", "Context context, @Nullable AttributeSet attrs")
    }

    /*
    뷰의 크기가 변경되었을 때 호출함( View가 처음으로 생성되거나 크기가 변경될 때마다 호출 )
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        mpaint.strokeWidth = 10f
        mpaint.isAntiAlias = true

        image_Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mcanvas.setBitmap(image_Bitmap)
        mcanvas.drawColor(Color.WHITE)

        val w = width
        val h = height

        if (m_filename?.contains(context.getString(R.string.img_memo)) == true) {
            Glide.with(context)
                .asBitmap()
                .load(m_filename)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bitmap = resource
                        Resize_Bitmap(bitmap!!, w, h)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } else {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(m_filename, options)

            bitmap = BitmapFactory.decodeFile(m_filename)

            // 이미지 회전 각도 구하기
            val exifOrientation = getOrientationOfImage(m_filename!!)

            // Exif 메타데이터 방향을 실제 각도로 변환
            val rotate = exifToDegrees(exifOrientation)

            // 이미지를 올바르게 회전
            bitmap = rotateImage(bitmap!!, rotate)

            var mutableBitmap = bitmap
            Resize_Bitmap(mutableBitmap!!, w, h)
        }
    }

    private fun getOrientationOfImage(filePath: String): Int {
        val exif = ExifInterface(filePath)
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun exifToDegrees(exifOrientation: Int): Float {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }


    private fun Resize_Bitmap(bitmap : Bitmap, w : Int, h: Int){
        var mutableBitmap = bitmap
        var bitmap_width: Float = bitmap.width.toFloat()
        var bitmap_height: Float = bitmap.height.toFloat()
        if (bitmap_height > h) { // 이미지가 주어진 높이를 초과하는 경우에만 이미지를 비율에 맞게 축소
            val percente = bitmap_height / 100f
            val scale = h / percente
            bitmap_width *= scale / 100f
            bitmap_height *= scale / 100f
        }
        // 주어진 비트맵을 지정된 크기로 조정하여 새로운 비트맵을 생성
        mutableBitmap = Bitmap.createScaledBitmap(bitmap, bitmap_width.toInt(), bitmap_height.toInt(), true)

        // 비트맵 크기
        val left = ((w - bitmap.width) / 2).toFloat()
        val top = ((h - bitmap.height) / 2).toFloat()
        Log.d("정보태그", "left=$left")
        Log.d("정보태그", "top=$top")

        // 비트맵 그리기
        mcanvas.drawBitmap(mutableBitmap,left,top,mpaint)
        invalidate() // onDraw 호출
    }




    /*
    onTouchEvent(MotionEvent event)
    => 터치화면 이벤트 핸들러의 콜백 메서드(화면에 터치가 발생했을 때 호출되는 콜백 메서드)
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val X = event.x.toInt()
        val Y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = X
                startY = Y
                path.moveTo(X.toFloat(), Y.toFloat())
            }
            MotionEvent.ACTION_MOVE -> {
                if (startX != -1) {
                    mcanvas.drawLine(startX.toFloat(), startY.toFloat(), X.toFloat(), Y.toFloat(), mpaint)
                    invalidate()
                    startX = X
                    startY = Y
                }
            }
            MotionEvent.ACTION_UP -> {
                if (startX != -1) {
                    mcanvas.drawLine(startX.toFloat(), startY.toFloat(), X.toFloat(), Y.toFloat(), mpaint)
                }
                invalidate()
                startX = -1
                startY = -1
            }
        }
        return true
    }

    // [개선] 지우개 기능

    // 페인트 초기화
    fun eraser() {
        image_Bitmap?.eraseColor(Color.WHITE)
        invalidate()

        val w = width
        val h = height

        if (m_filename?.contains(context.getString(R.string.img_memo)) == true) {
            // 서버에서 온 경우
            Glide.with(context)
                .asBitmap()
                .load(m_filename)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        bitmap = resource
                        Resize_Bitmap(bitmap!!, w, h)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // 작업 수행
                    }
                })
        } else {
            // 이용자의 기기에서
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(m_filename, options)

            bitmap = BitmapFactory.decodeFile(m_filename)
            var mutableBitmap = bitmap
            if (mutableBitmap != null) {
                Resize_Bitmap(mutableBitmap, w, h)
            }
        }
    }

    // 해당 이미지를 저장하고 이전엑티비티로 전송
    fun Save_Send(): String {
        // 내부 저장소 캐시 경로를 받아옴
        val storage = context.cacheDir
        // storage에 파일 인스턴스를 생성
        val tempFile = File(storage, "TEST_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.jpg")
        // 파일을 생성하고, 비크맵 이미지를 JPEG형식으로 파일에 쓰기
        try {
            //자동으로 빈 파일을 생성(지정된 위치에 새로운 빈 파일을 생성)
            tempFile.createNewFile()
            // 파일을 쓸 수 있는 스트림을 준비
            FileOutputStream(tempFile).use { out ->
                // 스트림에 비트맵을 저장
                // [개선] 변화가 있었을 때 변경하는 것으로
                image_Bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    // Bitmap객체를 압축하여 FileOutputStream에 쓴다
            }
            send_filepath = tempFile.path
            Log.d("정보태그", "send_filepath=$send_filepath")
        } catch (e: Exception) {
            Log.e("정보태그", "오류발생 -> ${e.javaClass.simpleName} : ${e.message}")
        }
        return send_filepath.toString()
    }


}

