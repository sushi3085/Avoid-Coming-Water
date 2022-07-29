package ncue.geo.avoidingcomingwater

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var informationArea: TextView
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener { refreshOnClick() }
        informationArea = findViewById(R.id.textView)

        handler = object: Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                informationArea.text = msg.data.getString("response")
                Log.i("RESPONSE", informationArea.text.toString())
            }
        }
        handler = Handler{
            informationArea.text = it.data.getString("response")
            true
        }
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

//        val thread = Thread {
//            informationArea.text = HttpClient().get("https://www.google.com")
//        }
//        thread.start()
    //        handler.sendEmptyMessage(0)
//        handler.postDelayed(kotlinx.coroutines.Runnable {
//            informationArea.text = HttpClient().get("https://jsonplaceholder.typicode.com/todos/1")
//        }, 100)
    }
}