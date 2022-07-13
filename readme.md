# 目前需要的功能
- [ ] 歡迎頁面
    - [ ] 包含前往不同頁面的按鈕(前往未來預報、前往過去回波資料之類的)
    - [ ] 訂閱按鈕

- [ ] 未來預報頁面
    - [ ] 刷新資料功能(以按鈕實作)

- [ ] 過去回波資料頁面(暫定)
    - [ ] 刷新資料功能(以按鈕實作)
    - [ ] ...

- [ ] 接收警戒頁面
    - [ ] 定位功能
        - 待學習
    - [ ] 接收功能
        - Android發警戒聲響的功能實現、通知
    - (將JAVA API運用於此)

- [x] JAVA API
    - [x] 自製抓資料API

- [ ] Python API， 網址為：`https://avoiding-coming-water.herokuapp.com/api`
    - 後面接`qpe` 則網址為 `https://avoiding-coming-water.herokuapp.com/api/qpe` 取得未來一小時指定區域之預測雨量  
      參數名 + 參數datatype + 參數說明：  
        > `start_x` `float` 矩形左下角經度  
        > `start_y` `float` 矩形左下角緯度  
        > `end_x` `float` 矩形右上角經度  
        > `end_y` `float` 矩形右上角緯度  
    - 後面接`radar` 則網址為 `https://avoiding-coming-water.herokuapp.com/api/radar` 取得過去特定時長之指定區域的雷達迴波紀錄  
      參數名 + 參數datatype + 參數說明：
        > `time_span` `int` 以小時為單位，取得過去n小時的回波資料  
        > `start_x` `float` 矩形左下角經度  
        > `start_y` `float` 矩形左下角緯度  
        > `end_x` `float` 矩形右上角經度  
        > `end_y` `float` 矩形右上角緯度  
    - 後面接`special` 則網址為`https://avoiding-coming-water.herokuapp.com/api/special` 取得中央氣象局發布之警特報資訊(縣市為最小單位)
        - 待補
    - 後面接`forcast` 則網址為`https://avoiding-coming-water.herokuapp.com/api/forcast` 取得未來6小時之各式天氣及體感資訊
        - 待補
