package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import java.time.LocalDate

val TEST_DATE: LocalDate = LocalDate.of(2023, 1, 1)

object TestPanelRecordFactory {

    fun empty() =
        PanelRecord(
            drivers = PanelDrivers(),
            date = TEST_DATE,
            characteristics = MolecularCharacteristics(),
            evidenceSource = "",
            type = ExperimentType.ARCHER
        )
}