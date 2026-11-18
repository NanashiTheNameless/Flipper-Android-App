package dev.namelessnanashi.flipperzero.keyparser.impl.parsers.url

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import java.net.URL
import java.net.URLEncoder

class FFFUrlEncoder : LogTagProvider {
    override val TAG = "FFFUrlEncoder"

    fun keyToUri(path: FlipperFilePath, fff: FlipperFileFormat): URL {
        val params = listOf(QUERY_KEY_PATH to path.pathToKey).plus(fff.orderedDict)
        val query = encodeQuery(params)
        return URL(
            PREFFERED_SCHEME,
            PREFFERED_HOST,
            "$PATH_FOR_FFF_LIHK#$query"
        )
    }

    fun encodeQuery(params: List<Pair<String, String>>): String {
        return params
            .filterNot { it.first.isBlank() || it.second.isBlank() }
            .joinToString(QUERY_DELIMITED_CHAR) {
                val field = URLEncoder.encode(it.first.trim(), QUERY_VALUE_CHARSET)
                val value = URLEncoder.encode(it.second.trim(), QUERY_VALUE_CHARSET)
                    .replace("%2F", "/") // We want safe / for readability
                "$field$QUERY_VALUE_DELIMITED_CHAR$value"
            }
    }
}
