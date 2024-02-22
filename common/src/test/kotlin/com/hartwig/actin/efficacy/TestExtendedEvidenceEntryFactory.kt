package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

object TestExtendedEvidenceEntryFactory {

    fun createProperTestExtendedEvidenceEntry(): ExtendedEvidenceEntry {
        return ExtendedEvidenceEntry(
            acronym = "Study of treatment 1 instead of treatment 2",
            phase = "Phase III",
            therapies = listOf(Therapy(therapyName = "Vemurafenib", synonyms = null)),
            therapeuticSetting = Intent.ADJUVANT,
            variantRequirements = listOf(VariantRequirement(name = "EGFR positive", requirementType = "required")),
            trialReferences = listOf(createReference())
        )
    }

    fun createReference(): TrialReference {
        return TrialReference(patientPopulations = listOf(createPatientPopulation()), url = "http://www.ncbi.nlm.nih.gov/pubmed/12345678")
    }

    fun createPatientPopulation(): PatientPopulation {
        return PatientPopulation(
            name = "VEMURAFENIB",
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
            therapy = "Vemurafenib",
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

    fun createAnalysisGroup(): AnalysisGroup {
        return AnalysisGroup(
            id = 1, primaryEndPoints = listOf(
                PrimaryEndPoint(
                    id = 2,
                    name = "PFS",
                    value = 6.8,
                    unitOfMeasure = PrimaryEndPointUnit.MONTHS,
                    confidenceInterval = null,
                    type = PrimaryEndPointType.PRIMARY,
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