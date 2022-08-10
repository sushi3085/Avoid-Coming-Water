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
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class SpecialForcastActivity : AppCompatActivity() {

    private lateinit var textViewInformationHandler: Handler
    private lateinit var informationUpdater: Handler
    private val buttonsInformation = HashMap<String, String>()
    private val specialButtons = Array(8){0}
    private val locationListener = LocationListener { println("handler print: $it"); this.hasUpdateLocation = true }
    private var location: Location? = null
    private var hasUpdateLocation = false

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
        /** 自 Controller 讀取資訊 **/
        buttonsInformation["新北市"] = ConditionController.specialButtonInformation["新北市"]!!;specialButtons[0] = R.id.shinbeiSpecialButton
        buttonsInformation["桃園市"] = ConditionController.specialButtonInformation["桃園市"]!!;specialButtons[1] = R.id.taouanSpecialButton
        buttonsInformation["新竹縣"] = ConditionController.specialButtonInformation["新竹縣"]!!;specialButtons[2] = R.id.shinchuSpecialButton
        buttonsInformation["苗栗縣"] = ConditionController.specialButtonInformation["苗栗縣"]!!;specialButtons[3] = R.id.miaoliSpecialButton
        buttonsInformation["臺中市"] = ConditionController.specialButtonInformation["臺中市"]!!;specialButtons[4] = R.id.taichungSpecialButton
        buttonsInformation["高雄市"] = ConditionController.specialButtonInformation["高雄市"]!!;specialButtons[5] = R.id.kaoshungSpecialButton
        buttonsInformation["臺東縣"] = ConditionController.specialButtonInformation["臺東縣"]!!;specialButtons[6] = R.id.taidongSpecialButton
        buttonsInformation["宜蘭縣"] = ConditionController.specialButtonInformation["宜蘭縣"]!!;specialButtons[7] = R.id.yilanSpecialButton

        updateButtonImages()

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

    private fun soSuchPlacePopup(){
        val dialog = AlertDialog.Builder(this)
        dialog.setIcon(R.drawable.i_icon)
        dialog.setTitle("Title here")
        dialog.setMessage("still not come up with the title and message content\ncurrently not supporting your county")
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
            val uri = "https://ff7b-61-221-225-125.jp.ngrok.io/api/special/%s"
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
                    buttonsInformation[countyName] = response.trim('\n').trim('"')
                }

                print("$countyName:")
                println(buttonsInformation[countyName])
            }
            updateButtonImages()
            val data = Bundle()
            data.putString("MESSAGE", "點擊氣象圖示 獲得更多警特報資訊")
            val msg = Message()
            msg.data = data
            textViewInformationHandler.sendMessage(msg)

            /** 儲存狀態至 Controller **/
            buttonsInformation.forEach{
                ConditionController.specialButtonInformation[it.key] = it.value
            }
        }
    }

    private fun updateButtonImages() {
        this.runOnUiThread {
            // change button background and icon
            for (id in specialButtons) {
                val btn: ImageButton = findViewById(id)
                if(buttonsInformation[btn.contentDescription] == "請先更新資訊，謝謝您！"){
                    btn.setImageResource(R.drawable.questionmark)
                } else if (buttonsInformation[btn.contentDescription] == "尚無相關警特報資訊") {
                    btn.setImageResource(R.drawable.no_information)
                    btn.setBackgroundResource(R.drawable.yellow_special_county_btn)
                } else {
                    btn.setImageResource(R.drawable.popout_warning_icon)
                    btn.setBackgroundResource(R.drawable.red_round_corner_img_btn)
                }
            }
        }
    }

    private fun updateLocal(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                /* 那就 */
                return
            }
        }
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,999999F,locationListener)

        /** 知道位置之後，取得位置資訊 **/
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
    }

    private fun positionButtonOnclick() {
        /** first update information **/
//        updateInformationButtonOnclick()
        /** -- --- -- **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                /* 那就 */
                return
            }
        }
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if (!ConditionController.IS_POSITIONING) {
            alertPositioning()
            return
        }
        // 獲取定位資訊
        updateLocal()
        // if not update yet, popup and return
        if(!this.hasUpdateLocation){
            popupInformation("", "初次更新位置資訊需要較久時間\n請再次嘗試")
            return
        }
        // 「藉由經緯取得所在縣市名稱」功能 // https://api.nlsc.gov.tw/other/TownVillagePointQuery/120.698659/24.156250/4326
        thread {
            val uri = "https://api.nlsc.gov.tw/other/TownVillagePointQuery/%f/%f/4326".format(
                this.location!!.longitude,
                this.location!!.latitude
            )
            val response = HttpClient().get(uri)
            if(response.split("ctyName").size == 1){
                this.runOnUiThread {
                    // TODO: popup failure and return
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("查無該處")
                    dialog.setMessage("未支援您所在的位置")
                    dialog.setIcon(R.drawable.i_icon)
                    dialog.setPositiveButton("知道了") {_,_ ->}
                    dialog.show()
                }
                return@thread
            }
            val countyName = response.split("ctyName")[1].subSequence(1..3).toString()
            if(countyName !in buttonsInformation.keys){
                this.runOnUiThread {
                    // TODO: popup failure and return
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle(countyName)
                    dialog.setMessage("未支援您所在的縣市")
                    dialog.setIcon(R.drawable.i_icon)
                    dialog.setPositiveButton("知道了") {_,_ ->}
                    dialog.show()
                }
                return@thread
            }
            this.runOnUiThread {
                popupInformation(countyName, buttonsInformation[countyName]!!)
            }
            println("PRINT FROM THREAD: 所在位置$countyName")
        }
    }

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}