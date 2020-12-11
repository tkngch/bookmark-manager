package tkngch.bookmarkManager.jvm.adapter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class WebpageInfo(val title: String, val url: String)

interface WebScraping {

    fun webpageInfo(url: String): WebpageInfo
}

class WebScrapingImpl : WebScraping {
    companion object {
        /* ktlint-disable max-line-length */
        private const val userAgent: String =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36"
        /* ktlint-enable max-line-length */

        private const val timeoutInMilliseconds: Int = 10_000
    }

    override fun webpageInfo(url: String): WebpageInfo {
        val doc: Document =
            Jsoup.connect(url).userAgent(userAgent).timeout(timeoutInMilliseconds).get()
        return WebpageInfo(title = doc.title(), url = doc.location())
    }
}
