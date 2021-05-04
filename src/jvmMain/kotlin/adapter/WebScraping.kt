package tkngch.bookmarkManager.jvm.adapter

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class WebpageInfo(val title: String, val url: String)

interface WebScraping {

    fun webpageInfo(url: String): WebpageInfo
}

class WebScrapingImpl : WebScraping {
    companion object {
        // Use the user-agent of well-known web-crawler, to skip the cookie consent page. Here, we
        // use the user-agent of Googlebot, taken from
        // https://developers.google.com/search/docs/advanced/crawling/overview-google-crawlers
        private const val userAgent: String =
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"

        private const val timeoutInMilliseconds: Int = 10_000
    }

    override fun webpageInfo(url: String): WebpageInfo {
        val doc: Document =
            Jsoup.connect(url).userAgent(userAgent).timeout(timeoutInMilliseconds).get()
        return WebpageInfo(title = doc.title(), url = doc.location())
    }
}
