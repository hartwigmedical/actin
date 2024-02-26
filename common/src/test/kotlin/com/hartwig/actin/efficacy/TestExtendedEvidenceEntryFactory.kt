package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

object TestExtendedEvidenceEntryFactory {

    fun createProperTestExtendedEvidenceEntries(): List<EfficacyEntry> {
        return listOf(
            EfficacyEntry(
                acronym = "Study of Pembrolizumab",
                phase = "Phase III",
                therapies = listOf("PEMBROLIZUMAB", "CAPECITABINE+OXALIPLATIN"),
                therapeuticSetting = Intent.ADJUVANT,

                variantRequirements = listOf(VariantRequirement(name = "MSI high", requirementType = "required")),
                trialReferences = listOf(createReference())
            )
        )
    }

    private fun createReference(): TrialReference {
        return TrialReference(
            patientPopulations = listOf(createPatientPopulation1(), createPatientPopulation2()),
            url = "http://www.ncbi.nlm.nih.gov/pubmed/12345678"
        )
    }

    private fun createPatientPopulation1(): PatientPopulation {
        return PatientPopulation(
            name = "Pembrolizumab",
            isControl = false,
            ageMin = 55,
            ageMax = 65,
            ageMedian = 60.0,
            numberOfPatients = 200,
            numberOfMale = 100,
            numberOfFemale = 100,
            patientsWithWho0 = 200,
            patientsWithWho1 = 0,
            patientsWithWho2 = 0,
            patientsWithWho3 = null,
            patientsWithWho4 = null,
            patientsWithWho0to1 = null,
            patientsWithWho1to2 = null,
            patientsPerPrimaryTumorLocation = mapOf("Sigmoid" to 200),
            mutations = null,
            patientsWithPrimaryTumorRemovedComplete = 150,
            patientsWithPrimaryTumorRemovedPartial = 25,
            patientsWithPrimaryTumorRemoved = 25,
            patientsPerMetastaticSites = mapOf("Lung" to ValuePercentage(200, 100.0)),
            timeOfMetastases = TimeOfMetastases.BOTH,
            therapy = "PEMBROLIZUMAB",
            priorSystemicTherapy = "Chemo",
            patientsWithMSI = 33,
            medianFollowUpForSurvival = "30",
            medianFollowUpPFS = "30",
            analysisGroups = listOf(createAnalysisGroup1()),
            priorTherapies = "5-FU",
            patientsPerRace = null,
            patientsPerRegion = null,
        )
    }

    private fun createAnalysisGroup1(): AnalysisGroup {
        return AnalysisGroup(
            id = 1, primaryEndPoints = listOf(
                PrimaryEndPoint(
                    id = 2,
                    name = "Median Progression-Free Survival",
                    value = 14.0,
                    unitOfMeasure = PrimaryEndPointUnit.MONTHS,
                    confidenceInterval = ConfidenceInterval(12.1, 16.6),
                    type = PrimaryEndPointType.PRIMARY,
                    derivedMetrics = listOf(
                        DerivedMetric(
                            relativeMetricId = 2,
                            value = 16.0,
                            type = "Hazard ratio",
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

    private fun createPatientPopulation2(): PatientPopulation {
        return PatientPopulation(
            name = "CAPOX",
            isControl = true,
            ageMin = 50,
            ageMax = 60,
            ageMedian = 55.0,
            numberOfPatients = 210,
            numberOfMale = 106,
            numberOfFemale = 104,
            patientsWithWho0 = 190,
            patientsWithWho1 = 20,
            patientsWithWho2 = 0,
            patientsWithWho3 = null,
            patientsWithWho4 = null,
            patientsWithWho0to1 = null,
            patientsWithWho1to2 = null,
            patientsPerPrimaryTumorLocation = mapOf("Sigmoid" to 210),
            mutations = null,
            patientsWithPrimaryTumorRemovedComplete = 150,
            patientsWithPrimaryTumorRemovedPartial = 35,
            patientsWithPrimaryTumorRemoved = 25,
            patientsPerMetastaticSites = mapOf("Lung" to ValuePercentage(210, 100.0)),
            timeOfMetastases = TimeOfMetastases.BOTH,
            therapy = "CAPECITABINE+OXALIPLATIN",
            priorSystemicTherapy = "Chemo",
            patientsWithMSI = 33,
            medianFollowUpForSurvival = "30",
            medianFollowUpPFS = "30",
            analysisGroups = listOf(createAnalysisGroup2()),
            priorTherapies = "5-FU",
            patientsPerRace = null,
            patientsPerRegion = null,
        )
    }

    private fun createAnalysisGroup2(): AnalysisGroup {
        return AnalysisGroup(
            id = 2, primaryEndPoints = listOf(
                PrimaryEndPoint(
                    id = 2,
                    name = "Median Progression-Free Survival",
                    value = 6.8,
                    unitOfMeasure = PrimaryEndPointUnit.MONTHS,
                    confidenceInterval = ConfidenceInterval(4.2, 8.4),
                    type = PrimaryEndPointType.PRIMARY,
                    derivedMetrics = listOf(
                        DerivedMetric(
                            relativeMetricId = 1,
                            value = 16.0,
                            type = "Hazard ratio",
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