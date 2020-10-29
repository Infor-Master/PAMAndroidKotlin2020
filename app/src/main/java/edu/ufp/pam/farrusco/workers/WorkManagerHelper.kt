package edu.ufp.pam.farrusco.workers

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import edu.ufp.pam.farrusco.FarruscoViewModel

class WorkManagerHelper(app: Application) {

    internal var fileURI: Uri? = null
    private val workManager = WorkManager.getInstance(app)
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    init {
        // Init block: Whenever current Worker changes WorkInfo we live listen to changes.
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_WORKER_DOWNLOAD_OUTPUT)
    }

    fun getOutputWorkInfos(): LiveData<List<WorkInfo>>{
        return outputWorkInfos
    }

    companion object {
        const val KEY_WORKER_DOWNLOAD_URL = "edu.ufp.pam.examples.farrusco.KEY_WORKER_DOWNLOAD_URL"
        const val TAG_WORKER_DOWNLOAD_OUTPUT = "edu.ufp.pam.examples.farrusco.TAG_WORKER_DOWNLOAD_OUTPUT"
    }

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

    /**
     * Create input data bundle which includes the URI to operate on
     * @return Data which contains the Uri as String
     */
    private fun createInputDataForUri(): Data {
        //Create Data object
        val builder = Data.Builder()
        fileURI?.let {
            builder.putString(KEY_WORKER_DOWNLOAD_URL, fileURI.toString())
        }
        return builder.build()
    }

    /**
     * Create DownloadWorker to sync execute WorkRequest.
     */
    public fun launchDownloadWorker() {
        Log.e(
            this.javaClass.simpleName,
            "launchDownloadWorker(): going to enqueue DownloadWorker to GET http file..."
        )
        //Create a OneTimeWorkRequest with some InputData & associated Tag
        val downloadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(createInputDataForUri())
                .addTag(TAG_WORKER_DOWNLOAD_OUTPUT)
                .build()
        //Enqueue WorkRequest to be executed async
        val enqueue = workManager.enqueue(downloadWorkRequest)
        Log.e(
            this.javaClass.simpleName,
            "launchDownloadWorker(): enqueue.result = ${enqueue.result.get()}"
        )
    }
}