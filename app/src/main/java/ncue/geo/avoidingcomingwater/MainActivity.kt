package ncue.geo.avoidingcomingwater

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var informationArea: TextView
    private lateinit var handler: Handler
    private lateinit var waveImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener { refreshOnClick() }
        informationArea = findViewById(R.id.homapage_subtitle)

        handler = Handler {
            informationArea.text = it.data.getString("response")
            true
        }

        waveImage = findViewById<ImageView>(R.id.waveImage_1)
        val waveimage2 = findViewById<ImageView>(R.id.waveImage_2)

        var ani = TranslateAnimation(-500.0F, 500.0F, 0.0F, 0.0F)
        ani.duration = 5_000
        ani.repeatCount = Animation.INFINITE
        ani.repeatMode = Animation.REVERSE
        waveImage.startAnimation(ani)

        ani = TranslateAnimation(-500.0F, 500.0F, 0.0F, 0.0F)
        ani.duration = 7_000
        ani.repeatCount = Animation.INFINITE
        ani.repeatMode = Animation.REVERSE
        waveimage2.startAnimation(ani)

        findViewById<Button>(R.id.recieveBtn).setOnClickListener(::redButtonClicked)

        // 要定位權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 100
            )
        }
    }

    @Deprecated("do not use")
    private fun refreshOnClick() {
        val thread = thread {
            val data: Bundle = Bundle()
            data.putString(
                "response",
                HttpClient().get("https://jsonplaceholder.typicode.com/todos/1")
            )

            val msg: Message = Message()
            msg.data = data
            handler.sendMessage(msg)
        }
        if (thread.state == Thread.State.NEW)
            thread.start()
    }

    private fun redButtonClicked(view: View) {
        if (ConditionController.IS_RECIEVING) {
            (view as Button).setBackgroundColor(Color.parseColor("#FFFF5454"))
            view.text = "未接收預警"
        } else {
            (view as Button).setBackgroundColor(Color.parseColor("#FF54FF54"))
            view.text = "接收預警中"
            // thread crawl to python API
            // TODO
        }
        ConditionController.IS_RECIEVING = !ConditionController.IS_RECIEVING
    }

    fun specialButtonOnClick(view: View) {
        val intent = Intent(this, SpecialForcastActivity::class.java)
        startActivity(intent)
    }

    fun settingButtonOnClick(view: View){
//        val intent = Intent(this, )
        return
    }
}