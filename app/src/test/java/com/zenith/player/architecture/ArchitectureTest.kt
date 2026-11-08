package com.zenith.player.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/** Reglas de capas de doc/02-arquitectura.md §4 y §5.1. */
class ArchitectureTest {

    @Test
    fun `las capas respetan sus dependencias`() {
        Konsist.scopeFromProduction()
            .assertArchitecture {
                val ui = Layer("UI", "com.zenith.player.ui..")
                val domain = Layer("Dominio", "com.zenith.player.domain..")
                val data = Layer("Datos", "com.zenith.player.data..")
                val playback = Layer("Reproducción", "com.zenith.player.playback..")

                ui.dependsOn(domain, playback)
                data.dependsOn(domain)
                playback.dependsOn(domain)
                domain.dependsOnNothing()
            }
    }

    @Test
    fun `el dominio no usa clases de Android`() {
        Konsist.scopeFromPackage("com.zenith.player.domain..")
            .files
            .assertFalse { file -> file.imports.any { it.name.startsWith("android.") } }
    }
}
