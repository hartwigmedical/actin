package com.hartwig.actin.datamodel.molecular

import java.time.LocalDate

val TEST_DATE: LocalDate = LocalDate.of(2023, 1, 1)

object TestPanelRecordFactory {

    fun empty() =
        PanelRecord(
            testedGenes = emptySet(),
            drivers = Drivers(),
            date = TEST_DATE,
            characteristics = MolecularCharacteristics(),
            evidenceSource = "",
            experimentType = ExperimentType.PANEL,
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
}