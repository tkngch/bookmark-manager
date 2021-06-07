package tkngch.bookmarkManager.jvm.domain

interface WebpageInfoFactory {
    fun webpageInfo(url: String): WebpageInfo
}
