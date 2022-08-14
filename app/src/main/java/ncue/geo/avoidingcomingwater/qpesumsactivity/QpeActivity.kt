package ncue.geo.avoidingcomingwater.qpesumsactivity

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
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ncue.geo.avoidingcomingwater.ConditionController
import ncue.geo.avoidingcomingwater.R
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import org.json.JSONArray
import kotlin.concurrent.thread

class QpeActivity : AppCompatActivity() {
    private val locationListener = LocationListener { println("handler print: $it") }
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qpe)

        val countySpinner = findViewById<Spinner>(R.id.qpeCountySpinner)
        countySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val positionStrings = when (parent.selectedItem) {
                    "新北市" -> R.array.新北市的鄉鎮
                    "桃園市" -> R.array.桃園市的鄉鎮
                    "宜蘭市" -> R.array.宜蘭市的鄉鎮
                    "新竹縣" -> R.array.新竹縣的鄉鎮
                    "台中市" -> R.array.台中市的鄉鎮
                    "高雄市" -> R.array.高雄市的鄉鎮
                    "台東縣" -> R.array.台東縣的鄉鎮
                    "南投縣" -> R.array.南投縣的鄉鎮
                    else -> R.array.請選擇縣市
                }
                val arrAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                    baseContext,
                    positionStrings,
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item
                )
                arrAdapter.setDropDownViewResource(androidx.transition.R.layout.support_simple_spinner_dropdown_item)
                runOnUiThread {
                    findViewById<Spinner>(R.id.qpeZoneSpinner).apply {
                        this.gravity = Gravity.CENTER
                        this.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
                        this.adapter = arrAdapter
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                TODO("Not yet implemented")
            }

        }

        val zoneSpinner = findViewById<Spinner>(R.id.qpeZoneSpinner)
        zoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // TODO("Not yet implemented")
                val positionStrings = when (parent.selectedItem) {
                    "金山區" -> R.array.金山區
                    "三峽區" -> R.array.三峽區
                    "烏來區" -> R.array.烏來區
                    "復興區" -> R.array.復興區
                    "大同鄉" -> R.array.大同鄉
                    "尖石鄉" -> R.array.尖石鄉
                    "和平區" -> R.array.和平區
                    "桃源區" -> R.array.桃源區
                    "六龜區" -> R.array.六龜區
                    "茂林區" -> R.array.茂林區
                    "海端鄉" -> R.array.海端鄉
                    "信義鄉" -> R.array.信義鄉
                    "仁愛鄉" -> R.array.仁愛鄉
                    else -> R.array.請選擇鄉鎮市區
                }
                val arrAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                    baseContext,
                    positionStrings,
                    androidx.transition.R.layout.support_simple_spinner_dropdown_item
                )
                arrAdapter.setDropDownViewResource(androidx.transition.R.layout.support_simple_spinner_dropdown_item)
                runOnUiThread {
                    findViewById<Spinner>(R.id.qpePositionSpinner).apply {
                        this.gravity = Gravity.CENTER
                        this.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
                        this.adapter = arrAdapter
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun qpeLookupButtonOnClick(view: View) {
        // blocks no select
        if(findViewById<Spinner>(R.id.qpeCountySpinner).selectedItem == "請選擇"){
            AlertDialog.Builder(this)
                .setIcon(R.drawable.i_icon)
                .setTitle("尚未選擇地點")
                .setMessage("請選擇地點\n或使用下方\"使用定位\"按鈕定位")
                .setNegativeButton("知道了"){_,_->}
                .show()
            return
        }
        // get response from api
        thread {
            // get position choosen
            val choosenTownName = findViewById<Spinner>(R.id.qpeZoneSpinner).selectedItem.toString()
            val choosenPositionName = findViewById<Spinner>(R.id.qpePositionSpinner).selectedItem.toString()
            val response = HttpClient().get("https://4b67-118-163-203-105.jp.ngrok.io/api/qpe/$choosenTownName/$choosenPositionName")
            // extract data
            val jsonArr = JSONArray(response)
            Log.wtf("qpe fetching data", response)
            // refresh UI
            runOnUiThread {
                val rain = jsonArr[0].toString().toFloat()
                Log.wtf("print rain value", rain.toString())
                findViewById<TextView>(R.id.qpeRainValueTV).text = "${jsonArr[0]}mm"
                val rainLevel = if (rain >= 40)
                    "大雨或以上"
                else if (rain >= 25)
                    "中雨"
                else if (rain >= 10)
                    "小雨"
                else if (rain >= 0)
                    "晴/小雨"
                else
                    "---"
                findViewById<TextView>(R.id.qpeRainLevelValueTV).text = rain.toString()
                findViewById<TextView>(R.id.qpeRainLevelValueTV).text = rainLevel
                findViewById<TextView>(R.id.qpeIsAlertValueTV).text = jsonArr[1].toString()
                AlertDialog.Builder(this)
                    .setIcon(R.drawable.i_icon)
                    .setTitle("更新完畢")
                    .setMessage("")
                    .setNegativeButton("知道了"){_,_->}
                    .show()
            }
        }
    }

    fun qpeLocateButtonOnClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if (!isGPSEnabled(baseContext)) {
            alertPositioning()
            return
        }
        // fetch location
        fetchLocal()
        // fetch nearest location
        thread {
            runOnUiThread { Toast.makeText(this, "請稍待定位抓取", Toast.LENGTH_LONG).show() }
            Thread.sleep(5000)
            val countySpinner = findViewById<Spinner>(R.id.qpeCountySpinner)
            val townSpinner = findViewById<Spinner>(R.id.qpeZoneSpinner)
            val positionSpinner = findViewById<Spinner>(R.id.qpePositionSpinner)
            val jsonArr = JSONArray(HttpClient().get("https://4b67-118-163-203-105.jp.ngrok.io/api/nearestPosition/${location!!.longitude}/${location!!.latitude}"))

            // set spinner
            val countyListID = when (jsonArr.getString(0)) {// countyName
                "新北市" -> 1
                "桃園市" -> 2
                "宜蘭縣" -> 3
                "新竹縣" -> 4
                "南投縣" -> 5
                "台中市" -> 6
                "高雄市" -> 7
                "台東縣" -> 8
                else -> 0
            }
            runOnUiThread {
                countySpinner.setSelection(countyListID)

                var townID = -1
                val townName = jsonArr.getString(1)
                for (i in 0 until townSpinner.adapter.count)
                    if (townName == townSpinner.adapter.getItem(i))
                        townID = i
                townSpinner.setSelection(townID)

                val positionName = jsonArr.getString(2)
                var positionID = -1
                for (i in 0 until positionSpinner.adapter.count)
                    if (positionName == positionSpinner.adapter.getItem(i))
                        positionID = i
                positionSpinner.setSelection(positionID)
                // call qpeLookup
                thread {
                    Thread.sleep(500)//等一下，不然會find到null的View
                    runOnUiThread { qpeLookupButtonOnClick(view) }
                }
            }
        }

        return
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

    private fun fetchLocal(){
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

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}

// TODO: 縣市的spinner要加 onchange的listener
//  兩個按鈕的 function