package ncue.geo.avoidingcomingwater

class ConditionController {
    companion object{
        var IS_RECIEVING: Boolean = false
        var IS_POSITIONING: Boolean = false

        val specialButtonInformation = mapOf(
            "新北市" to "請先更新資訊，謝謝您！",
            "桃園市" to "請先更新資訊，謝謝您！",
            "新竹縣" to "請先更新資訊，謝謝您！",
            "苗栗縣" to "請先更新資訊，謝謝您！",
            "臺中市" to "請先更新資訊，謝謝您！",
            "高雄市" to "請先更新資訊，謝謝您！",
            "臺東縣" to "請先更新資訊，謝謝您！",
            "宜蘭縣" to "請先更新資訊，謝謝您！",
        )
    }
}