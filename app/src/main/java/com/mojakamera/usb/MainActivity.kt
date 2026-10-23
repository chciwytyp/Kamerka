package com.mojakamera.usb

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.serenegiant.usb.USBMonitor
import com.serenegiant.usb.UVCCamera
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var usbMonitor: USBMonitor
    private var uvcCamera: UVCCamera? = null
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tv_status)
        val btnCapture = findViewById<Button>(R.id.btn_capture)
        val btnGallery = findViewById<Button>(R.id.btn_gallery)

        usbMonitor = USBMonitor(this, object : USBMonitor.OnDeviceConnectListener {
            override fun onAttach(device: UsbDevice?) {
                usbMonitor.requestPermission(device)
            }

            override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?, createNew: Boolean) {
                uvcCamera = UVCCamera().apply {
                    open(ctrlBlock)
                    startPreview()
                }
                tvStatus.text = "Połączono z kamerą USB"
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
                uvcCamera?.close()
                uvcCamera = null
                tvStatus.text = "Rozłączono kamerę USB"
            }

            override fun onDettach(device: UsbDevice?) {
                tvStatus.text = "Kamera odłączona"
            }

            override fun Cancel(device: UsbDevice?) {}
        })

        btnCapture.setOnClickListener {
            if (uvcCamera != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "IMG_$timeStamp.jpg"
                val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                val imageFile = File(storageDir, fileName)

                uvcCamera?.captureStill(imageFile.absolutePath) {
                    runOnUiThread {
                        tvStatus.text = "Zdjęcie zostało zapisane pod nazwą: $fileName"
                        Toast.makeText(this@MainActivity, "Zapisano: $fileName", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Kamera USB nie jest podłączona!", Toast.LENGTH_SHORT).show()
            }
        }

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        usbMonitor.register()
    }

    override fun onStop() {
        usbMonitor.unregister()
        super.onStop()
    }
}
