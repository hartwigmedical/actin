package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.datamodel.molecular.sort.driver.VirusComparator
import com.hartwig.actin.molecular.util.ExtractionUtil
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType

private val QC_PASS_STATUS = VirusBreakendQCStatus.NO_ABNORMALITIES

internal class VirusExtractor() {

    fun extract(virusInterpreter: VirusInterpreterData): List<Virus> {
        return virusInterpreter.allViruses().map { virus ->
            Virus(
                isReportable = virus.reported(),
                event = DriverEventFactory.virusEvent(virus),
                driverLikelihood = determineDriverLikelihood(virus.driverLikelihood()),
                evidence = ExtractionUtil.noEvidence(),
                name = virus.name(),
                isReliable = virus.qcStatus() == QC_PASS_STATUS,
                type = determineType(virus.interpretation()),
                integrations = virus.integrations()
            )
        }.sortedWith(VirusComparator())
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
                VirusType.MERKEL_CELL_VIRUS
            }

            VirusInterpretation.EBV -> {
                VirusType.EPSTEIN_BARR_VIRUS
            }

            VirusInterpretation.HPV -> {
                VirusType.HUMAN_PAPILLOMA_VIRUS
            }

            VirusInterpretation.HBV -> {
                VirusType.HEPATITIS_B_VIRUS
            }

            VirusInterpretation.HHV8 -> {
                VirusType.HUMAN_HERPES_VIRUS_8
            }

            else -> {
                throw IllegalStateException("Cannot determine virus type for interpretation: $interpretation")
            }
        }
    }
}