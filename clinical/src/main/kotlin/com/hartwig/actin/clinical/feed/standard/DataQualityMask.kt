package com.hartwig.actin.clinical.feed.standard

val ARCHER_ALWAYS_TESTED_GENES = setOf("ALK", "ROS1", "RET", "MET", "NTRK1", "NTRK2", "NTRK3", "NRG1")
val GENERIC_PANEL_ALWAYS_TESTED_GENES = setOf("EGFR", "BRAF", "KRAS")

private fun ProvidedPatientRecord.scrubModifications() =
    this.copy(treatmentHistory = this.treatmentHistory.map { it.copy(modifications = emptyList()) })

private fun ProvidedPatientRecord.scrubMedications() =
    this.copy(medications = null)

private fun ProvidedPatientRecord.scrubMolecularTests() =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(results = it.results.map { r ->
            r.copy(
                hgvsCodingImpact = checkImpact(r, ProvidedMolecularTestResult::hgvsCodingImpact),
                hgvsProteinImpact = checkImpact(r, ProvidedMolecularTestResult::hgvsProteinImpact)
            )
        }.toSet())
    })

private fun ProvidedPatientRecord.addAlwaysTestedGenes() =
    this.copy(molecularTests = this.molecularTests.map {
        it.copy(testedGenes = knownGenes(it))
    })

fun knownGenes(test: ProvidedMolecularTest): Set<String>? = when {
    test.test.lowercase().contains("archer") -> ARCHER_ALWAYS_TESTED_GENES + (test.testedGenes ?: emptySet())
    test.test.lowercase().contains("avl") -> GENERIC_PANEL_ALWAYS_TESTED_GENES + (test.testedGenes ?: emptySet())
    else -> test.testedGenes
}

private fun checkImpact(r: ProvidedMolecularTestResult, accessor: (ProvidedMolecularTestResult) -> String?): String? {
    if (accessor.invoke(r) == "NOT FOUND")
        return null
    return accessor.invoke(r)
}

class DataQualityMask {
    fun apply(ehrPatientRecord: ProvidedPatientRecord): ProvidedPatientRecord {
        return ehrPatientRecord.scrubMedications().scrubModifications().scrubMolecularTests().addAlwaysTestedGenes()
    }
} 