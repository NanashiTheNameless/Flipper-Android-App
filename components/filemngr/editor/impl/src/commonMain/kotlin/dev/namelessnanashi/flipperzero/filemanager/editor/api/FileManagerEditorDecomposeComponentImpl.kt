package dev.namelessnanashi.flipperzero.filemanager.editor.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItem
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.filemanager.editor.model.FileManagerEditorConfiguration
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import okio.Path

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, FileManagerEditorDecomposeComponent.Factory::class)
class FileManagerEditorDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val path: Path,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onFileChanged: (ListingItem) -> Unit,
    fileDownloadDecomposeComponentFactory: FileDownloadDecomposeComponent.Factory,
    private val editorDecomposeComponentFactory: EditorDecomposeComponent.Factory,
    private val storageProvider: FlipperStorageProvider,
    private val uploadFileDecomposeComponentFactory: UploadFileDecomposeComponent.Factory
) : FileManagerEditorDecomposeComponent<FileManagerEditorConfiguration>(),
    ComponentContext by componentContext {
    private val editorFileKeeper = instanceKeeper.getOrCreate {
        object : InstanceKeeper.Instance {
            val editorFile = storageProvider.getTemporaryFile()
            override fun onDestroy() {
                storageProvider.fileSystem.delete(editorFile)
            }
        }
    }

    override val stack: Value<ChildStack<FileManagerEditorConfiguration, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = FileManagerEditorConfiguration.serializer(),
            initialStack = { listOf(FileManagerEditorConfiguration.Download(path)) },
            handleBackButton = true,
            childFactory = { config, childContext ->
                when (config) {
                    is FileManagerEditorConfiguration.Download -> {
                        fileDownloadDecomposeComponentFactory.invoke(
                            componentContext = childContext,
                            fullPathOnFlipper = path,
                            fullPathOnDevice = editorFileKeeper.editorFile,
                            onBack = { navigation.popOr(onBack::invoke) },
                            onDownloaded = {
                                navigation.replaceCurrent(
                                    FileManagerEditorConfiguration.Editor(
                                        fullPathOnFlipper = config.fullPathOnFlipper,
                                        tempPathOnDevice = editorFileKeeper.editorFile
                                    )
                                )
                            }
                        )
                    }

                    is FileManagerEditorConfiguration.Editor -> {
                        editorDecomposeComponentFactory.invoke(
                            componentContext = componentContext,
                            fullPathOnFlipper = config.fullPathOnFlipper,
                            fullPathOnDevice = config.tempPathOnDevice,
                            onBack = { navigation.popOr(onBack::invoke) },
                            editFinishedCallback = { fullPathOnFlipper ->
                                navigation.pushNew(
                                    FileManagerEditorConfiguration.Upload(
                                        fullPathOnFlipper = fullPathOnFlipper,
                                        tempPathOnDevice = editorFileKeeper.editorFile
                                    )
                                )
                            }
                        )
                    }

                    is FileManagerEditorConfiguration.Upload -> {
                        uploadFileDecomposeComponentFactory.invoke(
                            componentContext = childContext,
                            onBack = { navigation.popOr(onBack::invoke) },
                            fullPathOnFlipper = config.fullPathOnFlipper,
                            fullPathOnDevice = config.tempPathOnDevice,
                            onFinished = {
                                navigation.popOr(onBack::invoke)
                            },
                            onProgress = onFileChanged
                        )
                    }
                }
            },
        )
}
