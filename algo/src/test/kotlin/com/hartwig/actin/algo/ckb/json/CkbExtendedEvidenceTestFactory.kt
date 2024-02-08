package com.hartwig.actin.algo.ckb.json

object CkbExtendedEvidenceTestFactory {

    fun createMinimalTestExtendedEvidenceDatabase(): List<JsonCkbExtendedEvidenceEntry> {
        return listOf(
            JsonCkbExtendedEvidenceEntry(
                nctId = "",
                title = "",
                phase = "",
                recruitment = "",
                therapies = listOf(),
                ageGroups = listOf(),
                gender = "",
                variantRequirements = "",
                sponsors = "",
                updateDate = "",
                indications = listOf(),
                variantRequirementDetails = listOf(),
                clinicalTrialLocations = listOf(),
                coveredCountries = listOf(),
                trialReferences = listOf(),
                otherTrialRegistrationNumbers = null,
                masking = "",
                allocation = "",
                cancerStage = "",
                diseaseAssessment = "",
                diseaseAssessmentCriteria = "",
                therapeuticSetting = null
            )
        )
    }

    fun createProperTestExtendedEvidenceDatabase(): List<JsonCkbExtendedEvidenceEntry> {
        return listOf(
            JsonCkbExtendedEvidenceEntry(
                nctId = "NCT01",
                title = "Study of treatment 1 instead of treatment 2",
                phase = "Phase III",
                recruitment = "Completed",
                therapies = createProperTestTherapies(),
                ageGroups = listOf("adult", "senior"),
                gender = "both",
                variantRequirements = "Yes",
                sponsors = "Hartwig Medical Foundation",
                updateDate = "12/05/2023",
                indications = listOf(JsonCkbIndication(id = 162, name = "cancer", source = "DOID")),
                variantRequirementDetails = createProperVariantRequirementDetails(),
                clinicalTrialLocations = createProperClinicalTrialLocations(),
                coveredCountries = listOf(),
                trialReferences = createProperTrialReferences(),
                otherTrialRegistrationNumbers = null,
                masking = "None (Open Label)",
                allocation = "randomized",
                cancerStage = "metastatic",
                diseaseAssessment = "Measurable",
                diseaseAssessmentCriteria = "RECIST",
                therapeuticSetting = null
            )
        )
    }

    private fun createProperTestTherapies(): List<JsonCkbTherapy> {
        return listOf(
            JsonCkbTherapy(
                id = 1,
                therapyName = "Treatment 1",
                synonyms = null
            ),
            JsonCkbTherapy(
                id = 2,
                therapyName = "Treatment 2",
                synonyms = null
            )
        )
    }

    private fun createProperVariantRequirementDetails(): List<JsonCkbVariantRequirementDetail> {
        return listOf(
            JsonCkbVariantRequirementDetail(
                molecularProfile = JsonCkbMolecularProfile(id = 3, profileName = "EGFR positive"),
                requirementType = "required"
            )
        )
    }

    private fun createProperClinicalTrialLocations(): List<JsonCkbClinicalTrialLocation> {
        return listOf(
            JsonCkbClinicalTrialLocation(
                nctId = "NCT01",
                facility = "HMF Sequencing Lab",
                city = "Amsterdam",
                country = "Netherlands",
                status = null,
                state = "NH",
                zip = "1234 AM",
                clinicalTrialContacts = listOf()
            )
        )
    }

    private fun createProperTrialReferences(): List<JsonCkbTrialReference> {
        return listOf(
            JsonCkbTrialReference(
                id = 10,
                patientPopulations = createPatientPopulations(),
                reference = createReference()
            )
        )
    }

    private fun createPatientPopulations(): List<JsonCkbPatientPopulation> {
        return listOf(
            createPatientPopulation1(),
            createPatientPopulation2()
        )
    }

    private fun createPatientPopulation1(): JsonCkbPatientPopulation {
        return JsonCkbPatientPopulation(
            id = 20,
            isControl = true,
            groupName = "Treatment 1",
            nPatientsEnrolled = null,
            nPatients = "40",
            nFemale = "30",
            nMale = "10",
            ageMin = "50",
            ageMax = "60",
            medianAge = "55",
            n65OrOlder = null,
            nEcog0 = "3",
            nEcog1 = "27",
            nEcog2 = "10",
            nEcog3 = null,
            nEcog4 = null,
            highestEcogScore = 2,
            nLocalizationPrimaryTumor = null,
            otherMutations = "EGFR Staining None: 0 (0%), Weak 1+: 40 (100.0%)",
            nPrimaryTumorRemovedComplete = null,
            nPrimaryTumorRemovedPartial = null,
            nPrimaryTumorRemoved = null,
            nPreviousLinesOfTherapy1 = "40",
            nPreviousLinesOfTherapy2 = null,
            nPreviousLinesOfTherapy3 = null,
            nPreviousLinesOfTherapy4OrMore = null,
            nPriorSystemicTherapy = null,
            nStageDescription = null,
            nMutationStatus = null,
            nHighMicrosatelliteStability = null,
            medianFollowUpForSurvival = null,
            medianFollowUpForProgressionFreeSurvival = null,
            medianFollowUpForRandomizationToDataCutOff = null,
            metastaticSites = "Liver: 20 (50.0%), Lung: 10 (25.0%)",
            analysisGroups = createAnalysisGroupsForPopulation1(),
            efficacyEvidence = listOf(),
            notes = null
        )
    }

    private fun createAnalysisGroupsForPopulation1(): List<JsonCkbAnalysisGroup> {
        return listOf(
            JsonCkbAnalysisGroup(
                id = 30,
                name = "Treatment 1",
                outcome = "N/A",
                nPatients = "40",
                endPointMetrics = listOf(
                    JsonCkbEndPointMetric(
                        id = 40,
                        trialAnalysisGroupId = 30,
                        endPoint = JsonCkbEndPoint(
                            id = 50,
                            name = "Disease Control Rate",
                            definition = "the proportion of patients who have achieved complete response, partial response, and stable disease",
                            unitOfMeasure = "Percent"
                        ),
                        endPointType = "NA",
                        value = "50.0",
                        confidenceInterval95 = null,
                        numerator = "20",
                        denominator = "40",
                        derivedMetrics = listOf()
                    ),
                    JsonCkbEndPointMetric(
                        id = 41,
                        trialAnalysisGroupId = 30,
                        endPoint = JsonCkbEndPoint(
                            id = 51,
                            name = "Objective/Overall Response Rate",
                            definition = "the proportion of patients with tumor size reduction of a predefined amount and for a minimum time period",
                            unitOfMeasure = "Percent"
                        ),
                        endPointType = "NA",
                        value = "25.0",
                        confidenceInterval95 = null,
                        numerator = "10",
                        denominator = "40",
                        derivedMetrics = listOf()
                    )
                ),
                notes = null
            )
        )
    }

    private fun createPatientPopulation2(): JsonCkbPatientPopulation {
        return JsonCkbPatientPopulation(
            id = 21,
            isControl = false,
            groupName = "Treatment 2",
            nPatientsEnrolled = null,
            nPatients = "40",
            nFemale = "21",
            nMale = "19",
            ageMin = "48",
            ageMax = "84",
            medianAge = "64",
            n65OrOlder = null,
            nEcog0 = null,
            nEcog1 = "37",
            nEcog2 = "3",
            nEcog3 = null,
            nEcog4 = null,
            highestEcogScore = 2,
            nLocalizationPrimaryTumor = null,
            otherMutations = "EGFR Staining None: 2 (5.0%), Weak 1+: 38 (95.0%)",
            nPrimaryTumorRemovedComplete = null,
            nPrimaryTumorRemovedPartial = null,
            nPrimaryTumorRemoved = null,
            nPreviousLinesOfTherapy1 = "40",
            nPreviousLinesOfTherapy2 = null,
            nPreviousLinesOfTherapy3 = null,
            nPreviousLinesOfTherapy4OrMore = null,
            nPriorSystemicTherapy = null,
            nStageDescription = null,
            nMutationStatus = null,
            nHighMicrosatelliteStability = null,
            medianFollowUpForSurvival = null,
            medianFollowUpForProgressionFreeSurvival = null,
            medianFollowUpForRandomizationToDataCutOff = null,
            metastaticSites = "Liver: 30 (75.0%), Lung: 10 (25.0%)",
            analysisGroups = createAnalysisGroupsForPopulation2(),
            efficacyEvidence = createEfficacyEvidenceForPopulation2(),
            notes = null
        )
    }

    private fun createAnalysisGroupsForPopulation2(): List<JsonCkbAnalysisGroup> {
        return listOf(
            JsonCkbAnalysisGroup(
                id = 31,
                name = "Treatment 2",
                outcome = "Positive",
                nPatients = "40",
                endPointMetrics = listOf(
                    JsonCkbEndPointMetric(
                        id = 42,
                        trialAnalysisGroupId = 31,
                        endPoint = JsonCkbEndPoint(
                            id = 52,
                            name = "Disease Control Rate",
                            definition = "the proportion of patients who have achieved complete response, partial response, and stable disease",
                            unitOfMeasure = "Percent"
                        ),
                        endPointType = "SECONDARY",
                        value = "75.0",
                        confidenceInterval95 = null,
                        numerator = "30",
                        denominator = "40",
                        derivedMetrics = listOf(
                            JsonCkbDerivedMetric(
                                relativeMetricId = 60,
                                comparatorStatistic = null,
                                comparatorStatisticType = null,
                                confidenceInterval95Cs = null,
                                pValue = "<0.0001"
                            )
                        )
                    ),
                    JsonCkbEndPointMetric(
                        id = 43,
                        trialAnalysisGroupId = 31,
                        endPoint = JsonCkbEndPoint(
                            id = 53,
                            name = "Objective/Overall Response Rate",
                            definition = "the proportion of patients with tumor size reduction of a predefined amount and for a minimum time period",
                            unitOfMeasure = "Percent"
                        ),
                        endPointType = "SECONDARY",
                        value = "50.0",
                        confidenceInterval95 = "40.0-60.0",
                        numerator = "20",
                        denominator = "40",
                        derivedMetrics = listOf(
                            JsonCkbDerivedMetric(
                                relativeMetricId = 61,
                                comparatorStatistic = null,
                                comparatorStatisticType = null,
                                confidenceInterval95Cs = null,
                                pValue = "<0.0001"
                            )
                        )
                    )
                ),
                notes = null
            )
        )
    }

    private fun createEfficacyEvidenceForPopulation2(): List<JsonCkbEfficacyEvidence> {
        return listOf(
            JsonCkbEfficacyEvidence(
                id = 70,
                approvalStatus = "Phase III",
                evidenceType = "Actionable",
                efficacyEvidence = "In a phase3 trial, treatment 1 seems better than treatment 2",
                molecularProfile = JsonCkbMolecularProfile(id = 3, profileName = "EGFR positive"),
                therapy = JsonCkbTherapy(id = 1, therapyName = "Treatment 1", synonyms = null),
                indication = JsonCkbIndication(id = 162, name = "cancer", source = "DOID"),
                responseType = "sensitive",
                references = listOf(createReference()),
                ampCapAscoEvidenceLevel = "B",
                ampCapAscoInferredTier = "I",
                referencedMetrics = listOf(
                    JsonCkbEndPointMetric(
                        id = 90,
                        trialAnalysisGroupId = 31,
                        endPoint = JsonCkbEndPoint(
                            id = 91,
                            name = "Median Overall Survival",
                            definition = "the time from randomization until death from any cause",
                            unitOfMeasure = "Months"
                        ),
                        endPointType = "PRIMARY",
                        value = "4.3",
                        confidenceInterval95 = "4.2-4.4",
                        numerator = null,
                        denominator = null,
                        derivedMetrics = listOf()
                    )
                )
            )
        )
    }

    private fun createReference(): JsonCkbReference {
        return JsonCkbReference(
            id = 92,
            pubMedId = 12345678,
            title = "In a phase3 trial, treatment 1 seems better than treatment 2",
            url = "http://www.ncbi.nlm.nih.gov/pubmed/12345678"
        )
    }
}