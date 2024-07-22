package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import java.time.LocalDate

val TEST_DATE: LocalDate = LocalDate.of(2023, 1, 1)

object TestPanelRecordFactory {

    fun empty() =
        PanelRecord(
            drivers = Drivers(),
            date = TEST_DATE,
            characteristics = MolecularCharacteristics(),
            evidenceSource = "",
            experimentType = ExperimentType.PANEL,
            panelExtraction = ArcherPanelExtraction()
        )
}