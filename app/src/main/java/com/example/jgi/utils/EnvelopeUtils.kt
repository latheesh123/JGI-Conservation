package com.example.jgi.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.docusign.androidsdk.DocuSign
import com.docusign.androidsdk.dsmodels.DSEnvelope
import com.docusign.androidsdk.dsmodels.DSDocument
import com.docusign.androidsdk.dsmodels.DSTab
import com.docusign.androidsdk.dsmodels.DSEnvelopeRecipient
import com.docusign.androidsdk.dsmodels.DSRecipientType
import com.docusign.androidsdk.dsmodels.DSTabType
import com.docusign.androidsdk.dsmodels.DSTextCustomField
import com.docusign.androidsdk.dsmodels.DSRecipientDefault
import com.docusign.androidsdk.dsmodels.DSEnvelopeDefaults
import com.docusign.androidsdk.dsmodels.DSListCustomField
import com.docusign.androidsdk.dsmodels.DSCustomFields
import com.docusign.androidsdk.dsmodels.DSEnvelopeDefaultsUniqueRecipientSelectorType
import com.docusign.androidsdk.exceptions.DSAuthenticationException
import com.docusign.androidsdk.exceptions.DSEnvelopeException
import com.docusign.androidsdk.exceptions.DocuSignNotInitializedException
import com.example.jgi.model.User

import com.google.gson.Gson
import java.io.File
import java.net.URI
import java.util.ArrayList

object EnvelopeUtils {

    private val TAG = EnvelopeUtils::class.java.simpleName

    fun buildEnvelope(
        context: Context,
        file: File,
        clientPref: String?,
        mainUser: User?,
        listusers: ArrayList<String>
    ): DSEnvelope? {

        val sharedPreferences =
            context.getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        if (clientPref == null) {
            Toast.makeText(context, "Client Preference not available", Toast.LENGTH_LONG).show()
            return null
        }

        try {
            val authenticationDelegate = DocuSign.getInstance().getAuthenticationDelegate()
            val user = authenticationDelegate.getLoggedInUser(context)
            val fileURI: URI = file.toURI()
            val documents = mutableListOf<DSDocument>()
            val document = DSDocument.Builder()
                .documentId(1)
                .uri(fileURI.toString())
                .name("Conservational aggrement")
                .build()
            documents.add(document)
            val tabs = mainUser?.let { createInvestmentAgreementTabs(it )}
            val recipients = mutableListOf<DSEnvelopeRecipient>()
            tabs?.let {
                DSEnvelopeRecipient.Builder()
                    .recipientId(1)
                    .routingOrder(1)
                    .hostName(user.name)
                    .hostEmail(user.email)
                    .signerName(mainUser.email.toString())
                    .signerEmail(mainUser.email.toString())
                    .type(DSRecipientType.IN_PERSON_SIGNER)
                    .tabs(it)
                    .build()
            }?.let {
                recipients.add(
                    it
                )
            }


            var value = 0


            recipients.add(
                DSEnvelopeRecipient.Builder()
                    .recipientId(2 + value.toLong())
                    .routingOrder(2 + value)
                    .signerName("Jack Doe") // if someone needs a signed copy, their name here
                    .signerEmail("j.d@gmail.com") // if someone needs a signed copy, their email here
                    .type(DSRecipientType.CARBON_COPY)
                    .build()
            )
            // DS: Envelope creation
            return DSEnvelope.Builder()
                .envelopeName("Conversation")
                .documents(documents)
                .recipients(recipients)
                .textCustomFields( // this is for free-form metadata
                    getTextCustomFields()!!
                )
                .build()
        } catch (exception: DSEnvelopeException) {
            Log.e(TAG, exception.message!!)
        } catch (exception: DocuSignNotInitializedException) {
            Log.e(TAG, exception.message!!)
        } catch (exception: DSAuthenticationException) {
            Log.e(TAG, exception.message!!)
        }
        return null
    }


    private fun createInvestmentAgreementTabs(user: User): List<DSTab> {
        val tabs = mutableListOf<DSTab>()
        tabs.add(
            DSTab.Builder()
                .documentId(1)
                .recipientId(1)
                .pageNumber(1)
                .xPosition(464)
                .yPosition(71)
                .type(DSTabType.TEXT)
                .value("08/13/2021")
                .optional(true)
                .build()
        )                      //Date
        tabs.add(
            DSTab.Builder()
                .documentId(1)
                .recipientId(1)
                .pageNumber(1)
                .xPosition(140)
                .yPosition(331)
                .type(DSTabType.TEXT)
                .value(user.versionNumber.toString())
                .optional(true)
                .build()
        )                        // vers

        tabs.add(
            DSTab.Builder()
                .documentId(1)
                .recipientId(1)
                .pageNumber(1)
                .xPosition(123)
                .yPosition(380)
                .type(DSTabType.TEXT)
                .value(user.comments.toString())
                .optional(true)
                .build()
        )                      // Address line 3
        tabs.add(
            DSTab.Builder()
                .documentId(1)
                .recipientId(1)
                .pageNumber(1)
                .xPosition(154)
                .yPosition(417)
                .type(DSTabType.TEXT).value(user.url)
                .optional(true)
                .build()
        )
        tabs.add(
            DSTab.Builder()
                .documentId(1)
                .recipientId(1)
                .pageNumber(1)
                .xPosition(485)
                .yPosition(369)
                .type(DSTabType.SIGNATURE)
                .build()
        )                        // Signature

        // Name of the applicant
        return tabs
    }

    private fun getTextCustomFields(): List<DSTextCustomField>? {
        val textCustomField1: DSTextCustomField
        val textCustomFields: MutableList<DSTextCustomField> = ArrayList()
        try {
            textCustomField1 = DSTextCustomField.Builder()
                .fieldId(123)
                .name("Phone number")
                .value("361878172")
                .build()
            textCustomFields.add(textCustomField1)
        } catch (exception: DSEnvelopeException) {
            Log.e(TAG, exception.message!!)
        }
        return textCustomFields
    }

}
