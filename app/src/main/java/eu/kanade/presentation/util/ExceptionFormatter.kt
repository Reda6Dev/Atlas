package eu.kanade.presentation.util

import android.content.Context
import eu.kanade.tachiyomi.network.HttpException
import eu.kanade.tachiyomi.util.system.isOnline
import kotlinx.coroutines.TimeoutCancellationException
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.data.source.NoResultsException
import tachiyomi.domain.source.model.SourceNotInstalledException
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateException
import javax.net.ssl.SSLException

data class FormattedError(
    val message: String,
    val detail: String? = null,
) {
    fun asSingleText(): String = if (detail.isNullOrBlank()) message else "$message\n$detail"
}

context(context: Context)
val Throwable.formattedMessage: String
    get() = toFormattedError(context).asSingleText()

fun Throwable.toFormattedError(context: Context): FormattedError {
    val chain = causeChain()

    if (chain.any { it is NoResultsException }) {
        return FormattedError(context.stringResource(MR.strings.no_results_found))
    }

    if (chain.any { it is SourceNotInstalledException }) {
        return FormattedError(context.stringResource(MR.strings.loader_not_implemented_error))
    }

    val httpCode = chain.firstNotNullOfOrNull { (it as? HttpException)?.code }
        ?: chain.firstNotNullOfOrNull { extractHttpCode(it.message) }

    val cfFailureText = context.stringResource(MR.strings.information_cloudflare_bypass_failure)
    val isCloudflare = chain.any {
        it::class.simpleName == "CloudflareBypassException" ||
            it.message?.contains(cfFailureText, ignoreCase = true) == true ||
            it.message?.contains("cloudflare", ignoreCase = true) == true
    }
    if (isCloudflare) {
        val detail = httpCode?.let { "Cloudflare · HTTP $it" } ?: "Cloudflare"
        return FormattedError(
            message = context.stringResource(SYMR.strings.error_cloudflare),
            detail = detail,
        )
    }

    if (httpCode != null) {
        val messageRes = when (httpCode) {
            403 -> SYMR.strings.error_http_403
            404 -> SYMR.strings.error_http_404
            429 -> SYMR.strings.error_http_429
            503 -> SYMR.strings.error_http_503
            in 500..599 -> SYMR.strings.error_http_5xx
            else -> SYMR.strings.error_http_other
        }
        return FormattedError(
            message = context.stringResource(messageRes),
            detail = "HTTP $httpCode",
        )
    }

    if (!context.isOnline() && chain.any { it is IOException }) {
        return FormattedError(context.stringResource(MR.strings.exception_offline))
    }

    val unknownHost = chain.firstNotNullOfOrNull { it as? UnknownHostException }
    if (unknownHost != null) {
        val host = unknownHostRegex.find(unknownHost.message.orEmpty())
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?: sanitizeDetail(unknownHost.message)
        return FormattedError(
            message = context.stringResource(SYMR.strings.error_unknown_host),
            detail = host,
        )
    }

    val isTimeout = chain.any {
        it is SocketTimeoutException ||
            it is TimeoutCancellationException ||
            (it is InterruptedIOException && it.message?.contains("timeout", ignoreCase = true) == true) ||
            (
                it is IOException &&
                    (
                        it.message?.contains("timeout", ignoreCase = true) == true ||
                            it.message?.contains("timed out", ignoreCase = true) == true
                        )
                )
    }
    if (isTimeout) {
        return FormattedError(
            message = context.stringResource(SYMR.strings.error_timeout),
            detail = "Timeout",
        )
    }

    val isSsl = chain.any {
        it is SSLException || it is CertPathValidatorException || it is CertificateException
    }
    if (isSsl) {
        return FormattedError(
            message = context.stringResource(SYMR.strings.error_ssl),
            detail = "SSL",
        )
    }

    val connectionError = chain.firstOrNull {
        it is ConnectException || it is SocketException || it is EOFException || it is ProtocolException
    }
    if (connectionError != null) {
        return FormattedError(
            message = context.stringResource(SYMR.strings.error_connection_failed),
            detail = sanitizeDetail(connectionError.message),
        )
    }

    return FormattedError(
        message = context.stringResource(SYMR.strings.error_generic),
        detail = sanitizeDetail(message ?: chain.firstNotNullOfOrNull { it.message }),
    )
}

private fun Throwable.causeChain(maxDepth: Int = 5): List<Throwable> {
    val result = mutableListOf<Throwable>()
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < maxDepth && current !in result) {
        result.add(current)
        current = current.cause
        depth++
    }
    return result
}

private fun extractHttpCode(message: String?): Int? {
    if (message.isNullOrBlank()) return null
    return httpErrorRegex.find(message)?.groupValues?.getOrNull(1)?.toIntOrNull()
}

private fun sanitizeDetail(raw: String?): String? {
    val line = raw
        ?.lineSequence()
        ?.map { it.trim() }
        ?.firstOrNull { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
        ?: return null
    return if (line.length > 80) "${line.take(79)}…" else line
}

private val httpErrorRegex = Regex("""HTTP\s+error\s+(\d{3})""", RegexOption.IGNORE_CASE)
private val unknownHostRegex = Regex("""Unable to resolve host "([^"]+)"""")
