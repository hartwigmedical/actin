package com.hartwig.actin.algo.ckb.json

object CkbExtendedEvidenceTestFactory {

    fun createMinimalTestExtendedEvidenceDatabase(): List<CkbExtendedEvidenceEntry> {
        return listOf(
            CkbExtendedEvidenceEntry(
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

    fun createProperTestExtendedEvidenceDatabase(): List<CkbExtendedEvidenceEntry> {
        return listOf(
            CkbExtendedEvidenceEntry(
                nctId = "NCT01",
                title = "Study of pembrolizumab instead of CAPOX",
                phase = "Phase III",
                recruitment = "Completed",
                therapies = createProperTestTherapies(),
                ageGroups = listOf("adult", "senior"),
                gender = "both",
                variantRequirements = "Yes",
                sponsors = "Hartwig Medical Foundation",
                updateDate = "12/05/2023",
                indications = listOf(CkbIndication(id = 162, name = "cancer", source = "DOID")),
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

    private fun createProperTestTherapies(): List<CkbTherapy> {
        return listOf(
            CkbTherapy(
                id = 1,
                therapyName = "Capecitabine + Oxaliplatin",
                synonyms = null
            ),
            CkbTherapy(
                id = 2,
                therapyName = "Pembrolizumab",
                synonyms = null
            )
        )
    }

    private fun createProperVariantRequirementDetails(): List<CkbVariantRequirementDetail> {
        return listOf(
            CkbVariantRequirementDetail(
                molecularProfile = CkbMolecularProfile(id = 3, profileName = "MSI high"),
                requirementType = "required"
            )
        )
    }

    private fun createProperClinicalTrialLocations(): List<CkbClinicalTrialLocation> {
        return listOf(
            CkbClinicalTrialLocation(
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

    private fun createProperTrialReferences(): List<CkbTrialReference> {
        return listOf(
            CkbTrialReference(
                id = 10,
                patientPopulations = createPatientPopulations(),
                reference = createReference()
            )
        )
    }

    private fun createPatientPopulations(): List<CkbPatientPopulation> {
        return listOf(
            createPatientPopulation1(),
            createPatientPopulation2()
        )
    }

    private fun createPatientPopulation1(): CkbPatientPopulation {
        return CkbPatientPopulation(
            id = 20,
            isControl = true,
            groupName = "CAPOX",
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
            nEcog0to1 = null,
            nEcog1to2 = null,
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
            timeOfMetastases = "Both",
            therapy = createTherapyForPopulation1(),
            analysisGroups = createAnalysisGroupsForPopulation1(),
            efficacyEvidence = listOf(),
            therapyDetails = null,
            priorTherapies = "Chemo, radiation",
            race = "Asian: 16,\nWhite: 24",
            region = "",
            notes = null
        )
    }

    private fun createTherapyForPopulation1(): CkbTherapyOfPopulation {
        return CkbTherapyOfPopulation(
            id = 1,
            therapyName = "Capecitabine + Oxaliplatin",
            synonyms = null,
            therapyDescriptions = listOf(
                CkbTherapyDescription(
                    description = "Description of CAPOX",
                    references = listOf(createReference())
                )
            ),
            createDate = "01/01/2001",
            updateDate = "02/02/2002"
        )
    }

    private fun createAnalysisGroupsForPopulation1(): List<CkbAnalysisGroup> {
        return listOf(
            CkbAnalysisGroup(
                id = 30,
                name = "CAPOX",
                outcome = "N/A",
                nPatients = "40",
                endPointMetrics = listOf(
                    CkbEndPointMetric(
                        id = 40,
                        trialAnalysisGroupId = 30,
                        endPoint = CkbEndPoint(
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
                    CkbEndPointMetric(
                        id = 41,
                        trialAnalysisGroupId = 30,
                        endPoint = CkbEndPoint(
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

    private fun createPatientPopulation2(): CkbPatientPopulation {
        return CkbPatientPopulation(
            id = 21,
            isControl = false,
            groupName = "Pembrolizumab",
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
            nEcog0to1 = null,
            nEcog1to2 = null,
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
            timeOfMetastases = null,
            therapy = createTherapyForPopulation2(),
            analysisGroups = createAnalysisGroupsForPopulation2(),
            efficacyEvidence = createEfficacyEvidenceForPopulation2(),
            therapyDetails = null,
            priorTherapies = "Chemo",
            race = "Asian: 10,\nWhite: 30",
            region = "",
            notes = null
        )
    }

    private fun createTherapyForPopulation2(): CkbTherapyOfPopulation {
        return CkbTherapyOfPopulation(
            id = 2,
            therapyName = "Pembrolizumab",
            synonyms = null,
            therapyDescriptions = listOf(
                CkbTherapyDescription(
                    description = "Description of pembrolizumab",
                    references = listOf(createReference())
                )
            ),
            createDate = "01/01/2001",
            updateDate = "02/02/2002"
        )
    }

    private fun createAnalysisGroupsForPopulation2(): List<CkbAnalysisGroup> {
        return listOf(
            CkbAnalysisGroup(
                id = 31,
                name = "Pembrolizumab",
                outcome = "Positive",
                nPatients = "40",
                endPointMetrics = listOf(
                    CkbEndPointMetric(
                        id = 42,
                        trialAnalysisGroupId = 31,
                        endPoint = CkbEndPoint(
                            id = 50,
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
                            CkbDerivedMetric(
                                relativeMetricId = 60,
                                comparatorStatistic = null,
                                comparatorStatisticType = null,
                                confidenceInterval95Cs = null,
                                pValue = "<0.0001"
                            )
                        )
                    ),
                    CkbEndPointMetric(
                        id = 43,
                        trialAnalysisGroupId = 31,
                        endPoint = CkbEndPoint(
                            id = 51,
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
                            CkbDerivedMetric(
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

    private fun createEfficacyEvidenceForPopulation2(): List<CkbEfficacyEvidence> {
        return listOf(
            CkbEfficacyEvidence(
                id = 70,
                approvalStatus = "Phase III",
                evidenceType = "Actionable",
                efficacyEvidence = "In a phase3 trial, pembrolizumab seems better than CAPOX",
                molecularProfile = CkbMolecularProfile(id = 3, profileName = "MSI high"),
                therapy = CkbTherapy(id = 1, therapyName = "Treatment 1", synonyms = null),
                indication = CkbIndication(id = 162, name = "cancer", source = "DOID"),
                responseType = "sensitive",
                references = listOf(createReference()),
                ampCapAscoEvidenceLevel = "B",
                ampCapAscoInferredTier = "I",
                referencedMetrics = listOf(
                    CkbEndPointMetric(
                        id = 90,
                        trialAnalysisGroupId = 31,
                        endPoint = CkbEndPoint(
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

    private fun createReference(): CkbReference {
        return CkbReference(
            id = 92,
            pubMedId = 12345678,
            title = "In a phase3 trial, pembrolizumab seems better than CAPOX",
            url = "http://www.ncbi.nlm.nih.gov/pubmed/12345678"
        )
    }
}