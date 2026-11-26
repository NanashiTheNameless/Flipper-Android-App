package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder

internal object TermuxBackend {
    const val PERMISSION = "com.termux.permission.RUN_COMMAND"
    val setup = """
        mkdir -p ~/.termux
        sed -i '/^[[:space:]]*allow-external-apps[[:space:]]*=/d' ~/.termux/termux.properties
        printf '\nallow-external-apps=true\n' >> ~/.termux/termux.properties
        pkg update -y && pkg install -y python python-pip python-pillow clang make git file glibc-repo && pkg install -y glibc-runner && python -m pip install --no-cache-dir 'ufbt==0.2.6' ansi colorlog cxxheaderparser heatshrink2 pyelftools scons && pkg clean
    """.trimIndent()

    fun installed(context: Context): Boolean = runCatching {
        context.packageManager.getPackageInfo("com.termux", 0)
    }.isSuccess

    fun allowed(context: Context) = ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    fun start(context: Context, id: String, url: String, token: String, probe: Boolean = false) {
        check(installed(context)) { "Install Termux and complete builder setup first" }
        check(allowed(context)) { "Grant Flipper-Nanashi permission to run commands in Termux" }
        check(Build.SUPPORTED_ABIS.contains("arm64-v8a")) { "The Beta builder requires an ARM64 Android phone" }
        val callback = Intent(context, SourceBuildResultReceiver::class.java).apply { putExtra("job_id", id) }
        val pending = PendingIntent.getBroadcast(context, id.hashCode(), callback,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_CANCEL_CURRENT or
                if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0)
        val helper = context.assets.open("nanashi_builder.py").bufferedReader().use { it.readText() }
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/python3")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", if (probe) arrayOf("-c", helper, url, token, "--probe") else arrayOf("-c", helper, url, token))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
            putExtra("com.termux.RUN_COMMAND_COMMAND_LABEL", "Flipper-Nanashi build")
            putExtra("com.termux.RUN_COMMAND_PENDING_INTENT", pending)
        }
        ContextCompat.startForegroundService(context, intent) ?: error("Termux could not start the builder")
    }
}

class SourceBuildResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("job_id") ?: return
        val bundle = intent.getBundleExtra("result")
        val error = bundle?.getString("errmsg")?.takeIf { it.isNotBlank() }
            ?: bundle?.getString("stderr")?.takeIf { it.isNotBlank() }?.takeLast(4096)
        ComponentHolder.component<SourceComponent>().sourceJobs().termuxResult(id, error)
    }
}
