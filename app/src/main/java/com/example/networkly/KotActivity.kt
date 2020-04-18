package com.example.networkly

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.networkly.databinding.ActivityKotBinding
import com.google.android.material.snackbar.Snackbar
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException

class KotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKotBinding
    private lateinit var wm: WifiManager

    val LOCATION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKotBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Log.d("IP ADDRESS", "Getting IPv6")

        checkLocationPermission()

        binding.textViewIPv4.text = getIPv4()
        binding.textViewIPv6.text = getIPv6()
    }

    private fun getSSID(): String? = wm.connectionInfo.ssid

    private fun getIPv4(): String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

    private fun getIPv6(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    Log.d("IP ADDRESS", "ip1 : $inetAddress")
                    Log.d("IP ADDRESS", "ip2 : ${inetAddress.hostAddress}")
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet6Address) {
                        Log.d("IP ADDRESS", "Found something")
                        val ip = inetAddress.hostAddress
                        return ip.split("%")[0]
                    }
                }
            }

        } catch (e: SocketException) {
            Log.e("IP ADDRESS", e.message.toString())
        }
        return null
    }


    private fun checkLocationPermission() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                        .setMessage("Location permission is required from Android 8 onwards to obtain SSID")
                        .setTitle("Permission required")
                        .setPositiveButton("OK") { _, _ -> requestLocationPermission() }
                val dialog = builder.create()
                dialog.show()
            } else {
                requestLocationPermission()
            }
        } else {
            binding.textViewSSID.text = getSSID()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showErrorSnackbar()
            } else {
                binding.textViewSSID.text = getSSID()
            }
        }
    }

    private fun showErrorSnackbar() {
        val snackbar: Snackbar = Snackbar.make(binding.root, "Location permission is required to get SSID!", Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(resources.getColor(R.color.error_red))
        snackbar.show()
    }

}
