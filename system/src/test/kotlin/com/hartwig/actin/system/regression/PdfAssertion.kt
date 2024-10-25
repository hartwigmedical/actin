package com.hartwig.actin.system.regression

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.utils.CompareTool
import org.assertj.core.api.Assertions.assertThat

fun assertThatPdf(actual: String): PdfAssertion {
    return PdfAssertion(actual)
}

class PdfAssertion(private val actual: String) {

    fun isEqualTo(expected: String) {
        val expectedText = extractText(expected)
        val actualText = extractText(actual)
        assertThat(expectedText).isEqualTo(actualText)
    }

    fun isEqualToVisually(expected: String) {
        val result = CompareTool().compareVisually(expected, actual, "target/test-classes", "diff")
        println(result)
    }

    private fun extractText(actual: String): String {
        val pdfDocument = PdfDocument(PdfReader(actual))
        val text = StringBuilder()
        for (i in 1..pdfDocument.numberOfPages) {
            val pageText = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i))
            text.append(pageText)
        }
        pdfDocument.close()
        return text.toString()
    }
}