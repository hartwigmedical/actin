package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.actionability.ActionableEvidenceFactory
import com.hartwig.actin.molecular.sort.driver.VirusComparator
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType

internal class VirusExtractor() {

    fun extract(virusInterpreter: VirusInterpreterData): Set<Virus> {
        return virusInterpreter.allViruses().map { virus ->
            Virus(
                isReportable = virus.reported(),
                event = DriverEventFactory.virusEvent(virus),
                driverLikelihood = determineDriverLikelihood(virus.driverLikelihood()),
                evidence = ActionableEvidenceFactory.createNoEvidence(),
                name = virus.name(),
                isReliable = virus.qcStatus() == QC_PASS_STATUS,
                type = determineType(virus.interpretation()),
                integrations = virus.integrations()
            )
        }.toSortedSet(VirusComparator())
    }

    companion object {
        val QC_PASS_STATUS: VirusBreakendQCStatus = VirusBreakendQCStatus.NO_ABNORMALITIES

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
                        "Cannot determine driver likelihood type for virus driver likelihood: $driverLikelihood")
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
}
