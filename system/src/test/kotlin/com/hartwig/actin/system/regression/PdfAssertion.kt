package com.hartwig.actin.system.regression

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.utils.CompareTool
import java.io.File
import org.assertj.core.api.Assertions.assertThat

fun assertThatPdf(actual: String): PdfAssertion {
    return PdfAssertion(actual)
}

private const val OUTPUT_PATH = "target/test-classes"

class PdfAssertion(private val actual: String) {

    fun isEqualToTextually(expected: String) {
        val expectedText = extractText(expected)
        val actualText = extractText(actual)
        assertThat(actualText).isEqualTo(expectedText)
    }

    fun isEqualToVisually(expected: String) {
        System.setProperty("ITEXT_GS_EXEC", "gs")
        System.setProperty("ITEXT_MAGICK_COMPARE_EXEC", "magick compare")
        val expectedPdfFileName = File(expected).name
        val reportRegressionDiff = "$expectedPdfFileName.diff"
        val result = CompareTool().compareVisually(expected, actual, OUTPUT_PATH, reportRegressionDiff)
        assertThat(result ?: "").withFailMessage("$result Difference file(s) can be found in $OUTPUT_PATH/$reportRegressionDiff[1..n].jpg")
            .doesNotContain("differs")

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