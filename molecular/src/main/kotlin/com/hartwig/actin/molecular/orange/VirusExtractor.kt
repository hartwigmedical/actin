package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType

private val QC_PASS_STATUS = VirusBreakendQCStatus.NO_ABNORMALITIES

class VirusExtractor() {

    fun extract(virusInterpreter: VirusInterpreterData): List<Virus> {
        return virusInterpreter.allViruses().map { virus ->
            Virus(
                name = virus.name(),
                type = determineType(virus.interpretation()),
                isReliable = virus.qcStatus() == QC_PASS_STATUS,
                integrations = virus.integrations(),
                isReportable = virus.reported(),
                event = DriverEventFactory.virusEvent(virus),
                driverLikelihood = determineDriverLikelihood(virus.driverLikelihood()),
                evidence = ExtractionUtil.noEvidence(),
            )
        }.sorted()
    }

    internal fun determineDriverLikelihood(driverLikelihood: VirusLikelihoodType): DriverLikelihood? {
        return when (driverLikelihood) {
            VirusLikelihoodType.HIGH -> {
                DriverLikelihood.HIGH
            }

            VirusLikelihoodType.LOW -> {
                DriverLikelihood.LOW
            }

            VirusLikelihoodType.UNKNOWN -> {
                null
            }

            else -> {
                throw IllegalStateException(
                    "Cannot determine driver likelihood type for virus driver likelihood: $driverLikelihood"
                )
            }
        }
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