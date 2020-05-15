package com.phauer.svgfontembedding

import io.quarkus.runtime.QuarkusApplication
import io.quarkus.runtime.annotations.QuarkusMain

@QuarkusMain
class SvgFontEmbeddingApplication(
    private val embedder: SvgFontEmbedder
) : QuarkusApplication {
    override fun run(vararg args: String) = when (val result = embedder.embedFont(*args)) {
        is EmbeddingResult.Success -> 0
        is EmbeddingResult.Failure -> {
            println(result.message)
            embedder.printHelp()
            -1
        }
    }
}