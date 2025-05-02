package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.characteristics.PredictedTumorOrigin
import org.assertj.core.api.Assertions.assertThat
import com.hartwig.actin.report.pdf.getCellContents
import org.junit.Test

class PredictedTumorOriginGeneratorTest {
    @Test
    fun `Should display (WGTS) expressionPairWiseClassifier and altSjCohortClassifier variables if CUPPA ran in WGTS mode`() {
        val molecular = TestMolecularFactory.createExhaustiveTestMolecularRecord()
        val molecularWTGS = molecular.copy(
            characteristics = molecular.characteristics.copy(
                predictedTumorOrigin = PredictedTumorOrigin(
                    listOf(
                        CupPrediction(
                            "Melanoma",
                            0.99,
                            0.98,
                            0.96,
                            0.84,
                            0.82,
                            0.93,
                            CuppaMode.WGTS
                        )
                    )
                )
            )
        )
        val table = PredictedTumorOriginGenerator(molecularWTGS)

        assertThat(table.title()).isEqualTo("Predicted tumor origin (WGTS)")

        assertThat(getCellContents(table.contents(), 5, 0)).isEqualTo("(4) Gene expression")
        assertThat(getCellContents(table.contents(), 5, 1)).isEqualTo("82%")
        assertThat(getCellContents(table.contents(), 6, 0)).isEqualTo("(5) Alternative splice junctions")
        assertThat(getCellContents(table.contents(), 6, 1)).isEqualTo("93%")
    }
}