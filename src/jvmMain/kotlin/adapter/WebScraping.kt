package tkngch.bookmarkManager.jvm.adapter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

interface WebScraping {

    fun title(url: String): String
}

class WebScrapingImpl : WebScraping {
    companion object {
        /* ktlint-disable max-line-length */
        private const val userAgent: String =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36"
        /* ktlint-enable max-line-length */

        private const val timeoutInMilliseconds: Int = 10_000
    }

    override fun title(url: String): String {
        val doc: Document =
            Jsoup.connect(url).userAgent(userAgent).timeout(timeoutInMilliseconds).get()
        return doc.title()
    }
}
