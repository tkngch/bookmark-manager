package tkngch.bookmarkManager.jvm.domain

import org.jsoup.Jsoup
import tkngch.bookmarkManager.common.model.URL
import java.io.IOException

data class WebpageInfo(val title: String, val url: String) {

    companion object {
        fun make(url: URL): WebpageInfo {
            // Use the user-agent of well-known web-crawler, to skip the cookie consent page. Here, we
            // use the user-agent of Googlebot, taken from
            // https://developers.google.com/search/docs/advanced/crawling/overview-google-crawlers
            val userAgentGoogleBot =
                "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"
            // Taken from https://www.whatismybrowser.com/guides/the-latest-user-agent/
            val userAgentFirefox = "Mozilla/5.0 (Linux x86_64; rv:89.0) Gecko/20100101 Firefox/89.0"

            val doc = try {
                getDocument(url, userAgentGoogleBot)
            } catch (err: IOException) {
                getDocument(url, userAgentFirefox)
            }

            return WebpageInfo(title = doc.title(), url = doc.location())
        }

        private fun getDocument(url: URL, userAgent: String) =
            Jsoup.connect(url).userAgent(userAgent).get()
    }
}
