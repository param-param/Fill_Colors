package com.fill.colors

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import java.util.*
import kotlin.math.roundToInt

class MainActivity : Activity() {
    private var drawingLayout: RelativeLayout? = null
    private var myView: MyView? = null
    private var btnRed: Button? = null
    private var btnGreen: Button? = null
    private var btnBlue: Button? = null
    private lateinit var paint: Paint


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myView = MyView(this)
        drawingLayout = findViewById<View>(R.id.relative_layout) as RelativeLayout
        drawingLayout!!.addView(myView, 900, 1200)

        btnRed = findViewById<View>(R.id.btnRed) as Button
        btnGreen = findViewById<View>(R.id.btnGreen) as Button
        btnBlue = findViewById<View>(R.id.btnBlue) as Button


        btnRed!!.setOnClickListener(View.OnClickListener {
            paint!!.color = Color.RED
        })

        btnGreen!!.setOnClickListener(View.OnClickListener {
            paint!!.color = Color.YELLOW
        })

        btnBlue!!.setOnClickListener(View.OnClickListener {
            paint!!.color = Color.BLUE
        })

    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )

        bm.recycle()
        return resizedBitmap
    }

    inner class MyView(context: Context?) : View(context) {
        val transformationMatrix = Matrix()
        private val path: Path
        var mBitmap: Bitmap
        var pd: ProgressDialog
        val p1 = Point()
        var canvas: Canvas? = null

        init {
            paint = Paint()
            paint.setAntiAlias(true)
            pd = ProgressDialog(context)
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeJoin(Paint.Join.ROUND)
            paint.setStrokeWidth(5f)
            mBitmap = BitmapFactory.decodeResource(
                resources,
                R.drawable.dummy_image
            ).copy(Bitmap.Config.ARGB_8888, true)
            mBitmap = getResizedBitmap(mBitmap, 900, 1200)
            path = Path()

        }
        
        override fun onDraw(canvas: Canvas) {
            this.canvas = canvas
            paint.setColor(Color.GREEN)
            canvas.drawBitmap(mBitmap, 0f, 0f, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            Log.e("coordinates", "=float x=>$x=float y=>$y")

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    p1.x = x.toInt()
                    p1.y = y.toInt()
                    val sourceColor = mBitmap.getPixel(x.toInt(), y.toInt())
                    val targetColor = paint.color
                    TheTask(mBitmap, p1, sourceColor, targetColor).execute()
                    invalidate()
                }
            }
            return true
        }

        inner class TheTask(
            var bmp: Bitmap,
            var pt: Point,
            var targetColor: Int,
            var replacementColor: Int
        ) : AsyncTask<Void?, Int?, Void?>() {


            override fun onPreExecute() {
            }

            override fun onProgressUpdate(vararg values: Int?) {}

            override fun doInBackground(vararg p0: Void?): Void? {
                val f = FloodFill()
                f.floodFill(bmp, pt, targetColor, replacementColor)
                return null
            }

            override fun onPostExecute(result: Void?) {
                invalidate()
            }

        }
    }

    // flood fill
    class FloodFill {
        fun floodFill(image: Bitmap, node: Point, target: Int, replacement: Int) {
            if (target == replacement) return
            val width = image.width
            val height = image.height
            val queue = LinkedList<Point>()
            var nnode: Point? = node

            do {
                var x = nnode!!.x
                val y = nnode.y
                while (x > 0 && image.getPixel(x - 1, y) == target) x--
                var spanUp = false
                var spanDown = false

                while (x < width && image.getPixel(x, y) == target) {
                    image.setPixel(x, y, replacement)

                    if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target) {
                        queue.add(Point(x, y - 1))
                        spanUp = true
                    }
                    else if (spanUp && y > 0 && image.getPixel(x, y - 1) != target) {
                        spanUp = false
                    }

                    if (!spanDown && y < height - 1 && image.getPixel(x, y + 1) == target) {
                        queue.add(Point(x, y + 1))
                        spanDown = true
                    }
                    else if (spanDown && y < height - 1 && image.getPixel(x, y + 1) != target) {
                        spanDown = false
                    }
                    x++
                }
                nnode = queue.pollFirst()
            }
            while (nnode != null)
        }

    }

}