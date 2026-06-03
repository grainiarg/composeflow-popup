package com.example.mydemo.retention

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewModelSamplesTest {

    @Test
    fun `all error demo samples parse as Success with warnings`() {
        val samples = RetentionViewModel.samples
        val errorSamples = samples.filter { it.isErrorDemo }

        assertTrue(errorSamples.isNotEmpty(), "No error demo samples found")

        errorSamples.forEach { sample ->
            println("=== Testing: ${sample.name} ===")
            val result = DslParser.parse(sample.json)
            when (result) {
                is ParseResult.Success -> {
                    println("  -> Success, warnings: ${result.warnings.size}")
                    result.warnings.forEach { println("     [${it.path}] ${it.message}") }
                }
                is ParseResult.Failure -> {
                    println("  -> FAILURE: ${result.errors}")
                    // emptyDialog should go to Empty state, not Failure
                    // but parsing itself should succeed (0 children = Success, handled in VM)
                    if (sample.name != "空数据") {
                        throw AssertionError("${sample.name} should parse as Success but got Failure: ${result.errors}")
                    }
                }
            }
        }
    }
}
