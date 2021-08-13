package com.example.jgi.ui.signin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.fragment.findNavController
import com.docusign.androidsdk.DocuSign
import com.docusign.androidsdk.dsmodels.DSUser
import com.docusign.androidsdk.exceptions.DSAuthenticationException
import com.docusign.androidsdk.exceptions.DocuSignNotInitializedException
import com.docusign.androidsdk.listeners.DSAuthenticationListener
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.security.OAuthConfiguration
import com.example.jgi.BaseFragment
import com.example.jgi.R
import com.example.jgi.utils.Constants

class SignInFragment : BaseFragment() {

    private lateinit var signInButton: Button

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var viewModel: SignInViewModel
    override fun onBackPressed(): Boolean {
        findNavController().popBackStack()
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sign_in_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInButton = view.findViewById(R.id.sign_in_button)


        signInButton.setOnClickListener {
            try {
                val authDelegate = DocuSign.getInstance().getAuthenticationDelegate()
                // DS: Login authentication using OAuth
                authDelegate.login(
                    Constants.LOGIN_REQUEST_CODE, requireContext(),
                    object : DSAuthenticationListener {
                        override fun onSuccess(@NonNull user: DSUser) {


                            findNavController().navigate(R.id.destination_showitems)
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
}