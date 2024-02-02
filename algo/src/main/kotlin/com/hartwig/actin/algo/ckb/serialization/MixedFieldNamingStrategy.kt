package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

class MixedFieldNamingStrategy : FieldNamingStrategy {

    override fun translateName(field: Field): String {
        val fieldName = extractFieldName(field)

        if (SNAKE_CASE_FIELDS.contains(fieldName)) {
            return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
        }

        if (CAMEL_CASE_FIELDS.contains(fieldName)) {
            return FieldNamingPolicy.IDENTITY.translateName(field)
        }

        throw IllegalStateException("Field not configured as either 'snake_case' or 'camelCase': $fieldName")
    }

    companion object {
        private val CAMEL_CASE_FIELDS =
            setOf(
                "CkbExtendedEvidenceEntry.nctId",
                "CkbExtendedEvidenceEntry.title",
                "CkbExtendedEvidenceEntry.phase",
                "CkbExtendedEvidenceEntry.recruitment",
                "CkbExtendedEvidenceEntry.therapies",
                "CkbExtendedEvidenceEntry.ageGroups",
                "CkbExtendedEvidenceEntry.gender",
                "CkbExtendedEvidenceEntry.variantRequirements",
                "CkbExtendedEvidenceEntry.sponsors",
                "CkbExtendedEvidenceEntry.updateDate",
                "CkbExtendedEvidenceEntry.indications",
                "CkbExtendedEvidenceEntry.variantRequirementDetails",
                "CkbExtendedEvidenceEntry.clinicalTrialLocations",
                "CkbExtendedEvidenceEntry.coveredCountries",

                "CkbTherapy.id",
                "CkbTherapy.therapyName",
                "CkbTherapy.synonyms",

                "CkbIndication.id",
                "CkbIndication.name",
                "CkbIndication.source",

                "CkbVariantRequirementDetail.molecularProfile",
                "CkbVariantRequirementDetail.requirementType",

                "CkbMolecularProfile.id",
                "CkbMolecularProfile.profileName",

                "CkbClinicalTrialLocation.nctId",
                "CkbClinicalTrialLocation.facility",
                "CkbClinicalTrialLocation.city",
                "CkbClinicalTrialLocation.country",
                "CkbClinicalTrialLocation.status",
                "CkbClinicalTrialLocation.state",
                "CkbClinicalTrialLocation.zip",
                "CkbClinicalTrialLocation.clinicalTrialContacts",

                "CkbClinicalTrialContact.name",
                "CkbClinicalTrialContact.email",
                "CkbClinicalTrialContact.phone",
                "CkbClinicalTrialContact.phoneExt",
                "CkbClinicalTrialContact.role",

                "CkbReference.id",
                "CkbReference.pubMedId",
                "CkbReference.title",
                "CkbReference.url",

                "CkbEfficacyEvidence.id",
                "CkbEfficacyEvidence.approvalStatus",
                "CkbEfficacyEvidence.evidenceType",
                "CkbEfficacyEvidence.efficacyEvidence",
                "CkbEfficacyEvidence.molecularProfile",
                "CkbEfficacyEvidence.therapy",
                "CkbEfficacyEvidence.indication",
                "CkbEfficacyEvidence.responseType",
                "CkbEfficacyEvidence.references",
                "CkbEfficacyEvidence.ampCapAscoEvidenceLevel",
                "CkbEfficacyEvidence.ampCapAscoInferredTier",
                "CkbEfficacyEvidence.referencedMetrics"
            )

        private val SNAKE_CASE_FIELDS =
            setOf(
                "CkbExtendedEvidenceEntry.trialReferences",
                "CkbExtendedEvidenceEntry.otherTrialRegistrationNumbers",
                "CkbExtendedEvidenceEntry.masking",
                "CkbExtendedEvidenceEntry.allocation",
                "CkbExtendedEvidenceEntry.cancerStage",
                "CkbExtendedEvidenceEntry.diseaseAssessment",
                "CkbExtendedEvidenceEntry.diseaseAssessmentCriteria",
                "CkbExtendedEvidenceEntry.therapeuticSetting",

                "CkbTrialReference.id",
                "CkbTrialReference.patientPopulations",
                "CkbTrialReference.reference",

                "CkbPatientPopulation.id",
                "CkbPatientPopulation.isControl",
                "CkbPatientPopulation.groupName",
                "CkbPatientPopulation.nPatientsEnrolled",
                "CkbPatientPopulation.nPatients",
                "CkbPatientPopulation.nFemale",
                "CkbPatientPopulation.nMale",
                "CkbPatientPopulation.ageMin",
                "CkbPatientPopulation.ageMax",
                "CkbPatientPopulation.medianAge",
                "CkbPatientPopulation.n65OrOlder",
                "CkbPatientPopulation.nEcog0",
                "CkbPatientPopulation.nEcog1",
                "CkbPatientPopulation.nEcog2",
                "CkbPatientPopulation.nEcog3",
                "CkbPatientPopulation.nEcog4",
                "CkbPatientPopulation.highestEcogScore",
                "CkbPatientPopulation.nLocalizationPrimaryTumor",
                "CkbPatientPopulation.otherMutations",
                "CkbPatientPopulation.nPrimaryTumorRemovedComplete",
                "CkbPatientPopulation.nPrimaryTumorRemovedPartial",
                "CkbPatientPopulation.nPrimaryTumorRemoved",
                "CkbPatientPopulation.nPreviousLinesOfTherapy1",
                "CkbPatientPopulation.nPreviousLinesOfTherapy2",
                "CkbPatientPopulation.nPreviousLinesOfTherapy3",
                "CkbPatientPopulation.nPreviousLinesOfTherapy4OrMore",
                "CkbPatientPopulation.nPriorSystemicTherapy",
                "CkbPatientPopulation.nStageDescription",
                "CkbPatientPopulation.nMutationStatus",
                "CkbPatientPopulation.nHighMicrosatelliteStability",
                "CkbPatientPopulation.medianFollowUpForSurvival",
                "CkbPatientPopulation.medianFollowUpForProgressionFreeSurvival",
                "CkbPatientPopulation.medianFollowUpForRandomizationToDataCutOff",
                "CkbPatientPopulation.metastaticSites",
                "CkbPatientPopulation.analysisGroups",
                "CkbPatientPopulation.efficacyEvidence",
                "CkbPatientPopulation.notes",

                "CkbAnalysisGroup.id",
                "CkbAnalysisGroup.name",
                "CkbAnalysisGroup.outcome",
                "CkbAnalysisGroup.nPatients",
                "CkbAnalysisGroup.endPointMetrics",
                "CkbAnalysisGroup.notes",

                "CkbEndPointMetric.id",
                "CkbEndPointMetric.trialAnalysisGroupId",
                "CkbEndPointMetric.endPoint",
                "CkbEndPointMetric.endPointType",
                "CkbEndPointMetric.value",
                "CkbEndPointMetric.confidenceInterval95",
                "CkbEndPointMetric.numerator",
                "CkbEndPointMetric.denominator",
                "CkbEndPointMetric.derivedMetrics",

                "CkbEndPoint.id",
                "CkbEndPoint.name",
                "CkbEndPoint.definition",
                "CkbEndPoint.unitOfMeasure",

                "CkbDerivedMetric.relativeMetricId",
                "CkbDerivedMetric.comparatorStatistic",
                "CkbDerivedMetric.comparatorStatisticType",
                "CkbDerivedMetric.comparatorInterval95Cs",
                "CkbDerivedMetric.pValue"
            )

        private fun extractFieldName(field: Field): String {
            // There does not seem to be a proper getter on Field to get the package of the Field.
            val packageList: List<String> = field.toString().split(".")

            val className = packageList[packageList.size - 2]
            val fieldName = packageList[packageList.size - 1]

            return "$className.$fieldName"
        }
    }
}