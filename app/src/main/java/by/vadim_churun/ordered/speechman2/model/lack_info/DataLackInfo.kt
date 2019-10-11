package by.vadim_churun.ordered.speechman2.model.lack_info

sealed class DataLackInfo
{
    class AppointmentInfo(val personName: String, val seminarName: String): DataLackInfo()
    class OrderInfo(val personName: String, val productName: String): DataLackInfo()
}