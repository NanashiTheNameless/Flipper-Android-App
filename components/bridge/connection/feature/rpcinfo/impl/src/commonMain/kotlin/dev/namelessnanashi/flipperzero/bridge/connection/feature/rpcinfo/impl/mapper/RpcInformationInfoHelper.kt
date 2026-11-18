package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.mapper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.RadioStackType
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.ktx.jre.isNotNull
import dev.namelessnanashi.flipperzero.core.ktx.jre.titlecaseFirstCharIfItIsLowercase

object RpcInformationInfoHelper {
    // softwareRevision Branch.Commit
    fun softwareRevision(firmwareCommit: String?, firmwareBranch: String?): String? {
        return if (isNotNull(firmwareCommit, firmwareBranch)) {
            val firmwareBranchCapitalize = firmwareBranch?.titlecaseFirstCharIfItIsLowercase()
            "$firmwareBranchCapitalize $firmwareCommit"
        } else {
            null
        }
    }

    // protobuf Major.Minor
    fun protobufVersion(protobufMajor: String?, protobufMinor: String?): SemVer? {
        return if (isNotNull(protobufMajor, protobufMinor)) {
            SemVer(
                protobufMajor?.toIntOrNull() ?: 0,
                protobufMinor?.toIntOrNull() ?: 0
            )
        } else {
            null
        }
    }

    // deviceInfo Major.Minor
    fun deviceInfoVersion(deviceInfoMajor: String?, deviceInfoMinor: String?): SemVer? {
        return if (isNotNull(deviceInfoMajor, deviceInfoMinor)) {
            SemVer(
                deviceInfoMajor?.toIntOrNull() ?: 0,
                deviceInfoMinor?.toIntOrNull() ?: 0
            )
        } else {
            null
        }
    }

    // radio Major.Minor.Sub
    fun radioFirmware(
        radioMajor: String?,
        radioMinor: String?,
        radioSub: String?,
    ): String? {
        return if (isNotNull(radioMajor, radioMinor, radioSub)) {
            "$radioMajor.$radioMinor.$radioSub"
        } else {
            null
        }
    }

    fun radioType(radioType: String?): RadioStackType? {
        return RadioStackType.find(radioType)
    }
}
