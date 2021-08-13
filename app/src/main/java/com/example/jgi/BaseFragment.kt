package com.example.jgi

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class
BaseFragment: Fragment() {
    private var alertDialog: AlertDialog? = null
    protected var backHandlerDelegate: IBackHandlerDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity !is IBackHandlerDelegate) {
            throw ClassCastException("Hosting activity must implement IBackHandlerDelegate")
        } else {
            backHandlerDelegate = activity as IBackHandlerDelegate?
        }
    }

    override fun onStart() {
        super.onStart()
        backHandlerDelegate?.setCurrentFragment(this)
    }

    fun showMessageDialog(message: String,
                          title: String = getString(R.string.app_name),
                          positiveBtnTitle: String = "OK",
                          actionBlock: () -> Unit = {}) {
        alertDialog?.dismiss()
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positiveBtnTitle) { _, _ ->
                actionBlock()
                alertDialog?.dismiss()
            }
        alertDialog = builder.create()
        alertDialog?.show()
    }

    fun showExceptionMessageDialog(message: String,
                                   title: String = getString(R.string.app_name),
                                   positiveBtnTitle: String = "OK",
                                   actionBlock: () -> Unit = {}) {
        alertDialog?.dismiss()
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positiveBtnTitle) { _, _ ->
                actionBlock()
                alertDialog?.dismiss()
            }
        alertDialog = builder.create()
        alertDialog?.show()
    }


    fun isPermissionGrantedFor(permission: String, activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = ContextCompat.checkSelfPermission(activity, permission)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    @Throws(Exception::class)
    fun requestPermissionFor(permissionRequestCode: Int, fragment: Fragment) {
        try {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                permissionRequestCode
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    abstract fun onBackPressed(): Boolean


}