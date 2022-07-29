package ncue.geo.avoidingcomingwater

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var informationArea: TextView
    private lateinit var handler: Handler
    private lateinit var waveImage: ImageView
    companion object{
        private var isRecieving = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener { refreshOnClick() }
        informationArea = findViewById(R.id.homapage_subtitle)

        // region handler
//        handler = object: Handler(Looper.getMainLooper()) {
//            override fun handleMessage(msg: Message) {
//                super.handleMessage(msg)
//                informationArea.text = msg.data.getString("response")
//                Log.i("RESPONSE", informationArea.text.toString())
//            }
//        }
        // endregion

        handler = Handler{
            informationArea.text = it.data.getString("response")
            true
        }

        waveImage = findViewById<ImageView>(R.id.waveImage_1)
        val waveImage_2 = findViewById<ImageView>(R.id.waveImage_2)

        var ani = TranslateAnimation(-500.0F, 500.0F, 0.0F, 0.0F)
        ani.duration = 5_000
        ani.repeatCount = Animation.INFINITE
        ani.repeatMode = Animation.REVERSE
        waveImage.startAnimation(ani)

        ani = TranslateAnimation(-500.0F, 500.0F, 0.0F, 0.0F)
        ani.duration = 7_000
        ani.repeatCount = Animation.INFINITE
        ani.repeatMode = Animation.REVERSE
        waveImage_2.startAnimation(ani)

        findViewById<Button>(R.id.recieveBtn).setOnClickListener (::redButtonClicked)
    }

    private fun refreshOnClick(){
        val thread = thread {
            val data: Bundle = Bundle()
            data.putString("response", HttpClient().get("https://jsonplaceholder.typicode.com/todos/1"))

            val msg: Message = Message()
            msg.data = data
            handler.sendMessage(msg)
        }
        if(thread.state == Thread.State.NEW)
            thread.start()
    }

    private fun redButtonClicked(view: View){
        if(isRecieving) {
            (view as Button).setBackgroundColor(Color.parseColor("#FFFF5454"))
            view.text = "未接收預警"
        }
        else {
            (view as Button).setBackgroundColor(Color.parseColor("#FF54FF54"))
            view.text = "接收預警中"
            // thread crawl to python API
        }
        isRecieving = !isRecieving
    }
}