package dev.namelessnanashi.flipperzero.filemanager.upload.impl.deeplink

import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.deeplink.model.openStream
import dev.zacsweers.metro.ContributesBinding
import okio.Source
import okio.source
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<DeeplinkContentProvider>())
class AndroidDeeplinkContentProvider @Inject constructor(
    context: Context,
) : DeeplinkContentProvider {
    private val contentResolver = context.contentResolver

    override fun source(deeplinkContent: DeeplinkContent): Source? {
        return deeplinkContent.openStream(contentResolver)?.source()
    }
}
