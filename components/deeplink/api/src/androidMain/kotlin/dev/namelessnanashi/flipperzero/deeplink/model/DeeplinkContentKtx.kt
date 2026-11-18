package dev.namelessnanashi.flipperzero.deeplink.model

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent.ExternalUri
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent.FFFContent
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent.FFFCryptoContent
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent.InternalStorageFile
import java.io.InputStream

val DeeplinkContent.ExternalUri.uri
    get() = Uri.parse(uriString)

fun DeeplinkContent.openStream(contentResolver: ContentResolver): InputStream? {
    return when (this) {
        is ExternalUri -> contentResolver.openInputStream(uri)
        is InternalStorageFile -> file.inputStream()
        is FFFContent -> flipperFileFormat.openStream()
        is FFFCryptoContent -> null
    }
}

fun DeeplinkContent.cleanUp(contentResolver: ContentResolver) {
    when (this) {
        is ExternalUri ->
            contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

        is InternalStorageFile -> file.delete()
        is FFFContent -> {} // Nothing
        is FFFCryptoContent -> {} // Nothing
    }
}
