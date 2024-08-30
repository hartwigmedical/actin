package com.hartwig.actin.datamodel.efficacy

import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

object TestExtendedEvidenceEntryFactory {

    fun createProperTestExtendedEvidenceEntries(): List<EfficacyEntry> {
        return listOf(
            EfficacyEntry(
                acronym = "Study of Pembrolizumab",
                phase = "Phase III",
                treatments = listOf(
                    DrugTreatment(
                        "PEMBROLIZUMAB",
                        setOf(
                            Drug(
                                name = "PEMBROLIZUMAB",
                                drugTypes = setOf(DrugType.TOPO1_INHIBITOR),
                                category = TreatmentCategory.CHEMOTHERAPY
                            )
                        )
                    )
                ),
                therapeuticSetting = Intent.ADJUVANT,

                variantRequirements = listOf(VariantRequirement(name = "MSI high", requirementType = "required")),
                trialReferences = listOf(createReference())
            )
        )
    }

    private fun createReference(): TrialReference {
        return TrialReference(patientPopulations = listOf(createPatientPopulation()), url = "http://www.ncbi.nlm.nih.gov/pubmed/12345678")
    }

    private fun createPatientPopulation(): PatientPopulation {
        return PatientPopulation(
            name = "Pembrolizumab",
            isControl = true,
            ageMin = 55,
            ageMax = 65,
            ageMedian = 60.0,
            numberOfPatients = 200,
            numberOfMale = 100,
            numberOfFemale = 100,
            patientsWithWho0 = 100,
            patientsWithWho1 = 0,
            patientsWithWho2 = 0,
            patientsWithWho3 = 0,
            patientsWithWho4 = 0,
            patientsWithWho0to1 = 0,
            patientsWithWho1to2 = 0,
            patientsPerPrimaryTumorLocation = mapOf("Rectum" to 100),
            mutations = null,
            patientsWithPrimaryTumorRemovedComplete = 50,
            patientsWithPrimaryTumorRemovedPartial = 25,
            patientsWithPrimaryTumorRemoved = 25,
            patientsPerMetastaticSites = mapOf("Lung" to ValuePercentage(100, 100.0)),
            timeOfMetastases = TimeOfMetastases.BOTH,
            treatment = DrugTreatment(
                "PEMBROLIZUMAB",
                setOf(Drug(name = "PEMBROLIZUMAB", drugTypes = setOf(DrugType.TOPO1_INHIBITOR), category = TreatmentCategory.CHEMOTHERAPY))
            ),
            priorSystemicTherapy = "Chemo",
            patientsWithMSI = 33,
            medianFollowUpForSurvival = "30",
            medianFollowUpPFS = "30",
            analysisGroups = listOf(createAnalysisGroup()),
            priorTherapies = "5-FU",
            patientsPerRace = null,
            patientsPerRegion = null,
        )
    }

    private fun createAnalysisGroup(): AnalysisGroup {
        return AnalysisGroup(
            id = 1, name = "Analyis group", nPatients = 200, endPoints = listOf(
                EndPoint(
                    id = 2,
                    name = "Median Progression-Free Survival",
                    value = 6.8,
                    unitOfMeasure = EndPointUnit.MONTHS,
                    confidenceInterval = null,
                    type = EndPointType.PRIMARY,
                    derivedMetrics = listOf(
                        DerivedMetric(
                            relativeMetricId = 1,
                            value = 16.0,
                            type = "PRIMARY",
                            confidenceInterval = ConfidenceInterval(
                                lowerLimit = 14.0,
                                upperLimit = 18.8
                            ),
                            pValue = "0.0002"
                        )
                    )
                )
            )
        )
    }
}