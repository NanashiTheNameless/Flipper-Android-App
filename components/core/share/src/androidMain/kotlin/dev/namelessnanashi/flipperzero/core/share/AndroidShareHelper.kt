package dev.namelessnanashi.flipperzero.core.share

import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.createClearNewFileWithMkDirs
import dev.zacsweers.metro.ContributesBinding
import okio.Path.Companion.toOkioPath
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<PlatformShareHelper>())
class AndroidShareHelper @Inject constructor(
    private val context: Context
) : PlatformShareHelper {

    override fun provideSharableFile(fileName: String): PlatformSharableFile {
        val sharableFile = SharableFile(context, fileName)
        sharableFile.createClearNewFileWithMkDirs()
        return PlatformSharableFile(sharableFile.toOkioPath())
    }

    override fun shareFile(file: PlatformSharableFile, title: String) {
        ShareHelper.shareFile(
            context = context,
            file = file,
            text = title
        )
    }
}
