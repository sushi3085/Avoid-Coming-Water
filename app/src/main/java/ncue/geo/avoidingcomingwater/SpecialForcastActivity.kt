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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import org.json.JSONArray
import org.json.JSONTokener
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class SpecialForcastActivity : AppCompatActivity() {

    private lateinit var textViewInformationHandler: Handler
    private lateinit var informationUpdater: Handler
    private val buttonsInformation = HashMap<String, String>()
    private val locationListener = LocationListener { println(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.special_forcast_forcast)

        // region set clickListener
        findViewById<Button>(R.id.refreshSpecial).setOnClickListener { updateInformationButtonOnclick() }
        findViewById<Button>(R.id.positioningRefreshSpecial).setOnClickListener { positionButtonOnclick() }
        // endregion

        /** handler definition  TODO: remove this **/
        informationUpdater = Handler {
            findViewById<TextView>(R.id.textView).text = it.data["MESSAGE"].toString()
            println(it.data["MESSAGE"])
            true
        }
        textViewInformationHandler = Handler {
            findViewById<TextView>(R.id.specialTitleTextView).text = it.data.getString("MESSAGE")
            true
        }

        /** define countyButtons' information map **/
        /** get component and click listener bound together **/
        buttonsInformation["新北市"] = ""
        buttonsInformation["桃園市"] = ""
        buttonsInformation["新竹縣"] = ""
        buttonsInformation["苗栗縣"] = ""
        buttonsInformation["臺中市"] = ""
        buttonsInformation["高雄市"] = ""
        buttonsInformation["臺東縣"] = ""
        buttonsInformation["宜蘭縣"] = ""

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

    private fun popupInformation(countyName: String, information: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setIcon(R.drawable.i_icon)
        dialog.setTitle(countyName + "警特報資訊：")
        dialog.setMessage(information)
        dialog.setPositiveButton("知道了") {_,_ ->}
        dialog.show()
    }

    fun showSpecialInformation(view: View) {
        val countyName = view.contentDescription.toString()
        popupInformation(countyName, buttonsInformation[countyName]!!)
    }

    // TODO: 屆時修正請求網址
    private fun updateInformationButtonOnclick() {
        findViewById<TextView>(R.id.specialTitleTextView).text = "更新中，請稍後～"
        thread {
            // TODO: 增加：如果回傳沒有警特報的處理流程
            val uri = "https://09cd-61-221-225-124.jp.ngrok.io/api/special/%s"
            val client = HttpClient()
            /** -- -- --- -- -- **/
            for (pair in buttonsInformation) {
                val countyName = pair.key
                val url = uri.format(countyName)
                val response = client.get(url)
                var result = ""
                if (response.startsWith("[")) {
                    val arr = JSONArray(response)
                    for (i in 0 until arr.length()) {
                        result += arr[i].toString() + "\n"
                    }
                    buttonsInformation[countyName] = result
                } else {
                    buttonsInformation[countyName] = response
                }

                println(countyName)
                println(buttonsInformation[countyName])
            }
            val data = Bundle()
            data.putString("MESSAGE", "點擊氣象圖示 獲得更多警特報資訊")
            val msg = Message()
            msg.data = data
            textViewInformationHandler.sendMessage(msg)

            /** -- -- --- -- -- **/
        }
    }

    @SuppressLint("SetTextI18n")
    // TODO : 抓資訊
    private fun printLocal(): Location? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                /* 那就 */
                return null
            }
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0.0F,
            locationListener
        )
        /** 知道位置之後 **/
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (location != null) {
            findViewById<TextView>(R.id.textView).text =
                "經度是：${location.longitude}\n緯度是：${location.latitude}\n"
            findViewById<TextView>(R.id.textView).append("地點在：")
            findViewById<TextView>(R.id.textView).append("relative information...")
        }
        locationManager.removeUpdates(locationListener)
        return location
    }

    private fun positionButtonOnclick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                /* 那就 */
                println("DIDN'T ENABLE LOCATING")
                return
            }
        }
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if (!ConditionController.IS_POSITIONING) {
            alertPositioning()
            return
        }
        // TODO: 獲取定位資訊
        val location = printLocal() ?: return
        // TODO: 新增「藉由經緯取得所在縣市名稱」功能 // https://api.nlsc.gov.tw/other/TownVillagePointQuery/120.698659/24.156250/4326
        val uri = "https://api.nlsc.gov.tw/other/TownVillagePointQuery/%f/%f/4326".format(
            location.longitude,
            location.latitude
        )
        thread {
            val response = HttpClient().get(uri)
            // TODO: 直接split 'ctyName'掉 就好了，用XML真的是...
            println("THREAD STARTED")
        }
        return
    }

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}