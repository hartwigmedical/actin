package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireReader
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireReader.read
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class QuestionnaireReaderTest {
    @Test
    fun shouldReadSimpleQuestionnaire() {
        val questionnaireText = """this: \n is: \n a: \n questionnaire:"""
        Assert.assertEquals(
            4,
            read(questionnaireText, listOf(*questionnaireText.split(": \\n ").dropLastWhile { it.isEmpty() }
                .toTypedArray())).size.toLong())
    }

    @Test
    fun shouldCleanTermsFromQuestionnaire() {
        val lines = read(
            """${QuestionnaireReader.TERMS_TO_CLEAN.joinToString("")}\ntest:""", listOf("test")
        )
        Assert.assertEquals(2, lines.size.toLong())
        Assert.assertEquals(Strings.EMPTY, lines[0])
    }

    @Test
    fun shouldMergeLinesForMultilineResponses() {
        val lines = read("""value1: x\nand y\nand more\nvalue2: z\n\nheader\nvalue3: 5\nvalue4: 6\n7""", listOf("value"))
        Assert.assertEquals(6, lines.size.toLong())
        Assert.assertEquals("value1: x,and y,and more", lines[0])
        Assert.assertEquals("value4: 6,7", lines[5])
    }
}