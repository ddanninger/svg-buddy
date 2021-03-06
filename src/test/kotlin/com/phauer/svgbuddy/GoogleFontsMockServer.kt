package com.phauer.svgbuddy

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.io.FileInputStream

class GoogleFontsMockServer : QuarkusTestResourceLifecycleManager {
    private var server: MockWebServer? = null

    override fun start(): Map<String, String> {
        server = MockWebServer().apply {
            dispatcher = GoogleFontsHelperDispatcher
            start()
        }
        return mapOf("com.phauer.svgbuddy.processing.GoogleFontsService/mp-rest/url" to server!!.url("/").toString())
    }

    override fun stop() {
        server?.shutdown()
    }

    override fun inject(testInstance: Any) {
        testInstance.javaClass.declaredFields.forEach { field ->
            if (field.type == MockWebServer::class.java) {
                field.set(testInstance, server)
            }
        }
    }
}

object GoogleFontsHelperDispatcher : Dispatcher() {
    private val zipBaseFolder = "src/test/resources/font-zips"
    private val robotoZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/roboto.zip")) }
    private val robotoMonoZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/roboto-mono.zip")) }
    private val pacificoZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/pacifico.zip")) }
    private val gochiHandZip by lazy { Buffer().readFrom(FileInputStream("$zipBaseFolder/gochi-hand.zip")) }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path!!
        return when {
            // add "?" to avoid mixing up roboto and roboto-mono
            path.contains("api/fonts/roboto?") -> createMockResponse(payload = robotoZip)
            path.contains("api/fonts/roboto-mono?") -> createMockResponse(payload = robotoMonoZip)
            path.contains("api/fonts/pacifico?") -> createMockResponse(payload = pacificoZip)
            path.contains("api/fonts/gochi-hand?") -> createMockResponse(payload = gochiHandZip)
            else -> MockResponse().setResponseCode(404)
        }
    }

    private fun createMockResponse(payload: Buffer) = MockResponse()
        .addHeader("Content-Disposition", "attachment; filename=font.zip")
        .addHeader("Content-Type", "application/zip")
        .setBody(payload)
        .setResponseCode(200)
}