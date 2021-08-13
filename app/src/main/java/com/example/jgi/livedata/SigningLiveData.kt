package com.example.jfi.livedata

import com.docusign.androidsdk.exceptions.DSSigningException

data class SignOfflineModel(val status: Status, val exception: DSSigningException?)
data class SignOnlineModel(val status: Status, val exception: DSSigningException?)
