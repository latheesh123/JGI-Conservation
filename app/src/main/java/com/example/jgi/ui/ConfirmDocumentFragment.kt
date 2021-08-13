package com.example.jgi.ui

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.docusign.androidsdk.DocuSign
import com.docusign.androidsdk.dsmodels.DSUser
import com.docusign.androidsdk.exceptions.DSAuthenticationException
import com.docusign.androidsdk.exceptions.DSEnvelopeException
import com.docusign.androidsdk.exceptions.DocuSignNotInitializedException
import com.docusign.androidsdk.listeners.DSAuthenticationListener
import com.docusign.androidsdk.listeners.DSComposeAndSendEnvelopeListener
import com.example.jgi.BaseFragment
import com.example.jgi.R
import com.example.jgi.model.User
import com.example.jgi.utils.CommonUtils
import com.example.jgi.utils.Constants
import com.example.jgi.utils.EnvelopeUtils
import com.example.jgi.utils.Utils
import com.example.jgi.viewmodel.SigningViewModel
import java.io.File

class ConfirmDocumentFragment : BaseFragment() {

    private lateinit var sendButton: AppCompatButton
    private lateinit var signingViewModel: SigningViewModel

    private lateinit var emailAddress: EditText
    private lateinit var versionName: EditText
    private lateinit var comments: EditText
    private lateinit var list: ArrayList<String>
    private lateinit var emailAddresstext: String


    companion object {
        fun newInstance() = ConfirmDocumentFragment()
    }

    private lateinit var viewModel: ConfirmDocumentViewModel
    override fun onBackPressed(): Boolean {
        findNavController().popBackStack()
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.confirm_document_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendButton = view.findViewById(R.id.show_documents)
        signingViewModel = SigningViewModel()

        emailAddress = view.findViewById(R.id.email_address)
        versionName = view.findViewById(R.id.versionTextName)
        comments = view.findViewById(R.id.commentTextName)
        list = ArrayList()
        sendButton.setOnClickListener {

            try {
                val authDelegate = DocuSign.getInstance().getAuthenticationDelegate()
                // DS: Login authentication using OAuth
                authDelegate.login(
                    Constants.LOGIN_REQUEST_CODE, requireContext(),
                    object : DSAuthenticationListener {
                        override fun onSuccess(@NonNull user: DSUser) {

                            createEnvelope(requireContext(), "client_a_details")
                        }

                        override fun onError(@NonNull exception: DSAuthenticationException) {
                            Log.d("TAG", exception.message!!)
                            Toast.makeText(
                                requireContext(),
                                "Failed to Login to DocuSign",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )

            } catch (exception: DocuSignNotInitializedException) {
                Log.d("TAG", exception.message!!)
                Toast.makeText(requireContext(), "Failed to Login to DocuSign", Toast.LENGTH_LONG)
                    .show()
            }

        }

    }

    private fun createEnvelope(context: Context, clientPref: String?) {

        // val giftDocument = (requireActivity().application as ShareGiftApplication).portfolioADoc
        val giftDocument = File(context.filesDir, "conservation.pdf")

        if (giftDocument == null) {
            Log.e("TAG", "Unable to retrieve document")
            Toast.makeText(context, "Unable to retrieve document", Toast.LENGTH_LONG).show()
            return
        }
        val user = User(
            "1",
            "testUser",
          "latheeshv90@gmail.com",
            "56712536012",
            "new",
            versionName.text.toString(),
            comments.text.toString(),
            Utils.service_url
        )

        emailAddresstext = emailAddress.text.toString()
        if (emailAddresstext.contains(",")) {
            list = emailAddresstext.split(",").toTypedArray().toList() as ArrayList<String>
        } else {
            list.add(emailAddresstext)
        }


        val envelope =
            giftDocument.let { EnvelopeUtils.buildEnvelope(context, it, clientPref, user, list) }

        if (envelope == null) {
            Log.e("OverviewFragment.TAG", "Unable to create envelope")
            Toast.makeText(context, "Unable to create envelope", Toast.LENGTH_LONG).show()
            return
        }
        val envelopeDelegate = DocuSign.getInstance().getEnvelopeDelegate()
        envelopeDelegate.composeAndSendEnvelope(envelope, object :
            DSComposeAndSendEnvelopeListener {

            override fun onSuccess(envelopeId: String, isEnvelopeSent: Boolean) {
                if (CommonUtils.isNetworkAvailable(context)) {
                    toggleProgressBar(true)
                    signingViewModel.signOnline(context, envelopeId)
                } else {
                    toggleProgressBar(false)
                    signingViewModel.signOffline(context, envelopeId)
                }
            }

            override fun onError(exception: DSEnvelopeException) {
                //
                //
                // Log.e(OverviewFragment.TAG, exception.message!!)
                Toast.makeText(context, exception.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun toggleProgressBar(isBusy: Boolean) {
        activity?.findViewById<ProgressBar>(R.id.envelopes_progress_bar)?.visibility = if (isBusy) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ConfirmDocumentViewModel::class.java)
        // TODO: Use the ViewModel
    }

}