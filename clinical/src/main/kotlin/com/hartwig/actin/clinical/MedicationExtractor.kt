package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationModel
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.feed.medication.MedicationEntry
import org.apache.logging.log4j.LogManager

class MedicationExtractor(private val curation: CurationModel, private val atc: AtcModel) {

    fun extractMedication(patientId: String, entry: MedicationEntry): Medication? {
        val name: String? = CurationUtil.capitalizeFirstLetterOnly(entry.code5ATCDisplay).ifEmpty {
            curation.curateMedicationName(patientId, CurationUtil.capitalizeFirstLetterOnly(entry.codeText))
        }
        if (name.isNullOrEmpty()) {
            return null
        }

        val administrationRoute = curation.translateAdministrationRoute(patientId, entry.dosageInstructionRouteDisplay)
        val dosage = if (dosageRequiresCuration(administrationRoute, entry)) {
            curation.curateMedicationDosage(patientId, entry.dosageInstructionText) ?: ImmutableDosage.builder().build()
        } else {
            ImmutableDosage.builder()
                .dosageMin(entry.dosageInstructionDoseQuantityValue)
                .dosageMax(correctDosageMax(entry))
                .dosageUnit(curation.translateDosageUnit(patientId, entry.dosageInstructionDoseQuantityUnit))
                .frequency(entry.dosageInstructionFrequencyValue)
                .frequencyUnit(entry.dosageInstructionFrequencyUnit)
                .periodBetweenValue(entry.dosageInstructionPeriodBetweenDosagesValue)
                .periodBetweenUnit(curation.curatePeriodBetweenUnit(patientId, entry.dosageInstructionPeriodBetweenDosagesUnit))
                .ifNeeded(extractIfNeeded(entry))
                .build()
        }

        val medication: Medication = ImmutableMedication.builder()
            .dosage(dosage)
            .name(name)
            .status(curation.curateMedicationStatus(patientId, entry.status))
            .administrationRoute(administrationRoute)
            .startDate(entry.periodOfUseValuePeriodStart)
            .stopDate(entry.periodOfUseValuePeriodEnd)
            .addAllCypInteractions(curation.curateMedicationCypInteractions(name))
            .qtProlongatingRisk(curation.annotateWithQTProlongating(name))
            .atc(atc.resolve(entry.code5ATCCode))
            .isSelfCare(entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isEmpty())
            .isTrialMedication(entry.code5ATCDisplay.isEmpty() && entry.code5ATCCode.isNotEmpty() && entry.code5ATCCode[0].lowercaseChar() !in 'a'..'z')
            .build()

        if (medication.atc() == null && !medication.isSelfCare && !medication.isTrialMedication) {
            LOGGER.warn("Medication ${medication.name()} has no ATC code and is not self-care or a trial")
        }

        return medication
    }

    private fun extractIfNeeded(entry: MedicationEntry) =
        entry.dosageInstructionAsNeededDisplay.trim().lowercase() == "zo nodig"

    private fun correctDosageMax(entry: MedicationEntry): Double? {
        return if (entry.dosageInstructionMaxDosePerAdministration == 0.0) {
            entry.dosageInstructionDoseQuantityValue
        } else {
            entry.dosageInstructionMaxDosePerAdministration
        }
    }

    private fun dosageRequiresCuration(administrationRoute: String?, entry: MedicationEntry) =
        administrationRoute?.lowercase() == "oral" && (entry.dosageInstructionDoseQuantityValue == 0.0 ||
                entry.dosageInstructionDoseQuantityUnit.isEmpty() ||
                entry.dosageInstructionFrequencyValue == 0.0 ||
                entry.dosageInstructionFrequencyUnit.isEmpty())

    companion object {
        private val LOGGER = LogManager.getLogger(CurationDatabaseReader::class.java)
    }
}