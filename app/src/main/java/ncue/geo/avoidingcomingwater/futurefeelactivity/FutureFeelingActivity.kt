package ncue.geo.avoidingcomingwater.futurefeelactivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import ncue.geo.avoidingcomingwater.ConditionController
import ncue.geo.avoidingcomingwater.R
import ncue.geo.avoidingcomingwater.crawlapi.HttpClient
import kotlin.concurrent.thread

class FutureFeelingActivity : AppCompatActivity() {
    private val locationListener = LocationListener { println("handler print: $it") }
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_future_feeling)
    }

    // region LOCATION BUTTON WORK SPACE
    fun locationButtonClicked(view: View){
        // check location is allowed or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        ConditionController.IS_POSITIONING = isGPSEnabled(baseContext)
        if (!ConditionController.IS_POSITIONING) {
            alertPositioning()
            return
        }
        // fetch location
        fetchLocal()

        // 提示稍後、API取得縣市名稱
        thread{
            runOnUiThread { Toast.makeText(this, "請稍後等待定位抓取", Toast.LENGTH_LONG).show() }
            Thread.sleep(5000)
            val uri = "https://api.nlsc.gov.tw/other/TownVillagePointQuery/%f/%f/4326".format(
                this.location!!.longitude,
                this.location!!.latitude
            )
            val response = HttpClient().get(uri)
            if(response.split("ctyName").size == 1){
                runOnUiThread {
                    noSuchPlace()
                    findViewById<Spinner>(R.id.spinner).setSelection(0)
                }
                return@thread
            }
            // get county name and set it to spinner
            runOnUiThread {
                val countyName = response.split("ctyName")[1].subSequence(1..3).toString().replace('臺','台')
                val spinner = findViewById<Spinner>(R.id.spinner)
                for(i in 0 until spinner.adapter.count){
                    if(spinner.getItemAtPosition(i) == countyName)
                        spinner.setSelection(i)
                }
                informLocateComplete()
            }
        }
    }

    private fun informLocateComplete() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.i_icon)
            .setTitle("定位完成")
            .setMessage("已完成定位，\n可點選\"查看\"按鈕\n查看未來12小時天氣預報！")
            .setPositiveButton("知道了") { _, _ -> }
            .show()
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

    private fun noSuchPlace(){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("查無該處")
        dialog.setMessage("未支援您所在的位置\n請手動選擇縣市")
        dialog.setIcon(R.drawable.i_icon)
        dialog.setPositiveButton("知道了") {_,_ ->}
        dialog.show()
    }
    // endregion

    fun futureFeelingChoiceButtonOnClick(view: View){
        val spinner = findViewById<Spinner>(R.id.spinner)
        if(spinner.selectedItem == "請選擇"){
            pleaseChooseCountyDialog()
            return
        }
        Intent(this, FutureFeelingDisplayActivity::class.java).also {
            it.putExtra("countyName", spinner.selectedItem.toString())
            startActivity(it)
        }
    }

    private fun pleaseChooseCountyDialog() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.i_icon)
            .setTitle("尚未選擇縣市")
            .setMessage("請選擇縣市\n或輕觸下方\"使用定位\"按鈕\n定位所在地")
            .setPositiveButton("知道了") {_,_ ->}
            .show()
    }
}