package dev.namelessnanashi.flipperzero.changelog.api

interface ChangelogFormatterApi {
    fun format(changelog: String): String
}
