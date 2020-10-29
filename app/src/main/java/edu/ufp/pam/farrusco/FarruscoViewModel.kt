package edu.ufp.pam.farrusco

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import edu.ufp.pam.farrusco.workers.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class FarruscoViewModel(app: Application) : AndroidViewModel(app) {

    internal var fileURI: Uri? = null

    internal fun setFileURI(uri: String?) {
        fileURI = uriOrNull(uri)
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private val httpReply: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            //launchAsyncDownload()
        }
    }

    fun getHttpReply(): LiveData<String> {
        return httpReply
    }

    public fun launchAsyncDownload() {
        // Do an asynchronous operation to fetch file:
        /* 1. Execute and AsynTask... deprecated, use viewModel.launch() instead. */
        Log.e(this.javaClass.simpleName,
            "launchAsyncDownload(): going to async exe http Request URI =$fileURI")
        /* 2. Create a new coroutine to move the execution off the UI thread */
        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(fileURI.toString())
                .get()
                .build()
            val response = client.newCall(request).execute()

            val responseBody = response.body?.string()
            Log.e(this.javaClass.simpleName, "launch(): response.request=" + response.request)
            Log.e(this.javaClass.simpleName, "launch(): response.message=" + response.message)
            Log.e(this.javaClass.simpleName, "launch(): response.body=$responseBody")

            //Update LiveData value with Http response
            httpReply.postValue(responseBody)
        }
    }
}
