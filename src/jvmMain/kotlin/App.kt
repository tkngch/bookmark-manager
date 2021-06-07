package tkngch.bookmarkManager.jvm

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserHashedTableAuth
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.basic
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.coroutines.launch
import org.sqlite.javax.SQLiteConnectionPoolDataSource
import tkngch.bookmarkManager.common.model.PayloadBookmarkCreate
import tkngch.bookmarkManager.common.model.PayloadBookmarkDelete
import tkngch.bookmarkManager.common.model.PayloadBookmarkRefresh
import tkngch.bookmarkManager.common.model.PayloadBookmarkUpdateTags
import tkngch.bookmarkManager.common.model.PayloadBookmarkVisit
import tkngch.bookmarkManager.common.model.PayloadTagCreate
import tkngch.bookmarkManager.common.model.PayloadTagDelete
import tkngch.bookmarkManager.common.model.PayloadTagUpdate
import tkngch.bookmarkManager.common.model.TagId
import tkngch.bookmarkManager.jvm.adapter.BookmarkJdbcSqliteRepository
import tkngch.bookmarkManager.jvm.adapter.WebpageInfoJsoupFactory
import tkngch.bookmarkManager.jvm.configuration.AppEnv
import tkngch.bookmarkManager.jvm.configuration.Configuration
import tkngch.bookmarkManager.jvm.service.BookmarkService
import tkngch.bookmarkManager.jvm.service.BookmarkServiceImpl
import tkngch.bookmarkManager.jvm.service.ScoringService
import tkngch.bookmarkManager.jvm.service.ScoringServiceImpl

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args) // Manually using Netty's EngineMain
}

val Application.appEnv get() =
    when (environment.config.property("ktor.deployment.environment").getString()) {
        "development" -> AppEnv.DEVELOPMENT
        "dev" -> AppEnv.DEVELOPMENT
        else -> AppEnv.PRODUCTION
    }

fun Application.module() {
    val config = Configuration.getInstance(appEnv)

    val dataSource = SQLiteConnectionPoolDataSource()
    dataSource.url = config.jdbcSqliteURL
    val connectionPool = dataSource.pooledConnection
    val repository = BookmarkJdbcSqliteRepository(connectionPool)

    val webScraper = WebpageInfoJsoupFactory()
    val bookmarkService = BookmarkServiceImpl(repository, webScraper)
    val scoringService = ScoringServiceImpl(repository)
    module(config.userTable, bookmarkService, scoringService)
}

fun Application.module(
    userTable: UserHashedTableAuth,
    bookmarkService: BookmarkService,
    scoringService: ScoringService
) {
    install(Authentication) {
        basic {
            realm = "bookmark"
            validate { credentials -> userTable.authenticate(credentials) }
        }
    }
    install(AutoHeadResponse)
    install(CallLogging)
    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
            throw cause
        }
    }
    install(ContentNegotiation) { json() } // JSON content using kotlinx.serialization library
    install(DefaultHeaders)
    install(Routing)

    routing {
        authenticate {
            get("/") {
                val principal: UserIdPrincipal? = call.authentication.principal()
                principal ?.let { launch { scoringService.updateScores(it.name) } }
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }
            userAPI()
            bookmarkAPI(bookmarkService)
            static("/") { resource("output.js") }
        }

        get("/health_check") { call.respondText("OK") }
    }
}

fun Route.userAPI() {
    route("/api/user") {
        get {
            val principal: UserIdPrincipal? = call.authentication.principal()
            principal?.let { call.respond(it.name) }
        }
    }
}

fun Route.bookmarkAPI(service: BookmarkService) {
    route("/api/tag") {
        get {
            val principal: UserIdPrincipal? = call.authentication.principal()
            principal?.let { call.respond(service.getTags(it.name)) }
        }
        post {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadTagCreate>()
            principal?.let { service.createTag(it.name, payload.tagName, payload.visibility) }
            call.respond("OK")
        }
        put {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadTagUpdate>()
            principal?.let {
                service.updateTag(
                    it.name,
                    payload.tagId,
                    payload.updatedTagName,
                    payload.updatedVisibility
                )
            }
            call.respond("OK")
        }
        delete {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadTagDelete>()
            principal?.let { service.deleteTag(it.name, payload.tagId) }
            call.respond("OK")
        }
    }

    route("/api/bookmark") {
        get {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val tags: List<TagId>? = call.request.queryParameters.getAll("tag")
            principal?.let { call.respond(service.getBookmarks(it.name, tags)) }
        }
        post {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadBookmarkCreate>()
            principal?.let { service.createBookmark(it.name, payload.url, payload.tags) }
            call.respond("OK")
        }
        put {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadBookmarkRefresh>()
            principal?.let { service.refreshBookmark(it.name, payload.bookmarkId) }
            call.respond("OK")
        }
        delete {
            val principal: UserIdPrincipal? = call.authentication.principal()
            val payload = call.receive<PayloadBookmarkDelete>()
            principal?.let { service.deleteBookmark(it.name, payload.bookmarkId) }
            call.respond("OK")
        }

        route("tag") {
            post {
                val principal: UserIdPrincipal? = call.authentication.principal()
                val payload = call.receive<PayloadBookmarkUpdateTags>()
                principal?.let {
                    service.addTagsToBookmark(it.name, payload.bookmarkId, payload.tags)
                }
                call.respond("OK")
            }
            delete {
                val principal: UserIdPrincipal? = call.authentication.principal()
                val payload = call.receive<PayloadBookmarkUpdateTags>()
                principal?.let {
                    service.dropTagsFromBookmark(it.name, payload.bookmarkId, payload.tags)
                }
                call.respond("OK")
            }
        }

        route("visit") {
            post {
                val principal: UserIdPrincipal? = call.authentication.principal()
                val payload = call.receive<PayloadBookmarkVisit>()
                principal?.let { service.logBookmarkVisit(it.name, payload.bookmarkId) }
                call.respond("OK")
            }
        }
    }
}
