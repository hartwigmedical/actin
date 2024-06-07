package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.panel.PanelDrivers
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