package com.sonapakhi.autotapper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var statusView: TextView
    private lateinit var btnImport: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.status)
        btnImport = findViewById(R.id.btnImport)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnImport.setOnClickListener { pickConfig() }
        btnStart.setOnClickListener { startTapping() }
        btnStop.setOnClickListener { stopTapping() }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private val picker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val json = contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
            if (json.isNotBlank()) {
                if (ConfigStore.save(this, json)) {
                    statusView.text = "Config imported ✓"
                } else {
                    statusView.text = "Invalid/Expired config"
                }
            }
        }
    }

    private fun pickConfig() {
        picker.launch(arrayOf("application/json", "text/plain"))
    }

    private fun startTapping() {
        if (!TapService.isEnabled(this)) {
            statusView.text = "Enable Accessibility first"
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }
        val cfg = ConfigStore.load(this)
        if (cfg == null) {
            statusView.text = "No valid config"
            return
        }
        if (!Security.verify(this, cfg.first, cfg.second)) {
            statusView.text = "Config expired or signature mismatch"
            return
        }
        TapService.startLoop(cfg.first, this)
        statusView.text = "Running…"
    }

    private fun stopTapping() {
        TapService.stopLoop()
        statusView.text = "Stopped"
    }
}
