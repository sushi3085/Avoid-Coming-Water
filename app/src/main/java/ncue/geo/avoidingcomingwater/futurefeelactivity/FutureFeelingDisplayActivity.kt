package ncue.geo.avoidingcomingwater.futurefeelactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import ncue.geo.avoidingcomingwater.R
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.thread

class FutureFeelingDisplayActivity : AppCompatActivity() {
    lateinit var countyName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future_feeling_display)

        countyName = intent.getStringExtra("countyName")!!
        // use simplefied here: '台'
        val client = HttpClient()
        thread {
            val response = client.get("https://24b3-61-221-225-124.jp.ngrok.io/api/forcast/$countyName")
            var result = "$countyName：\n"
            val jsonArr = JSONArray(response)
            for(i in 0 until jsonArr.length()){
                val jsonMap = jsonArr.getJSONObject(i)
                val townName = jsonMap["townName"]
                val discription = jsonMap.getJSONArray("天氣預報綜合描述").getJSONObject(0).getString("value")
                val uvi = jsonMap.getJSONArray("紫外線指數").getJSONObject(1).getString("value")
                result += "$townName:\n" +
                        "天氣預報綜合描述:\n" +
                        "${discription.replace('。','\n')}" +
                        "紫外線曝曬級數:\n" +
                        "$uvi\n\n"
            }
            runOnUiThread{ findViewById<TextView>(R.id.futureFeelingDisplayTV).text = result }
        }
    }
}