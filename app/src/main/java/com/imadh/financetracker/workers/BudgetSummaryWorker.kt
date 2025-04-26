package com.imadh.financetracker.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.imadh.financetracker.notifications.NotificationHelper
import com.imadh.financetracker.utils.SharedPreferencesManager

class BudgetSummaryWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val sharedPreferencesManager = SharedPreferencesManager(applicationContext)
        val budget = sharedPreferencesManager.getCurrentMonthBudget()
        val totalExpense = sharedPreferencesManager.getTotalExpense()

        if (budget != null) {
            val remaining = budget.getRemainingBudget(totalExpense)

            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.sendNotification(
                "Budget Summary",
                "You've spent $${totalExpense}. Remaining budget: $${remaining}.",
                2001
            )
        }

        return Result.success()
    }
}