package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.clinical.SequencingTest
import java.time.LocalDate

object PanelSpecificationFunctions {

    fun determineTestVersion(
        test: SequencingTest,
        panelTestSpecifications: Set<PanelTestSpecification>,
        registrationDate: LocalDate
    ): LocalDate? {
        val referenceDate = test.date ?: registrationDate
        return panelTestSpecifications
            .filter { it.testName == test.test }
            .filter { it.versionDate?.isAfter(referenceDate) == false }
            .maxByOrNull { it.versionDate!! }
            ?.versionDate
    }
}