package ncue.geo.avoidingcomingwater

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import ncue.geo.avoidingcomingwater.service.AlertService
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private lateinit var informationArea: TextView
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        findViewById<Button>(R.id.button).setOnClickListener { refreshOnClick() }
        informationArea = findViewById(R.id.homapage_subtitle)

        handler = Handler {
            informationArea.text = it.data.getString("response")
            true
        }

        val waveImage = findViewById<ImageView>(R.id.waveImage_1)
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

        // check services number/is running
        // update recieve button condition
        updateRecievingButtonColor()

    }

    private fun updateRecievingButtonColor() {
        if (getSharedPreferences("AVOIDING_WATER", MODE_PRIVATE).getBoolean("IS_RECIEVING", false))
            findViewById<Button>(R.id.recieveBtn).backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FF54FF54"))// green color
        else
            findViewById<Button>(R.id.recieveBtn).backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FFFF5454"))// red color
    }

    @Deprecated("do not use")
    private fun refreshOnClick() {
        val thread = thread {
            val data = Bundle()
            data.putString(
                "response",
                HttpClient().get("https://jsonplaceholder.typicode.com/todos/1")
            )

            val msg = Message()
            msg.data = data
            handler.sendMessage(msg)
        }
        if (thread.state == Thread.State.NEW)
            thread.start()
    }

    private fun redButtonClicked(view: View) {
        val setting = getSharedPreferences("AVOIDING_WATER", MODE_PRIVATE)
        if (setting.getBoolean("IS_RECIEVING", false)) {
            (view as Button).text = "未接收預警"
        } else {
            (view as Button).text = "接收預警中"
            // thread crawl to python API
            // TODO
            //  search DB for service number, decide 'startService()' or not
            // start service
            Intent(this, AlertService::class.java).also {
                startService(it)
            }
        }
        val recievingStatus = setting.getBoolean("IS_RECIEVING", false)
        setting.edit().putBoolean("IS_RECIEVING", !recievingStatus).apply()
        updateRecievingButtonColor()
    }

    fun specialButtonOnClick(view: View) {
        val intent = Intent(this, SpecialForcastActivity::class.java)
        startActivity(intent)
    }

    fun radarButtonOnClick(view: View){
//        startActivity(Intent(this, RadarActivity::class.java))
        return
    }

    fun settingButtonOnClick(view: View){
//        val intent = Intent(this, )
        Toast.makeText(this, "opening settings", Toast.LENGTH_SHORT).show()
        return
    }
}