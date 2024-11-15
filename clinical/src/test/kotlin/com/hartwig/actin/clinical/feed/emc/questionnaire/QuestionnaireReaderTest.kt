package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireReader.read
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


val SECTION_HEADERS = QuestionnaireMapping.SECTION_HEADERS

class QuestionnaireReaderTest {

    @Test
    fun `Should read simple questionnaire`() {
        val questionnaireText = """this: \n is: \n a: \n questionnaire:"""
        val lines = read(
            questionnaireText, listOf(*questionnaireText.split(": \\n ").dropLastWhile { it.isEmpty() }.toTypedArray()), SECTION_HEADERS
        )
        assertThat(lines.size).isEqualTo(4)
    }

    @Test
    fun `Should read simple questionnaire with section headers`() {
        val questionnaireText = """this: \n\nTumor details\nis: \n a: \n questionnaire:"""
        val lines = read(questionnaireText, listOf("this", "is", "a", "questionnaire"), SECTION_HEADERS)
        assertThat(lines.size).isEqualTo(5)
        println(lines.toList().toString())
        assertThat(lines[0]).isEqualTo("this:")
        assertThat(lines[1]).isEqualTo("Tumor details")
        assertThat(lines[2]).isEqualTo("is: ")
        assertThat(lines[3]).isEqualTo(" a: ")
        assertThat(lines[4]).isEqualTo(" questionnaire:")
    }

    @Test
    fun `Should clean terms from questionnaire`() {
        val lines = read(
            """${QuestionnaireReader.TERMS_TO_CLEAN.joinToString("")}\ntest:""", listOf("test"), SECTION_HEADERS
        )
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo(Strings.EMPTY)

    }

    @Test
    fun `Should merge lines for multi line responses`() {
        val lines = read("""value1: x\nand y\nand more\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7""", listOf("value"), SECTION_HEADERS)
        assertThat(lines.size).isEqualTo(6)
        assertThat(lines[0]).isEqualTo("value1: x,and y,and more")
        assertThat(lines[5]).isEqualTo("value4: 6,7")
    }

    @Test
    fun `Should keep one new line between key and value`() {
        val text = "- IHC test results: \\nERBB2 3+\\n- PD L1 test results: Positive"
        val lines = read(text, listOf("IHC test results", "PD L1 test results"), SECTION_HEADERS)
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo("- IHC test results: ,ERBB2 3+")
        assertThat(lines[1]).isEqualTo("- PD L1 test results: Positive")
    }

    @Test
    fun `Should replace multiple empty lines with one line between key and value when value exists`() {
        val text = "- IHC test results: \\n\\nERBB2 3+\\n- PD L1 test results: Positive"
        val lines = read(text, listOf("IHC test results", "PD L1 test results"), SECTION_HEADERS)
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo("- IHC test results:,ERBB2 3+")
        assertThat(lines[1]).isEqualTo("- PD L1 test results: Positive")
    }

    @Test
    fun `Should replace multiple empty lines with one line between key and value when value is missing`() {
        val text = "- IHC test results: \\n\\n- PD L1 test results: Positive"
        val lines = read(text, listOf("IHC test results", "PD L1 test results"), SECTION_HEADERS)
        assertThat(lines.size).isEqualTo(2)
        assertThat(lines[0]).isEqualTo("- IHC test results:")
        assertThat(lines[1]).isEqualTo("- PD L1 test results: Positive")
    }
}