package ncue.geo.avoidingcomingwater

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.concurrent.thread

class FutureForcastActivity : AppCompatActivity() {

    private lateinit var informationUpdater: Handler
    private val locationListener = LocationListener {
        println(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future_forcast)

        // region set clickListener
        findViewById<Button>(R.id.futureUpdateBtn).setOnClickListener {
            updateInformationButtonOnclick()
        }
        // endregion

        informationUpdater = Handler{
            findViewById<TextView>(R.id.textView).text = it.data["MESSAGE"].toString()
            println(it.data["MESSAGE"])
            true
        }

        // check for positioning
        // change the value of ConditionController.IS_POSITIONING
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if (!ConditionController.IS_POSITIONING) {
            alertPositioning()
        }
    }

    private fun alertPositioning() {
        val dialog = AlertDialog.Builder(this)
        dialog.setIcon(R.drawable.popout_warning_icon)    // 取得小綠人圖示
        dialog.setTitle("定位已關閉")
        dialog.setMessage("您似乎沒有開啟手機的衛星定位功能\n請確認已開啟定位功能")
        dialog.setNegativeButton("開啟設定") { _, _ ->
//                Toast.makeText(this, "繼續使用", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            baseContext.startActivity(intent)
        }

        dialog.setPositiveButton("知道了") { _, _ ->
//                Toast.makeText(this, "結束程式", Toast.LENGTH_LONG).show()
        }
        dialog.show()
    }

    // TODO: 屆時修正請求網址
    private fun updateInformationButtonOnclick() {
        findViewById<TextView>(R.id.textView).text = "更新中，請稍後～"
        thread {
            val data= Bundle()
            // TODO
            val uri = "https://71f6-118-163-203-105.jp.ngrok.io/api/special/%s"
            val client = HttpClient()
            var result = "可向下捲動    閱覽更多資訊\n\n"
            for(countyName in arrayOf("桃園市", "新竹縣", "宜蘭縣", "臺中市", "南投縣", "臺東縣", "高雄市", "新北市")){
                val response = client.get(uri.format(countyName))
                val jsonOb = JSONTokener(response).nextValue() as JSONArray
                for(i in 0 until jsonOb.length()){
                    val line = jsonOb[i]
                    val information = (line.toString()).split("\n").toTypedArray()
                    val phenomena = information[0]
                    val start = information[1]
                    val end = information[2]
                    result += "$countyName：\n❗$phenomena 將持續：\n從 $start\n至 $end\n"
                }
                result += "\n"
            }
            data.putString("MESSAGE", result)

            val msg = Message()
            msg.data = data
            informationUpdater.sendMessage(msg)
        }
    }

    @SuppressLint("SetTextI18n")
    // TODO : 抓資訊
    private fun printLocal(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                /* 那就 */
                return
            }
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0F, locationListener)
        /** 知道位置之後 **/
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null){
            findViewById<TextView>(R.id.textView).text = "經度是：${location.longitude}\n緯度是：${location.latitude}\n"
            findViewById<TextView>(R.id.textView).append("地點在：")
            findViewById<TextView>(R.id.textView).append("relative information...")
        }
        locationManager.removeUpdates(locationListener)
        return
    }

    fun positionButtonOnclick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if( checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                /* 那就 */
                println("ASD")
                return
            }
        }
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if(!ConditionController.IS_POSITIONING){
            alertPositioning()
            return
        }
        // TODO: 獲取定位資訊
        printLocal()
        return
    }

    private fun isGPSEnabled(context: Context): Boolean{
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}