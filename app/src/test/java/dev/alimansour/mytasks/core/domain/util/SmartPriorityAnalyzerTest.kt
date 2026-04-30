package dev.alimansour.mytasks.core.domain.util

import dev.alimansour.mytasks.core.domain.model.TaskPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SmartPriorityAnalyzerTest {

    private lateinit var analyzer: SmartPriorityAnalyzer

    @BeforeEach
    fun setUp() {
        analyzer = SmartPriorityAnalyzer()
    }

    @ParameterizedTest
    @ValueSource(strings = ["urgent", "asap", "tomorrow", "URGENT", "Asap", "ToMoRrOw"])
    fun `analyze with high priority keywords returns HIGH`(keyword: String) {
        // GIVEN
        val title = "Task with $keyword keyword"

        // WHEN
        val result = analyzer.analyze(title)

        // THEN
        assertEquals(TaskPriority.HIGH, result)
    }

    @Test
    fun `analyze with title containing urgent returns HIGH`() {
        // GIVEN
        val title = "This is urgent"

        // WHEN
        val result = analyzer.analyze(title)

        // THEN
        assertEquals(TaskPriority.HIGH, result)
    }

    @Test
    fun `analyze with title containing normal words returns LOW`() {
        // GIVEN
        val title = "Buy groceries"

        // WHEN
        val result = analyzer.analyze(title)

        // THEN
        assertEquals(TaskPriority.LOW, result)
    }

    @Test
    fun `analyze with empty title returns LOW`() {
        // GIVEN
        val title = ""

        // WHEN
        val result = analyzer.analyze(title)

        // THEN
        assertEquals(TaskPriority.LOW, result)
    }
}
