package com.yog.sangeet

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat.getSystemService
import kotlin.random.Random


object DownloadUtil {
    fun download(songName:String,url: String, downloadManager: DownloadManager) {
        val uri = Uri.parse(url)

        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(
            DownloadManager.Request.NETWORK_WIFI or
                    DownloadManager.Request.NETWORK_MOBILE
        )

        // set title and description
        request.setTitle(songName)
        //request.setDescription("Downloading Song")

        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

//set the local destination for download file to a path within the application's external files directory

//set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$songName.mp3"
        )
        request.setMimeType("*/*")
        downloadManager.enqueue(request)
    }
}