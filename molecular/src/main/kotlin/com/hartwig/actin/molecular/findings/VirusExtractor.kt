package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.finding.DriverFindingList
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation

private val QC_PASS_STATUS = VirusBreakendQCStatus.NO_ABNORMALITIES

class VirusExtractor() {

    fun extract(viruses: DriverFindingList<com.hartwig.hmftools.datamodel.finding.Virus>): List<Virus> {
        return viruses.findings.map { virus ->
            Virus(
                name = virus.name(),
                type = determineType(virus.interpretation()),
                isReliable = virus.qcStatus() == QC_PASS_STATUS,
                integrations = virus.integrations(),
                isReportable = virus.isReported,
                event = virus.event(),
                driverLikelihood = MappingUtil.determineDriverLikelihood(virus),
                evidence = ExtractionUtil.noEvidence(),
            )
        }.sorted()
    }

    internal fun determineType(interpretation: VirusInterpretation?): VirusType {
        return if (interpretation == null) {
            VirusType.OTHER
        } else when (interpretation) {
            VirusInterpretation.MCV -> {
                VirusType.MCV
            }

            VirusInterpretation.EBV -> {
                VirusType.EBV
            }

            VirusInterpretation.HPV -> {
                VirusType.HPV
            }

            VirusInterpretation.HBV -> {
                VirusType.HBV
            }

            VirusInterpretation.HHV8 -> {
                VirusType.HHV8
            }
        }
    }
}