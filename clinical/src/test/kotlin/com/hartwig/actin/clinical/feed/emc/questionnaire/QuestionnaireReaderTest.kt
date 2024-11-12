package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireReader.read
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QuestionnaireReaderTest {

    @Test
    fun `Should read simple questionnaire`() {
        val questionnaireText = """this: \n is: \n a: \n questionnaire:"""
        val lines = read(
            questionnaireText, listOf(*questionnaireText.split(": \\n ").dropLastWhile { it.isEmpty() }.toTypedArray())
        )
        assertThat(lines.size).isEqualTo(4)
    }

    @Test
    fun `Should clean terms from questionnaire`() {
        val lines = read(
            """${QuestionnaireReader.TERMS_TO_CLEAN.joinToString("")}\ntest:""", listOf("test")
        )
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo(Strings.EMPTY)

    }

    @Test
    fun `Should merge lines for multi line responses`() {
        val lines = read("""value1: x\nand y\nand more\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7""", listOf("value"))
        assertThat(lines.size).isEqualTo(6)
        assertThat(lines[0]).isEqualTo("value1: x,and y,and more")
        assertThat(lines[5]).isEqualTo("value4: 6,7")
    }

    @Test
    fun `Should merge lines with one new line between key and text`() {
        val text = "- IHC test results: \\nERBB2 3+\\n- PD L1 test results: Positive"
        val lines = read(text, listOf("IHC test results", "PD L1 test results"))
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo("- IHC test results: ,ERBB2 3+")
        assertThat(lines[1]).isEqualTo("- PD L1 test results: Positive")
    }

    @Test
    fun `Should clean empty lines between keys and text`() {
        val text = "- IHC test results: \\n\\nERBB2 3+\\n- PD L1 test results: Positive"
        val lines = read(text, listOf("IHC test results", "PD L1 test results"))
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo("- IHC test results: ERBB2 3+")
        assertThat(lines[1]).isEqualTo("- PD L1 test results: Positive")
    }
}