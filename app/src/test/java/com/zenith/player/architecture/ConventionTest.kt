package com.zenith.player.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withParentNamed
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Convenciones de nombres de doc/02-arquitectura.md §5.1. */
class ConventionTest {

    @Test
    fun `los ViewModel terminan en ViewModel y viven en ui`() {
        Konsist.scopeFromProduction()
            .classes()
            .withParentNamed("ViewModel")
            .assertTrue { it.hasNameEndingWith("ViewModel") && it.resideInPackage("..ui..") }
    }

    @Test
    fun `los casos de uso terminan en UseCase y tienen un solo invoke`() {
        Konsist.scopeFromPackage("com.zenith.player.domain.usecase..")
            .classes()
            .assertTrue {
                it.hasNameEndingWith("UseCase") &&
                    it.functions().count { f -> f.name == "invoke" && f.hasOperatorModifier } == 1
            }
    }

    @Test
    fun `las entidades de Room solo existen en data_local`() {
        Konsist.scopeFromProduction()
            .classes()
            .withAnnotationNamed("Entity")
            .assertTrue { it.resideInPackage("com.zenith.player.data.local..") }
    }

    @Test
    fun `los repositorios implementados terminan en RepositoryImpl`() {
        Konsist.scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("com.zenith.player.data.repository..") }
            .assertTrue { it.hasNameEndingWith("RepositoryImpl") }
    }
}
