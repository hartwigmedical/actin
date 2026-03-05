package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.finding.datamodel.DriverFindingList
import com.hartwig.hmftools.finding.datamodel.Virus.OncogenicVirus

private val QC_PASS_STATUS = com.hartwig.hmftools.finding.datamodel.Virus.VirusBreakendQCStatus.NO_ABNORMALITIES

class VirusExtractor() {

    fun extract(viruses: DriverFindingList<com.hartwig.hmftools.finding.datamodel.Virus>): List<Virus> {
        return viruses.findings.map { virus ->
            Virus(
                name = virus.name(),
                type = determineType(virus.oncogenicVirus()),
                isReliable = virus.qcStatus() == QC_PASS_STATUS,
                integrations = virus.integrations(),
                isReportable = virus.isReported,
                event = DriverEventFactory.event(virus),
                driverLikelihood = MappingUtil.determineDriverLikelihood(virus),
                evidence = ExtractionUtil.noEvidence(),
            )
        }.sorted()
    }

    internal fun determineType(oncogenicVirus: OncogenicVirus?): VirusType {
        return if (oncogenicVirus == null) {
            VirusType.OTHER
        } else when (oncogenicVirus) {
            OncogenicVirus.MCV -> {
                VirusType.MCV
            }

            OncogenicVirus.EBV -> {
                VirusType.EBV
            }

            OncogenicVirus.HPV -> {
                VirusType.HPV
            }

            OncogenicVirus.HBV -> {
                VirusType.HBV
            }

            OncogenicVirus.HHV8 -> {
                VirusType.HHV8
            }
        }
    }
}