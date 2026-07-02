package com.example.milkiwai.repository

import com.example.milkiwai.data.dao.BillItemDao
import com.example.milkiwai.data.dao.MonthlyBillDao
import com.example.milkiwai.data.entity.BillItem
import com.example.milkiwai.data.entity.MonthlyBill

class BillRepository(
    private val billDao: MonthlyBillDao,
    private val itemDao: BillItemDao
) {
    val allBills = billDao.getAllBillsWithCustomer()

    suspend fun insertBill(bill: MonthlyBill): Long {
        return billDao.insertBill(bill)
    }

    suspend fun insertItem(item: BillItem) {
        itemDao.insertItem(item)
    }

    suspend fun updateBill(bill: MonthlyBill) {
        billDao.updateBill(bill)
    }

    fun getItemsForBill(billId: Int) = itemDao.getItemsWithBrandByBill(billId)
}
