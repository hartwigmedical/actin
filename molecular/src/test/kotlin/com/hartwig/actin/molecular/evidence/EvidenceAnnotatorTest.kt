package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory

private val brafActionableHotspot =
    TestServeMolecularFactory.hotspot(
        TestServeMolecularFactory.createVariantAnnotation(
            gene = "BRAF",
            chromosome = "7",
            position = 140453136,
            ref = "T",
            alt = "A"
        )
    )

private val brafMolecularTestVariant = TestVariantFactory.createMinimal()
    .copy(
        gene = "BRAF",
        chromosome = "7",
        position = 140453136,
        ref = "T",
        alt = "A",
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true,
        evidence = TestClinicalEvidenceFactory.withApprovedTreatment("Vemurafenib")
    )

private val properVariant = TestMolecularFactory.createProperVariant()

