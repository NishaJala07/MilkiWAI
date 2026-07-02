package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.BillItemDao
import com.example.milkiwai.data.dao.MonthlyBillDao

class ReportRepository(
    private val billDao: MonthlyBillDao,
    private val itemDao: BillItemDao
) {
    fun getDailyRevenue(today: String) = billDao.getDailyRevenue(today)
    fun getMonthlyRevenue(month: Int, year: Int) = billDao.getMonthlyRevenue(month, year)
    fun getPendingPayments() = billDao.getPendingPayments()
    fun getCustomerWiseRevenue() = billDao.getCustomerWiseRevenue()
    fun getBrandWiseSales() = itemDao.getBrandWiseSales()
}
