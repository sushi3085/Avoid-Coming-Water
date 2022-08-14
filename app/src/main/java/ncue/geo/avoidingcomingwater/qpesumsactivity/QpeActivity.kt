package ncue.geo.avoidingcomingwater.qpesumsactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import ncue.geo.avoidingcomingwater.R

class QpeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qpe)

        val countySpinner = findViewById<Spinner>(R.id.qpeCountySpinner)
        countySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val positionStrings = when(parent.selectedItem){
                    "新北市"->R.array.新北市的鄉鎮
                    "桃園市"->R.array.桃園市的鄉鎮
                    "宜蘭市"->R.array.宜蘭市的鄉鎮
                    "新竹縣"->R.array.新竹縣的鄉鎮
                    "台中市"->R.array.台中市的鄉鎮
                    "高雄市"->R.array.高雄市的鄉鎮
                    "台東縣"->R.array.台東縣的鄉鎮
                    "南投縣"->R.array.南投縣的鄉鎮
                    else -> R.array.請選擇縣市
                }
                val arrAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(baseContext, positionStrings, androidx.transition.R.layout.support_simple_spinner_dropdown_item)
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
        zoneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                // TODO("Not yet implemented")
                val positionStrings = when(parent.selectedItem){
                    "金山區"->R.array.金山區
                    "三峽區"->R.array.三峽區
                    "烏來區"->R.array.烏來區
                    "復興區"->R.array.復興區
                    "大同鄉"->R.array.大同鄉
                    "尖石鄉"->R.array.尖石鄉
                    "和平區"->R.array.和平區
                    "桃源區"->R.array.桃源區
                    "六龜區"->R.array.六龜區
                    "茂林區"->R.array.茂林區
                    "海端鄉"->R.array.海端鄉
                    "信義鄉"->R.array.信義鄉
                    "仁愛鄉"->R.array.仁愛鄉
                    else -> R.array.請選擇鄉鎮市區
                }
                val arrAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(baseContext, positionStrings, androidx.transition.R.layout.support_simple_spinner_dropdown_item)
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

    fun qpeLookupButtonOnClick(view: View){

    }

    fun qpeLocateButtonOnClick(view: View){
        return
    }
}

// TODO: 縣市的spinner要加 onchange的listener
//  兩個按鈕的 function