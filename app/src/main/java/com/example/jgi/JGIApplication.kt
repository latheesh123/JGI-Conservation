package com.example.jgi

import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.docusign.androidsdk.DSEnvironment
import com.docusign.androidsdk.DocuSign
import com.docusign.androidsdk.dsmodels.DSAppearance
import com.docusign.androidsdk.exceptions.DocuSignNotInitializedException
import com.docusign.androidsdk.util.DSMode
import com.example.jgi.utils.CommonUtils
import com.example.jgi.utils.Constants

import java.io.File

class JGIApplication : Application() {
    var portfolioADoc: File? = null
    var portfolioBDoc: File? = null
    var accreditedInvestorDoc: File? = null

    companion object {
        val TAG = JGIApplication::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        initializeDocuSign()
        portfolioADoc = CommonUtils.convertAssetToFile(this,
            Constants.PORTFOLIO_A_DOCUMENT_FILE_NAME,
            filesDir.absolutePath + "/" + Constants.PORTFOLIO_A_DOCUMENT_FILE_NAME)
        portfolioBDoc = CommonUtils.convertAssetToFile(this,
            Constants.PORTFOLIO_B_DOCUMENT_FILE_NAME,
            filesDir.absolutePath + "/" + Constants.PORTFOLIO_B_DOCUMENT_FILE_NAME)
        accreditedInvestorDoc = CommonUtils.convertAssetToFile(this,
            Constants.ACCREDITED_INVESTOR_VERIFICATION_FILE_NAME,
            filesDir.absolutePath + "/" + Constants.ACCREDITED_INVESTOR_VERIFICATION_FILE_NAME)
    }

    fun initializeDocuSign() {
        val sharedPreferences = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val integratorKey = sharedPreferences.getString(Constants.DOCUSIGN_INTEGRATOR_KEY_PREF, Constants.DOCUSIGN_INTEGRATOR_KEY)
        val clientSecret = sharedPreferences.getString(Constants.CLIENT_SECRET_KEY_PREF, Constants.CLIENT_SECRET_KEY)
        val redirectUri = sharedPreferences.getString(Constants.REDIRECT_URI_PREF, Constants.REDIRECT_URI)

        if (TextUtils.isEmpty(integratorKey)) {
            Toast.makeText(this, "Please provide Integrator Key", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(clientSecret)) {
            Toast.makeText(this, "Please provide Client Secret", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(redirectUri)) {
            Toast.makeText(this, "Please provide Redirect Uri", Toast.LENGTH_LONG).show()
            return
        }

        // DS: Initialize DocuSign instance
        try {
            DocuSign.init(
                applicationContext,
                integratorKey!!,
                clientSecret,
                redirectUri,
                DSMode.DEBUG
            ).setEnvironment(DSEnvironment.DEMO_ENVIRONMENT)
        } catch (exception: DocuSignNotInitializedException) {
            Log.d(TAG, exception.message!!)
            Toast.makeText(applicationContext, "Failed to Initialize DocuSign. " + exception.message, Toast.LENGTH_LONG).show()
            return
        }
        // DS: Set branding for your app
        val appearanceDelegate = DocuSign.getInstance().getAppearanceDelegate()
        val appearance = DSAppearance.Builder()
            .setActionBarColor(ColorDrawable(resources.getColor(R.color.colorPrimary)))
            .setStatusBarColor(ColorDrawable(resources.getColor(R.color.colorPrimaryDark)))
            .setActionBarTitleTextColor(ColorDrawable(resources.getColor(android.R.color.white)))
            .setBottomToolbarButtonColor(ColorDrawable(resources.getColor(R.color.colorPrimary)))
            .setBottomToolbarButtonTextColor(ColorDrawable(resources.getColor(android.R.color.white)))
            .setBottomToolbarDocuSignImageVisibility(true)
            .build()
        appearanceDelegate.appearance = appearance
    }
}