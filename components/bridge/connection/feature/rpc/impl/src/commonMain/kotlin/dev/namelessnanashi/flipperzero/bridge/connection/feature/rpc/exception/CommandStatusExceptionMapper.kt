package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.exception

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcAppCantStartException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcAppCmdErrorException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcAppNotRunningException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcAppSystemLockedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcBusyException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcContinuousCommandInterruptedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcDecodeException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcGeneralException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcGpioModeIncorrectException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcGpioUnknownPinModeException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcInvalidParametersException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcNotImplementedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageAlreadyOpenException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageDeniedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageDirNotEmptyException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageExistException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageInternalException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageInvalidNameException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageInvalidParameterException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageNotExistException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageNotImplementedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageNotReadyException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcVirtualDisplayAlreadyStartedException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcVirtualDisplayNotStartedException
import com.flipperdevices.protobuf.CommandStatus
import com.flipperdevices.protobuf.Main

fun Main.resultOrError(): Result<Main> {
    val exception = asException()

    return if (exception == null) {
        Result.success(this)
    } else {
        Result.failure(exception)
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Main.asException(): FRpcException? = when (command_status) {
    CommandStatus.ERROR_APP_CANT_START -> FRpcAppCantStartException(this)
    CommandStatus.ERROR_APP_CMD_ERROR -> FRpcAppCmdErrorException(this)
    CommandStatus.ERROR_APP_NOT_RUNNING -> FRpcAppNotRunningException(this)
    CommandStatus.ERROR_APP_SYSTEM_LOCKED -> FRpcAppSystemLockedException(this)
    CommandStatus.ERROR_BUSY -> FRpcBusyException(this)
    CommandStatus.ERROR_CONTINUOUS_COMMAND_INTERRUPTED ->
        FRpcContinuousCommandInterruptedException(this)

    CommandStatus.ERROR_DECODE -> FRpcDecodeException(this)
    CommandStatus.ERROR_GPIO_MODE_INCORRECT -> FRpcGpioModeIncorrectException(this)
    CommandStatus.ERROR_GPIO_UNKNOWN_PIN_MODE ->
        FRpcGpioUnknownPinModeException(this)

    CommandStatus.ERROR_INVALID_PARAMETERS -> FRpcInvalidParametersException(this)
    CommandStatus.ERROR_NOT_IMPLEMENTED -> FRpcNotImplementedException(this)
    CommandStatus.ERROR_STORAGE_ALREADY_OPEN ->
        FRpcStorageAlreadyOpenException(this)

    CommandStatus.ERROR_STORAGE_DENIED -> FRpcStorageDeniedException(this)
    CommandStatus.ERROR_STORAGE_DIR_NOT_EMPTY ->
        FRpcStorageDirNotEmptyException(this)

    CommandStatus.ERROR_STORAGE_EXIST -> FRpcStorageExistException(this)
    CommandStatus.ERROR_STORAGE_INTERNAL -> FRpcStorageInternalException(this)
    CommandStatus.ERROR_STORAGE_INVALID_NAME ->
        FRpcStorageInvalidNameException(this)

    CommandStatus.ERROR_STORAGE_INVALID_PARAMETER ->
        FRpcStorageInvalidParameterException(this)

    CommandStatus.ERROR_STORAGE_NOT_EXIST -> FRpcStorageNotExistException(this)
    CommandStatus.ERROR_STORAGE_NOT_IMPLEMENTED ->
        FRpcStorageNotImplementedException(this)

    CommandStatus.ERROR_STORAGE_NOT_READY -> FRpcStorageNotReadyException(this)
    CommandStatus.ERROR_VIRTUAL_DISPLAY_ALREADY_STARTED ->
        FRpcVirtualDisplayAlreadyStartedException(this)

    CommandStatus.ERROR_VIRTUAL_DISPLAY_NOT_STARTED ->
        FRpcVirtualDisplayNotStartedException(this)

    is CommandStatus.Unrecognized,
    CommandStatus.ERROR -> FRpcGeneralException(this)

    CommandStatus.OK -> null
}
