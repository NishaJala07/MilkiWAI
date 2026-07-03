package com.example.milkiwai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.milkiwai.ui.customer.AddCustomerActivity
import com.example.milkiwai.ui.customer.CustomerListActivity
import com.example.milkiwai.ui.auth.LoginActivity
import com.example.milkiwai.ui.settings.SettingsActivity
import com.example.milkiwai.voice.VoiceAction
import com.example.milkiwai.voice.VoiceManager
import com.example.milkiwai.voice.ParsedCommand
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.example.milkiwai.ui.procurement.ProcurementActivity
import com.example.milkiwai.ui.PaymentReminderActivity
import com.example.milkiwai.ui.RouteActivity
import com.example.milkiwai.utils.DailyReminderWorker
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.milkiwai.viewmodel.ReportViewModel
import com.example.milkiwai.viewmodel.procurement.ProcurementViewModel
import com.example.milkiwai.viewmodel.DeliveryViewModel
import java.util.*
import java.text.SimpleDateFormat
import com.example.milkiwai.ui.billing.BillAdapter
import com.example.milkiwai.ui.procurement.DeliveryAdapter
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.milkiwai.data.entity.BillWithCustomer
import com.example.milkiwai.data.model.DeliveryItem

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var voiceManager: VoiceManager
    private val procurementViewModel: ProcurementViewModel by viewModels()
    private val reportViewModel: ReportViewModel by viewModels()
    private val deliveryViewModel: DeliveryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // setSupportActionBar(findViewById(R.id.toolbar))
        // supportActionBar?.setDisplayShowTitleEnabled(false)

        voiceManager = VoiceManager(this)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val tvWelcome = findViewById<TextView>(R.id.tvWelcomeUser)
            val tvUserName = findViewById<TextView>(R.id.tvUserName)
            val ivHeaderProfile = findViewById<ImageView>(R.id.ivHeaderProfile)
            
            fun updateWelcome(name: String?) {
                val displayName = name ?: currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "User"
                val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    in 17..20 -> "Good Evening"
                    else -> "Hello"
                }
                tvWelcome.text = "$greeting,"
                tvUserName.text = displayName
            }

            // Initial set from Auth
            updateWelcome(null)

            if (currentUser.photoUrl != null) {
                Glide.with(this)
                    .load(currentUser.photoUrl)
                    .placeholder(R.drawable.app_logo)
                    .error(R.drawable.app_logo)
                    .circleCrop()
                    .into(ivHeaderProfile)
            } else {
                ivHeaderProfile.setImageResource(R.drawable.app_logo)
            }

            // Try to get updated name from Firestore
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firestoreName = document.getString("name")
                        if (!firestoreName.isNullOrEmpty()) {
                            updateWelcome(firestoreName)
                        }
                    }
                }
        }

        findViewById<View>(R.id.ivActionSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<View>(R.id.ivActionMore).setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
            // Hide the settings item since it's already a separate button
            popup.menu.findItem(R.id.action_settings)?.isVisible = false
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_profile -> {
                        startActivity(Intent(this, com.example.milkiwai.ui.profile.ProfileActivity::class.java))
                        true
                    }
                    R.id.action_logout -> {
                        auth.signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        setupDashboard()
        setupRecentActivity()
        setupDailyWork()

        findViewById<View>(R.id.btnAddCustomer).setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        findViewById<View>(R.id.btnViewCustomers).setOnClickListener {
            startActivity(Intent(this, CustomerListActivity::class.java))
        }

        findViewById<View>(R.id.btnManageBrands).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.brand.BrandListActivity::class.java))
        }

        findViewById<View>(R.id.btnViewBills).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.billing.BillListActivity::class.java))
        }

        findViewById<View>(R.id.btnReports).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.reports.ReportsActivity::class.java))
        }

        findViewById<View>(R.id.btnMilkRegister).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.register.MilkRegisterActivity::class.java))
        }

        findViewById<View>(R.id.btnOcrParser).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.RequestParserActivity::class.java))
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, com.example.milkiwai.ui.profile.ProfileActivity::class.java))
                true
            }
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    */

    private fun setupDashboard() {
        val tvMainProcurementQty = findViewById<TextView>(R.id.tvMainProcurementQty)
        val tvMainPendingPayments = findViewById<TextView>(R.id.tvMainPendingPayments)
        val tvMainDeliverySummary = findViewById<TextView>(R.id.tvMainDeliverySummary)
        val tvMonthlyRevenue = findViewById<TextView>(R.id.tvMonthlyRevenue)
        val tvActiveCustomers = findViewById<TextView>(R.id.tvActiveCustomers)
        val tvSuccessRate = findViewById<TextView>(R.id.tvSuccessRate)

        findViewById<View>(R.id.cardProcurement).setOnClickListener {
            startActivity(Intent(this, ProcurementActivity::class.java))
        }

        findViewById<View>(R.id.cardTodayDeliveries).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.delivery.DeliveryActivity::class.java))
        }

        findViewById<View>(R.id.cardPendingPayments).setOnClickListener {
            startActivity(Intent(this, PaymentReminderActivity::class.java))
        }

        procurementViewModel.procurementReport.observe(this) { reports ->
            val total = reports?.sumOf { it.requiredQuantity } ?: 0.0
            tvMainProcurementQty.text = String.format("%.1f L", total)
        }

        reportViewModel.getPendingPayments().observe(this) { pendingBills ->
            val totalPending = pendingBills?.sumOf { it.bill.totalAmount } ?: 0.0
            tvMainPendingPayments.text = "₹${totalPending.toInt()}"
        }

        deliveryViewModel.deliveryItems.observe(this) { items ->
            val total = items?.size ?: 0
            val delivered = items?.count { it.delivery?.status == "DELIVERED" } ?: 0
            tvMainDeliverySummary.text = "$delivered/$total"
            
            val rate = if (total > 0) (delivered.toDouble() / total * 100).toInt() else 0
            tvSuccessRate.text = "$rate%"
        }

        // Monthly Revenue
        val calendar = Calendar.getInstance()
        reportViewModel.getMonthlyRevenue(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
            .observe(this) { revenue ->
                tvMonthlyRevenue.text = "₹${(revenue ?: 0.0).toInt()}"
            }

        // Active Customers
        reportViewModel.getAllCustomers().observe(this) { customers ->
            tvActiveCustomers.text = "${customers.size}"
        }

        val today = Date()
        deliveryViewModel.loadDeliveries(today)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        procurementViewModel.loadProcurementData(tomorrow.time)
    }

    private fun setupRecentActivity() {
        val rvRecentPayments = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRecentPayments)

        val billAdapter = BillAdapter({ item ->
            val intent = Intent(this, com.example.milkiwai.ui.billing.BillDetailsActivity::class.java).apply {
                putExtra("BILL_ID", item.bill.id)
                putExtra("CUSTOMER_NAME", item.customer?.name ?: "Unknown")
                putExtra("MONTH", item.bill.month)
                putExtra("YEAR", item.bill.year)
                putExtra("TOTAL", item.bill.totalAmount)
                putExtra("STATUS", item.bill.paymentStatus)
                putExtra("PDF_PATH", item.bill.pdfPath)
                putExtra("CUSTOMER_MOBILE", item.customer?.mobile ?: "")
            }
            startActivity(intent)
        }, { item ->
            val monthName = java.text.DateFormatSymbols().months[item.bill.month - 1]
            val intent = Intent(this, com.example.milkiwai.ui.billing.PaymentActivity::class.java).apply {
                putExtra("BILL_ID", item.bill.id)
                putExtra("AMOUNT", item.bill.totalAmount)
                putExtra("CUSTOMER_NAME", item.customer?.name ?: "Unknown")
                putExtra("MONTH_YEAR", "$monthName ${item.bill.year}")
            }
            startActivity(intent)
        })
        rvRecentPayments.layoutManager = LinearLayoutManager(this)
        rvRecentPayments.adapter = billAdapter

        reportViewModel.getPendingPayments().observe(this) { bills ->
            billAdapter.setBills(bills?.take(3) ?: emptyList())
        }

        findViewById<View>(R.id.btnViewAllPayments).setOnClickListener {
            startActivity(Intent(this, com.example.milkiwai.ui.billing.BillListActivity::class.java))
        }
    }

    private fun setupDailyWork() {
        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayToMorning(), TimeUnit.MILLISECONDS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyMilkReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateDelayToMorning(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
    }

    private fun checkVoicePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        } else {
            startVoiceListening()
        }
    }

    private fun startVoiceListening() {
        voiceManager.startListening(object : VoiceManager.VoiceCallback {
            override fun onCommandParsed(command: ParsedCommand) {
                handleVoiceCommand(command)
            }

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleVoiceCommand(command: ParsedCommand) {
        when (command.action) {
            VoiceAction.ADD_CUSTOMER -> {
                val intent = Intent(this, AddCustomerActivity::class.java)
                command.data["name"]?.let { intent.putExtra("VOICE_NAME", it) }
                command.data["building"]?.let { intent.putExtra("VOICE_BUILDING", it) }
                command.data["room"]?.let { intent.putExtra("VOICE_ROOM", it) }
                command.data["mobile"]?.let { intent.putExtra("VOICE_MOBILE", it) }
                startActivity(intent)
            }
            
            VoiceAction.GENERATE_BILLS -> {
                startActivity(Intent(this, com.example.milkiwai.ui.billing.GenerateBillActivity::class.java))
            }
            
            VoiceAction.SHOW_PENDING -> {
                startActivity(Intent(this, PaymentReminderActivity::class.java))
            }
            
            VoiceAction.PROCUREMENT_REPORT -> {
                startActivity(Intent(this, com.example.milkiwai.ui.procurement.ProcurementActivity::class.java))
            }

            VoiceAction.SHOW_ROUTE -> {
                startActivity(Intent(this, com.example.milkiwai.ui.RouteMapActivity::class.java))
            }

            VoiceAction.MARK_DELIVERED -> {
                val customerName = command.data["customer"]
                Toast.makeText(this, "Marking $customerName as Delivered...", Toast.LENGTH_SHORT).show()
                // In a real app, we'd find the customer by name and call viewModel.markAsDelivered
                // For now, opening the delivery list is a safe fallback
                startActivity(Intent(this, com.example.milkiwai.ui.delivery.DeliveryActivity::class.java))
            }

            VoiceAction.MARK_MISSED -> {
                val customerName = command.data["customer"]
                Toast.makeText(this, "Marking $customerName as Missed...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.example.milkiwai.ui.delivery.DeliveryActivity::class.java))
            }

            VoiceAction.ADD_SUBSCRIPTION -> {
                Toast.makeText(this, "Opening Customer list to add subscription for ${command.data["customer"]}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CustomerListActivity::class.java))
            }

            VoiceAction.PAUSE_DELIVERY -> {
                Toast.makeText(this, "Opening Customer list to pause delivery for ${command.data["customer"]}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CustomerListActivity::class.java))
            }

            VoiceAction.QTY_CHANGE -> {
                Toast.makeText(this, "Opening Customer list to change quantity for ${command.data["customer"]}", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CustomerListActivity::class.java))
            }

            VoiceAction.UNKNOWN -> {
                Toast.makeText(this, "Command not recognized. Try 'Generate Bills' or 'Add customer Rahul'", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceListening()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 201 && resultCode == RESULT_OK && data != null) {
            val actionStr = data.getStringExtra("VOICE_ACTION")
            val action = VoiceAction.valueOf(actionStr ?: "UNKNOWN")
            
            val params = mutableMapOf<String, String>()
            data.extras?.keySet()?.filter { it.startsWith("DATA_") }?.forEach { key ->
                params[key.substring(5)] = data.getStringExtra(key) ?: ""
            }
            
            handleVoiceCommand(ParsedCommand(action, params))
        }
    }
}
