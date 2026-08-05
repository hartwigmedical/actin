package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.configuration.ReportIntendedUse
import java.text.MessageFormat
import java.time.LocalDate
import java.util.Properties

class EvaluationLabels(private val properties: Properties) {

    val bloodTransfusion = BloodTransfusion()
    val cardiacFunction = CardiacFunction()
    val comorbidity = Comorbidity()
    val general = General()
    val infection = Infection()
    val laboratory = Laboratory()
    val medication = Medication()
    val molecular = Molecular()
    val priorTumor = PriorTumor()
    val reproduction = Reproduction()
    val surgery = Surgery()
    val toxicity = Toxicity()
    val treatment = Treatment()
    val tumor = Tumor()
    val vitalFunction = VitalFunction()
    val washout = Washout()

    inner class BloodTransfusion {
        fun hasHadRecentBloodTransfusionPass(product: Any) =
            format("bloodtransfusion.has.had.recent.blood.transfusion.pass", product)

        fun hasHadRecentBloodTransfusionFail(product: Any) =
            format("bloodtransfusion.has.had.recent.blood.transfusion.fail", product)

        fun requiresRegularHematopoieticSupportPassTransfusion(product: Any) =
            format("bloodtransfusion.requires.regular.hematopoietic.support.pass.transfusion", product)

        fun requiresRegularHematopoieticSupportPassMedication(medications: Any) =
            format("bloodtransfusion.requires.regular.hematopoietic.support.pass.medication", medications)

        fun requiresRegularHematopoieticSupportFail() =
            get("bloodtransfusion.requires.regular.hematopoietic.support.fail")
    }

    inner class CardiacFunction {
        fun ecgMeasureEvaluationFunctionNoIntervalKnown(measureName: Any) =
            format("cardiacfunction.ecg.measure.evaluation.function.no.interval.known", measureName)

        fun ecgMeasureEvaluationFunctionWrongUnit(unitsDisplay: String, expectedUnitSymbol: String) =
            format("cardiacfunction.ecg.measure.evaluation.function.wrong.unit", unitsDisplay, expectedUnitSymbol)

        fun ecgMeasureEvaluationFunctionConflicting(measureName: String) =
            format("cardiacfunction.ecg.measure.evaluation.function.conflicting", measureName)

        fun ecgMeasureEvaluationFunctionMaximumPass(measureName: Any, value: Any, unit: Any, threshold: Any) =
            format("cardiacfunction.ecg.measure.evaluation.function.maximum.pass", measureName, value, unit, threshold)

        fun ecgMeasureEvaluationFunctionMaximumFail(measureName: Any, value: Any, unit: Any, threshold: Any) =
            format("cardiacfunction.ecg.measure.evaluation.function.maximum.fail", measureName, value, unit, threshold)

        fun ecgMeasureEvaluationFunctionMinimumPass(measureName: Any, value: Any, unit: Any, threshold: Any) =
            format("cardiacfunction.ecg.measure.evaluation.function.minimum.pass", measureName, value, unit, threshold)

        fun ecgMeasureEvaluationFunctionMinimumFail(measureName: Any, value: Any, unit: Any, threshold: Any) =
            format("cardiacfunction.ecg.measure.evaluation.function.minimum.fail", measureName, value, unit, threshold)

        fun hasLongQtSyndromePass() = get("cardiacfunction.has.long.qt.syndrome.pass")

        fun hasLongQtSyndromeFail() = get("cardiacfunction.has.long.qt.syndrome.fail")

        fun hasEcgAberrationRecoverablePassBoth(aberrationsDisplay: String, arrhythmiaDisplay: String) =
            format("cardiacfunction.has.ecg.aberration.recoverable.pass.both", aberrationsDisplay, arrhythmiaDisplay)

        fun hasEcgAberrationRecoverablePassEcgOnly(aberrationsDisplay: String) =
            format("cardiacfunction.has.ecg.aberration.recoverable.pass.ecg.only", aberrationsDisplay)

        fun hasEcgAberrationRecoverablePassArrhythmiaOnly(arrhythmiaDisplay: String) =
            format("cardiacfunction.has.ecg.aberration.recoverable.pass.arrhythmia.only", arrhythmiaDisplay)

        fun hasEcgAberrationRecoverableFail() = get("cardiacfunction.has.ecg.aberration.recoverable.fail")

        fun hasNormalCardiacFunctionByMugaOrTteWarn() = get("cardiacfunction.has.normal.cardiac.function.by.muga.or.tte.warn")

        fun hasNormalCardiacFunctionByMugaOrTteUndetermined() =
            get("cardiacfunction.has.normal.cardiac.function.by.muga.or.tte.undetermined")

        fun meetsCardiacStressTestRequirementsUndetermined() =
            get("cardiacfunction.meets.cardiac.stress.test.requirements.undetermined")

        fun hasSufficientLvefRecoverableUndetermined() = get("cardiacfunction.has.sufficient.lvef.recoverable.undetermined")

        fun hasSufficientLvefRecoverablePass(lvef: Double, minLvef: Double) =
            format("cardiacfunction.has.sufficient.lvef.recoverable.pass", lvef, minLvef)

        fun hasSufficientLvefRecoverableFail(lvef: Double, minLvef: Double) =
            format("cardiacfunction.has.sufficient.lvef.recoverable.fail", lvef, minLvef)

        fun hasQtcfWithGenderFail(genderDisplay: String, otherGenderDisplay: String) =
            format("cardiacfunction.has.qtcf.with.gender.fail", genderDisplay, otherGenderDisplay)

        fun descriptionPotentialSignificantHeartDisease() = get("cardiacfunction.description.potential.significant.heart.disease")

        fun descriptionTorsadesDePointes() = get("cardiacfunction.description.torsades.de.pointes")

        fun descriptionIdiopathicSuddenDeath() = get("cardiacfunction.description.idiopathic.sudden.death")

        fun descriptionCardiovascularDisease() = get("cardiacfunction.description.cardiovascular.disease")

        fun descriptionLongQtSyndrome() = get("cardiacfunction.description.long.qt.syndrome")
    }

    inner class Comorbidity {
        fun hasAdequateVenousAccessPass() = get("comorbidity.has.adequate.venous.access.pass")

        fun hasMriScanDocumentingStableDiseaseUndetermined() =
            get("comorbidity.has.mri.scan.documenting.stable.disease.undetermined")

        fun hasPotentialContraIndicationForStereotacticRadiosurgeryFail() =
            get("comorbidity.has.potential.contra.indication.for.stereotactic.radiosurgery.fail")

        fun meetsSixMinuteWalkingTestRequirementsUndetermined() =
            get("comorbidity.meets.six.minute.walking.test.requirements.undetermined")

        fun hasSevereConcomitantIllnessWarn(whoDisplay: String) =
            format("comorbidity.has.severe.concomitant.illness.warn", whoDisplay)

        fun hasSevereConcomitantIllnessFail() = get("comorbidity.has.severe.concomitant.illness.fail")

        fun hasPotentialDisruptionOfLymphaticDrainageWarn(conditionsDisplay: String) =
            format("comorbidity.has.potential.disruption.of.lymphatic.drainage.warn", conditionsDisplay)

        fun hasPotentialDisruptionOfLymphaticDrainageFail() =
            get("comorbidity.has.potential.disruption.of.lymphatic.drainage.fail")

        fun hasPotentialAbsorptionDifficultiesPass(conditionsDisplay: String) =
            format("comorbidity.has.potential.absorption.difficulties.pass", conditionsDisplay)

        fun hasPotentialAbsorptionDifficultiesFail() = get("comorbidity.has.potential.absorption.difficulties.fail")

        fun hasChildPughScoreWarn(requestedScoresDisplay: String) =
            format("comorbidity.has.child.pugh.score.warn", requestedScoresDisplay)

        fun hasChildPughScoreUndetermined(requestedScoresDisplay: String) =
            format("comorbidity.has.child.pugh.score.undetermined", requestedScoresDisplay)

        fun hasHadOrganTransplantSinceSuffix(minYear: Int) =
            format("comorbidity.has.had.organ.transplant.since.suffix", minYear)

        fun hasHadOrganTransplantPass(dateMessage: String) =
            format("comorbidity.has.had.organ.transplant.pass", dateMessage)

        fun hasHadOrganTransplantUndetermined(minYearDisplay: String) =
            format("comorbidity.has.had.organ.transplant.undetermined", minYearDisplay)

        fun hasHadOrganTransplantFail() = get("comorbidity.has.had.organ.transplant.fail")

        fun hasInheritedPredispositionToBleedingOrThrombosisPass(detail: String) =
            format("comorbidity.has.inherited.predisposition.to.bleeding.or.thrombosis.pass", detail)

        fun hasInheritedPredispositionToBleedingOrThrombosisFail() =
            get("comorbidity.has.inherited.predisposition.to.bleeding.or.thrombosis.fail")

        fun hasContraindicationToCtRecoverablePass(detail: String) =
            format("comorbidity.has.contraindication.to.ct.recoverable.pass", detail)

        fun hasContraindicationToCtFail() = get("comorbidity.has.contraindication.to.ct.fail")

        fun hasContraindicationToMriRecoverablePass(detail: String) =
            format("comorbidity.has.contraindication.to.mri.recoverable.pass", detail)

        fun hasContraindicationToMriFail() = get("comorbidity.has.contraindication.to.mri.fail")

        fun hasHistoryOfCongestiveHeartFailureWithNyhaPass(nyhaClassDisplay: String) =
            format("comorbidity.has.history.of.congestive.heart.failure.with.nyha.pass", nyhaClassDisplay)

        fun hasHistoryOfCongestiveHeartFailureWithNyhaUndetermined(nyhaClassDisplay: String) =
            format("comorbidity.has.history.of.congestive.heart.failure.with.nyha.undetermined", nyhaClassDisplay)

        fun hasHistoryOfCongestiveHeartFailureWithNyhaFail(nyhaClassDisplay: String) =
            format("comorbidity.has.history.of.congestive.heart.failure.with.nyha.fail", nyhaClassDisplay)

        fun hasPotentialSymptomaticHypercalcemiaWarn() = get("comorbidity.has.potential.symptomatic.hypercalcemia.warn")

        fun hasPotentialSymptomaticHypercalcemiaRecoverableUndetermined() =
            get("comorbidity.has.potential.symptomatic.hypercalcemia.recoverable.undetermined")

        fun hasPotentialSymptomaticHypercalcemiaRecoverableFail() =
            get("comorbidity.has.potential.symptomatic.hypercalcemia.recoverable.fail")

        fun hasLeptomeningealDiseasePass() = get("comorbidity.has.leptomeningeal.disease.pass")

        fun hasLeptomeningealDiseaseFail() = get("comorbidity.has.leptomeningeal.disease.fail")

        fun hasLeptomeningealDiseaseWarn(suspectedSuffix: String, lesionsDisplay: String) =
            format("comorbidity.has.leptomeningeal.disease.warn", suspectedSuffix, lesionsDisplay)

        fun hasSpecificFamilyHistoryPass(conditionDescription: String) =
            format("comorbidity.has.specific.family.history.pass", conditionDescription)

        fun hasSpecificFamilyHistoryUndetermined(diseaseType: String, conditionsDisplay: String, conditionDescription: String) =
            format("comorbidity.has.specific.family.history.undetermined", diseaseType, conditionsDisplay, conditionDescription)

        fun hasSpecificFamilyHistoryFail(conditionDescription: String) =
            format("comorbidity.has.specific.family.history.fail", conditionDescription)

        fun hasPotentialUncontrolledTumorRelatedPainUndeterminedHistory() =
            get("comorbidity.has.potential.uncontrolled.tumor.related.pain.undetermined.history")

        fun hasPotentialUncontrolledTumorRelatedPainUndeterminedAcute() =
            get("comorbidity.has.potential.uncontrolled.tumor.related.pain.undetermined.acute")

        fun hasPotentialUncontrolledTumorRelatedPainWarn(activeMedicationDisplay: String) =
            format("comorbidity.has.potential.uncontrolled.tumor.related.pain.warn", activeMedicationDisplay)

        fun hasPotentialUncontrolledTumorRelatedPainWarnPlanned(plannedMedicationDisplay: String) =
            format("comorbidity.has.potential.uncontrolled.tumor.related.pain.warn.planned", plannedMedicationDisplay)

        fun hasPotentialUncontrolledTumorRelatedPainFail() =
            get("comorbidity.has.potential.uncontrolled.tumor.related.pain.fail")

        fun hasHadComorbidityWithIcdCodeIntolerancePassPart(displayList: String) =
            format("comorbidity.has.had.comorbidity.with.icd.code.intolerance.pass.part", displayList)

        fun hasHadComorbidityWithIcdCodeHistoryPassPart(displayList: String) =
            format("comorbidity.has.had.comorbidity.with.icd.code.history.pass.part", displayList)

        fun hasHadComorbidityWithIcdCodeUndeterminedUnknownExtension(displayList: String, diseaseDescription: String) =
            format("comorbidity.has.had.comorbidity.with.icd.code.undetermined.unknown.extension", displayList, diseaseDescription)

        fun hasHadComorbidityWithIcdCodeUndeterminedUnknownGrade(displayList: String) =
            format("comorbidity.has.had.comorbidity.with.icd.code.undetermined.unknown.grade", displayList)

        fun hasHadComorbidityWithIcdCodeUndeterminedUnknownExtensionAndGrade(displayList: String, diseaseDescription: String) =
            format(
                "comorbidity.has.had.comorbidity.with.icd.code.undetermined.unknown.extension.and.grade",
                displayList,
                diseaseDescription
            )

        fun hasHadComorbidityWithIcdCodeFail(diseaseDescription: String) =
            format("comorbidity.has.had.comorbidity.with.icd.code.fail", diseaseDescription)

        fun hasHadOtherConditionWithIcdCodeFromSetRecentlyPass(diseaseDescription: String, conditionsSuffix: String) =
            format("comorbidity.has.had.other.condition.with.icd.code.from.set.recently.pass", diseaseDescription, conditionsSuffix)

        fun hasHadOtherConditionWithIcdCodeFromSetRecentlyWarn(diseaseDescription: String, maxMonthsAgo: Int, conditionsSuffix: String) =
            format(
                "comorbidity.has.had.other.condition.with.icd.code.from.set.recently.warn",
                diseaseDescription,
                maxMonthsAgo,
                conditionsSuffix
            )

        fun hasHadOtherConditionWithIcdCodeFromSetRecentlyUndeterminedWithinMonths(
            diseaseDescription: String,
            conditionsSuffix: String,
            maxMonthsAgo: Int
        ) = format(
            "comorbidity.has.had.other.condition.with.icd.code.from.set.recently.undetermined.within.months",
            diseaseDescription,
            conditionsSuffix,
            maxMonthsAgo
        )

        fun hasHadOtherConditionWithIcdCodeFromSetRecentlyUndeterminedUnknownExtension(
            conditionsDisplay: String,
            diseaseDescription: String
        ) = format(
            "comorbidity.has.had.other.condition.with.icd.code.from.set.recently.undetermined.unknown.extension",
            conditionsDisplay,
            diseaseDescription
        )

        fun hasHadOtherConditionWithIcdCodeFromSetRecentlyFail(diseaseDescription: String) =
            format("comorbidity.has.had.other.condition.with.icd.code.from.set.recently.fail", diseaseDescription)

        fun descriptionAutoimmuneDisease() = get("comorbidity.description.autoimmune.disease")

        fun descriptionCardiacDisease() = get("comorbidity.description.cardiac.disease")

        fun descriptionCardiovascularDisease() = get("comorbidity.description.cardiovascular.disease")

        fun descriptionCnsDisease() = get("comorbidity.description.cns.disease")

        fun descriptionEyeDisease() = get("comorbidity.description.eye.disease")

        fun descriptionGastrointestinalDisease() = get("comorbidity.description.gastrointestinal.disease")

        fun descriptionGastrointestinalFistula() = get("comorbidity.description.gastrointestinal.fistula")

        fun descriptionImmunodeficiency() = get("comorbidity.description.immunodeficiency")

        fun descriptionInterstitialLungDisease() = get("comorbidity.description.interstitial.lung.disease")

        fun descriptionLiverDisease() = get("comorbidity.description.liver.disease")

        fun descriptionLungDisease() = get("comorbidity.description.lung.disease")

        fun descriptionPotentialRespiratoryCompromise() = get("comorbidity.description.potential.respiratory.compromise")

        fun descriptionMyocardialInfarct() = get("comorbidity.description.myocardial.infarct")

        fun descriptionPneumonitis() = get("comorbidity.description.pneumonitis")

        fun descriptionCva() = get("comorbidity.description.cva")

        fun descriptionThromboembolicEvent() = get("comorbidity.description.thromboembolic.event")

        fun descriptionArterialThromboembolicEvent() = get("comorbidity.description.arterial.thromboembolic.event")

        fun descriptionVenousThromboembolicEvent() = get("comorbidity.description.venous.thromboembolic.event")

        fun descriptionVascularDisease() = get("comorbidity.description.vascular.disease")

        fun descriptionUlcer() = get("comorbidity.description.ulcer")

        fun descriptionBleeding() = get("comorbidity.description.bleeding")

        fun descriptionWound() = get("comorbidity.description.wound")

        fun descriptionBoneFracture() = get("comorbidity.description.bone.fracture")

        fun descriptionGilbertDisease() = get("comorbidity.description.gilbert.disease")

        fun descriptionHypertension() = get("comorbidity.description.hypertension")

        fun descriptionHypotension() = get("comorbidity.description.hypotension")

        fun descriptionDiabetes() = get("comorbidity.description.diabetes")

        fun descriptionPotentialOralMedicationDifficulties() = get("comorbidity.description.potential.oral.medication.difficulties")

        fun descriptionRenalDialysis() = get("comorbidity.description.renal.dialysis")

        fun descriptionSpinalCordCompression() = get("comorbidity.description.spinal.cord.compression")

        fun descriptionPleuralEffusion() = get("comorbidity.description.pleural.effusion")

        fun descriptionPeritonealEffusion() = get("comorbidity.description.peritoneal.effusion")

        fun descriptionUnspecifiedDisease() = get("comorbidity.description.unspecified.disease")
    }

    inner class General {
        fun adheresToBloodDonationPrescriptionsPass() = get("general.adheres.to.blood.donation.prescriptions.pass")

        fun canGiveAdequateInformedConsentPass() = get("general.can.give.adequate.informed.consent.pass")

        fun hasAtLeastCertainAgePass(minAge: Int) = format("general.has.at.least.certain.age.pass", minAge)
        fun hasAtLeastCertainAgeUndetermined(birthYear: Int, minAge: Int) =
            format("general.has.at.least.certain.age.undetermined", birthYear, minAge)

        fun hasAtLeastCertainAgeFail(minAge: Int) = format("general.has.at.least.certain.age.fail", minAge)

        fun hasMaximumWhoStatusUndeterminedMissing(maximumWho: Int) =
            format("general.has.maximum.who.status.undetermined.missing", maximumWho)

        fun hasMaximumWhoStatusPass(patientWho: String, maximumWho: Int) =
            format("general.has.maximum.who.status.pass", patientWho, maximumWho)

        fun hasMaximumWhoStatusRecoverableFail(patientWho: String, maximumWho: Int) =
            format("general.has.maximum.who.status.recoverable.fail", patientWho, maximumWho)

        fun hasMaximumWhoStatusFail(patientWho: String, maximumWho: Int) =
            format("general.has.maximum.who.status.fail", patientWho, maximumWho)

        fun hasMaximumWhoStatusUndetermined(patientWho: String, maximumWho: Int) =
            format("general.has.maximum.who.status.undetermined", patientWho, maximumWho)

        fun hasMinimumLanskyKarnofskyScoreUndeterminedMissing(scoreDisplay: String, minScore: Int) =
            format("general.has.minimum.lansky.karnofsky.score.undetermined.missing", scoreDisplay, minScore)

        fun hasMinimumLanskyKarnofskyScorePass(scoreDisplay: String, minScore: Int) =
            format("general.has.minimum.lansky.karnofsky.score.pass", scoreDisplay, minScore)

        fun hasMinimumLanskyKarnofskyScoreUndetermined(scoreDisplay: String, minScore: Int) =
            format("general.has.minimum.lansky.karnofsky.score.undetermined", scoreDisplay, minScore)

        fun hasMinimumLanskyKarnofskyScoreRecoverableFail(scoreDisplay: String, minScore: Int) =
            format("general.has.minimum.lansky.karnofsky.score.recoverable.fail", scoreDisplay, minScore)

        fun hasMinimumLanskyKarnofskyScoreFail(scoreDisplay: String, minScore: Int) =
            format("general.has.minimum.lansky.karnofsky.score.fail", scoreDisplay, minScore)

        fun hasMinimumMouthOpeningUndetermined(mouthOpeningSize: Int) =
            format("general.has.minimum.mouth.opening.undetermined", mouthOpeningSize)

        fun hasSufficientLifeExpectancyPass() = get("general.has.sufficient.life.expectancy.pass")

        fun hasWhoStatusUndeterminedMissing(requiredWho: Int) = format("general.has.who.status.undetermined.missing", requiredWho)
        fun hasWhoStatusPass(patientWho: String, requiredWho: Int) = format("general.has.who.status.pass", patientWho, requiredWho)
        fun hasWhoStatusFail(patientWho: String, requiredWho: Int) = format("general.has.who.status.fail", patientWho, requiredWho)
        fun hasWhoStatusUndetermined(patientWho: String, requiredWho: Int) =
            format("general.has.who.status.undetermined", patientWho, requiredWho)

        fun isFemalePass() = get("general.is.female.pass")
        fun isFemaleFail() = get("general.is.female.fail")
        fun isFemaleUndetermined() = get("general.is.female.undetermined")

        fun isInvolvedInStudyProceduresFail() = get("general.is.involved.in.study.procedures.fail")

        fun isLegallyInstitutionalizedFail() = get("general.is.legally.institutionalized.fail")

        fun isMalePass() = get("general.is.male.pass")
        fun isMaleFail() = get("general.is.male.fail")
        fun isMaleUndetermined() = get("general.is.male.undetermined")

        fun usesTobaccoProductsUndetermined() = get("general.uses.tobacco.products.undetermined")

        fun willParticipateInTrialInCountryPass(country: String) = format("general.will.participate.in.trial.in.country.pass", country)
        fun willParticipateInTrialInCountryFail(country: String) = format("general.will.participate.in.trial.in.country.fail", country)
    }

    inner class Infection {
        fun canAdhereToAttenuatedVaccineUsePass() = get("infection.can.adhere.to.attenuated.vaccine.use.pass")

        fun hasActiveInfectionRecoverablePass(descriptionDisplay: String) =
            format("infection.has.active.infection.recoverable.pass", descriptionDisplay)

        fun hasActiveInfectionWarnAntimicrobials() = get("infection.has.active.infection.warn.antimicrobials")

        fun hasActiveInfectionWarnHistory(matchesDisplay: String) =
            format("infection.has.active.infection.warn.history", matchesDisplay)

        fun hasActiveInfectionRecoverableUndetermined() = get("infection.has.active.infection.recoverable.undetermined")

        fun hasActiveInfectionFail() = get("infection.has.active.infection.fail")

        fun hasReceivedNonLiveVaccineWithinWeeksUndetermined(minWeeks: Int) =
            format("infection.has.received.non.live.vaccine.within.weeks.undetermined", minWeeks)

        fun hasReceivedLiveVaccineWithinMonthsUndetermined(minMonths: Int) =
            format("infection.has.received.live.vaccine.within.months.undetermined", minMonths)

        fun hasSpecificInfectionPass(termDisplay: String) = format("infection.has.specific.infection.pass", termDisplay)

        fun hasSpecificInfectionUndetermined(term: String) = format("infection.has.specific.infection.undetermined", term)

        fun hasSpecificInfectionFail(term: String) = format("infection.has.specific.infection.fail", term)

        fun meetsCovid19InfectionRequirementsPass() = get("infection.meets.covid19.infection.requirements.pass")

        fun descriptionEpsteinBarrVirus() = get("infection.description.epstein.barr.virus")

        fun descriptionHepatitisAVirus() = get("infection.description.hepatitis.a.virus")

        fun descriptionHepatitisBVirus() = get("infection.description.hepatitis.b.virus")

        fun descriptionHepatitisCVirus() = get("infection.description.hepatitis.c.virus")

        fun descriptionHiv() = get("infection.description.hiv")

        fun descriptionHsv() = get("infection.description.hsv")

        fun descriptionCytomegalovirus() = get("infection.description.cytomegalovirus")

        fun descriptionTuberculosis() = get("infection.description.tuberculosis")
    }

    inner class Laboratory {
        fun hasAbnormalElectrolyteLevelsPass(measurementString: String) =
            format("laboratory.has.abnormal.electrolyte.levels.pass", measurementString)

        fun hasAbnormalElectrolyteLevelsRecoverableFail() = get("laboratory.has.abnormal.electrolyte.levels.recoverable.fail")

        fun hasAdequateOrganFunctionWarnBelowLln(measurements: String) =
            format("laboratory.has.adequate.organ.function.warn.below.lln", measurements)

        fun hasAdequateOrganFunctionWarnAboveUln(measurements: String) =
            format("laboratory.has.adequate.organ.function.warn.above.uln", measurements)

        fun hasAdequateOrganFunctionRecoverableUndetermined(labValues: String) =
            format("laboratory.has.adequate.organ.function.recoverable.undetermined", labValues)

        fun hasAdequateOrganFunctionPass() = get("laboratory.has.adequate.organ.function.pass")

        fun hasLabValueWithinInstitutionalNormalLimitRecoverableUndetermined(measurementDisplay: String) =
            format("laboratory.has.lab.value.within.institutional.normal.limit.recoverable.undetermined", measurementDisplay)

        fun hasLabValueWithinInstitutionalNormalLimitRecoverableFail(measurementDisplay: String) =
            format("laboratory.has.lab.value.within.institutional.normal.limit.recoverable.fail", measurementDisplay)

        fun hasLabValueWithinInstitutionalNormalLimitPass(measurementDisplay: String) =
            format("laboratory.has.lab.value.within.institutional.normal.limit.pass", measurementDisplay)

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedNoData() =
            get("laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.recoverable.undetermined.no.data")

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesExceedMaxBoth(asatLabValueString: String, alatLabValueString: String) =
            format(
                "laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.exceed.max.both",
                asatLabValueString,
                alatLabValueString
            )

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesExceedMax(labValueString: String, referenceString: String) =
            format("laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.exceed.max", labValueString, referenceString)

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesUndeterminedSuffixUnknownLiverMetastases() =
            get("laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.undetermined.suffix.unknown.liver.metastases")

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedWithinMarginBoth(
            asatLabValueString: String,
            alatLabValueString: String
        ) = format(
            "laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.recoverable.undetermined.within.margin.both",
            asatLabValueString,
            alatLabValueString
        )

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedWithinMargin(
            labValueString: String,
            referenceString: String
        ) = format(
            "laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.recoverable.undetermined.within.margin",
            labValueString,
            referenceString
        )

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesPass(
            asatLabValueString: String,
            alatLabValueString: String,
            asatReferenceString: String,
            alatReferenceString: String
        ) = format(
            "laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.pass",
            asatLabValueString,
            alatLabValueString,
            asatReferenceString,
            alatReferenceString
        )

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedBoth(asatMeasurement: String, alatMeasurement: String) =
            format(
                "laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.recoverable.undetermined.both",
                asatMeasurement,
                alatMeasurement
            )

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesRecoverableUndeterminedSingle(measurement: String) =
            format("laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.recoverable.undetermined.single", measurement)

        fun hasLimitedAsatAndAlatDependingOnLiverMetastasesUndeterminedUnableToDetermine() =
            get("laboratory.has.limited.asat.and.alat.depending.on.liver.metastases.undetermined.unable.to.determine")

        fun hasLimitedBilirubinPercentageOfTotalRecoverableUndetermined() =
            get("laboratory.has.limited.bilirubin.percentage.of.total.recoverable.undetermined")

        fun hasLimitedBilirubinPercentageOfTotalMessageStart(labMeasurementDisplay: String, totalMeasurementDisplay: String) =
            format("laboratory.has.limited.bilirubin.percentage.of.total.message.start", labMeasurementDisplay, totalMeasurementDisplay)

        fun hasLimitedBilirubinPercentageOfTotalPass(messageStart: String, maxPercentage: Double) =
            format("laboratory.has.limited.bilirubin.percentage.of.total.pass", messageStart, maxPercentage)

        fun hasLimitedBilirubinPercentageOfTotalRecoverableFail(messageStart: String, maxPercentage: Double) =
            format("laboratory.has.limited.bilirubin.percentage.of.total.recoverable.fail", messageStart, maxPercentage)

        fun hasLimitedDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightFail() =
            get("laboratory.has.limited.derived.creatinine.clearance.recoverable.undetermined.unknown.weight.fail")

        fun hasLimitedDerivedCreatinineClearanceRecoverableFail(code: String, maxCreatinineClearance: Double) =
            format("laboratory.has.limited.derived.creatinine.clearance.recoverable.fail", code, maxCreatinineClearance)

        fun hasLimitedDerivedCreatinineClearanceRecoverableUndetermined(code: String) =
            format("laboratory.has.limited.derived.creatinine.clearance.recoverable.undetermined", code)

        fun hasLimitedDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightPass(maxCreatinineClearance: Double) =
            format(
                "laboratory.has.limited.derived.creatinine.clearance.recoverable.undetermined.unknown.weight.pass",
                maxCreatinineClearance
            )

        fun hasLimitedDerivedCreatinineClearancePass(code: String, maxCreatinineClearance: Double) =
            format("laboratory.has.limited.derived.creatinine.clearance.pass", code, maxCreatinineClearance)

        fun hasLimitedIndirectBilirubinUlnRecoverableUndeterminedNoTotalBilirubin() =
            get("laboratory.has.limited.indirect.bilirubin.uln.recoverable.undetermined.no.total.bilirubin")

        fun hasLimitedIndirectBilirubinUlnRecoverableFail(labValueString: String, referenceString: String) =
            format("laboratory.has.limited.indirect.bilirubin.uln.recoverable.fail", labValueString, referenceString)

        fun hasLimitedIndirectBilirubinUlnRecoverableUndeterminedWithinMargin(labValueString: String, referenceString: String) =
            format(
                "laboratory.has.limited.indirect.bilirubin.uln.recoverable.undetermined.within.margin",
                labValueString,
                referenceString
            )

        fun hasLimitedIndirectBilirubinUlnRecoverableUndeterminedCannotDetermine(measurementDisplay: String) =
            format("laboratory.has.limited.indirect.bilirubin.uln.recoverable.undetermined.cannot.determine", measurementDisplay)

        fun hasLimitedIndirectBilirubinUlnPass(labValueString: String, referenceString: String) =
            format("laboratory.has.limited.indirect.bilirubin.uln.pass", labValueString, referenceString)

        fun hasLimitedLabValueRecoverableUndeterminedCouldNotConvert(measurementDisplay: String, targetUnitDisplay: String) =
            format(
                "laboratory.has.limited.lab.value.recoverable.undetermined.could.not.convert",
                measurementDisplay,
                targetUnitDisplay
            )

        fun hasLimitedLabValueConvertedFromSuffix(value: Double, unitDisplay: String) =
            format("laboratory.has.limited.lab.value.converted.from.suffix", value, unitDisplay)

        fun hasLimitedLabValueRecoverableFail(labValueString: String, refString: String) =
            format("laboratory.has.limited.lab.value.recoverable.fail", labValueString, refString)

        fun hasLimitedLabValueRecoverableUndeterminedWithinMargin(labValueString: String, refString: String) =
            format("laboratory.has.limited.lab.value.recoverable.undetermined.within.margin", labValueString, refString)

        fun hasLimitedLabValueRecoverableUndeterminedRequirements(measurementDisplay: String) =
            format("laboratory.has.limited.lab.value.recoverable.undetermined.requirements", measurementDisplay)

        fun hasLimitedLabValuePass(labValueString: String, refString: String) =
            format("laboratory.has.limited.lab.value.pass", labValueString, refString)

        fun hasLimitedLabValueUlnExceedsMax(labValueString: String, referenceString: String) =
            format("laboratory.has.limited.lab.value.uln.exceeds.max", labValueString, referenceString)

        fun hasLimitedLabValueUlnRecoverableUndeterminedCannotDetermine(measurementDisplay: String) =
            format("laboratory.has.limited.lab.value.uln.recoverable.undetermined.cannot.determine", measurementDisplay)

        fun hasLimitedLabValueUlnPass(labValueString: String, referenceString: String) =
            format("laboratory.has.limited.lab.value.uln.pass", labValueString, referenceString)

        fun hasLimitedPttRecoverableUndetermined() = get("laboratory.has.limited.ptt.recoverable.undetermined")

        fun evaluateInvalidLabValueNoMeasurement(measurementDisplay: String) =
            format("laboratory.evaluate.invalid.lab.value.no.measurement", measurementDisplay)

        fun evaluateInvalidLabValueUnexpectedUnit(measurementDisplay: String, unitDisplay: String) =
            format("laboratory.evaluate.invalid.lab.value.unexpected.unit", measurementDisplay, unitDisplay)

        fun evaluateInvalidLabValueTooOld(measurementDisplay: String) =
            format("laboratory.evaluate.invalid.lab.value.too.old", measurementDisplay)

        fun sameDateLabValueSelectorNoSharedDate(measurementsDisplay: String) =
            format("laboratory.same.date.lab.value.selector.no.shared.date", measurementsDisplay)

        fun labMeasurementEvaluatorOccurredBeforeSuffix(dateString: String) =
            format("laboratory.lab.measurement.evaluator.occurred.before.suffix", dateString)

        fun hasSufficientLabValueRecoverableUndeterminedCouldNotConvert(measurementDisplay: String, targetUnitDisplay: String) =
            format(
                "laboratory.has.sufficient.lab.value.recoverable.undetermined.could.not.convert",
                measurementDisplay,
                targetUnitDisplay
            )

        fun hasSufficientLabValueConvertedFromSuffix(value: Double, unitDisplay: String) =
            format("laboratory.has.sufficient.lab.value.converted.from.suffix", value, unitDisplay)

        fun hasSufficientLabValueRecoverableFail(labValueString: String, refString: String) =
            format("laboratory.has.sufficient.lab.value.recoverable.fail", labValueString, refString)

        fun hasSufficientLabValueRecoverableUndeterminedWithinMargin(labValueString: String, refString: String) =
            format("laboratory.has.sufficient.lab.value.recoverable.undetermined.within.margin", labValueString, refString)

        fun hasSufficientLabValueRecoverableUndeterminedCannotDetermine(measurementDisplay: String) =
            format("laboratory.has.sufficient.lab.value.recoverable.undetermined.cannot.determine", measurementDisplay)

        fun hasSufficientLabValuePass(labValueString: String, refString: String) =
            format("laboratory.has.sufficient.lab.value.pass", labValueString, refString)

        fun hasSufficientLabValueLlnExceedsMin(labValueString: String, referenceString: String) =
            format("laboratory.has.sufficient.lab.value.lln.exceeds.min", labValueString, referenceString)

        fun hasSufficientLabValueLlnRecoverableUndeterminedCannotDetermine(measurementDisplay: String) =
            format("laboratory.has.sufficient.lab.value.lln.recoverable.undetermined.cannot.determine", measurementDisplay)

        fun hasSufficientLabValueLlnPass(labValueString: String, referenceString: String) =
            format("laboratory.has.sufficient.lab.value.lln.pass", labValueString, referenceString)

        fun hasSufficientLabValueUlnExceedsMin(labValueString: String, referenceString: String) =
            format("laboratory.has.sufficient.lab.value.uln.exceeds.min", labValueString, referenceString)

        fun hasSufficientLabValueUlnRecoverableUndeterminedCannotDetermine(measurementDisplay: String) =
            format("laboratory.has.sufficient.lab.value.uln.recoverable.undetermined.cannot.determine", measurementDisplay)

        fun hasSufficientLabValueUlnPass(labValueString: String, referenceString: String) =
            format("laboratory.has.sufficient.lab.value.uln.pass", labValueString, referenceString)

        fun hasSufficientMeasuredCreatinineClearanceUndeterminedNoValue(measurementDisplay: String) =
            format("laboratory.has.sufficient.measured.creatinine.clearance.undetermined.no.value", measurementDisplay)

        fun hasSufficientMeasuredCreatinineClearanceUndeterminedZeroValue(measurementDisplay: String) =
            format("laboratory.has.sufficient.measured.creatinine.clearance.undetermined.zero.value", measurementDisplay)

        fun hasSufficientMeasuredCreatinineClearanceUndeterminedAmbiguousComparator() =
            get("laboratory.has.sufficient.measured.creatinine.clearance.undetermined.ambiguous.comparator")

        fun hasSufficientMeasuredCreatinineClearancePass(refString: String) =
            format("laboratory.has.sufficient.measured.creatinine.clearance.pass", refString)

        fun hasSufficientMeasuredCreatinineClearanceRecoverableFail(refString: String) =
            format("laboratory.has.sufficient.measured.creatinine.clearance.recoverable.fail", refString)

        fun hasSufficientMeasuredCreatinineClearanceRecoverableUndetermined() =
            get("laboratory.has.sufficient.measured.creatinine.clearance.recoverable.undetermined")

        fun hasSpecificAlbiGradeCannotCalculate() = get("laboratory.has.specific.albi.grade.cannot.calculate")

        fun hasSpecificAlbiGradePass() = get("laboratory.has.specific.albi.grade.pass")

        fun hasSpecificAlbiGradeOccurredBeforeSuffix(date: LocalDate) =
            format("laboratory.has.specific.albi.grade.occurred.before.suffix", date)

        fun hasSpecificAlbiGradeRecoverableFail(albiGradeDisplay: String, gradeDisplay: String) =
            format("laboratory.has.specific.albi.grade.recoverable.fail", albiGradeDisplay, gradeDisplay)

        fun hasSufficientDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightFail(unitDisplay: String) =
            format("laboratory.has.sufficient.derived.creatinine.clearance.recoverable.undetermined.unknown.weight.fail", unitDisplay)

        fun hasSufficientDerivedCreatinineClearanceCockcroftGaultRecoverableFail(formattedValue: String, minCreatinineClearance: Double) =
            format(
                "laboratory.has.sufficient.derived.creatinine.clearance.cockcroft.gault.recoverable.fail",
                formattedValue,
                minCreatinineClearance
            )

        fun hasSufficientDerivedCreatinineClearanceCockcroftGaultRecoverableUndetermined() =
            get("laboratory.has.sufficient.derived.creatinine.clearance.cockcroft.gault.recoverable.undetermined")

        fun hasSufficientDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightPass(
            unitDisplay: String,
            minCreatinineClearance: Double
        ) = format(
            "laboratory.has.sufficient.derived.creatinine.clearance.recoverable.undetermined.unknown.weight.pass",
            unitDisplay,
            minCreatinineClearance
        )

        fun hasSufficientDerivedCreatinineClearanceCockcroftGaultPass(formattedValue: String, minCreatinineClearance: Double) =
            format(
                "laboratory.has.sufficient.derived.creatinine.clearance.cockcroft.gault.pass",
                formattedValue,
                minCreatinineClearance
            )

        fun hasSufficientDerivedCreatinineClearanceRecoverableFail(code: String, minCreatinineClearance: Double) =
            format("laboratory.has.sufficient.derived.creatinine.clearance.recoverable.fail", code, minCreatinineClearance)

        fun hasSufficientDerivedCreatinineClearanceRecoverableUndetermined(code: String) =
            format("laboratory.has.sufficient.derived.creatinine.clearance.recoverable.undetermined", code)

        fun hasSufficientDerivedCreatinineClearancePass(code: String, minCreatinineClearance: Double) =
            format("laboratory.has.sufficient.derived.creatinine.clearance.pass", code, minCreatinineClearance)

        fun hasLimitedSystemicImmuneInflammationIndexCannotCalculate() =
            get("laboratory.has.limited.systemic.immune.inflammation.index.cannot.calculate")

        fun hasLimitedSystemicImmuneInflammationIndexPass(index: Double) =
            format("laboratory.has.limited.systemic.immune.inflammation.index.pass", index)

        fun hasLimitedSystemicImmuneInflammationIndexOccurredBeforeSuffix(date: LocalDate) =
            format("laboratory.has.limited.systemic.immune.inflammation.index.occurred.before.suffix", date)

        fun hasLimitedSystemicImmuneInflammationIndexRecoverableFail(index: Double) =
            format("laboratory.has.limited.systemic.immune.inflammation.index.recoverable.fail", index)

        fun undeterminedLabValue(measure: String) = format("laboratory.undetermined.lab.value", measure)

        fun descriptionCorrectedMagnesium() = get("laboratory.description.corrected.magnesium")

        fun descriptionCorrectedPotassium() = get("laboratory.description.corrected.potassium")

        fun descriptionFastingPlasmaGlucose() = get("laboratory.description.fasting.plasma.glucose")

        fun descriptionHlaAntibodiesAgainstPdcLine() = get("laboratory.description.hla.antibodies.against.pdc.line")

        fun descriptionPotentialHypokalemia() = get("laboratory.description.potential.hypokalemia")

        fun descriptionPotentialHypomagnesemia() = get("laboratory.description.potential.hypomagnesemia")

        fun descriptionPotentialHypocalcemia() = get("laboratory.description.potential.hypocalcemia")
    }

    inner class Medication {
        fun medicationNotProvided() = get("medication.medication.not.provided")

        fun currentlyGetsHerbalMedicationUndeterminedActive() =
            get("medication.currently.gets.herbal.medication.undetermined.active")

        fun currentlyGetsHerbalMedicationUndeterminedPlanned() =
            get("medication.currently.gets.herbal.medication.undetermined.planned")

        fun currentlyGetsHerbalMedicationFail() = get("medication.currently.gets.herbal.medication.fail")

        fun currentlyGetsMedicationOfNameRecoverablePass(termsDisplay: String) =
            format("medication.currently.gets.medication.of.name.recoverable.pass", termsDisplay)

        fun currentlyGetsMedicationOfNameWarn(termsDisplay: String) =
            format("medication.currently.gets.medication.of.name.warn", termsDisplay)

        fun currentlyGetsMedicationOfNameRecoverableFail(termsDisplay: String) =
            format("medication.currently.gets.medication.of.name.recoverable.fail", termsDisplay)

        fun currentlyGetsMedicationOfAtcLevelRecoverablePass(categoryName: String, medicationsDisplay: String) =
            format("medication.currently.gets.medication.of.atc.level.recoverable.pass", categoryName, medicationsDisplay)

        fun currentlyGetsMedicationOfAtcLevelWarn(categoryName: String, medicationsDisplay: String) =
            format("medication.currently.gets.medication.of.atc.level.warn", categoryName, medicationsDisplay)

        fun currentlyGetsMedicationOfAtcLevelRecoverableFail(categoryName: String) =
            format("medication.currently.gets.medication.of.atc.level.recoverable.fail", categoryName)

        fun hasRecentlyReceivedMedicationOfAtcLevelUndetermined(categoryName: String) =
            format("medication.has.recently.received.medication.of.atc.level.undetermined", categoryName)

        fun hasRecentlyReceivedMedicationOfAtcLevelRecoverablePass(categoryName: String, medicationsDisplay: String) =
            format("medication.has.recently.received.medication.of.atc.level.recoverable.pass", categoryName, medicationsDisplay)

        fun hasRecentlyReceivedMedicationOfAtcLevelRecoverableFail(categoryName: String) =
            format("medication.has.recently.received.medication.of.atc.level.recoverable.fail", categoryName)

        fun currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationRecoverableFailNoMedication(typesDisplay: String) =
            format(
                "medication.currently.gets.any.non.evaluable.transporter.substrate.or.inhibiting.medication.recoverable.fail.no.medication",
                typesDisplay
            )

        fun currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationRecoverableFailNoActiveOrPlanned(
            typesDisplay: String
        ) = format(
            "medication.currently.gets.any.non.evaluable.transporter.substrate.or.inhibiting.medication.recoverable.fail.no.active.or.planned",
            typesDisplay
        )

        fun currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationWarn(typesDisplay: String) =
            format("medication.currently.gets.any.non.evaluable.transporter.substrate.or.inhibiting.medication.warn", typesDisplay)

        fun currentlyGetsQtProlongatingMedicationRecoverablePass(medicationsDisplay: String) =
            format("medication.currently.gets.qt.prolongating.medication.recoverable.pass", medicationsDisplay)

        fun currentlyGetsQtProlongatingMedicationWarn(medicationsDisplay: String) =
            format("medication.currently.gets.qt.prolongating.medication.warn", medicationsDisplay)

        fun currentlyGetsQtProlongatingMedicationRecoverableFail() =
            get("medication.currently.gets.qt.prolongating.medication.recoverable.fail")

        fun currentlyGetsAnyCypMedicationOfTypesRecoverablePass(typesDisplay: String, medicationsDisplay: String) =
            format("medication.currently.gets.any.cyp.medication.of.types.recoverable.pass", typesDisplay, medicationsDisplay)

        fun currentlyGetsAnyCypMedicationOfTypesWarn(typesDisplay: String, medicationsDisplay: String) =
            format("medication.currently.gets.any.cyp.medication.of.types.warn", typesDisplay, medicationsDisplay)

        fun currentlyGetsAnyCypMedicationOfTypesRecoverableFail(typesDisplay: String) =
            format("medication.currently.gets.any.cyp.medication.of.types.recoverable.fail", typesDisplay)

        fun currentlyGetsCypXInhibitingMedicationRecoverablePass(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inhibiting.medication.recoverable.pass", cyp, medicationsDisplay)

        fun currentlyGetsCypXInhibitingMedicationUndetermined(cyp: String) =
            format("medication.currently.gets.cyp.x.inhibiting.medication.undetermined", cyp)

        fun currentlyGetsCypXInhibitingMedicationWarn(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inhibiting.medication.warn", cyp, medicationsDisplay)

        fun currentlyGetsCypXInhibitingMedicationRecoverableFail(cyp: String) =
            format("medication.currently.gets.cyp.x.inhibiting.medication.recoverable.fail", cyp)

        fun currentlyGetsCypXInducingMedicationRecoverablePass(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inducing.medication.recoverable.pass", cyp, medicationsDisplay)

        fun currentlyGetsCypXInducingMedicationUndetermined(cyp: String) =
            format("medication.currently.gets.cyp.x.inducing.medication.undetermined", cyp)

        fun currentlyGetsCypXInducingMedicationWarn(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inducing.medication.warn", cyp, medicationsDisplay)

        fun currentlyGetsCypXInducingMedicationRecoverableFail(cyp: String) =
            format("medication.currently.gets.cyp.x.inducing.medication.recoverable.fail", cyp)

        fun currentlyGetsCypXInhibitingOrInducingMedicationRecoverablePass(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inhibiting.or.inducing.medication.recoverable.pass", cyp, medicationsDisplay)

        fun currentlyGetsCypXInhibitingOrInducingMedicationWarn(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.inhibiting.or.inducing.medication.warn", cyp, medicationsDisplay)

        fun currentlyGetsCypXInhibitingOrInducingMedicationUndetermined(cyp: String) =
            format("medication.currently.gets.cyp.x.inhibiting.or.inducing.medication.undetermined", cyp)

        fun currentlyGetsCypXInhibitingOrInducingMedicationRecoverableFail(cyp: String) =
            format("medication.currently.gets.cyp.x.inhibiting.or.inducing.medication.recoverable.fail", cyp)

        fun currentlyGetsCypXSubstrateMedicationRecoverablePass(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.substrate.medication.recoverable.pass", cyp, medicationsDisplay)

        fun currentlyGetsCypXSubstrateMedicationUndetermined(cyp: String) =
            format("medication.currently.gets.cyp.x.substrate.medication.undetermined", cyp)

        fun currentlyGetsCypXSubstrateMedicationWarn(cyp: String, medicationsDisplay: String) =
            format("medication.currently.gets.cyp.x.substrate.medication.warn", cyp, medicationsDisplay)

        fun currentlyGetsCypXSubstrateMedicationRecoverableFail(cyp: String) =
            format("medication.currently.gets.cyp.x.substrate.medication.recoverable.fail", cyp)

        fun currentlyGetsTransporterInteractingMedicationRecoverablePass(
            termToFind: String,
            typeText: String,
            medicationsDisplay: String
        ) = format(
            "medication.currently.gets.transporter.interacting.medication.recoverable.pass", termToFind, typeText, medicationsDisplay
        )

        fun currentlyGetsTransporterInteractingMedicationWarn(termToFind: String, typeText: String, medicationsDisplay: String) =
            format("medication.currently.gets.transporter.interacting.medication.warn", termToFind, typeText, medicationsDisplay)

        fun currentlyGetsTransporterInteractingMedicationRecoverableFail(termToFind: String, typeText: String) =
            format("medication.currently.gets.transporter.interacting.medication.recoverable.fail", termToFind, typeText)

        fun currentlyGetsStableMedicationOfCategoryRecoverablePass(categoriesDisplay: String) =
            format("medication.currently.gets.stable.medication.of.category.recoverable.pass", categoriesDisplay)

        fun currentlyGetsStableMedicationOfCategoryRecoverableFail(categoriesDisplay: String) =
            format("medication.currently.gets.stable.medication.of.category.recoverable.fail", categoriesDisplay)

        fun hasRecentlyReceivedCypXInducingMedicationRecoverablePass(cyp: String, medicationsDisplay: String) =
            format("medication.has.recently.received.cyp.x.inducing.medication.recoverable.pass", cyp, medicationsDisplay)

        fun hasRecentlyReceivedCypXInducingMedicationUndetermined(cyp: String) =
            format("medication.has.recently.received.cyp.x.inducing.medication.undetermined", cyp)

        fun hasRecentlyReceivedCypXInducingMedicationRecoverableFail(cyp: String) =
            format("medication.has.recently.received.cyp.x.inducing.medication.recoverable.fail", cyp)
    }

    inner class Molecular {
        fun anyGeneFromSetIsNotExpressedUndetermined(genes: String) =
            format("molecular.any.gene.from.set.is.not.expressed.undetermined", genes)

        fun anyGeneHasDriverEventWithApprovedTherapyFailNoData() =
            get("molecular.any.gene.has.driver.event.with.approved.therapy.fail.no.data")

        fun anyGeneHasDriverEventWithApprovedTherapyUndeterminedUnevaluated(genes: String) =
            format("molecular.any.gene.has.driver.event.with.approved.therapy.undetermined.unevaluated", genes)

        fun anyGeneHasDriverEventWithApprovedTherapyUndetermined() =
            get("molecular.any.gene.has.driver.event.with.approved.therapy.undetermined")

        fun hasAvailablePdl1StatusPass() = get("molecular.has.available.pdl1.status.pass")
        fun hasAvailablePdl1StatusRecoverableFail() = get("molecular.has.available.pdl1.status.fail")

        fun hasAvailableProteinExpressionPass(protein: String) = format("molecular.has.available.protein.expression.pass", protein)
        fun hasAvailableProteinExpressionRecoverableFail(protein: String) = format("molecular.has.available.protein.expression.fail", protein)

        fun hasCodeletionOfChromosomeArmsUndetermined(chromosomeArm1: String, chromosomeArm2: String) =
            format("molecular.has.codeletion.of.chromosome.arms.undetermined", chromosomeArm1, chromosomeArm2)

        fun hasHeterozygousDpydDeficiencyUndeterminedNoData() = get("molecular.has.heterozygous.dpyd.deficiency.undetermined.no.data")
        fun hasHeterozygousDpydDeficiencyUndetermined() = get("molecular.has.heterozygous.dpyd.deficiency.undetermined")
        fun hasHeterozygousDpydDeficiencyPass() = get("molecular.has.heterozygous.dpyd.deficiency.pass")
        fun hasHeterozygousDpydDeficiencyFail() = get("molecular.has.heterozygous.dpyd.deficiency.fail")

        fun hasHomozygousDpydDeficiencyUndeterminedNoData() = get("molecular.has.homozygous.dpyd.deficiency.undetermined.no.data")
        fun hasHomozygousDpydDeficiencyUndetermined() = get("molecular.has.homozygous.dpyd.deficiency.undetermined")
        fun hasHomozygousDpydDeficiencyPass() = get("molecular.has.homozygous.dpyd.deficiency.pass")
        fun hasHomozygousDpydDeficiencyFail() = get("molecular.has.homozygous.dpyd.deficiency.fail")

        fun hasKnownHpvStatusPassWgs() = get("molecular.has.known.hpv.status.pass.wgs")
        fun hasKnownHpvStatusPass() = get("molecular.has.known.hpv.status.pass")
        fun hasKnownHpvStatusPassIhc() = get("molecular.has.known.hpv.status.pass.ihc")
        fun hasKnownHpvStatusWarnIndeterminate() = get("molecular.has.known.hpv.status.warn.indeterminate")
        fun hasKnownHpvStatusRecoverableFailNoTumorCells() = get("molecular.has.known.hpv.status.fail.no.tumor.cells")
        fun hasKnownHpvStatusRecoverableFail() = get("molecular.has.known.hpv.status.fail")

        fun hasMtapDeletionFail() = get("molecular.has.mtap.deletion.fail")
        fun hasMtapDeletionWarnCorrelated() = get("molecular.has.mtap.deletion.warn.correlated")
        fun hasMtapDeletionUndetermined() = get("molecular.has.mtap.deletion.undetermined")

        fun hasPositivePetScanForTracerUndetermined(tracer: String) =
            format("molecular.has.positive.pet.scan.for.tracer.undetermined", tracer)

        fun hasSufficientTumorMutationalBurdenUndetermined(minTumorMutationalBurden: Double) =
            format("molecular.has.sufficient.tumor.mutational.burden.undetermined", minTumorMutationalBurden)

        fun hasSufficientTumorMutationalBurdenPass(minTumorMutationalBurden: Double) =
            format("molecular.has.sufficient.tumor.mutational.burden.pass", minTumorMutationalBurden)

        fun hasSufficientTumorMutationalBurdenWarn(tumorMutationalBurden: Double, minTumorMutationalBurden: Double) =
            format("molecular.has.sufficient.tumor.mutational.burden.warn", tumorMutationalBurden, minTumorMutationalBurden)

        fun hasSufficientTumorMutationalBurdenFail(tumorMutationalBurden: Double, minTumorMutationalBurden: Double) =
            format("molecular.has.sufficient.tumor.mutational.burden.fail", tumorMutationalBurden, minTumorMutationalBurden)

        fun hasTumorMutationalLoadWithinRangeUndetermined() =
            get("molecular.has.tumor.mutational.load.within.range.undetermined")

        fun hasTumorMutationalLoadWithinRangePass(message: String) =
            format("molecular.has.tumor.mutational.load.within.range.pass", message)

        fun hasTumorMutationalLoadWithinRangeWarn(tumorMutationalLoad: Int, message: String) =
            format("molecular.has.tumor.mutational.load.within.range.warn", tumorMutationalLoad, message)

        fun hasTumorMutationalLoadWithinRangeFail(tumorMutationalLoad: Int, message: String) =
            format("molecular.has.tumor.mutational.load.within.range.fail", tumorMutationalLoad, message)

        fun hasTumorMutationalLoadWithinRangeMessageAbove(minTumorMutationalLoad: Int) =
            format("molecular.has.tumor.mutational.load.within.range.message.above", minTumorMutationalLoad)

        fun hasTumorMutationalLoadWithinRangeMessageBetween(minTumorMutationalLoad: Int, maxTumorMutationalLoad: Int) =
            format("molecular.has.tumor.mutational.load.within.range.message.between", minTumorMutationalLoad, maxTumorMutationalLoad)

        fun hasUgt1a1HaplotypeUndeterminedNoData() = get("molecular.has.ugt1a1.haplotype.undetermined.no.data")
        fun hasUgt1a1HaplotypeUndetermined() = get("molecular.has.ugt1a1.haplotype.undetermined")
        fun hasUgt1a1HaplotypePass(haplotypeToFind: String) = format("molecular.has.ugt1a1.haplotype.pass", haplotypeToFind)
        fun hasUgt1a1HaplotypeFail(haplotypeToFind: String) = format("molecular.has.ugt1a1.haplotype.fail", haplotypeToFind)

        fun hrdStatusIsAvailablePass() = get("molecular.hrd.status.is.available.pass")
        fun hrdStatusIsAvailableRecoverableFail() = get("molecular.hrd.status.is.available.fail")

        fun mmrStatusIsAvailablePass() = get("molecular.mmr.status.is.available.pass")
        fun mmrStatusIsAvailableRecoverableFail() = get("molecular.mmr.status.is.available.fail")

        fun molecularResultsAreGenerallyAvailablePass() = get("molecular.molecular.results.are.generally.available.pass")
        fun molecularResultsAreGenerallyAvailableRecoverableFail() = get("molecular.molecular.results.are.generally.available.fail")

        fun molecularResultsAreKnownForGenePassWgs(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.pass.wgs", gene)

        fun molecularResultsAreKnownForGenePassOncoact(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.pass.oncoact", gene)

        fun molecularResultsAreKnownForGeneWarnOncoactUnsure(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.warn.oncoact.unsure", gene)

        fun molecularResultsAreKnownForGenePassPanel(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.pass.panel", gene)

        fun molecularResultsAreKnownForGenePassIhc(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.pass.ihc", gene)

        fun molecularResultsAreKnownForGeneUndeterminedWgs(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.undetermined.wgs", gene)

        fun molecularResultsAreKnownForGeneUndeterminedOncoact(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.undetermined.oncoact", gene)

        fun molecularResultsAreKnownForGeneUndeterminedIhc(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.undetermined.ihc", gene)

        fun molecularResultsAreKnownForGeneRecoverableFail(gene: String) =
            format("molecular.molecular.results.are.known.for.gene.fail", gene)

        fun molecularResultsAreKnownForPromoterOfGenePass(gene: String) =
            format("molecular.molecular.results.are.known.for.promoter.of.gene.pass", gene)

        fun molecularResultsAreKnownForPromoterOfGeneWarn(gene: String) =
            format("molecular.molecular.results.are.known.for.promoter.of.gene.warn", gene)

        fun molecularResultsAreKnownForPromoterOfGeneRecoverableFail(gene: String) =
            format("molecular.molecular.results.are.known.for.promoter.of.gene.fail", gene)

        fun nsclcDriverGeneStatusesAreAvailablePass() = get("molecular.nsclc.driver.gene.statuses.are.available.pass")

        fun nsclcDriverGeneStatusesAreAvailableFailInsufficientQuality() =
            get("molecular.nsclc.driver.gene.statuses.are.available.fail.insufficient.quality")

        fun nsclcDriverGeneStatusesAreAvailableFailMissing(missing: String) =
            format("molecular.nsclc.driver.gene.statuses.are.available.fail.missing", missing)

        fun genesMeetSpecificMrnaExpressionRequirementsUndetermined(genes: String) =
            format("molecular.genes.meet.specific.mrna.expression.requirements.undetermined", genes)

        fun proteinHasPolymorphismUndetermined(protein: String, polymorphism: String) =
            format("molecular.protein.has.polymorphism.undetermined", protein, polymorphism)

        fun proteinIsExpressedByIhcUndeterminedNoResult(protein: String) =
            format("molecular.protein.is.expressed.by.ihc.undetermined.no.result", protein)

        fun proteinIsExpressedByIhcPass(protein: String) = format("molecular.protein.is.expressed.by.ihc.pass", protein)
        fun proteinIsExpressedByIhcFail(protein: String) = format("molecular.protein.is.expressed.by.ihc.fail", protein)
        fun proteinIsExpressedByIhcWarn(protein: String) = format("molecular.protein.is.expressed.by.ihc.warn", protein)

        fun proteinIsLostByIhcUndeterminedNoResult(protein: String) =
            format("molecular.protein.is.lost.by.ihc.undetermined.no.result", protein)

        fun proteinIsLostByIhcPass(protein: String) = format("molecular.protein.is.lost.by.ihc.pass", protein)
        fun proteinIsLostByIhcFail(protein: String) = format("molecular.protein.is.lost.by.ihc.fail", protein)
        fun proteinIsLostByIhcWarn(protein: String) = format("molecular.protein.is.lost.by.ihc.warn", protein)

        fun proteinIsWildTypeByIhcUndeterminedNoResult(protein: String) =
            format("molecular.protein.is.wild.type.by.ihc.undetermined.no.result", protein)

        fun proteinIsWildTypeByIhcPass(protein: String) = format("molecular.protein.is.wild.type.by.ihc.pass", protein)
        fun proteinIsWildTypeByIhcWarn(protein: String) = format("molecular.protein.is.wild.type.by.ihc.warn", protein)

        fun anyGeneFromSetIsOverexpressedWarn(genes: String) = format("molecular.any.gene.from.set.is.overexpressed.warn", genes)
        fun anyGeneFromSetIsOverexpressedUndetermined(genes: String, dnaClarification: String) =
            format("molecular.any.gene.from.set.is.overexpressed.undetermined", genes, dnaClarification)

        fun geneHasSpecificExonSkippingMessagePrefix(exonToSkip: Int) =
            format("molecular.gene.has.specific.exon.skipping.message.prefix", exonToSkip)

        fun geneHasSpecificExonSkippingPassFusion(gene: String, exonToSkip: Int, events: String) =
            format("molecular.gene.has.specific.exon.skipping.pass.fusion", gene, exonToSkip, events)

        fun geneHasSpecificExonSkippingPassFusionWithConfirmed(gene: String, exonToSkip: Int, fusionEvents: String, confirmedEvents: String) =
            format("molecular.gene.has.specific.exon.skipping.pass.fusion.with.confirmed", gene, exonToSkip, fusionEvents, confirmedEvents)

        fun geneHasSpecificExonSkippingWarnFusionWithPotential(gene: String, exonToSkip: Int, fusionEvents: String, splicingEvents: String) =
            format("molecular.gene.has.specific.exon.skipping.warn.fusion.with.potential", gene, exonToSkip, fusionEvents, splicingEvents)

        fun geneHasSpecificExonSkippingPassConfirmed(gene: String, exonToSkip: Int, events: String) =
            format("molecular.gene.has.specific.exon.skipping.pass.confirmed", gene, exonToSkip, events)

        fun geneHasSpecificExonSkippingWarnPotential(gene: String, exonToSkip: Int, events: String) =
            format("molecular.gene.has.specific.exon.skipping.warn.potential", gene, exonToSkip, events)

        fun geneHasSpecificExonSkippingWarnUnknownRelevance(gene: String, exonToSkip: Int, events: String) =
            format("molecular.gene.has.specific.exon.skipping.warn.unknown.relevance", gene, exonToSkip, events)

        fun geneHasSpecificExonSkippingFail(gene: String, exonToSkip: Int) =
            format("molecular.gene.has.specific.exon.skipping.fail", gene, exonToSkip)

        fun geneHasSufficientCopyNumberMessagePrefix() = get("molecular.gene.has.sufficient.copy.number.message.prefix")

        fun geneHasSufficientCopyNumberPass(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.pass", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberPassFullAmpAssumed(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.pass.full.amp.assumed", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberWarnFullAmpUndetermined(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.warn.full.amp.undetermined", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberFail(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.fail", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberWarnLossOfFunction(gene: String, requestedMinCopyNumber: Int, evidenceSource: String) =
            format("molecular.gene.has.sufficient.copy.number.warn.loss.of.function", gene, requestedMinCopyNumber, evidenceSource)

        fun geneHasSufficientCopyNumberWarnTsg(gene: String, requestedMinCopyNumber: Int, evidenceSource: String) =
            format("molecular.gene.has.sufficient.copy.number.warn.tsg", gene, requestedMinCopyNumber, evidenceSource)

        fun geneHasSufficientCopyNumberWarnNonCanonical(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.warn.non.canonical", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberWarnPartial(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.warn.partial", gene, requestedMinCopyNumber)

        fun geneHasSufficientCopyNumberWarnPartialAmpUndetermined(gene: String, requestedMinCopyNumber: Int) =
            format("molecular.gene.has.sufficient.copy.number.warn.partial.amp.undetermined", gene, requestedMinCopyNumber)

        fun geneHasUtr3LossMessagePrefix() = get("molecular.gene.has.utr3.loss.message.prefix")

        fun geneHasUtr3LossPass(events: String, gene: String) = format("molecular.gene.has.utr3.loss.pass", events, gene)
        fun geneHasUtr3LossFail(gene: String) = format("molecular.gene.has.utr3.loss.fail", gene)
        fun geneHasUtr3LossWarnUnreportable(events: String, gene: String) =
            format("molecular.gene.has.utr3.loss.warn.unreportable", events, gene)

        fun geneHasUtr3LossWarnVus(events: String, gene: String) = format("molecular.gene.has.utr3.loss.warn.vus", events, gene)
        fun geneHasUtr3LossWarnDisruption(events: String, gene: String) =
            format("molecular.gene.has.utr3.loss.warn.disruption", events, gene)

        fun geneIsWildTypeMessagePrefix() = get("molecular.gene.is.wild.type.message.prefix")

        fun geneIsWildTypeFail(gene: String, events: String) = format("molecular.gene.is.wild.type.fail", gene, events)
        fun geneIsWildTypeWarnLowPurity(gene: String) = format("molecular.gene.is.wild.type.warn.low.purity", gene)
        fun geneIsWildTypePass(gene: String) = format("molecular.gene.is.wild.type.pass", gene)
        fun geneIsWildTypeWarnNoEffect(events: String, gene: String, evidenceSource: String) =
            format("molecular.gene.is.wild.type.warn.no.effect", events, gene, evidenceSource)

        fun geneIsWildTypeWarnPotentiallyWildtype(events: String, gene: String) =
            format("molecular.gene.is.wild.type.warn.potentially.wildtype", events, gene)

        fun hasAnyHlaTypeUndeterminedNotTested() = get("molecular.has.any.hla.type.undetermined.not.tested")
        fun hasAnyHlaTypeUndeterminedUnreliable() = get("molecular.has.any.hla.type.undetermined.unreliable")
        fun hasAnyHlaTypeFail(requiredTypes: String) = format("molecular.has.any.hla.type.fail", requiredTypes)
        fun hasAnyHlaTypeWarnQuality(matchedEvents: String) = format("molecular.has.any.hla.type.warn.quality", matchedEvents)
        fun hasAnyHlaTypePassNoSomatic(matchingAlleles: String) = format("molecular.has.any.hla.type.pass.no.somatic", matchingAlleles)
        fun hasAnyHlaTypeWarnSomatic(matchingAlleles: String) = format("molecular.has.any.hla.type.warn.somatic", matchingAlleles)
        fun hasAnyHlaTypeWarnLowCopyNumber(matchingAlleles: String) =
            format("molecular.has.any.hla.type.warn.low.copy.number", matchingAlleles)

        fun hasAnyHlaTypePass(matchingAlleles: String) = format("molecular.has.any.hla.type.pass", matchingAlleles)

        fun hasHer2ExpressionByIhcUndeterminedNoTest() = get("molecular.has.her2.expression.by.ihc.undetermined.no.test")
        fun hasHer2ExpressionByIhcSuffixErbb2Amplified() = get("molecular.has.her2.expression.by.ihc.suffix.erbb2.amplified")
        fun hasHer2ExpressionByIhcSuffixErbb2NotAmplified() = get("molecular.has.her2.expression.by.ihc.suffix.erbb2.not.amplified")
        fun hasHer2ExpressionByIhcWarnStatus(ihcResultString: String, suffix: String) =
            format("molecular.has.her2.expression.by.ihc.warn.status", ihcResultString, suffix)

        fun hasHer2ExpressionByIhcPass(ihcResultString: String) = format("molecular.has.her2.expression.by.ihc.pass", ihcResultString)
        fun hasHer2ExpressionByIhcFail(ihcResultString: String) = format("molecular.has.her2.expression.by.ihc.fail", ihcResultString)
        fun hasHer2ExpressionByIhcUndeterminedScore(ihcResultString: String) =
            format("molecular.has.her2.expression.by.ihc.undetermined.score", ihcResultString)

        fun evaluationFunctionNoSufficientQuality() = get("molecular.evaluation.function.no.sufficient.quality")
        fun evaluationFunctionInsufficientData() = get("molecular.evaluation.function.insufficient.data")

        fun isHomologousRecombinationDeficientUndeterminedBiallelic(genes: String) =
            format("molecular.is.homologous.recombination.deficient.undetermined.biallelic", genes)

        fun isHomologousRecombinationDeficientUndeterminedNonBiallelic(genes: String) =
            format("molecular.is.homologous.recombination.deficient.undetermined.non.biallelic", genes)

        fun isHomologousRecombinationDeficientUndeterminedUnknownAllelic(genes: String) =
            format("molecular.is.homologous.recombination.deficient.undetermined.unknown.allelic", genes)

        fun isHomologousRecombinationDeficientUndetermined() =
            get("molecular.is.homologous.recombination.deficient.undetermined")

        fun isHomologousRecombinationDeficientPass(genes: String) =
            format("molecular.is.homologous.recombination.deficient.pass", genes)

        fun isHomologousRecombinationDeficientWarnNonBiallelic(genes: String) =
            format("molecular.is.homologous.recombination.deficient.warn.non.biallelic", genes)

        fun isHomologousRecombinationDeficientWarnNoDriver() =
            get("molecular.is.homologous.recombination.deficient.warn.no.driver")

        fun isHomologousRecombinationDeficientFail() = get("molecular.is.homologous.recombination.deficient.fail")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXUndeterminedBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.undetermined.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXUndeterminedNonBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.undetermined.non.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXUndetermined() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.undetermined")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXFail() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.fail")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXFailVariant(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.fail.variant", genes)

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXWarnNonBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.warn.non.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXWarnNoDriver() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.warn.no.driver")

        fun isHomologousRecombinationDeficientWithoutMutationInGenesXPass(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.in.genes.x.pass", genes)

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndeterminedBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.undetermined.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndeterminedNonBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.undetermined.non.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXUndetermined() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.undetermined")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFail() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.fail")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFailCav(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.fail.cav", genes)

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXFailDeletion(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.fail.deletion", genes)

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnPathogenic(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.warn.pathogenic", genes)

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnNonBiallelic() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.warn.non.biallelic")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXWarnNoDriver() =
            get("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.warn.no.driver")

        fun isHomologousRecombinationDeficientWithoutMutationOrWithVusMutationInGenesXPass(genes: String) =
            format("molecular.is.homologous.recombination.deficient.without.mutation.or.with.vus.mutation.in.genes.x.pass", genes)

        fun isMmrDeficientUndeterminedNoTestResult() = get("molecular.is.mmr.deficient.undetermined.no.test.result")
        fun isMmrDeficientWarnProficientIhcMsiMolecular() = get("molecular.is.mmr.deficient.warn.proficient.ihc.msi.molecular")
        fun isMmrDeficientWarnDmmrIhcMssMolecular() = get("molecular.is.mmr.deficient.warn.dmmr.ihc.mss.molecular")
        fun isMmrDeficientPassDmmrIhc() = get("molecular.is.mmr.deficient.pass.dmmr.ihc")
        fun isMmrDeficientFail() = get("molecular.is.mmr.deficient.fail")
        fun isMmrDeficientIhcOnlyPass() = get("molecular.is.mmr.deficient.ihc.only.pass")
        fun isMmrDeficientIhcOnlyFail() = get("molecular.is.mmr.deficient.ihc.only.fail")
        fun isMmrDeficientIhcOnlyUndeterminedGeneLoss() = get("molecular.is.mmr.deficient.ihc.only.undetermined.gene.loss")
        fun isMmrDeficientIhcOnlyUndetermined() = get("molecular.is.mmr.deficient.ihc.only.undetermined")
        fun isMmrDeficientMsiPass(genes: String) = format("molecular.is.mmr.deficient.msi.pass", genes)
        fun isMmrDeficientMsiWarnNonBiallelic(genes: String) = format("molecular.is.mmr.deficient.msi.warn.non.biallelic", genes)
        fun isMmrDeficientMsiWarnNoDriver() = get("molecular.is.mmr.deficient.msi.warn.no.driver")
        fun isMmrDeficientUndetermined(suffix: String) = format("molecular.is.mmr.deficient.undetermined", suffix)
        fun isMmrDeficientUndeterminedSuffixBiallelic(genes: String) =
            format("molecular.is.mmr.deficient.undetermined.suffix.biallelic", genes)

        fun isMmrDeficientUndeterminedSuffixNonBiallelic(genes: String) =
            format("molecular.is.mmr.deficient.undetermined.suffix.non.biallelic", genes)

        fun isMmrDeficientUndeterminedSuffixUnknown(genes: String) =
            format("molecular.is.mmr.deficient.undetermined.suffix.unknown", genes)

        fun pdl1EvaluationFunctionsUndeterminedConflicting(comparatorMessage: String, pdl1Reference: Double, unit: String) =
            format("molecular.pdl1.evaluation.functions.undetermined.conflicting", comparatorMessage, pdl1Reference, unit)

        fun pdl1EvaluationFunctionsPass(comparatorMessage: String, pdl1Reference: Double) =
            format("molecular.pdl1.evaluation.functions.pass", comparatorMessage, pdl1Reference)

        fun pdl1EvaluationFunctionsFail(messageEnding: String, unit: String) =
            format("molecular.pdl1.evaluation.functions.fail", messageEnding, unit)

        fun pdl1EvaluationFunctionsUndeterminedBounds(testMessage: String, unit: String, comparatorMessage: String, pdl1Reference: Double) =
            format("molecular.pdl1.evaluation.functions.undetermined.bounds", testMessage, unit, comparatorMessage, pdl1Reference)

        fun pdl1EvaluationFunctionsUndeterminedUnclear(status: String, comparatorMessage: String, pdl1Reference: Double, unit: String) =
            format("molecular.pdl1.evaluation.functions.undetermined.unclear", status, comparatorMessage, pdl1Reference, unit)

        fun pdl1EvaluationFunctionsRecoverableFailMeasure(measure: String) =
            format("molecular.pdl1.evaluation.functions.recoverable.fail.measure", measure)

        fun pdl1EvaluationFunctionsRecoverableFailNoMeasure() =
            get("molecular.pdl1.evaluation.functions.recoverable.fail.no.measure")

        fun pdl1EvaluationFunctionsUndeterminedNotTested() = get("molecular.pdl1.evaluation.functions.undetermined.not.tested")

        fun proteinExpressionByIhcFunctionsUndeterminedNoResult(protein: String) =
            format("molecular.protein.expression.by.ihc.functions.undetermined.no.result", protein)

        fun proteinExpressionByIhcFunctionsPass(protein: String, comparisonText: String, referenceExpressionLevel: Int) =
            format("molecular.protein.expression.by.ihc.functions.pass", protein, comparisonText, referenceExpressionLevel)

        fun proteinExpressionByIhcFunctionsWarn(protein: String, comparisonText: String, referenceExpressionLevel: Int) =
            format("molecular.protein.expression.by.ihc.functions.warn", protein, comparisonText, referenceExpressionLevel)

        fun proteinExpressionByIhcFunctionsFail(protein: String, comparisonText: String, referenceExpressionLevel: Int) =
            format("molecular.protein.expression.by.ihc.functions.fail", protein, comparisonText, referenceExpressionLevel)

        fun hasMolecularDriverEventInNsclcMessage(soc: String, events: String) =
            format("molecular.has.molecular.driver.event.in.nsclc.message", soc, events)

        fun hasMolecularDriverEventInNsclcMessageSoc() = get("molecular.has.molecular.driver.event.in.nsclc.message.soc")
        fun hasMolecularDriverEventInNsclcWarnMustWarn(message: String) =
            format("molecular.has.molecular.driver.event.in.nsclc.warn.must.warn", message)

        fun hasMolecularDriverEventInNsclcWarn(message: String) =
            format("molecular.has.molecular.driver.event.in.nsclc.warn", message)

        fun hasMolecularDriverEventInNsclcUndeterminedMissingData() =
            get("molecular.has.molecular.driver.event.in.nsclc.undetermined.missing.data")

        fun hasMolecularDriverEventInNsclcUndeterminedGeneTarget(genes: String, target: String) =
            format("molecular.has.molecular.driver.event.in.nsclc.undetermined.gene.target", genes, target)

        fun hasMolecularDriverEventInNsclcUndetermined(details: String) =
            format("molecular.has.molecular.driver.event.in.nsclc.undetermined", details)

        fun hasMolecularDriverEventInNsclcFail() = get("molecular.has.molecular.driver.event.in.nsclc.fail")

        fun geneHasActivatingMutationMessagePrefix() = get("molecular.gene.has.activating.mutation.message.prefix")

        fun geneHasActivatingMutationPass(gene: String, variantsString: String) =
            format("molecular.gene.has.activating.mutation.pass", gene, variantsString)

        fun geneHasActivatingMutationWarnKinase(gene: String, variantsString: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.kinase", gene, variantsString, inKinaseDomainString)

        fun geneHasActivatingMutationWarnWithOther(gene: String, variantsString: String, otherWarnings: String) =
            format("molecular.gene.has.activating.mutation.warn.with.other", gene, variantsString, otherWarnings)

        fun geneHasActivatingMutationFail(gene: String) = format("molecular.gene.has.activating.mutation.fail", gene)

        fun geneHasActivatingMutationWarnNonOncogene(gene: String, events: String, evidenceSource: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.non.oncogene", gene, events, evidenceSource, inKinaseDomainString)

        fun geneHasActivatingMutationWarnNoCav(gene: String, events: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.no.cav", gene, events, inKinaseDomainString)

        fun geneHasActivatingMutationWarnSubclonal(gene: String, events: String, percentage: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.subclonal", gene, events, percentage, inKinaseDomainString)

        fun geneHasActivatingMutationWarnNonHighDriverSubclonal(gene: String, events: String, percentage: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.non.high.driver.subclonal", gene, events, percentage, inKinaseDomainString)

        fun geneHasActivatingMutationWarnNonHighDriver(gene: String, events: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.non.high.driver", gene, events, inKinaseDomainString)

        fun geneHasActivatingMutationWarnOtherMissense(gene: String, events: String, inKinaseDomainString: String) =
            format("molecular.gene.has.activating.mutation.warn.other.missense", gene, events, inKinaseDomainString)

        fun geneHasActivatingMutationDescriptionNoCav() = get("molecular.gene.has.activating.mutation.description.no.cav")
        fun geneHasActivatingMutationDescriptionSubclonal(percentage: String) =
            format("molecular.gene.has.activating.mutation.description.subclonal", percentage)

        fun geneHasVariantInCodonMessagePrefix(codons: List<String>) =
            format("molecular.gene.has.variant.in.codon.message.prefix", codons.joinToString())

        fun geneHasVariantInCodonPass(events: String, codons: String, gene: String) =
            format("molecular.gene.has.variant.in.codon.pass", events, codons, gene)

        fun geneHasVariantInCodonWarnExtended(events: String, codons: String, gene: String, extension: String) =
            format("molecular.gene.has.variant.in.codon.warn.extended", events, codons, gene, extension)

        fun geneHasVariantInCodonFail(codons: String, gene: String) = format("molecular.gene.has.variant.in.codon.fail", codons, gene)

        fun geneHasVariantInCodonWarnSubclonal(events: String, gene: String, percentage: String) =
            format("molecular.gene.has.variant.in.codon.warn.subclonal", events, gene, percentage)

        fun geneHasVariantInCodonWarnUnreportable(codons: String, gene: String) =
            format("molecular.gene.has.variant.in.codon.warn.unreportable", codons, gene)

        fun geneHasVariantInCodonWarnNonCanonical(codons: String, gene: String) =
            format("molecular.gene.has.variant.in.codon.warn.non.canonical", codons, gene)

        fun geneHasVariantInCodonExtensionNonCanonical(events: String, codons: String) =
            format("molecular.gene.has.variant.in.codon.extension.non.canonical", events, codons)

        fun geneHasVariantInCodonExtensionSubclonal(events: String, codons: String, percentage: String) =
            format("molecular.gene.has.variant.in.codon.extension.subclonal", events, codons, percentage)

        fun geneHasVariantInExonRangeOfTypeMessagePrefix(exonRangeText: String, variantTypeMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.message.prefix", exonRangeText, variantTypeMessage)

        fun geneHasVariantInExonRangeOfTypePassVariant(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.pass.variant", baseMessage)

        fun geneHasVariantInExonRangeOfTypePassExonSkip(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.pass.exon.skip", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnVariant(events: String, baseMessage: String, extensions: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.variant", events, baseMessage, extensions)

        fun geneHasVariantInExonRangeOfTypeWarnExonSkip(baseMessage: String, events: String, extensions: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.exon.skip", baseMessage, events, extensions)

        fun geneHasVariantInExonRangeOfTypeFail(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.fail", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnUnreportable(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.unreportable", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnNonCanonical(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.non.canonical", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnExonSkipUnreportable(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.exon.skip.unreportable", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnNonHighDriver(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.non.high.driver", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnExonSkipNonHighDriver(baseMessage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.exon.skip.non.high.driver", baseMessage)

        fun geneHasVariantInExonRangeOfTypeWarnSubclonal(baseMessage: String, percentage: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.warn.subclonal", baseMessage, percentage)

        fun geneHasVariantInExonRangeOfTypeExtensionNonCanonical(events: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.extension.non.canonical", events)

        fun geneHasVariantInExonRangeOfTypeExtensionSubclonal(percentage: String, events: String) =
            format("molecular.gene.has.variant.in.exon.range.of.type.extension.subclonal", percentage, events)

        fun geneHasVariantWithProteinImpactMessagePrefix(allowedProteinImpacts: Set<String>) =
            format("molecular.gene.has.variant.with.protein.impact.message.prefix", allowedProteinImpacts.joinToString())

        fun geneHasVariantWithProteinImpactPass(impactString: String, gene: String) =
            format("molecular.gene.has.variant.with.protein.impact.pass", impactString, gene)

        fun geneHasVariantWithProteinImpactFail(impacts: String, gene: String) =
            format("molecular.gene.has.variant.with.protein.impact.fail", impacts, gene)

        fun geneHasVariantWithProteinImpactWarnSubclonal(impacts: String, gene: String, percentage: String) =
            format("molecular.gene.has.variant.with.protein.impact.warn.subclonal", impacts, gene, percentage)

        fun geneHasVariantWithProteinImpactWarnUnreportable(impacts: String, gene: String) =
            format("molecular.gene.has.variant.with.protein.impact.warn.unreportable", impacts, gene)

        fun geneHasVariantWithProteinImpactWarnNonCanonical(impacts: String, gene: String) =
            format("molecular.gene.has.variant.with.protein.impact.warn.non.canonical", impacts, gene)

        fun geneIsAmplifiedMessagePrefix() = get("molecular.gene.is.amplified.message.prefix")

        fun geneIsAmplifiedPass(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.pass", gene, requestedCopiesMessage)

        fun geneIsAmplifiedPassFullAmpAssumed(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.pass.full.amp.assumed", gene, requestedCopiesMessage)

        fun geneIsAmplifiedWarnFullAmpUndetermined(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.warn.full.amp.undetermined", gene, requestedCopiesMessage)

        fun geneIsAmplifiedFail(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.fail", gene, requestedCopiesMessage)

        fun geneIsAmplifiedWarnLossOfFunction(gene: String, requestedCopiesMessage: String, evidenceSource: String) =
            format("molecular.gene.is.amplified.warn.loss.of.function", gene, requestedCopiesMessage, evidenceSource)

        fun geneIsAmplifiedWarnTsg(gene: String, requestedCopiesMessage: String, evidenceSource: String) =
            format("molecular.gene.is.amplified.warn.tsg", gene, requestedCopiesMessage, evidenceSource)

        fun geneIsAmplifiedWarnPartial(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.warn.partial", gene, requestedCopiesMessage)

        fun geneIsAmplifiedWarnNonCanonical(gene: String, requestedCopiesMessage: String) =
            format("molecular.gene.is.amplified.warn.non.canonical", gene, requestedCopiesMessage)

        fun geneIsAmplifiedWarnMeetsCutoff(gene: String, ploidyAmplificationFactor: Double) =
            format("molecular.gene.is.amplified.warn.meets.cutoff", gene, ploidyAmplificationFactor)

        fun geneIsAmplifiedWarnMeetsRequested(gene: String, requestedMinCopyNumber: Int?) =
            format("molecular.gene.is.amplified.warn.meets.requested", gene, requestedMinCopyNumber.toString())

        fun geneIsAmplifiedWarnPartialUnknown(gene: String, requestedMinCopyNumber: Int?) =
            format("molecular.gene.is.amplified.warn.partial.unknown", gene, requestedMinCopyNumber.toString())

        fun geneIsAmplifiedWarnIhc(gene: String, requestedCopiesMessage: String, protein: String) =
            format("molecular.gene.is.amplified.warn.ihc", gene, requestedCopiesMessage, protein)

        fun geneIsInactivatedMessagePrefixDeletion() = get("molecular.gene.is.inactivated.message.prefix.deletion")

        fun geneIsInactivatedMessagePrefixInactivation() = get("molecular.gene.is.inactivated.message.prefix.inactivation")

        fun geneIsInactivatedPass(gene: String, messageSubject: String, events: String) =
            format("molecular.gene.is.inactivated.pass", gene, messageSubject, events)

        fun geneIsInactivatedFail(gene: String, messageSubject: String) =
            format("molecular.gene.is.inactivated.fail", gene, messageSubject)

        fun geneIsInactivatedWarnIhcLoss(events: String, gene: String, messageSubject: String) =
            format("molecular.gene.is.inactivated.warn.ihc.loss", events, gene, messageSubject)

        fun geneIsInactivatedWarnIhcIndeterminate(events: String, messageSubject: String) =
            format("molecular.gene.is.inactivated.warn.ihc.indeterminate", events, messageSubject)

        fun geneIsInactivatedWarnUnreportable(messageSubjectCapitalized: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.unreportable", messageSubjectCapitalized, events, gene)

        fun geneIsInactivatedWarnNoTsg(messageSubjectCapitalized: String, events: String, gene: String, evidenceSource: String) =
            format("molecular.gene.is.inactivated.warn.no.tsg", messageSubjectCapitalized, events, gene, evidenceSource)

        fun geneIsInactivatedWarnGainOfFunction(messageSubjectCapitalized: String, events: String, gene: String, evidenceSource: String) =
            format("molecular.gene.is.inactivated.warn.gain.of.function", messageSubjectCapitalized, events, gene, evidenceSource)

        fun geneIsInactivatedWarnNoEffect(messageSubjectCapitalized: String, events: String, gene: String, evidenceSource: String) =
            format("molecular.gene.is.inactivated.warn.no.effect", messageSubjectCapitalized, events, gene, evidenceSource)

        fun geneIsInactivatedWarnNonBiallelic(messageSubjectCapitalized: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.non.biallelic", messageSubjectCapitalized, events, gene)

        fun geneIsInactivatedWarnUnknownBiallelic(messageSubjectCapitalized: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.unknown.biallelic", messageSubjectCapitalized, events, gene)

        fun geneIsInactivatedWarnNonCanonical(messageSubjectCapitalized: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.non.canonical", messageSubjectCapitalized, events, gene)

        fun geneIsInactivatedWarnPotentialNonHighDriver(messageSubject: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.potential.non.high.driver", messageSubject, events, gene)

        fun geneIsInactivatedWarnPotentialNonBiallelicNonHighDriver(messageSubject: String, events: String, gene: String) =
            format("molecular.gene.is.inactivated.warn.potential.non.biallelic.non.high.driver", messageSubject, events, gene)

        fun geneIsInactivatedWarnTransPhased(gene: String, events: String, messageSubject: String) =
            format("molecular.gene.is.inactivated.warn.trans.phased", gene, events, messageSubject)

        fun hasFusionInGeneMessagePrefix() = get("molecular.has.fusion.in.gene.message.prefix")

        fun hasFusionInGenePass(events: String, gene: String) = format("molecular.has.fusion.in.gene.pass", events, gene)
        fun hasFusionInGeneWarnWithOther(events: String, gene: String, otherEvents: String) =
            format("molecular.has.fusion.in.gene.warn.with.other", events, gene, otherEvents)

        fun hasFusionInGeneFail(gene: String) = format("molecular.has.fusion.in.gene.fail", gene)
        fun hasFusionInGeneWarnNoEffect(events: String, gene: String, evidenceSource: String) =
            format("molecular.has.fusion.in.gene.warn.no.effect", events, gene, evidenceSource)

        fun hasFusionInGeneWarnNonHighDriver(events: String, gene: String) =
            format("molecular.has.fusion.in.gene.warn.non.high.driver", events, gene)

        fun hasFusionInGeneWarnUnreportableGainOfFunction(events: String, gene: String, evidenceSource: String) =
            format("molecular.has.fusion.in.gene.warn.unreportable.gain.of.function", events, gene, evidenceSource)

        fun hasFusionInGeneWarnIhcQualify(gene: String) = format("molecular.has.fusion.in.gene.warn.ihc.qualify", gene)
        fun hasFusionInGeneWarnIhcIndeterminate(gene: String) = format("molecular.has.fusion.in.gene.warn.ihc.indeterminate", gene)
        fun hasFusionInGeneDescriptionNoEffect(event: String) = format("molecular.has.fusion.in.gene.description.no.effect", event)
        fun hasFusionInGeneDescriptionNonHighDriver(event: String) =
            format("molecular.has.fusion.in.gene.description.non.high.driver", event)

        fun hasFusionInGeneDescriptionUnreportableGainOfFunction(event: String) =
            format("molecular.has.fusion.in.gene.description.unreportable.gain.of.function", event)

        fun hasFusionInGeneDescriptionIhcQualify(finding: String) =
            format("molecular.has.fusion.in.gene.description.ihc.qualify", finding)

        fun hasFusionInGeneDescriptionIhcIndeterminate(finding: String) =
            format("molecular.has.fusion.in.gene.description.ihc.indeterminate", finding)
    }

    inner class PriorTumor {
        fun hasActiveSecondMalignancyPass() = get("priortumor.has.active.second.malignancy.pass")

        fun hasActiveSecondMalignancyWarn() = get("priortumor.has.active.second.malignancy.warn")

        fun hasActiveSecondMalignancyFail() = get("priortumor.has.active.second.malignancy.fail")

        fun hasHistoryOfSecondMalignancyPass() = get("priortumor.has.history.of.second.malignancy.pass")

        fun hasHistoryOfSecondMalignancyFail() = get("priortumor.has.history.of.second.malignancy.fail")

        fun hasHistoryOfSecondMalignancyIgnoringDoidTermsPass(recentSuffix: String, priorPrimarySuffix: String) =
            format("priortumor.has.history.of.second.malignancy.ignoring.doid.terms.pass", recentSuffix, priorPrimarySuffix)

        fun hasHistoryOfSecondMalignancyIgnoringDoidTermsUndetermined(priorPrimarySuffix: String) =
            format("priortumor.has.history.of.second.malignancy.ignoring.doid.terms.undetermined", priorPrimarySuffix)

        fun hasHistoryOfSecondMalignancyIgnoringDoidTermsFailExcluding(recentSuffix: String, excludedDisplay: String) =
            format("priortumor.has.history.of.second.malignancy.ignoring.doid.terms.fail.excluding", recentSuffix, excludedDisplay)

        fun hasHistoryOfSecondMalignancyIgnoringDoidTermsFailOther(recentSuffix: String) =
            format("priortumor.has.history.of.second.malignancy.ignoring.doid.terms.fail.other", recentSuffix)

        fun hasHistoryOfSecondMalignancyIgnoringDoidTermsSuffixRecent() =
            get("priortumor.has.history.of.second.malignancy.ignoring.doid.terms.suffix.recent")

        fun hasHistoryOfSecondMalignancyWithDoidPass(doidTermDisplay: String) =
            format("priortumor.has.history.of.second.malignancy.with.doid.pass", doidTermDisplay)

        fun hasHistoryOfSecondMalignancyWithDoidFail(doidTermDisplay: String) =
            format("priortumor.has.history.of.second.malignancy.with.doid.fail", doidTermDisplay)

        fun hasHistoryOfSecondMalignancyWithinYearsPass() = get("priortumor.has.history.of.second.malignancy.within.years.pass")

        fun hasHistoryOfSecondMalignancyWithinYearsUndeterminedPotentialMatch() =
            get("priortumor.has.history.of.second.malignancy.within.years.undetermined.potential.match")

        fun hasHistoryOfSecondMalignancyWithinYearsFail() = get("priortumor.has.history.of.second.malignancy.within.years.fail")

        fun hasHistoryOfSecondMalignancyWithinYearsUndeterminedUnknownDate() =
            get("priortumor.has.history.of.second.malignancy.within.years.undetermined.unknown.date")
    }

    inner class Reproduction {
        fun isBreastfeedingFail() = get("reproduction.is.breastfeeding.fail")

        fun isPregnantFail() = get("reproduction.is.pregnant.fail")

        fun canUseAdequateAnticonceptionPass() = get("reproduction.can.use.adequate.anticonception.pass")

        fun willingToAdhereToDonationPrescriptionsPass() =
            get("reproduction.willing.to.adhere.to.donation.prescriptions.pass")
    }

    inner class Surgery {
        fun hasHadAnySurgeryAfterSpecificDatePass(date: Any) =
            format("surgery.has.had.any.surgery.after.specific.date.pass", date)

        fun hasHadAnySurgeryAfterSpecificDateWarnPlanned() =
            get("surgery.has.had.any.surgery.after.specific.date.warn.planned")

        fun hasHadAnySurgeryAfterSpecificDateWarnUnexpected() =
            get("surgery.has.had.any.surgery.after.specific.date.warn.unexpected")

        fun hasHadAnySurgeryAfterSpecificDateUndeterminedRecent() =
            get("surgery.has.had.any.surgery.after.specific.date.undetermined.recent")

        fun hasHadAnySurgeryAfterSpecificDateUndeterminedWhen() =
            get("surgery.has.had.any.surgery.after.specific.date.undetermined.when")

        fun hasHadAnySurgeryAfterSpecificDateFailCancelled() =
            get("surgery.has.had.any.surgery.after.specific.date.fail.cancelled")

        fun hasHadAnySurgeryAfterSpecificDateFail(date: Any) =
            format("surgery.has.had.any.surgery.after.specific.date.fail", date)

        fun hasHadCytoreductiveSurgeryPass() = get("surgery.has.had.cytoreductive.surgery.pass")

        fun hasHadCytoreductiveSurgeryUndeterminedCytoreductive() =
            get("surgery.has.had.cytoreductive.surgery.undetermined.cytoreductive")

        fun hasHadCytoreductiveSurgeryUndeterminedDebulking() =
            get("surgery.has.had.cytoreductive.surgery.undetermined.debulking")

        fun hasHadCytoreductiveSurgeryFail() = get("surgery.has.had.cytoreductive.surgery.fail")

        fun hasHadOncologicalSurgeryInSpecificBodyLocationPass(locations: Any) =
            format("surgery.has.had.oncological.surgery.in.specific.body.location.pass", locations)

        fun hasHadOncologicalSurgeryInSpecificBodyLocationUndetermined(locationString: Any) =
            format("surgery.has.had.oncological.surgery.in.specific.body.location.undetermined", locationString)

        fun hasHadOncologicalSurgeryInSpecificBodyLocationFail(locationString: Any) =
            format("surgery.has.had.oncological.surgery.in.specific.body.location.fail", locationString)
    }

    inner class Toxicity {
        fun hasToxicityWithGradePassOrWarn(minGrade: Int, toxicityDisplay: String) =
            format("toxicity.has.toxicity.with.grade.pass.or.warn", minGrade, toxicityDisplay)

        fun hasToxicityWithGradeUndetermined(toxicityDisplay: String, minGrade: Int) =
            format("toxicity.has.toxicity.with.grade.undetermined", toxicityDisplay, minGrade)

        fun hasToxicityWithGradeFail(icdTitleText: String, minGrade: Int) =
            format("toxicity.has.toxicity.with.grade.fail", icdTitleText, minGrade)

        fun hadToxicityWithGradeDuringPreviousTreatmentUndetermined(toxicityName: String, grade: Int) =
            format("toxicity.had.toxicity.with.grade.during.previous.treatment.undetermined", toxicityName, grade)

        fun hasDrugIntoleranceWithAnyIcdCodeOrNamePass(description: String, matchesDisplay: String) =
            format("toxicity.has.drug.intolerance.with.any.icd.code.or.name.pass", description, matchesDisplay)

        fun hasDrugIntoleranceWithAnyIcdCodeOrNameUndetermined(description: String) =
            format("toxicity.has.drug.intolerance.with.any.icd.code.or.name.undetermined", description)

        fun hasDrugIntoleranceWithAnyIcdCodeOrNameFail(description: String) =
            format("toxicity.has.drug.intolerance.with.any.icd.code.or.name.fail", description)

        fun hasHistoryOfAnaphylaxisPass(matchesDisplay: String) =
            format("toxicity.has.history.of.anaphylaxis.pass", matchesDisplay)

        fun hasHistoryOfAnaphylaxisFail() = get("toxicity.has.history.of.anaphylaxis.fail")

        fun hasExperiencedImmunotherapyRelatedAdverseEventsWarnStart() =
            get("toxicity.has.experienced.immunotherapy.related.adverse.events.warn.start")

        fun hasExperiencedImmunotherapyRelatedAdverseEventsWarnStopReason() =
            get("toxicity.has.experienced.immunotherapy.related.adverse.events.warn.stop.reason")

        fun hasExperiencedImmunotherapyRelatedAdverseEventsRecoverableUndeterminedPrior() =
            get("toxicity.has.experienced.immunotherapy.related.adverse.events.recoverable.undetermined.prior")

        fun hasExperiencedImmunotherapyRelatedAdverseEventsRecoverableUndeterminedDrugAllergy(allergyDisplay: String) =
            format(
                "toxicity.has.experienced.immunotherapy.related.adverse.events.recoverable.undetermined.drug.allergy",
                allergyDisplay
            )

        fun hasExperiencedImmunotherapyRelatedAdverseEventsFail() =
            get("toxicity.has.experienced.immunotherapy.related.adverse.events.fail")

        fun hasIntoleranceForPd1OrPdl1InhibitorsPass(matchesDisplay: String) =
            format("toxicity.has.intolerance.for.pd1.or.pdl1.inhibitors.pass", matchesDisplay)

        fun hasIntoleranceForPd1OrPdl1InhibitorsUndeterminedDrug() =
            get("toxicity.has.intolerance.for.pd1.or.pdl1.inhibitors.undetermined.drug")

        fun hasIntoleranceForPd1OrPdl1InhibitorsUndeterminedMonoclonal(matchesDisplay: String) =
            format("toxicity.has.intolerance.for.pd1.or.pdl1.inhibitors.undetermined.monoclonal", matchesDisplay)

        fun hasIntoleranceForPd1OrPdl1InhibitorsWarn(matchesDisplay: String) =
            format("toxicity.has.intolerance.for.pd1.or.pdl1.inhibitors.warn", matchesDisplay)

        fun hasIntoleranceForPd1OrPdl1InhibitorsFail() = get("toxicity.has.intolerance.for.pd1.or.pdl1.inhibitors.fail")

        fun hasIntoleranceWithSpecificNamePass(matchesDisplay: String) =
            format("toxicity.has.intolerance.with.specific.name.pass", matchesDisplay)

        fun hasIntoleranceWithSpecificNameFail(termToFind: String) =
            format("toxicity.has.intolerance.with.specific.name.fail", termToFind)

        fun hasIntoleranceRelatedToStudyMedicationUndetermined(matchesDisplay: String) =
            format("toxicity.has.intolerance.related.to.study.medication.undetermined", matchesDisplay)

        fun hasIntoleranceRelatedToStudyMedicationFail() = get("toxicity.has.intolerance.related.to.study.medication.fail")

        fun hasIntoleranceWithSpecificIcdTitlePass(matchesDisplay: String, targetIcdTitle: String) =
            format("toxicity.has.intolerance.with.specific.icd.title.pass", matchesDisplay, targetIcdTitle)

        fun hasIntoleranceWithSpecificIcdTitleUndetermined(targetIcdTitle: String) =
            format("toxicity.has.intolerance.with.specific.icd.title.undetermined", targetIcdTitle)

        fun hasIntoleranceWithSpecificIcdTitleFail(targetIcdTitle: String) =
            format("toxicity.has.intolerance.with.specific.icd.title.fail", targetIcdTitle)

        fun descriptionPlatinumCompounds() = get("toxicity.description.platinum.compounds")

        fun descriptionTaxanes() = get("toxicity.description.taxanes")
    }

    inner class Treatment {
        fun hasAcquiredResistanceToAnyDrugPass(drugs: String) =
            format("treatment.has.acquired.resistance.to.any.drug.pass", drugs)

        fun hasAcquiredResistanceToAnyDrugUndeterminedTrial(drugs: String) =
            format("treatment.has.acquired.resistance.to.any.drug.undetermined.trial", drugs)

        fun hasAcquiredResistanceToAnyDrugToxicitySuffix() =
            get("treatment.has.acquired.resistance.to.any.drug.toxicity.suffix")

        fun hasAcquiredResistanceToAnyDrugUndetermined(drugs: String, toxicitySuffix: String) =
            format("treatment.has.acquired.resistance.to.any.drug.undetermined", drugs, toxicitySuffix)

        fun hasAcquiredResistanceToAnyDrugFailReceivedNoPD(drugs: String) =
            format("treatment.has.acquired.resistance.to.any.drug.fail.received.no.pd", drugs)

        fun hasAcquiredResistanceToAnyDrugFail(drugs: String) =
            format("treatment.has.acquired.resistance.to.any.drug.fail", drugs)

        fun hasExhaustedSocTreatmentsPass() = get("treatment.has.exhausted.soc.treatments.pass")

        fun hasExhaustedSocTreatmentsWarnPotentialIntolerance(remainingTreatments: String) =
            format("treatment.has.exhausted.soc.treatments.warn.potential.intolerance", remainingTreatments)

        fun hasExhaustedSocTreatmentsWarnMissingMolecular(remainingTreatments: String) =
            format("treatment.has.exhausted.soc.treatments.warn.missing.molecular", remainingTreatments)

        fun hasExhaustedSocTreatmentsFail(remainingTreatments: String) =
            format("treatment.has.exhausted.soc.treatments.fail", remainingTreatments)

        fun hasExhaustedSocTreatmentsPassPlatinumDoublet() =
            get("treatment.has.exhausted.soc.treatments.pass.platinum.doublet")

        fun hasExhaustedSocTreatmentsPassChemoradiation() =
            get("treatment.has.exhausted.soc.treatments.pass.chemoradiation")

        fun hasExhaustedSocTreatmentsPassChemoImmunotherapy() =
            get("treatment.has.exhausted.soc.treatments.pass.chemo.immunotherapy")

        fun hasExhaustedSocTreatmentsUndeterminedChemotherapy() =
            get("treatment.has.exhausted.soc.treatments.undetermined.chemotherapy")

        fun hasExhaustedSocTreatmentsWarnNoPlatinumDoublet() =
            get("treatment.has.exhausted.soc.treatments.warn.no.platinum.doublet")

        fun hasExhaustedSocTreatmentsUndeterminedNoHistory() =
            get("treatment.has.exhausted.soc.treatments.undetermined.no.history")

        fun hasExhaustedSocTreatmentsPassAssumed() = get("treatment.has.exhausted.soc.treatments.pass.assumed")

        fun hasHadAdjuvantTreatmentWithCategoryPass(category: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.pass", category)

        fun hasHadAdjuvantTreatmentWithCategoryPassWithinWeeks(category: String, weeksAgo: Int?) =
            format("treatment.has.had.adjuvant.treatment.with.category.pass.within.weeks", category, weeksAgo.toString())

        fun hasHadAdjuvantTreatmentWithCategoryUndetermined(category: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.undetermined", category)

        fun hasHadAdjuvantTreatmentWithCategoryFail(category: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.fail", category)

        fun hasHadAdjuvantTreatmentWithCategoryFailTooLongAgo(category: String, weeksAgo: Int?) =
            format("treatment.has.had.adjuvant.treatment.with.category.fail.too.long.ago", category, weeksAgo.toString())

        fun hasHadAnyCancerTreatmentPass() = get("treatment.has.had.any.cancer.treatment.pass")

        fun hasHadAnyCancerTreatmentUndetermined() = get("treatment.has.had.any.cancer.treatment.undetermined")

        fun hasHadAnyCancerTreatmentFail() = get("treatment.has.had.any.cancer.treatment.fail")

        fun hasHadAnyCancerTreatmentSinceDateSystemicSuffix() =
            get("treatment.has.had.any.cancer.treatment.since.date.systemic.suffix")

        fun hasHadAnyCancerTreatmentSinceDateIgnoringSuffix(types: String) =
            format("treatment.has.had.any.cancer.treatment.since.date.ignoring.suffix", types)

        fun hasHadAnyCancerTreatmentSinceDatePass(systemicSuffix: String, monthsAgo: Int) =
            format("treatment.has.had.any.cancer.treatment.since.date.pass", systemicSuffix, monthsAgo)

        fun hasHadAnyCancerTreatmentSinceDateUndeterminedTrial(systemicSuffix: String) =
            format("treatment.has.had.any.cancer.treatment.since.date.undetermined.trial", systemicSuffix)

        fun hasHadAnyCancerTreatmentSinceDateUndeterminedDateUnknown(systemicSuffix: String, monthsAgo: Int) =
            format("treatment.has.had.any.cancer.treatment.since.date.undetermined.date.unknown", systemicSuffix, monthsAgo)

        fun hasHadAnyCancerTreatmentSinceDateFailNotReceived(systemicSuffix: String, monthsAgo: Int, ignoringSuffix: String) =
            format(
                "treatment.has.had.any.cancer.treatment.since.date.fail.not.received",
                systemicSuffix, monthsAgo, ignoringSuffix
            )

        fun hasHadAnyCancerTreatmentSinceDateFailNoTreatment(systemicSuffix: String, ignoringSuffix: String) =
            format("treatment.has.had.any.cancer.treatment.since.date.fail.no.treatment", systemicSuffix, ignoringSuffix)

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingSettingMessage(settingDescription: String) =
            format("treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.setting.message", settingDescription)

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingPassNoPrior(settingMessage: String, maximumLines: Int) =
            format(
                "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.pass.no.prior",
                settingMessage, maximumLines
            )

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingFailPalliative(maximumLines: Int, settingMessage: String) =
            format(
                "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.fail.palliative",
                maximumLines, settingMessage
            )

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingFailLikelyExceeded(
            maximumLines: Int, settingMessage: String, probableCount: Int
        ) = format(
            "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.fail.likely.exceeded",
            maximumLines, settingMessage, probableCount
        )

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingUndeterminedUncertain(
            maximumLines: Int, settingMessage: String, probableCount: Int
        ) = format(
            "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.undetermined.uncertain",
            maximumLines, settingMessage, probableCount
        )

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingUndeterminedNonExcluded(
            maximumLines: Int, settingMessage: String, count: Int
        ) = format(
            "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.undetermined.non.excluded",
            maximumLines, settingMessage, count
        )

        fun hasHadAtMostSystemicTreatmentLinesInSpecificSettingPassAtMost(maximumLines: Int, settingMessage: String) =
            format(
                "treatment.has.had.at.most.systemic.treatment.lines.in.specific.setting.pass.at.most",
                maximumLines, settingMessage
            )

        fun hasHadBrainRadiationTherapyPass() = get("treatment.has.had.brain.radiation.therapy.pass")

        fun hasHadBrainRadiationTherapyFailNotToBrain() =
            get("treatment.has.had.brain.radiation.therapy.fail.not.to.brain")

        fun hasHadBrainRadiationTherapySuspectedSuffix() =
            get("treatment.has.had.brain.radiation.therapy.suspected.suffix")

        fun hasHadBrainRadiationTherapyUndetermined(suspectedSuffix: String) =
            format("treatment.has.had.brain.radiation.therapy.undetermined", suspectedSuffix)

        fun hasHadBrainRadiationTherapyFail() = get("treatment.has.had.brain.radiation.therapy.fail")

        fun hasHadCategoryAndTypesCombinedWithOtherCategoryAndTypesDescription(
            types1: String, category1: String, types2: String, category2: String
        ) = format(
            "treatment.has.had.category.and.types.combined.with.other.category.and.types.description",
            types1, category1, types2, category2
        )

        fun hasHadCategoryAndTypesCombinedWithOtherCategoryAndTypesPass(description: String) =
            format("treatment.has.had.category.and.types.combined.with.other.category.and.types.pass", description)

        fun hasHadCategoryAndTypesCombinedWithOtherCategoryAndTypesUndetermined(description: String) =
            format("treatment.has.had.category.and.types.combined.with.other.category.and.types.undetermined", description)

        fun hasHadCategoryAndTypesCombinedWithOtherCategoryAndTypesFail(description: String) =
            format("treatment.has.had.category.and.types.combined.with.other.category.and.types.fail", description)

        fun hasHadChemoradiotherapyWithDrugAndCyclesUndetermined(drugs: String, cycles: Int) =
            format("treatment.has.had.chemoradiotherapy.with.drug.and.cycles.undetermined", drugs, cycles)

        fun hasHadCombinedTreatmentNamesWithCyclesDescriptionBetween(minCycles: Int, maxCycles: Int) =
            format("treatment.has.had.combined.treatment.names.with.cycles.description.between", minCycles, maxCycles)

        fun hasHadCombinedTreatmentNamesWithCyclesDescriptionAtLeast(minCycles: Int) =
            format("treatment.has.had.combined.treatment.names.with.cycles.description.at.least", minCycles)

        fun hasHadCombinedTreatmentNamesWithCyclesCycleSuffix(cycles: String) =
            format("treatment.has.had.combined.treatment.names.with.cycles.cycle.suffix", cycles)

        fun hasHadCombinedTreatmentNamesWithCyclesFailNoMatching(treatmentName: String, cyclesDescription: String) =
            format("treatment.has.had.combined.treatment.names.with.cycles.fail.no.matching", treatmentName, cyclesDescription)

        fun hasHadCombinedTreatmentNamesWithCyclesPass(treatmentsList: String, cyclesDescription: String) =
            format("treatment.has.had.combined.treatment.names.with.cycles.pass", treatmentsList, cyclesDescription)

        fun hasHadCombinedTreatmentNamesWithCyclesUndetermined(treatmentsList: String) =
            format("treatment.has.had.combined.treatment.names.with.cycles.undetermined", treatmentsList)

        fun hasHadCombinedTreatmentNamesWithCyclesWarn(cyclesDescription: String, treatmentsList: String) =
            format("treatment.has.had.combined.treatment.names.with.cycles.warn", cyclesDescription, treatmentsList)

        fun hasHadCompleteResectionPass() = get("treatment.has.had.complete.resection.pass")

        fun hasHadCompleteResectionUndetermined() = get("treatment.has.had.complete.resection.undetermined")

        fun hasHadCompleteResectionFail() = get("treatment.has.had.complete.resection.fail")

        fun hasHadDefinitiveLocoregionalTherapyWithCurativeIntentPass() =
            get("treatment.has.had.definitive.locoregional.therapy.with.curative.intent.pass")

        fun hasHadDefinitiveLocoregionalTherapyWithCurativeIntentUndetermined() =
            get("treatment.has.had.definitive.locoregional.therapy.with.curative.intent.undetermined")

        fun hasHadFirstLineTreatmentCategoryOfTypesUndetermined() =
            get("treatment.has.had.first.line.treatment.category.of.types.undetermined")

        fun hasHadIntratumoralInjectionTreatmentUndetermined() =
            get("treatment.has.had.intratumoral.injection.treatment.undetermined")

        fun hasHadLimitedSystemicTreatmentsPass(maxSystemicTreatments: Int) =
            format("treatment.has.had.limited.systemic.treatments.pass", maxSystemicTreatments)

        fun hasHadLimitedSystemicTreatmentsUndetermined(maxSystemicTreatments: Int) =
            format("treatment.has.had.limited.systemic.treatments.undetermined", maxSystemicTreatments)

        fun hasHadLimitedSystemicTreatmentsFail(maxSystemicTreatments: Int) =
            format("treatment.has.had.limited.systemic.treatments.fail", maxSystemicTreatments)

        fun hasHadLimitedTreatmentsWithCategoryOfTypesMessageEnding(maxTreatmentLines: Int, treatmentString: String) =
            format(
                "treatment.has.had.limited.treatments.with.category.of.types.message.ending",
                maxTreatmentLines, treatmentString
            )

        fun hasHadLimitedTreatmentsWithCategoryOfTypesPass(messageEnding: String) =
            format("treatment.has.had.limited.treatments.with.category.of.types.pass", messageEnding)

        fun hasHadLimitedTreatmentsWithCategoryOfTypesFailNotReceived(treatmentString: String) =
            format("treatment.has.had.limited.treatments.with.category.of.types.fail.not.received", treatmentString)

        fun hasHadLimitedTreatmentsWithCategoryOfTypesUndetermined(messageEnding: String) =
            format("treatment.has.had.limited.treatments.with.category.of.types.undetermined", messageEnding)

        fun hasHadLimitedTreatmentsWithCategoryOfTypesFail(messageEnding: String) =
            format("treatment.has.had.limited.treatments.with.category.of.types.fail", messageEnding)

        fun hasHadSomeSystemicTreatmentsPass(minSystemicTreatments: Int) =
            format("treatment.has.had.some.systemic.treatments.pass", minSystemicTreatments)

        fun hasHadSomeSystemicTreatmentsUndetermined(minSystemicTreatments: Int) =
            format("treatment.has.had.some.systemic.treatments.undetermined", minSystemicTreatments)

        fun hasHadSomeSystemicTreatmentsFail(minSystemicTreatments: Int) =
            format("treatment.has.had.some.systemic.treatments.fail", minSystemicTreatments)

        fun hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsPass(
            intentsList: String, drugTypeString: String, category: String, treatments: List<String>
        ) = format(
            "treatment.has.had.some.treatments.with.category.and.type.with.intents.pass",
            intentsList, drugTypeString, category, treatments.joinToString(", ")
        )

        fun hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedApproximate(
            allowedTypesString: String, category: String, intentsList: String
        ) = format(
            "treatment.has.had.some.treatments.with.category.and.type.with.intents.undetermined.approximate",
            allowedTypesString, category, intentsList
        )

        fun hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedTrial(
            intentsList: String, allowedTypesString: String, category: String
        ) = format(
            "treatment.has.had.some.treatments.with.category.and.type.with.intents.undetermined.trial",
            intentsList, allowedTypesString, category
        )

        fun hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedUnknownDate(
            intentsList: String, drugTypeString: String, category: String, treatments: List<String>
        ) = format(
            "treatment.has.had.some.treatments.with.category.and.type.with.intents.undetermined.unknown.date",
            intentsList, drugTypeString, category, treatments.joinToString(", ")
        )

        fun hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsFail(intentsList: String, allowedTypesString: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.and.type.with.intents.fail", intentsList, allowedTypesString, category)

        fun hasHadSomeTreatmentsWithCategoryOfAllTypesPass(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.all.types.pass", minTreatmentLines, typesList, category)

        fun hasHadSomeTreatmentsWithCategoryOfAllTypesUndetermined(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.all.types.undetermined", minTreatmentLines, typesList, category)

        fun hasHadSomeTreatmentsWithCategoryOfAllTypesUndeterminedTrial(minTreatmentLines: Int, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.all.types.undetermined.trial", minTreatmentLines, category)

        fun hasHadSomeTreatmentsWithCategoryOfAllTypesFail(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.all.types.fail", minTreatmentLines, typesList, category)

        fun hasHadSomeTreatmentsWithCategoryOfTypesPass(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.types.pass", minTreatmentLines, typesList, category)

        fun hasHadSomeTreatmentsWithCategoryOfTypesUndetermined(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.types.undetermined", minTreatmentLines, typesList, category)

        fun hasHadSomeTreatmentsWithCategoryOfTypesFail(minTreatmentLines: Int, typesList: String, category: String) =
            format("treatment.has.had.some.treatments.with.category.of.types.fail", minTreatmentLines, typesList, category)

        fun hasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineUndeterminedLine(treatmentDesc: String, line: Int) =
            format("treatment.has.had.specific.drug.combined.with.category.and.optionally.types.as.line.undetermined.line", treatmentDesc, line)

        fun hasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLinePass(treatmentDesc: String) =
            format("treatment.has.had.specific.drug.combined.with.category.and.optionally.types.as.line.pass", treatmentDesc)

        fun hasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineUndetermined(treatmentDesc: String) =
            format("treatment.has.had.specific.drug.combined.with.category.and.optionally.types.as.line.undetermined", treatmentDesc)

        fun hasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineFail(treatmentDesc: String) =
            format("treatment.has.had.specific.drug.combined.with.category.and.optionally.types.as.line.fail", treatmentDesc)

        fun hasHadSpecificFirstLineSystemicTreatmentPass(treatmentToFindDisplay: String) =
            format("treatment.has.had.specific.first.line.systemic.treatment.pass", treatmentToFindDisplay)

        fun hasHadSpecificFirstLineSystemicTreatmentUndetermined(treatmentToFindDisplay: String) =
            format("treatment.has.had.specific.first.line.systemic.treatment.undetermined", treatmentToFindDisplay)

        fun hasHadSpecificFirstLineSystemicTreatmentUndeterminedTrial(treatmentToFindDisplay: String) =
            format("treatment.has.had.specific.first.line.systemic.treatment.undetermined.trial", treatmentToFindDisplay)

        fun hasHadSpecificFirstLineSystemicTreatmentFail(treatmentToFindDisplay: String) =
            format("treatment.has.had.specific.first.line.systemic.treatment.fail", treatmentToFindDisplay)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesPass(treatmentDisplay: String, minCycles: Int) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.pass", treatmentDisplay, minCycles)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedDatesMissing(treatmentDisplay: String) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.undetermined.dates.missing", treatmentDisplay)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedPdStatus(treatmentDisplay: String) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.undetermined.pd.status", treatmentDisplay)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedCycles(treatmentDisplay: String, minCycles: Int) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.undetermined.cycles", treatmentDisplay, minCycles)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesUndeterminedTrial(treatmentDisplay: String) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.undetermined.trial", treatmentDisplay)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesWarnInsufficientCycles(treatmentDisplay: String, minCycles: Int) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.warn.insufficient.cycles", treatmentDisplay, minCycles)

        fun hasHadSystemicFirstLineTreatmentWithoutPdAndWithCyclesFail(treatmentDisplay: String, minCycles: Int) =
            format("treatment.has.had.systemic.first.line.treatment.without.pd.and.with.cycles.fail", treatmentDisplay, minCycles)

        fun hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsPass(comparatorMessage: String, referenceTreatmentCount: Int) =
            format(
                "treatment.has.had.systemic.lines.only.including.neo.or.adjuvant.if.next.line.within.months.pass",
                comparatorMessage, referenceTreatmentCount
            )

        fun hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsUndetermined(
            comparatorMessage: String, referenceTreatmentCount: Int, undeterminedMessageEnding: String
        ) = format(
            "treatment.has.had.systemic.lines.only.including.neo.or.adjuvant.if.next.line.within.months.undetermined",
            comparatorMessage, referenceTreatmentCount, undeterminedMessageEnding
        )

        fun hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsUndeterminedEnding(maxMonthsBeforeNextLine: Int) =
            format(
                "treatment.has.had.systemic.lines.only.including.neo.or.adjuvant.if.next.line.within.months.undetermined.ending",
                maxMonthsBeforeNextLine
            )

        fun hasHadSystemicLinesOnlyIncludingNeoOrAdjuvantIfNextLineWithinMonthsFail(comparatorMessage: String, referenceTreatmentCount: Int) =
            format(
                "treatment.has.had.systemic.lines.only.including.neo.or.adjuvant.if.next.line.within.months.fail",
                comparatorMessage, referenceTreatmentCount
            )

        fun hasHadSystemicTherapyWithAnyIntentPassAny(intentsLowercase: String) =
            format("treatment.has.had.systemic.therapy.with.any.intent.pass.any", intentsLowercase)

        fun hasHadSystemicTherapyWithAnyIntentPassWithinWeeks(intentsLowercase: String, weeks: Int?) =
            format("treatment.has.had.systemic.therapy.with.any.intent.pass.within.weeks", intentsLowercase, weeks.toString())

        fun hasHadSystemicTherapyWithAnyIntentPassAtLeastWeeksAgo(intentsLowercase: String, weeks: Int?) =
            format("treatment.has.had.systemic.therapy.with.any.intent.pass.at.least.weeks.ago", intentsLowercase, weeks.toString())

        fun hasHadSystemicTherapyWithAnyIntentUndeterminedDateUnknown(intentsLowercase: String) =
            format("treatment.has.had.systemic.therapy.with.any.intent.undetermined.date.unknown", intentsLowercase)

        fun hasHadSystemicTherapyWithAnyIntentUndeterminedIntentUnknown(treatmentsDisplay: String, intentsLowercase: String) =
            format("treatment.has.had.systemic.therapy.with.any.intent.undetermined.intent.unknown", treatmentsDisplay, intentsLowercase)

        fun hasHadSystemicTherapyWithAnyIntentFailNoTherapy(intentsLowercase: String) =
            format("treatment.has.had.systemic.therapy.with.any.intent.fail.no.therapy", intentsLowercase)

        fun hasHadSystemicTherapyWithAnyIntentFailMoreThanWeeksAgo(intentsLowercase: String, weeks: Int?) =
            format("treatment.has.had.systemic.therapy.with.any.intent.fail.more.than.weeks.ago", intentsLowercase, weeks.toString())

        fun hasHadSystemicTherapyWithAnyIntentFailNotAtLeastWeeksAgo(intentsLowercase: String, weeks: Int?) =
            format("treatment.has.had.systemic.therapy.with.any.intent.fail.not.at.least.weeks.ago", intentsLowercase, weeks.toString())

        fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesFailNotOnly(typesList: String, category: String) =
            format("treatment.has.had.systemic.treatment.only.of.category.of.types.fail.not.only", typesList, category)

        fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesUndeterminedType(category: String, typesList: String) =
            format("treatment.has.had.systemic.treatment.only.of.category.of.types.undetermined.type", category, typesList)

        fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesUndeterminedTrial(typesList: String, category: String) =
            format("treatment.has.had.systemic.treatment.only.of.category.of.types.undetermined.trial", typesList, category)

        fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesPass(typesList: String, category: String) =
            format("treatment.has.had.systemic.treatment.only.of.category.of.types.pass", typesList, category)

        fun hasHadSystemicTreatmentOnlyOfCategoryOfTypesFailNoPrior(typesList: String, category: String) =
            format("treatment.has.had.systemic.treatment.only.of.category.of.types.fail.no.prior", typesList, category)

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingSettingMessage(settingDescription: String) =
            format("treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.setting.message", settingDescription)

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingCategoryToIgnoreMessage(category: String) =
            format("treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.category.to.ignore.message", category)

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingWithTreatments(baseMessage: String, treatmentsDisplay: String) =
            format("treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.with.treatments", baseMessage, treatmentsDisplay)

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingFailExcludedIntent(intentsList: String, settingMessage: String) =
            format(
                "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.fail.excluded.intent",
                intentsList, settingMessage
            )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassInSetting(settingMessage: String, categoryToIgnoreMessage: String) =
            format(
                "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.pass.in.setting",
                settingMessage, categoryToIgnoreMessage
            )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassRecent(categoryToIgnoreMessage: String, settingMessage: String) =
            format(
                "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.pass.recent",
                categoryToIgnoreMessage, settingMessage
            )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassMultipleLines(categoryToIgnoreMessage: String, settingMessage: String) =
            format(
                "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.pass.multiple.lines",
                categoryToIgnoreMessage, settingMessage
            )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingPassNotFollowedByRadiotherapyOrSurgery(
            categoryToIgnoreMessage: String, settingMessage: String
        ) = format(
            "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.pass.not.followed.by.radiotherapy.or.surgery",
            categoryToIgnoreMessage, settingMessage
        )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingUndeterminedUnknownStopDate(
            categoryToIgnoreMessage: String, settingMessage: String
        ) = format(
            "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.undetermined.unknown.stop.date",
            categoryToIgnoreMessage, settingMessage
        )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingUndeterminedNonRecent(
            categoryToIgnoreMessage: String, settingMessage: String
        ) = format(
            "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.undetermined.non.recent",
            categoryToIgnoreMessage, settingMessage
        )

        fun hasHadSystemicTreatmentWithUnknownOrSpecificIntentAndSettingFailNoPriorSystemicTreatment(settingMessage: String) =
            format(
                "treatment.has.had.systemic.treatment.with.unknown.or.specific.intent.and.setting.fail.no.prior.systemic.treatment",
                settingMessage
            )

        fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayPass(treatments: List<String>) =
            format("treatment.has.had.targeted.therapy.interfering.with.ras.mek.mapk.pathway.pass", treatments.joinToString(", "))

        fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayWarn(treatments: List<String>) =
            format("treatment.has.had.targeted.therapy.interfering.with.ras.mek.mapk.pathway.warn", treatments.joinToString(", "))

        fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayUndetermined() =
            get("treatment.has.had.targeted.therapy.interfering.with.ras.mek.mapk.pathway.undetermined")

        fun hasHadTargetedTherapyInterferingWithRasMekMapkPathwayFail() =
            get("treatment.has.had.targeted.therapy.interfering.with.ras.mek.mapk.pathway.fail")

        fun hasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentUndetermined(category: String, typesList: String, months: Int) =
            format("treatment.has.had.treatment.category.of.only.types.and.minimum.months.as.most.recent.undetermined", category, typesList, months)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayWithTreatments(treatments: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.display.with.treatments", treatments)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayOfCategoryAndTypes(category: String, typesList: String) =
            format(
                "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.display.of.category.and.types",
                category, typesList
            )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesDisplayOfCategory(category: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.display.of.category", category)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesResponseMessageObjectiveBenefit() =
            get("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.response.message.objective.benefit")

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesResponseMessageOther(responsesList: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.response.message.other", responsesList)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesSimilarDrugMessage(similarTreatmentsDisplay: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.similar.drug.message", similarTreatmentsDisplay)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnUncertainWithOther(
            responseMessage: String, treatmentDisplay: String, otherResponses: String
        ) = format(
            "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.warn.uncertain.with.other",
            responseMessage, treatmentDisplay, otherResponses
        )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesUndeterminedClinicalBenefit(
            treatmentDisplay: String, similarDrugMessage: String
        ) = format(
            "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.undetermined.clinical.benefit",
            treatmentDisplay, similarDrugMessage
        )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailSimilarWithPd(similarDrugMessage: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.fail.similar.with.pd", similarDrugMessage)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailNotReceived(treatmentDisplay: String) =
            format("treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.fail.not.received", treatmentDisplay)

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesPass(responseMessage: String, treatmentDisplay: String) =
            format(
                "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.pass",
                responseMessage, treatmentDisplay
            )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnStableDisease(responseMessage: String, treatmentDisplay: String) =
            format(
                "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.warn.stable.disease",
                responseMessage, treatmentDisplay
            )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesWarnMixed(responseMessage: String, treatmentDisplay: String) =
            format(
                "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.warn.mixed",
                responseMessage, treatmentDisplay
            )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesUndeterminedUnknownResponse(
            responseMessage: String, treatmentDisplay: String
        ) = format(
            "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.undetermined.unknown.response",
            responseMessage, treatmentDisplay
        )

        fun hasHadTreatmentResponseFollowingSomeTreatmentOrCategoryOfTypesFailNoResponse(responseMessage: String, treatmentDisplay: String) =
            format(
                "treatment.has.had.treatment.response.following.some.treatment.or.category.of.types.fail.no.response",
                responseMessage, treatmentDisplay
            )

        fun isEligibleForLocoRegionalTherapyUndetermined() = get("treatment.is.eligible.for.loco.regional.therapy.undetermined")

        fun isEligibleForOnLabelTreatmentUndetermined(treatmentDisplay: String) =
            format("treatment.is.eligible.for.on.label.treatment.undetermined", treatmentDisplay)

        fun isEligibleForOnLabelTreatmentFail(treatmentDisplay: String) =
            format("treatment.is.eligible.for.on.label.treatment.fail", treatmentDisplay)

        fun isEligibleForOnLabelTreatmentPass(treatmentDisplay: String) =
            format("treatment.is.eligible.for.on.label.treatment.pass", treatmentDisplay)

        fun isEligibleForOnLabelTreatmentWarn(treatmentDisplay: String) =
            format("treatment.is.eligible.for.on.label.treatment.warn", treatmentDisplay)

        fun isEligibleForPalliativeRadiotherapyUndetermined() =
            get("treatment.is.eligible.for.palliative.radiotherapy.undetermined")

        fun isEligibleForRadiotherapyUndetermined(messageAddition: String) =
            format("treatment.is.eligible.for.radiotherapy.undetermined", messageAddition)

        fun isEligibleForRadiotherapySuffixToLocation(bodyLocation: String) =
            format("treatment.is.eligible.for.radiotherapy.suffix.to.location", bodyLocation)

        fun isEligibleForSpecificSurgeryRecoverableUndetermined(surgeryName: String) =
            format("treatment.is.eligible.for.specific.surgery.recoverable.undetermined", surgeryName)

        fun isEligibleForTreatmentLinesMessage(nextTreatmentLine: Int) =
            format("treatment.is.eligible.for.treatment.lines.message", nextTreatmentLine)

        fun isEligibleForTreatmentOfCategoryAndTypeWarn(category: String, types: String) =
            format("treatment.is.eligible.for.treatment.of.category.and.type.warn", category, types)

        fun isEligibleForTreatmentOfCategoryAndTypeRecoverableUndetermined(category: String, types: String) =
            format("treatment.is.eligible.for.treatment.of.category.and.type.recoverable.undetermined", category, types)

        fun isNotEligibleForCurativeTreatmentPass() = get("treatment.is.not.eligible.for.curative.treatment.pass")

        fun isNotParticipatingInAnotherInterventionalTrialWarn() =
            get("treatment.is.not.participating.in.another.interventional.trial.warn")

        fun isNotParticipatingInAnotherInterventionalTrialPass() =
            get("treatment.is.not.participating.in.another.interventional.trial.pass")

        fun isPlatinumResistantPass() = get("treatment.is.platinum.resistant.pass")
        fun isPlatinumResistantUndetermined() = get("treatment.is.platinum.resistant.undetermined")
        fun isPlatinumResistantUndeterminedNoPlatinumTreatment() =
            get("treatment.is.platinum.resistant.undetermined.no.platinum.treatment")

        fun isPlatinumResistantFail() = get("treatment.is.platinum.resistant.fail")

        fun isPlatinumSensitiveFail() = get("treatment.is.platinum.sensitive.fail")
        fun isPlatinumSensitiveUndetermined() = get("treatment.is.platinum.sensitive.undetermined")
        fun isPlatinumSensitiveUndeterminedNoPlatinumTreatment() =
            get("treatment.is.platinum.sensitive.undetermined.no.platinum.treatment")

        fun isPlatinumSensitivePass() = get("treatment.is.platinum.sensitive.pass")

        fun isPrimaryPlatinumRefractoryWithinMonthsPass() = get("treatment.is.primary.platinum.refractory.within.months.pass")
        fun isPrimaryPlatinumRefractoryWithinMonthsUndetermined() =
            get("treatment.is.primary.platinum.refractory.within.months.undetermined")

        fun isPrimaryPlatinumRefractoryWithinMonthsUndeterminedNoPlatinumTreatment() =
            get("treatment.is.primary.platinum.refractory.within.months.undetermined.no.platinum.treatment")

        fun isPrimaryPlatinumRefractoryWithinMonthsFail() = get("treatment.is.primary.platinum.refractory.within.months.fail")

        fun meetsSpecificCriteriaForResectionRecoverableUndetermined() =
            get("treatment.meets.specific.criteria.for.resection.recoverable.undetermined")

        fun treatmentDurationEvaluatorMoreThan() = get("treatment.treatment.duration.evaluator.more.than")
        fun treatmentDurationEvaluatorLessThan() = get("treatment.treatment.duration.evaluator.less.than")
        fun treatmentDurationEvaluatorAtLeast() = get("treatment.treatment.duration.evaluator.at.least")

        fun treatmentDurationEvaluatorFailIncorrectWeeks(treatmentMessage: String, weeks: Int?) =
            format("treatment.treatment.duration.evaluator.fail.incorrect.weeks", treatmentMessage, weeks.toString())

        fun treatmentDurationEvaluatorUndeterminedMultiple(treatmentMessage: String, unacceptable: String, weeks: Int?) =
            format("treatment.treatment.duration.evaluator.undetermined.multiple", treatmentMessage, unacceptable, weeks.toString())

        fun treatmentDurationEvaluatorWeeksSuffix(acceptable: String, weeks: Int) =
            format("treatment.treatment.duration.evaluator.weeks.suffix", acceptable, weeks)

        fun treatmentDurationEvaluatorPass(treatmentMessage: String, weeksString: String) =
            format("treatment.treatment.duration.evaluator.pass", treatmentMessage, weeksString)

        fun treatmentDurationEvaluatorUndeterminedUnclearWeeks(treatmentMessage: String) =
            format("treatment.treatment.duration.evaluator.undetermined.unclear.weeks", treatmentMessage)

        fun treatmentDurationEvaluatorUndeterminedUnclearTreatment(treatmentMessage: String, weeksString: String) =
            format("treatment.treatment.duration.evaluator.undetermined.unclear.treatment", treatmentMessage, weeksString)

        fun treatmentDurationEvaluatorUndeterminedTrial(treatmentMessage: String, weeksString: String) =
            format("treatment.treatment.duration.evaluator.undetermined.trial", treatmentMessage, weeksString)

        fun treatmentDurationEvaluatorFailNotReceived(treatmentMessage: String) =
            format("treatment.treatment.duration.evaluator.fail.not.received", treatmentMessage)

        fun treatmentVersusDateFunctionsPass(predicateDescription: String, date: String) =
            format("treatment.treatment.versus.date.functions.pass", predicateDescription, date)

        fun treatmentVersusDateFunctionsUndetermined(predicateDescription: String) =
            format("treatment.treatment.versus.date.functions.undetermined", predicateDescription)

        fun treatmentVersusDateFunctionsFailBeforeDate(predicateDescription: String, date: String) =
            format("treatment.treatment.versus.date.functions.fail.before.date", predicateDescription, date)

        fun treatmentVersusDateFunctionsFailNoTreatments(predicateDescription: String) =
            format("treatment.treatment.versus.date.functions.fail.no.treatments", predicateDescription)

        fun hasHadTreatmentWithCategoryAndTypeButNotWithDrugsTypeSuffix(types: String) =
            format("treatment.has.had.treatment.with.category.and.type.but.not.with.drugs.type.suffix", types)

        fun hasHadTreatmentWithCategoryAndTypeButNotWithDrugsPass(categoryDisplay: String, typeSuffix: String, ignoreDrugsList: String) =
            format(
                "treatment.has.had.treatment.with.category.and.type.but.not.with.drugs.pass", categoryDisplay, typeSuffix, ignoreDrugsList
            )

        fun hasHadTreatmentWithCategoryAndTypeButNotWithDrugsUndetermined(
            categoryDisplay: String, typeSuffix: String, ignoreDrugsList: String
        ) = format(
            "treatment.has.had.treatment.with.category.and.type.but.not.with.drugs.undetermined", categoryDisplay, typeSuffix,
            ignoreDrugsList
        )

        fun hasHadTreatmentWithCategoryAndTypeButNotWithDrugsFail(categoryDisplay: String, typeSuffix: String, ignoreDrugsList: String) =
            format(
                "treatment.has.had.treatment.with.category.and.type.but.not.with.drugs.fail", categoryDisplay, typeSuffix, ignoreDrugsList
            )

        fun hasHadTreatmentWithCategoryButNotOfTypesPass(categoryDisplay: String, ignoreTypesList: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.pass", categoryDisplay, ignoreTypesList)

        fun hasHadTreatmentWithCategoryButNotOfTypesUndetermined(categoryDisplay: String, ignoreTypesList: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.undetermined", categoryDisplay, ignoreTypesList)

        fun hasHadTreatmentWithCategoryButNotOfTypesFail(categoryDisplay: String, ignoreTypesList: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.fail", categoryDisplay, ignoreTypesList)

        fun hasHadTreatmentWithCategoryButNotOfTypesRecentlyPass(categoryDisplay: String, ignoringTypesList: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.recently.pass", categoryDisplay, ignoringTypesList)

        fun hasHadTreatmentWithCategoryButNotOfTypesRecentlyUndeterminedPotential(categoryDisplay: String, ignoringTypesList: String) =
            format(
                "treatment.has.had.treatment.with.category.but.not.of.types.recently.undetermined.potential", categoryDisplay,
                ignoringTypesList
            )

        fun hasHadTreatmentWithCategoryButNotOfTypesRecentlyUndeterminedInconclusiveDate(
            categoryDisplay: String, ignoringTypesList: String
        ) = format(
            "treatment.has.had.treatment.with.category.but.not.of.types.recently.undetermined.inconclusive.date", categoryDisplay,
            ignoringTypesList
        )

        fun hasHadTreatmentWithCategoryButNotOfTypesRecentlyUndeterminedTrial(categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.recently.undetermined.trial", categoryDisplay)

        fun hasHadTreatmentWithCategoryButNotOfTypesRecentlyFail(categoryDisplay: String, ignoringTypesList: String) =
            format("treatment.has.had.treatment.with.category.but.not.of.types.recently.fail", categoryDisplay, ignoringTypesList)

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNoPriorDrugs() =
            get("treatment.has.had.treatment.with.category.of.types.as.most.recent.fail.no.prior.drugs")

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentPassWithTypes(typeString: String, categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.as.most.recent.pass.with.types", typeString, categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentPass(categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.as.most.recent.pass", categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentUndetermined(typeString: String, categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.as.most.recent.undetermined", typeString, categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNotMostRecent(typeString: String, categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.as.most.recent.fail.not.most.recent", typeString, categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesAsMostRecentFailNotReceived(typeString: String, categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.as.most.recent.fail.not.received", typeString, categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesRecentlyPass(typesAndCategoryString: String) =
            format("treatment.has.had.treatment.with.category.of.types.recently.pass", typesAndCategoryString)

        fun hasHadTreatmentWithCategoryOfTypesRecentlyUndeterminedPotential(typesAndCategoryString: String) =
            format("treatment.has.had.treatment.with.category.of.types.recently.undetermined.potential", typesAndCategoryString)

        fun hasHadTreatmentWithCategoryOfTypesRecentlyUndeterminedInconclusive(typesAndCategoryString: String) =
            format("treatment.has.had.treatment.with.category.of.types.recently.undetermined.inconclusive", typesAndCategoryString)

        fun hasHadTreatmentWithCategoryOfTypesRecentlyUndeterminedTrial(categoryDisplay: String) =
            format("treatment.has.had.treatment.with.category.of.types.recently.undetermined.trial", categoryDisplay)

        fun hasHadTreatmentWithCategoryOfTypesRecentlyFail(typesAndCategoryString: String) =
            format("treatment.has.had.treatment.with.category.of.types.recently.fail", typesAndCategoryString)

        fun hasHadTreatmentWithDrugAndCyclesCyclesSuffix(minCycles: Int) =
            format("treatment.has.had.treatment.with.drug.and.cycles.cycles.suffix", minCycles)

        fun hasHadTreatmentWithDrugAndCyclesPass(drugsMatchingCycles: String, cyclesSuffix: String) =
            format("treatment.has.had.treatment.with.drug.and.cycles.pass", drugsMatchingCycles, cyclesSuffix)

        fun hasHadTreatmentWithDrugAndCyclesUndeterminedCycles(drugsWithUnknownCycles: String, minCycles: Int?) =
            format("treatment.has.had.treatment.with.drug.and.cycles.undetermined.cycles", drugsWithUnknownCycles, minCycles.toString())

        fun hasHadTreatmentWithDrugAndCyclesUndeterminedTrial(drugList: String) =
            format("treatment.has.had.treatment.with.drug.and.cycles.undetermined.trial", drugList)

        fun hasHadTreatmentWithDrugAndCyclesWarn(drugsNotMatchingCycles: String, minCycles: Int?) =
            format("treatment.has.had.treatment.with.drug.and.cycles.warn", drugsNotMatchingCycles, minCycles.toString())

        fun hasHadTreatmentWithDrugAndCyclesFail(drugList: String) =
            format("treatment.has.had.treatment.with.drug.and.cycles.fail", drugList)

        fun hasHadTreatmentWithDrugAndDoseReductionUndetermined(drug: String) =
            format("treatment.has.had.treatment.with.drug.and.dose.reduction.undetermined", drug)

        fun hasHadTreatmentWithDrugAndDoseReductionFail(drug: String) =
            format("treatment.has.had.treatment.with.drug.and.dose.reduction.fail", drug)

        fun hasHadTreatmentWithDrugFromSetAsMostRecentFailNoHistory() =
            get("treatment.has.had.treatment.with.drug.from.set.as.most.recent.fail.no.history")

        fun hasHadTreatmentWithDrugFromSetAsMostRecentPass(matchingDrugDisplay: String) =
            format("treatment.has.had.treatment.with.drug.from.set.as.most.recent.pass", matchingDrugDisplay)

        fun hasHadTreatmentWithDrugFromSetAsMostRecentUndetermined(drugList: String) =
            format("treatment.has.had.treatment.with.drug.from.set.as.most.recent.undetermined", drugList)

        fun hasHadTreatmentWithDrugFromSetAsMostRecentUndeterminedTrial(drugsToMatchOr: String) =
            format("treatment.has.had.treatment.with.drug.from.set.as.most.recent.undetermined.trial", drugsToMatchOr)

        fun hasHadTreatmentWithDrugFromSetAsMostRecentFailNotMostRecent(drugsToMatchOr: String) =
            format("treatment.has.had.treatment.with.drug.from.set.as.most.recent.fail.not.most.recent", drugsToMatchOr)

        fun hasHadTreatmentWithDrugFromSetAsMostRecentFailNotReceived(drugsToMatchOr: String) =
            format("treatment.has.had.treatment.with.drug.from.set.as.most.recent.fail.not.received", drugsToMatchOr)

        fun hasLimitedCumulativeAnthracyclineExposureUndeterminedDosage() =
            get("treatment.has.limited.cumulative.anthracycline.exposure.undetermined.dosage")

        fun hasLimitedCumulativeAnthracyclineExposureUndeterminedCancerType() =
            get("treatment.has.limited.cumulative.anthracycline.exposure.undetermined.cancer.type")

        fun hasLimitedCumulativeAnthracyclineExposureUndeterminedPriorTumor() =
            get("treatment.has.limited.cumulative.anthracycline.exposure.undetermined.prior.tumor")

        fun hasLimitedCumulativeAnthracyclineExposurePass() =
            get("treatment.has.limited.cumulative.anthracycline.exposure.pass")

        fun hasPathologicalCompleteResponseAfterSurgeryUndetermined() =
            get("treatment.has.pathological.complete.response.after.surgery.undetermined")

        fun hasPreviouslyParticipatedInTrialPass(acronym: String) =
            format("treatment.has.previously.participated.in.trial.pass", acronym)

        fun hasPreviouslyParticipatedInTrialUndetermined(acronym: String) =
            format("treatment.has.previously.participated.in.trial.undetermined", acronym)

        fun hasPreviouslyParticipatedInTrialFail(acronym: String) =
            format("treatment.has.previously.participated.in.trial.fail", acronym)

        fun hasReceivedPlatinumBasedDoubletPass() = get("treatment.has.received.platinum.based.doublet.pass")

        fun hasReceivedPlatinumBasedDoubletPassUndefined(treatmentType: String, cancerType: String) =
            format("treatment.has.received.platinum.based.doublet.pass.undefined", treatmentType, cancerType)

        fun hasReceivedPlatinumBasedDoubletWarn() = get("treatment.has.received.platinum.based.doublet.warn")

        fun hasReceivedPlatinumBasedDoubletFail() = get("treatment.has.received.platinum.based.doublet.fail")

        fun hasReceivedSystemicTherapyForBrainMetastasesSuspectedSuffix() =
            get("treatment.has.received.systemic.therapy.for.brain.metastases.suspected.suffix")

        fun hasReceivedSystemicTherapyForBrainMetastasesWarn(suspectedSuffix: String) =
            format("treatment.has.received.systemic.therapy.for.brain.metastases.warn", suspectedSuffix)

        fun hasReceivedSystemicTherapyForBrainMetastasesFail() =
            get("treatment.has.received.systemic.therapy.for.brain.metastases.fail")

        fun isEligibleForFirstLinePalliativeChemotherapyFailNoMetastatic() =
            get("treatment.is.eligible.for.first.line.palliative.chemotherapy.fail.no.metastatic")

        fun isEligibleForFirstLinePalliativeChemotherapyFailHadChemo() =
            get("treatment.is.eligible.for.first.line.palliative.chemotherapy.fail.had.chemo")

        fun isEligibleForFirstLinePalliativeChemotherapyUndeterminedHadPalliative(categoriesList: String) =
            format("treatment.is.eligible.for.first.line.palliative.chemotherapy.undetermined.had.palliative", categoriesList)

        fun isEligibleForFirstLinePalliativeChemotherapyUndeterminedMetastatic() =
            get("treatment.is.eligible.for.first.line.palliative.chemotherapy.undetermined.metastatic")

        fun isEligibleForFirstLinePalliativeChemotherapyUndetermined() =
            get("treatment.is.eligible.for.first.line.palliative.chemotherapy.undetermined")

        fun isEligibleForIntensiveTreatmentUndetermined() = get("treatment.is.eligible.for.intensive.treatment.undetermined")

        fun isEligibleForLocalLiverTreatmentUndeterminedLiverCancer() =
            get("treatment.is.eligible.for.local.liver.treatment.undetermined.liver.cancer")

        fun isEligibleForLocalLiverTreatmentFail() = get("treatment.is.eligible.for.local.liver.treatment.fail")

        fun isEligibleForLocalLiverTreatmentUndetermined() = get("treatment.is.eligible.for.local.liver.treatment.undetermined")

        fun isEligibleForLocalLiverTreatmentUndeterminedLesionsUnknown() =
            get("treatment.is.eligible.for.local.liver.treatment.undetermined.lesions.unknown")

        fun isEligibleForLocalTreatmentOfMetastasesFail() = get("treatment.is.eligible.for.local.treatment.of.metastases.fail")

        fun isEligibleForLocalTreatmentOfMetastasesUndetermined() =
            get("treatment.is.eligible.for.local.treatment.of.metastases.undetermined")

        fun isEligibleForLocalTreatmentOfMetastasesUndeterminedMetastaticUnknown() =
            get("treatment.is.eligible.for.local.treatment.of.metastases.undetermined.metastatic.unknown")

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesTreatmentDescription(concatenatedTypes: String, categoryDisplay: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.treatment.description", concatenatedTypes, categoryDisplay)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDTreatmentDescription(concatenatedTypes: String, categoryDisplay: String) =
            format(
                "treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.treatment.description",
                concatenatedTypes,
                categoryDisplay
            )

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDPass(treatmentDescription: String, suffix: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.pass", treatmentDescription, suffix)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDSuffixForLessThanWeeks(maxWeeks: Int) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.suffix.for.less.than.weeks", maxWeeks)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDUndetermined(treatmentDescription: String, suffix: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.undetermined", treatmentDescription, suffix)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDSuffixUnknownWeeks() =
            get("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.suffix.unknown.weeks")

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDWeekMessageForLessThan(maxWeeks: Int) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.week.message.for.less.than", maxWeeks)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDButUncertainIfPD() =
            get("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.but.uncertain.if.pd")

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDWeekMessageUnclearWeeks() =
            get("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.week.message.unclear.weeks")

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDUndeterminedUnclearCategory(categoryDisplay: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.undetermined.unclear.category", categoryDisplay)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDFail(treatmentDescription: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.fail", treatmentDescription)

        fun hasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDFailNoTreatment(treatmentDescription: String) =
            format("treatment.has.had.limited.weeks.of.treatment.of.category.with.types.and.stop.reason.not.pd.fail.no.treatment", treatmentDescription)

        fun hasHadLiverResectionPass() = get("treatment.has.had.liver.resection.pass")
        fun hasHadLiverResectionUndetermined() = get("treatment.has.had.liver.resection.undetermined")
        fun hasHadLiverResectionFail() = get("treatment.has.had.liver.resection.fail")

        fun hasHadLocalHepaticTherapyWithinWeeksUndetermined() =
            get("treatment.has.had.local.hepatic.therapy.within.weeks.undetermined")

        fun hasHadNonInternalRadiotherapyPass(treatments: String) =
            format("treatment.has.had.non.internal.radiotherapy.pass", treatments)

        fun hasHadNonInternalRadiotherapyFail() = get("treatment.has.had.non.internal.radiotherapy.fail")

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksUndeterminedMultiple(treatmentDescription: String, minWeeks: Int) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.undetermined.multiple",
                treatmentDescription,
                minWeeks
            )

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksPass(treatmentDescription: String, minWeeks: Int) =
            format("treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.pass", treatmentDescription, minWeeks)

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksUndeterminedUnknownWeeks(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.undetermined.unknown.weeks",
                treatmentDescription
            )

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksUndeterminedUncertainPd(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.undetermined.uncertain.pd",
                treatmentDescription
            )

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksUndeterminedIfReceived(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.undetermined.if.received",
                treatmentDescription
            )

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksFailInsufficientWeeks(treatmentDescription: String, minWeeks: Int) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.fail.insufficient.weeks",
                treatmentDescription,
                minWeeks
            )

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksFailNoPd(treatmentDescription: String) =
            format("treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.fail.no.pd", treatmentDescription)

        fun hasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksFailNotReceived(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.specific.drug.combined.with.category.and.types.and.minimum.weeks.fail.not.received",
                treatmentDescription
            )

        fun hasHadPDFollowingSpecificTreatmentPass(treatments: String) =
            format("treatment.has.had.pd.following.specific.treatment.pass", treatments)

        fun hasHadPDFollowingSpecificTreatmentUndeterminedTrial(treatments: String) =
            format("treatment.has.had.pd.following.specific.treatment.undetermined.trial", treatments)

        fun hasHadPDFollowingSpecificTreatmentUndeterminedUnclearPd(treatments: String) =
            format("treatment.has.had.pd.following.specific.treatment.undetermined.unclear.pd", treatments)

        fun hasHadPDFollowingSpecificTreatmentFailNoPd(treatments: String) =
            format("treatment.has.had.pd.following.specific.treatment.fail.no.pd", treatments)

        fun hasHadPDFollowingSpecificTreatmentFailNotReceived(treatments: String) =
            format("treatment.has.had.pd.following.specific.treatment.fail.not.received", treatments)

        fun hasHadPDFollowingTreatmentWithAnyDrugPass(drugs: String) =
            format("treatment.has.had.pd.following.treatment.with.any.drug.pass", drugs)

        fun hasHadPDFollowingTreatmentWithAnyDrugUndeterminedTrial(drugs: String) =
            format("treatment.has.had.pd.following.treatment.with.any.drug.undetermined.trial", drugs)

        fun hasHadPDFollowingTreatmentWithAnyDrugUndeterminedUnclearPd(drugs: String) =
            format("treatment.has.had.pd.following.treatment.with.any.drug.undetermined.unclear.pd", drugs)

        fun hasHadPDFollowingTreatmentWithAnyDrugFailNoPd(drugs: String) =
            format("treatment.has.had.pd.following.treatment.with.any.drug.fail.no.pd", drugs)

        fun hasHadPDFollowingTreatmentWithAnyDrugFailNotReceived(drugs: String) =
            format("treatment.has.had.pd.following.treatment.with.any.drug.fail.not.received", drugs)

        fun hasHadPDFollowingTreatmentWithCategoryPass(categoryDisplay: String) =
            format("treatment.has.had.pd.following.treatment.with.category.pass", categoryDisplay)

        fun hasHadPDFollowingTreatmentWithCategoryUndeterminedApproximate(categoryDisplay: String) =
            format("treatment.has.had.pd.following.treatment.with.category.undetermined.approximate", categoryDisplay)

        fun hasHadPDFollowingTreatmentWithCategoryUndeterminedTrial(category: String) =
            format("treatment.has.had.pd.following.treatment.with.category.undetermined.trial", category)

        fun hasHadPDFollowingTreatmentWithCategoryFail(categoryDisplay: String) =
            format("treatment.has.had.pd.following.treatment.with.category.fail", categoryDisplay)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTreatmentDescription(concatenatedTypes: String, categoryDisplay: String) =
            format(
                "treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.treatment.description",
                concatenatedTypes,
                categoryDisplay
            )

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksMessage(treatmentDescription: String, suffix: String) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.message", treatmentDescription, suffix)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixAndAtLeastCycles(minCycles: Int) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.and.at.least.cycles", minCycles)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixForAtLeastWeeks(minWeeks: Int) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.for.at.least.weeks", minWeeks)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixUnknownCycles() =
            get("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.unknown.cycles")

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixUnknownWeeks() =
            get("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.unknown.weeks")

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPd(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.recoverable.undetermined.uncertain.pd",
                treatmentDescription
            )

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPdUnknownCycles(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.recoverable.undetermined.uncertain.pd.unknown.cycles",
                treatmentDescription
            )

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksRecoverableUndeterminedUncertainPdUnclearWeeks(treatmentDescription: String) =
            format(
                "treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.recoverable.undetermined.uncertain.pd.unclear.weeks",
                treatmentDescription
            )

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksUndeterminedIfReceived(treatmentDescription: String) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.undetermined.if.received", treatmentDescription)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixLessThanCycles(minCycles: Int?) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.less.than.cycles", minCycles.toString())

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksSuffixLessThanWeeks(minWeeks: Int?) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.suffix.less.than.weeks", minWeeks.toString())

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksFailNoPdAfter(categoryDisplay: String, suffix: String) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.fail.no.pd.after", categoryDisplay, suffix)

        fun hasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksFailNoTreatment(treatmentDescription: String, suffix: String) =
            format("treatment.has.had.pd.following.treatment.with.category.of.types.and.cycles.or.weeks.fail.no.treatment", treatmentDescription, suffix)

        fun hasHadPartialResectionPass() = get("treatment.has.had.partial.resection.pass")
        fun hasHadPartialResectionUndetermined() = get("treatment.has.had.partial.resection.undetermined")
        fun hasHadPartialResectionFail() = get("treatment.has.had.partial.resection.fail")

        fun hasHadRadiologicalResponseFollowingDrugTreatmentFailNoMatch(drugDisplay: String) =
            format("treatment.has.had.radiological.response.following.drug.treatment.fail.no.match", drugDisplay)

        fun hasHadRadiologicalResponseFollowingDrugTreatmentPass(drugDisplay: String) =
            format("treatment.has.had.radiological.response.following.drug.treatment.pass", drugDisplay)

        fun hasHadRadiologicalResponseFollowingDrugTreatmentUndeterminedMixed(drugDisplay: String) =
            format("treatment.has.had.radiological.response.following.drug.treatment.undetermined.mixed", drugDisplay)

        fun hasHadRadiologicalResponseFollowingDrugTreatmentFailOtherResponses(responsesJoined: String, drugDisplay: String) =
            format("treatment.has.had.radiological.response.following.drug.treatment.fail.other.responses", responsesJoined, drugDisplay)

        fun hasHadRadiologicalResponseFollowingDrugTreatmentUndeterminedDefault(drugDisplay: String) =
            format("treatment.has.had.radiological.response.following.drug.treatment.undetermined.default", drugDisplay)

        fun hasHadRadiotherapyToSomeBodyLocationSuffixForAtLeastLines(lines: Int) =
            format("treatment.has.had.radiotherapy.to.some.body.location.suffix.for.at.least.lines", lines)

        fun hasHadRadiotherapyToSomeBodyLocationPass(bodyLocation: String, messageEnding: String) =
            format("treatment.has.had.radiotherapy.to.some.body.location.pass", bodyLocation, messageEnding)

        fun hasHadRadiotherapyToSomeBodyLocationRecoverableUndetermined(bodyLocation: String) =
            format("treatment.has.had.radiotherapy.to.some.body.location.recoverable.undetermined", bodyLocation)

        fun hasHadRadiotherapyToSomeBodyLocationFail(bodyLocation: String, messageEnding: String) =
            format("treatment.has.had.radiotherapy.to.some.body.location.fail", bodyLocation, messageEnding)

        fun hasHadRecentResectionPass() = get("treatment.has.had.recent.resection.pass")
        fun hasHadRecentResectionWarn() = get("treatment.has.had.recent.resection.warn")
        fun hasHadRecentResectionUndetermined() = get("treatment.has.had.recent.resection.undetermined")
        fun hasHadRecentResectionFail() = get("treatment.has.had.recent.resection.fail")

        fun hasHadSOCTargetedTherapyForNSCLCPass(matches: String) =
            format("treatment.has.had.soc.targeted.therapy.for.nsclc.pass", matches)

        fun hasHadSOCTargetedTherapyForNSCLCFail() = get("treatment.has.had.soc.targeted.therapy.for.nsclc.fail")

        fun hasHadSomeApprovedTreatmentsFail() = get("treatment.has.had.some.approved.treatments.fail")
        fun hasHadSomeApprovedTreatmentsUndetermined() = get("treatment.has.had.some.approved.treatments.undetermined")

        fun hasHadSomeSpecificTreatmentsWithDoseReductionUndeterminedReceived(treatmentName: String) =
            format("treatment.has.had.some.specific.treatments.with.dose.reduction.undetermined.received", treatmentName)

        fun hasHadSomeSpecificTreatmentsWithDoseReductionUndeterminedMayHaveReceived(treatmentName: String) =
            format("treatment.has.had.some.specific.treatments.with.dose.reduction.undetermined.may.have.received", treatmentName)

        fun hasHadSomeSpecificTreatmentsWithDoseReductionFail(treatmentName: String) =
            format("treatment.has.had.some.specific.treatments.with.dose.reduction.fail", treatmentName)

        fun hasHadAdjuvantTreatmentWithCategoryOfTypesPass(treatments: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.of.types.pass", treatments)

        fun hasHadAdjuvantTreatmentWithCategoryOfTypesWarn(category: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.of.types.warn", category)

        fun hasHadAdjuvantTreatmentWithCategoryOfTypesUndetermined(category: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.of.types.undetermined", category)

        fun hasHadAdjuvantTreatmentWithCategoryOfTypesFail(types: String) =
            format("treatment.has.had.adjuvant.treatment.with.category.of.types.fail", types)

        fun hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesPass(type: String, minCycles: Int) =
            format("treatment.has.had.chemoradiotherapy.with.specific.chemotherapy.type.and.minimum.cycles.pass", type, minCycles)

        fun hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesWarn(type: String, minCycles: Int) =
            format("treatment.has.had.chemoradiotherapy.with.specific.chemotherapy.type.and.minimum.cycles.warn", type, minCycles)

        fun hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesUndetermined(type: String, minCycles: Int) =
            format("treatment.has.had.chemoradiotherapy.with.specific.chemotherapy.type.and.minimum.cycles.undetermined", type, minCycles)

        fun hasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesFail(type: String) =
            format("treatment.has.had.chemoradiotherapy.with.specific.chemotherapy.type.and.minimum.cycles.fail", type)

        fun hasHadProgressionFollowingLatestTreatmentLineFailNoSystemic() =
            get("treatment.has.had.progression.following.latest.treatment.line.fail.no.systemic")

        fun hasHadProgressionFollowingLatestTreatmentLinePassAllPd() =
            get("treatment.has.had.progression.following.latest.treatment.line.pass.all.pd")

        fun hasHadProgressionFollowingLatestTreatmentLineUndeterminedNoStartDate() =
            get("treatment.has.had.progression.following.latest.treatment.line.undetermined.no.start.date")

        fun hasHadProgressionFollowingLatestTreatmentLinePass(radiologicalNote: String) =
            format("treatment.has.had.progression.following.latest.treatment.line.pass", radiologicalNote)

        fun hasHadProgressionFollowingLatestTreatmentLineFail() =
            get("treatment.has.had.progression.following.latest.treatment.line.fail")

        fun hasHadProgressionFollowingLatestTreatmentLineRecoverableUndetermined() =
            get("treatment.has.had.progression.following.latest.treatment.line.recoverable.undetermined")

        fun hasHadSomeTreatmentsWithCategoryPass(minTreatmentLines: Int, category: String) =
            format("treatment.has.had.some.treatments.with.category.pass", minTreatmentLines, category)

        fun hasHadSomeTreatmentsWithCategoryUndetermined(minTreatmentLines: Int, category: String) =
            format("treatment.has.had.some.treatments.with.category.undetermined", minTreatmentLines, category)

        fun hasHadSomeTreatmentsWithCategoryFail(minTreatmentLines: Int, category: String) =
            format("treatment.has.had.some.treatments.with.category.fail", minTreatmentLines, category)

        fun descriptionMetastatic() = get("treatment.description.metastatic")

        fun descriptionAdvancedOrMetastatic() = get("treatment.description.advanced.or.metastatic")
    }

    inner class Tumor {
        fun canProvideFreshSampleForFurtherAnalysisRecoverableUndetermined() =
            get("tumor.can.provide.fresh.sample.for.further.analysis.recoverable.undetermined")

        fun canProvideFreshSampleForFurtherAnalysisPass() = get("tumor.can.provide.fresh.sample.for.further.analysis.pass")

        fun canProvideSampleForFurtherAnalysisRecoverableUndetermined() =
            get("tumor.can.provide.sample.for.further.analysis.recoverable.undetermined")

        fun canProvideSampleForFurtherAnalysisPass() = get("tumor.can.provide.sample.for.further.analysis.pass")

        fun derivedTumorStageEvaluationFunctionUndetermined(messageEnd: String) =
            format("tumor.derived.tumor.stage.evaluation.function.undetermined", messageEnd)

        fun hasAnyLesionPass() = get("tumor.has.any.lesion.pass")
        fun hasAnyLesionWarn() = get("tumor.has.any.lesion.warn")
        fun hasAnyLesionUndetermined() = get("tumor.has.any.lesion.undetermined")
        fun hasAnyLesionFail() = get("tumor.has.any.lesion.fail")

        fun hasAnyRiskCancerUndetermined(risks: String) = format("tumor.has.any.risk.cancer.undetermined", risks)

        fun hasBclcStageUndetermined() = get("tumor.has.bclc.stage.undetermined")

        fun hasBiopsyAmenableLesionRecoverableUndetermined() =
            get("tumor.has.biopsy.amenable.lesion.recoverable.undetermined")

        fun hasBiopsyAmenableLesionPass() = get("tumor.has.biopsy.amenable.lesion.pass")

        fun hasBreastCancerWithPositiveReceptorOfTypeFailNotBreastCancer() =
            get("tumor.has.breast.cancer.with.positive.receptor.of.type.fail.not.breast.cancer")

        fun hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDoidsMissing(receptorType: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.undetermined.doids.missing", receptorType)

        fun hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDataMissingHer2Amp(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.undetermined.data.missing.her2.amp", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeUndeterminedDataMissing(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.undetermined.data.missing", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeUndeterminedInconsistentData(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.undetermined.inconsistent.data", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypePass(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.pass", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeUndeterminedBorderline(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.undetermined.borderline", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeWarnLowHer2Amp(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.warn.low.her2.amp", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeFail(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.fail", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeWarnLowClinicalRelevanceUnknown(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.warn.low.clinical.relevance.unknown", receptorDisplay)

        fun hasBreastCancerWithPositiveReceptorOfTypeWarnNegativeHer2Amp(receptorDisplay: String) =
            format("tumor.has.breast.cancer.with.positive.receptor.of.type.warn.negative.her2.amp", receptorDisplay)

        fun hasCancerOfUnknownPrimaryUndeterminedNoDoids() = get("tumor.has.cancer.of.unknown.primary.undetermined.no.doids")
        fun hasCancerOfUnknownPrimaryPass() = get("tumor.has.cancer.of.unknown.primary.pass")
        fun hasCancerOfUnknownPrimaryWarn(tumorName: String) = format("tumor.has.cancer.of.unknown.primary.warn", tumorName)
        fun hasCancerOfUnknownPrimaryUndeterminedCupButType(tumorTypeDisplay: String) =
            format("tumor.has.cancer.of.unknown.primary.undetermined.cup.but.type", tumorTypeDisplay)

        fun hasCancerOfUnknownPrimaryFail() = get("tumor.has.cancer.of.unknown.primary.fail")

        fun hasCancerWithLargeCellComponentUndetermined() = get("tumor.has.cancer.with.large.cell.component.undetermined")
        fun hasCancerWithLargeCellComponentPass() = get("tumor.has.cancer.with.large.cell.component.pass")
        fun hasCancerWithLargeCellComponentUndeterminedNeuroendocrine() =
            get("tumor.has.cancer.with.large.cell.component.undetermined.neuroendocrine")

        fun hasCancerWithLargeCellComponentFail() = get("tumor.has.cancer.with.large.cell.component.fail")

        fun hasCancerWithNeuroendocrineComponentUndeterminedTumorTypeMissing() =
            get("tumor.has.cancer.with.neuroendocrine.component.undetermined.tumor.type.missing")

        fun hasCancerWithNeuroendocrineComponentPass() = get("tumor.has.cancer.with.neuroendocrine.component.pass")
        fun hasCancerWithNeuroendocrineComponentUndeterminedSmallCellComponent() =
            get("tumor.has.cancer.with.neuroendocrine.component.undetermined.small.cell.component")

        fun hasCancerWithNeuroendocrineComponentUndeterminedMolecularProfile(inactivatedGenes: List<String>) =
            format("tumor.has.cancer.with.neuroendocrine.component.undetermined.molecular.profile", inactivatedGenes.joinToString(", "))

        fun hasCancerWithNeuroendocrineComponentFail() = get("tumor.has.cancer.with.neuroendocrine.component.fail")

        fun hasCancerWithSmallCellComponentUndetermined() = get("tumor.has.cancer.with.small.cell.component.undetermined")
        fun hasCancerWithSmallCellComponentPass() = get("tumor.has.cancer.with.small.cell.component.pass")
        fun hasCancerWithSmallCellComponentWarnCertainPositive() =
            get("tumor.has.cancer.with.small.cell.component.warn.certain.positive")

        fun hasCancerWithSmallCellComponentWarnPossiblePositive() =
            get("tumor.has.cancer.with.small.cell.component.warn.possible.positive")

        fun hasCancerWithSmallCellComponentUndeterminedNeuroendocrine() =
            get("tumor.has.cancer.with.small.cell.component.undetermined.neuroendocrine")

        fun hasCancerWithSmallCellComponentFail() = get("tumor.has.cancer.with.small.cell.component.fail")

        fun hasDocumentationOfTumorTypePass(type: String) =
            format("tumor.has.documentation.of.tumor.type.pass", type.lowercase())

        fun hasEvaluableDiseasePass() = get("tumor.has.evaluable.disease.pass")
        fun hasEvaluableDiseaseRecoverableUndetermined() = get("tumor.has.evaluable.disease.recoverable.undetermined")

        fun hasEvidenceOfCnsHemorrhageByMriUndetermined() = get("tumor.has.evidence.of.cns.hemorrhage.by.mri.undetermined")

        fun hasExtensiveAbdominalTumorSpreadFail() = get("tumor.has.extensive.abdominal.tumor.spread.fail")
        fun hasExtensiveAbdominalTumorSpreadUndeterminedMetastaticUnclear() =
            get("tumor.has.extensive.abdominal.tumor.spread.undetermined.metastatic.unclear")

        fun hasExtensiveAbdominalTumorSpreadUndetermined() = get("tumor.has.extensive.abdominal.tumor.spread.undetermined")

        fun hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisFail() =
            get("tumor.has.extensive.systemic.metastases.predominantly.determining.prognosis.fail")

        fun hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisUndeterminedMetastaticUnclear() =
            get("tumor.has.extensive.systemic.metastases.predominantly.determining.prognosis.undetermined.metastatic.unclear")

        fun hasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisUndetermined() =
            get("tumor.has.extensive.systemic.metastases.predominantly.determining.prognosis.undetermined")

        fun hasExtracranialMetastasesPass() = get("tumor.has.extracranial.metastases.pass")
        fun hasExtracranialMetastasesWarn() = get("tumor.has.extracranial.metastases.warn")
        fun hasExtracranialMetastasesUndetermined() = get("tumor.has.extracranial.metastases.undetermined")
        fun hasExtracranialMetastasesFail() = get("tumor.has.extracranial.metastases.fail")

        fun hasHifuAmenableLesionUndetermined() = get("tumor.has.hifu.amenable.lesion.undetermined")

        fun hasInTransitMetastasesUndetermined() = get("tumor.has.in.transit.metastases.undetermined")

        fun hasIncurableCancerUndeterminedMissing() = get("tumor.has.incurable.cancer.undetermined.missing")
        fun hasIncurableCancerPass(stageMessage: String) = format("tumor.has.incurable.cancer.pass", stageMessage)
        fun hasIncurableCancerUndetermined(stageMessage: String) = format("tumor.has.incurable.cancer.undetermined", stageMessage)
        fun hasIncurableCancerFail(stageMessage: String) = format("tumor.has.incurable.cancer.fail", stageMessage)

        fun hasInjectionAmenableLesionUndetermined() = get("tumor.has.injection.amenable.lesion.undetermined")

        fun hasIntratumoralHemorrhageByMriUndetermined() = get("tumor.has.intratumoral.hemorrhage.by.mri.undetermined")

        fun hasIrradiationAmenableLesionFail() = get("tumor.has.irradiation.amenable.lesion.fail")
        fun hasIrradiationAmenableLesionUndetermined() = get("tumor.has.irradiation.amenable.lesion.undetermined")
        fun hasIrradiationAmenableLesionRecoverableUndetermined() =
            get("tumor.has.irradiation.amenable.lesion.recoverable.undetermined")

        fun hasKnownActiveBrainMetastasesUndeterminedActivity(prefix: String) =
            format("tumor.has.known.active.brain.metastases.undetermined.activity", prefix)

        fun hasKnownActiveBrainMetastasesUndeterminedMissing() =
            get("tumor.has.known.active.brain.metastases.undetermined.missing")

        fun hasKnownActiveBrainMetastasesPass() = get("tumor.has.known.active.brain.metastases.pass")
        fun hasKnownActiveBrainMetastasesFail() = get("tumor.has.known.active.brain.metastases.fail")

        fun hasKnownActiveCnsMetastasesUndetermined() = get("tumor.has.known.active.cns.metastases.undetermined")
        fun hasKnownActiveCnsMetastasesUndeterminedSuspected() =
            get("tumor.has.known.active.cns.metastases.undetermined.suspected")

        fun hasKnownActiveCnsMetastasesUndeterminedMissing() =
            get("tumor.has.known.active.cns.metastases.undetermined.missing")

        fun hasKnownActiveCnsMetastasesPass() = get("tumor.has.known.active.cns.metastases.pass")
        fun hasKnownActiveCnsMetastasesPassBrain() = get("tumor.has.known.active.cns.metastases.pass.brain")
        fun hasKnownActiveCnsMetastasesFail() = get("tumor.has.known.active.cns.metastases.fail")

        fun hasKnownBrainMetastasesPass() = get("tumor.has.known.brain.metastases.pass")
        fun hasKnownBrainMetastasesWarn() = get("tumor.has.known.brain.metastases.warn")
        fun hasKnownBrainMetastasesUndetermined() = get("tumor.has.known.brain.metastases.undetermined")
        fun hasKnownBrainMetastasesFail() = get("tumor.has.known.brain.metastases.fail")

        fun hasKnownCnsMetastasesPass() = get("tumor.has.known.cns.metastases.pass")
        fun hasKnownCnsMetastasesPassBrain() = get("tumor.has.known.cns.metastases.pass.brain")
        fun hasKnownCnsMetastasesWarn() = get("tumor.has.known.cns.metastases.warn")
        fun hasKnownCnsMetastasesUndetermined() = get("tumor.has.known.cns.metastases.undetermined")
        fun hasKnownCnsMetastasesFail() = get("tumor.has.known.cns.metastases.fail")

        fun hasKnownSclcTransformationPass() = get("tumor.has.known.sclc.transformation.pass")
        fun hasKnownSclcTransformationWarn() = get("tumor.has.known.sclc.transformation.warn")
        fun hasKnownSclcTransformationUndeterminedSmallCellComponent() =
            get("tumor.has.known.sclc.transformation.undetermined.small.cell.component")

        fun hasKnownSclcTransformationUndeterminedInactivation(genes: String) =
            format("tumor.has.known.sclc.transformation.undetermined.inactivation", genes)

        fun hasKnownSclcTransformationUndeterminedUncertainType() =
            get("tumor.has.known.sclc.transformation.undetermined.uncertain.type")

        fun hasKnownSclcTransformationFail() = get("tumor.has.known.sclc.transformation.fail")
        fun hasKnownSclcTransformationRecoverableFail() = get("tumor.has.known.sclc.transformation.recoverable.fail")

        fun hasKnownSymptomaticBrainMetastasesUndetermined() =
            get("tumor.has.known.symptomatic.brain.metastases.undetermined")

        fun hasKnownSymptomaticBrainMetastasesUndeterminedMissing() =
            get("tumor.has.known.symptomatic.brain.metastases.undetermined.missing")

        fun hasKnownSymptomaticBrainMetastasesPass() = get("tumor.has.known.symptomatic.brain.metastases.pass")
        fun hasKnownSymptomaticBrainMetastasesFail() = get("tumor.has.known.symptomatic.brain.metastases.fail")

        fun hasKnownSymptomaticCnsMetastasesUndetermined() =
            get("tumor.has.known.symptomatic.cns.metastases.undetermined")

        fun hasKnownSymptomaticCnsMetastasesUndeterminedSuspected() =
            get("tumor.has.known.symptomatic.cns.metastases.undetermined.suspected")

        fun hasKnownSymptomaticCnsMetastasesUndeterminedMissing() =
            get("tumor.has.known.symptomatic.cns.metastases.undetermined.missing")

        fun hasKnownSymptomaticCnsMetastasesPass() = get("tumor.has.known.symptomatic.cns.metastases.pass")
        fun hasKnownSymptomaticCnsMetastasesPassBrain() = get("tumor.has.known.symptomatic.cns.metastases.pass.brain")
        fun hasKnownSymptomaticCnsMetastasesFail() = get("tumor.has.known.symptomatic.cns.metastases.fail")

        fun hasLeftSidedColorectalTumorUndetermined() = get("tumor.has.left.sided.colorectal.tumor.undetermined")
        fun hasLeftSidedColorectalTumorFailNotColorectal() =
            get("tumor.has.left.sided.colorectal.tumor.fail.not.colorectal")

        fun hasLeftSidedColorectalTumorPass(name: String) = format("tumor.has.left.sided.colorectal.tumor.pass", name)
        fun hasLeftSidedColorectalTumorFail(name: String) = format("tumor.has.left.sided.colorectal.tumor.fail", name)
        fun hasLeftSidedColorectalTumorUndeterminedLocation(name: String) =
            format("tumor.has.left.sided.colorectal.tumor.undetermined.location", name)

        fun hasLesionsCloseToOrInvolvingAirwayPass() = get("tumor.has.lesions.close.to.or.involving.airway.pass")
        fun hasLesionsCloseToOrInvolvingAirwaySubjectSuspectedLung() =
            get("tumor.has.lesions.close.to.or.involving.airway.subject.suspected.lung")

        fun hasLesionsCloseToOrInvolvingAirwaySubjectLung() =
            get("tumor.has.lesions.close.to.or.involving.airway.subject.lung")

        fun hasLesionsCloseToOrInvolvingAirwayWarn(subject: String) =
            format("tumor.has.lesions.close.to.or.involving.airway.warn", subject)

        fun hasLesionsCloseToOrInvolvingAirwayFail() = get("tumor.has.lesions.close.to.or.involving.airway.fail")
        fun hasLesionsCloseToOrInvolvingAirwayUndetermined() =
            get("tumor.has.lesions.close.to.or.involving.airway.undetermined")

        fun hasLesionsInfiltratingBloodVesselUndetermined() =
            get("tumor.has.lesions.infiltrating.blood.vessel.undetermined")

        fun hasLimitedTumorLengthUndetermined(length: Int) = format("tumor.has.limited.tumor.length.undetermined", length)

        fun hasLocallyAdvancedCancerUndeterminedStageMissing() =
            get("tumor.has.locally.advanced.cancer.undetermined.stage.missing")

        fun hasLocallyAdvancedCancerPass(stage: String) = format("tumor.has.locally.advanced.cancer.pass", stage)
        fun hasLocallyAdvancedCancerUndetermined(stage: String) = format("tumor.has.locally.advanced.cancer.undetermined", stage)
        fun hasLocallyAdvancedCancerFail(stage: String) = format("tumor.has.locally.advanced.cancer.fail", stage)

        fun hasLowGradeCancerPass() = get("tumor.has.low.grade.cancer.pass")
        fun hasLowGradeCancerFail() = get("tumor.has.low.grade.cancer.fail")
        fun hasLowGradeCancerUndetermined() = get("tumor.has.low.grade.cancer.undetermined")

        fun hasLowRiskOfHemorrhageUponTreatmentUndetermined() =
            get("tumor.has.low.risk.of.hemorrhage.upon.treatment.undetermined")

        fun hasMriVolumeAmenableLesionUndetermined() = get("tumor.has.mri.volume.amenable.lesion.undetermined")

        fun hasMeasurableDiseaseRecoverableUndetermined() = get("tumor.has.measurable.disease.recoverable.undetermined")
        fun hasMeasurableDiseasePass() = get("tumor.has.measurable.disease.pass")
        fun hasMeasurableDiseaseRecoverableFail() = get("tumor.has.measurable.disease.recoverable.fail")

        fun hasMeasurableDiseasePercistRecoverableUndetermined() =
            get("tumor.has.measurable.disease.percist.recoverable.undetermined")

        fun hasMeasurableDiseasePercistWarn() = get("tumor.has.measurable.disease.percist.warn")
        fun hasMeasurableDiseasePercistPass() = get("tumor.has.measurable.disease.percist.pass")
        fun hasMeasurableDiseasePercistRecoverableFail() = get("tumor.has.measurable.disease.percist.recoverable.fail")

        fun hasMeasurableDiseaseRanoRecoverableUndetermined() =
            get("tumor.has.measurable.disease.rano.recoverable.undetermined")

        fun hasMeasurableDiseaseRanoPass() = get("tumor.has.measurable.disease.rano.pass")
        fun hasMeasurableDiseaseRanoWarn() = get("tumor.has.measurable.disease.rano.warn")
        fun hasMeasurableDiseaseRanoRecoverableFail() = get("tumor.has.measurable.disease.rano.recoverable.fail")

        fun hasMeasurableDiseaseRecistRecoverableUndetermined() =
            get("tumor.has.measurable.disease.recist.recoverable.undetermined")

        fun hasMeasurableDiseaseRecistWarn() = get("tumor.has.measurable.disease.recist.warn")
        fun hasMeasurableDiseaseRecistPass() = get("tumor.has.measurable.disease.recist.pass")
        fun hasMeasurableDiseaseRecistRecoverableFail() = get("tumor.has.measurable.disease.recist.recoverable.fail")

        fun hasMetastaticCancerUndeterminedStageMissing() = get("tumor.has.metastatic.cancer.undetermined.stage.missing")
        fun hasMetastaticCancerPass(stage: String) = format("tumor.has.metastatic.cancer.pass", stage)
        fun hasMetastaticCancerUndetermined(stage: String) = format("tumor.has.metastatic.cancer.undetermined", stage)
        fun hasMetastaticCancerFail(stage: String) = format("tumor.has.metastatic.cancer.fail", stage)

        fun hasMinimumLesionsInSpecificBodyLocationPass(minLesions: Int, bodyLocation: String) =
            format("tumor.has.minimum.lesions.in.specific.body.location.pass", minLesions, bodyLocation)

        fun hasMinimumLesionsInSpecificBodyLocationUndetermined(minLesions: Int, bodyLocation: String) =
            format("tumor.has.minimum.lesions.in.specific.body.location.undetermined", minLesions, bodyLocation)

        fun hasMinimumLesionsInSpecificBodyLocationFail(minLesions: Int, bodyLocation: String) =
            format("tumor.has.minimum.lesions.in.specific.body.location.fail", minLesions, bodyLocation)

        fun hasMinimumModifiedOberlinPrognosticScoreUndetermined(score: Int) =
            format("tumor.has.minimum.modified.oberlin.prognostic.score.undetermined", score)

        fun hasMinimumRiskForSentinelNodePositivityUndetermined(minimumRisk: Int) =
            format("tumor.has.minimum.risk.for.sentinel.node.positivity.undetermined", minimumRisk)

        fun hasMinimumSitesWithLesionsPass(minimumSites: Int) = format("tumor.has.minimum.sites.with.lesions.pass", minimumSites)
        fun hasMinimumSitesWithLesionsWarn(minimumSites: Int) = format("tumor.has.minimum.sites.with.lesions.warn", minimumSites)
        fun hasMinimumSitesWithLesionsUndetermined(minimumSites: Int) =
            format("tumor.has.minimum.sites.with.lesions.undetermined", minimumSites)

        fun hasMinimumSitesWithLesionsUndeterminedSuspected(minimumSites: Int) =
            format("tumor.has.minimum.sites.with.lesions.undetermined.suspected", minimumSites)

        fun hasMinimumSitesWithLesionsFail(minimumSites: Int) = format("tumor.has.minimum.sites.with.lesions.fail", minimumSites)

        fun hasNonMuscleInvasiveBladderCancerUndeterminedDoidsMissing() =
            get("tumor.has.non.muscle.invasive.bladder.cancer.undetermined.doids.missing")

        fun hasNonMuscleInvasiveBladderCancerPass() = get("tumor.has.non.muscle.invasive.bladder.cancer.pass")
        fun hasNonMuscleInvasiveBladderCancerUndetermined() = get("tumor.has.non.muscle.invasive.bladder.cancer.undetermined")
        fun hasNonMuscleInvasiveBladderCancerFail() = get("tumor.has.non.muscle.invasive.bladder.cancer.fail")

        fun hasNonSquamousNsclcUndeterminedTumorTypeMissing() =
            get("tumor.has.non.squamous.nsclc.undetermined.tumor.type.missing")

        fun hasNonSquamousNsclcFail() = get("tumor.has.non.squamous.nsclc.fail")
        fun hasNonSquamousNsclcWarnPositiveScc() = get("tumor.has.non.squamous.nsclc.warn.positive.scc")
        fun hasNonSquamousNsclcWarnPossiblePositiveScc() = get("tumor.has.non.squamous.nsclc.warn.possible.positive.scc")
        fun hasNonSquamousNsclcPass() = get("tumor.has.non.squamous.nsclc.pass")
        fun hasNonSquamousNsclcUndeterminedType() = get("tumor.has.non.squamous.nsclc.undetermined.type")

        fun hasTnmTScoreUndeterminedNoStage() = get("tumor.has.tnm.t.score.undetermined.no.stage")
        fun hasTnmTScoreUndeterminedMetastatic(targetTnmTs: String) =
            format("tumor.has.tnm.t.score.undetermined.metastatic", targetTnmTs)

        fun hasTnmTScorePass(possibleTnmTs: String) = format("tumor.has.tnm.t.score.pass", possibleTnmTs)
        fun hasTnmTScoreUndetermined(targetTnmTs: String, possibleTnmTs: String) =
            format("tumor.has.tnm.t.score.undetermined", targetTnmTs, possibleTnmTs)

        fun hasTnmTScoreFail(targetTnmTs: String) = format("tumor.has.tnm.t.score.fail", targetTnmTs)

        fun hasTripleNegativeBreastCancerUndeterminedMissingDoids() =
            get("tumor.has.triple.negative.breast.cancer.undetermined.missing.doids")

        fun hasTripleNegativeBreastCancerFail() = get("tumor.has.triple.negative.breast.cancer.fail")
        fun hasTripleNegativeBreastCancerUndeterminedErbb2Inconsistent() =
            get("tumor.has.triple.negative.breast.cancer.undetermined.erbb2.inconsistent")

        fun hasTripleNegativeBreastCancerPass() = get("tumor.has.triple.negative.breast.cancer.pass")
        fun hasTripleNegativeBreastCancerUndeterminedIhcLow() =
            get("tumor.has.triple.negative.breast.cancer.undetermined.ihc.low")

        fun hasTripleNegativeBreastCancerUndeterminedHer2Missing() =
            get("tumor.has.triple.negative.breast.cancer.undetermined.her2.missing")

        fun hasTripleNegativeBreastCancerUndetermined() = get("tumor.has.triple.negative.breast.cancer.undetermined")

        fun hasTumorStageUndeterminedMissing() = get("tumor.has.tumor.stage.undetermined.missing")
        fun hasTumorStagePass(stageMessage: String, stagesToMatchMessage: String) =
            format("tumor.has.tumor.stage.pass", stageMessage, stagesToMatchMessage)

        fun hasTumorStageUndetermined(stageMessage: String, stagesToMatchMessage: String) =
            format("tumor.has.tumor.stage.undetermined", stageMessage, stagesToMatchMessage)

        fun hasTumorStageFail(stageMessage: String, stagesToMatchMessage: String) =
            format("tumor.has.tumor.stage.fail", stageMessage, stagesToMatchMessage)

        fun hasUnresectableCancerUndeterminedMissing() = get("tumor.has.unresectable.cancer.undetermined.missing")
        fun hasUnresectableCancerPass(stageMessage: String) = format("tumor.has.unresectable.cancer.pass", stageMessage)
        fun hasUnresectableCancerUndetermined(stageMessage: String) =
            format("tumor.has.unresectable.cancer.undetermined", stageMessage)

        fun hasUnresectableCancerFail(stageMessage: String) = format("tumor.has.unresectable.cancer.fail", stageMessage)

        fun hasUnresectablePeritonealMetastasesUndeterminedMissing() =
            get("tumor.has.unresectable.peritoneal.metastases.undetermined.missing")

        fun hasUnresectablePeritonealMetastasesSuffixSuspected() =
            get("tumor.has.unresectable.peritoneal.metastases.suffix.suspected")

        fun hasUnresectablePeritonealMetastasesWarn(suspectedSuffix: String) =
            format("tumor.has.unresectable.peritoneal.metastases.warn", suspectedSuffix)

        fun hasUnresectablePeritonealMetastasesFail() = get("tumor.has.unresectable.peritoneal.metastases.fail")

        fun hasUnresectableStageIIICancerUndeterminedMissing() =
            get("tumor.has.unresectable.stage.iii.cancer.undetermined.missing")

        fun hasUnresectableStageIIICancerUndetermined() = get("tumor.has.unresectable.stage.iii.cancer.undetermined")
        fun hasUnresectableStageIIICancerFail() = get("tumor.has.unresectable.stage.iii.cancer.fail")

        fun hasVisceralMetastasesUndetermined() = get("tumor.has.visceral.metastases.undetermined")

        fun hasVisibleLesionByCystoscopyRecoverableUndetermined() =
            get("tumor.has.visible.lesion.by.cystoscopy.recoverable.undetermined")

        fun hasWellDifferentiatedTumorPass() = get("tumor.has.well.differentiated.tumor.pass")
        fun hasWellDifferentiatedTumorFail() = get("tumor.has.well.differentiated.tumor.fail")
        fun hasWellDifferentiatedTumorUndetermined() = get("tumor.has.well.differentiated.tumor.undetermined")

        fun meetsSpecificBiopsyRequirementsRecoverableUndetermined() =
            get("tumor.meets.specific.biopsy.requirements.recoverable.undetermined")

        fun meetsSpecificCriteriaRegardingBrainMetastasesUndetermined() =
            get("tumor.meets.specific.criteria.regarding.brain.metastases.undetermined")

        fun meetsSpecificCriteriaRegardingBrainMetastasesUndeterminedSuspected() =
            get("tumor.meets.specific.criteria.regarding.brain.metastases.undetermined.suspected")

        fun meetsSpecificCriteriaRegardingBrainMetastasesUndeterminedMissing() =
            get("tumor.meets.specific.criteria.regarding.brain.metastases.undetermined.missing")

        fun meetsSpecificCriteriaRegardingBrainMetastasesFail() =
            get("tumor.meets.specific.criteria.regarding.brain.metastases.fail")

        fun meetsSpecificCriteriaRegardingLiverMetastasesUndeterminedMissing() =
            get("tumor.meets.specific.criteria.regarding.liver.metastases.undetermined.missing")

        fun meetsSpecificCriteriaRegardingLiverMetastasesUndetermined() =
            get("tumor.meets.specific.criteria.regarding.liver.metastases.undetermined")

        fun meetsSpecificCriteriaRegardingLiverMetastasesFail() =
            get("tumor.meets.specific.criteria.regarding.liver.metastases.fail")

        fun meetsSpecificCriteriaRegardingMetastasesFail() =
            get("tumor.meets.specific.criteria.regarding.metastases.fail")

        fun meetsSpecificCriteriaRegardingMetastasesUndetermined() =
            get("tumor.meets.specific.criteria.regarding.metastases.undetermined")

        fun meetsSpecificCriteriaRegardingMetastasesUndeterminedUnknown() =
            get("tumor.meets.specific.criteria.regarding.metastases.undetermined.unknown")

        fun meetsSpecificCriteriaRegardingRecurrentCancerUndetermined() =
            get("tumor.meets.specific.criteria.regarding.recurrent.cancer.undetermined")

        fun primaryTumorLocationBelongsToDoidUndeterminedUnknownType() =
            get("tumor.primary.tumor.location.belongs.to.doid.undetermined.unknown.type")

        fun primaryTumorLocationBelongsToDoidPassSpecificQuery(doidTerms: String, specificQuery: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.pass.specific.query", doidTerms, specificQuery)

        fun primaryTumorLocationBelongsToDoidWarnSpecificQuery(doidTerms: String, specificQuery: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.warn.specific.query", doidTerms, specificQuery)

        fun primaryTumorLocationBelongsToDoidPass(doidTerms: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.pass", doidTerms)

        fun primaryTumorLocationBelongsToDoidWarnAdenoSquamous(matches: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.warn.adeno.squamous", matches)

        fun primaryTumorLocationBelongsToDoidUndetermined(terms: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.undetermined", terms)

        fun primaryTumorLocationBelongsToDoidFail(terms: String) =
            format("tumor.primary.tumor.location.belongs.to.doid.fail", terms)

        fun primaryTumorLocationBelongsToDoidWarnCuppa(cancerType: String, likelihoodPct: Int) =
            format("tumor.primary.tumor.location.belongs.to.doid.warn.cuppa", cancerType, likelihoodPct)

        fun tumorMetastasisEvaluatorPass(metastasisType: String) = format("tumor.tumor.metastasis.evaluator.pass", metastasisType)
        fun tumorMetastasisEvaluatorWarn(metastasisType: String) = format("tumor.tumor.metastasis.evaluator.warn", metastasisType)
        fun tumorMetastasisEvaluatorUndetermined(metastasisType: String) =
            format("tumor.tumor.metastasis.evaluator.undetermined", metastasisType)

        fun tumorMetastasisEvaluatorFail(metastasisType: String) = format("tumor.tumor.metastasis.evaluator.fail", metastasisType)

        fun hasOligometastaticCancerUndeterminedMissing() = get("tumor.has.oligometastatic.cancer.undetermined.missing")
        fun hasOligometastaticCancerUndetermined() = get("tumor.has.oligometastatic.cancer.undetermined")
        fun hasOligometastaticCancerFail(stage: String?) = format("tumor.has.oligometastatic.cancer.fail", stage.toString())

        fun hasOligoprogressiveDiseaseUndetermined() = get("tumor.has.oligoprogressive.disease.undetermined")

        fun hasOnlyLungAndOrLungLymphNodeMetastasesUndetermined() =
            get("tumor.has.only.lung.and.or.lung.lymph.node.metastases.undetermined")

        fun hasOvarianBorderlineTumorUndeterminedNoDoids() = get("tumor.has.ovarian.borderline.tumor.undetermined.no.doids")
        fun hasOvarianBorderlineTumorPass() = get("tumor.has.ovarian.borderline.tumor.pass")
        fun hasOvarianBorderlineTumorWarn() = get("tumor.has.ovarian.borderline.tumor.warn")
        fun hasOvarianBorderlineTumorFail() = get("tumor.has.ovarian.borderline.tumor.fail")

        fun hasOvarianCancerWithMucinousComponentUndetermined() =
            get("tumor.has.ovarian.cancer.with.mucinous.component.undetermined")

        fun hasOvarianCancerWithMucinousComponentPass() = get("tumor.has.ovarian.cancer.with.mucinous.component.pass")
        fun hasOvarianCancerWithMucinousComponentFail() = get("tumor.has.ovarian.cancer.with.mucinous.component.fail")

        fun hasPrimaryTumorAtUnfavourableSiteUndetermined() =
            get("tumor.has.primary.tumor.at.unfavourable.site.undetermined")

        fun hasRapidProgressiveDiseaseUndetermined() = get("tumor.has.rapid.progressive.disease.undetermined")

        fun hasRecurrentCancerUndetermined() = get("tumor.has.recurrent.cancer.undetermined")

        fun hasSecondaryGlioblastomaUndetermined() = get("tumor.has.secondary.glioblastoma.undetermined")
        fun hasSecondaryGlioblastomaWarn(doidTerm: String?) = format("tumor.has.secondary.glioblastoma.warn", doidTerm.toString())
        fun hasSecondaryGlioblastomaFail() = get("tumor.has.secondary.glioblastoma.fail")

        fun hasSiewertTypeUndetermined(type: String) = format("tumor.has.siewert.type.undetermined", type)

        fun hasSoftTissueMetastasesUndetermined() = get("tumor.has.soft.tissue.metastases.undetermined")
        fun hasSoftTissueMetastasesPass() = get("tumor.has.soft.tissue.metastases.pass")
        fun hasSoftTissueMetastasesWarn() = get("tumor.has.soft.tissue.metastases.warn")
        fun hasSoftTissueMetastasesFail() = get("tumor.has.soft.tissue.metastases.fail")

        fun hasSolidPrimaryTumorUndetermined() = get("tumor.has.solid.primary.tumor.undetermined")
        fun hasSolidPrimaryTumorFail() = get("tumor.has.solid.primary.tumor.fail")
        fun hasSolidPrimaryTumorWarn() = get("tumor.has.solid.primary.tumor.warn")
        fun hasSolidPrimaryTumorPass() = get("tumor.has.solid.primary.tumor.pass")

        fun hasSolidPrimaryTumorIncludingLymphomaUndetermined() =
            get("tumor.has.solid.primary.tumor.including.lymphoma.undetermined")

        fun hasSolidPrimaryTumorIncludingLymphomaFail() = get("tumor.has.solid.primary.tumor.including.lymphoma.fail")
        fun hasSolidPrimaryTumorIncludingLymphomaWarn() = get("tumor.has.solid.primary.tumor.including.lymphoma.warn")
        fun hasSolidPrimaryTumorIncludingLymphomaPass() = get("tumor.has.solid.primary.tumor.including.lymphoma.pass")

        fun hasSpecificMetastasesOnlyPass(typeOfMetastases: String) =
            format("tumor.has.specific.metastases.only.pass", typeOfMetastases)

        fun hasSpecificMetastasesOnlyFail(typeOfMetastases: String) =
            format("tumor.has.specific.metastases.only.fail", typeOfMetastases)

        fun hasSpecificMetastasesOnlyUndeterminedSuspected(typeOfMetastases: String) =
            format("tumor.has.specific.metastases.only.undetermined.suspected", typeOfMetastases)

        fun hasSpecificMetastasesOnlyUndeterminedMissing(typeOfMetastases: String) =
            format("tumor.has.specific.metastases.only.undetermined.missing", typeOfMetastases)

        fun hasSpecificProgressiveDiseaseCriteriaRecoverableUndetermined() =
            get("tumor.has.specific.progressive.disease.criteria.recoverable.undetermined")

        fun hasSpleenMetastasesUndetermined() = get("tumor.has.spleen.metastases.undetermined")
        fun hasSpleenMetastasesPass() = get("tumor.has.spleen.metastases.pass")
        fun hasSpleenMetastasesWarn() = get("tumor.has.spleen.metastases.warn")
        fun hasSpleenMetastasesFail() = get("tumor.has.spleen.metastases.fail")

        fun hasSuperScanBoneScanUndetermined() = get("tumor.has.super.scan.bone.scan.undetermined")

        fun hasSymptomsOfPrimaryTumorInSituUndetermined() =
            get("tumor.has.symptoms.of.primary.tumor.in.situ.undetermined")

        fun descriptionCytological() = get("tumor.description.cytological")

        fun descriptionHistological() = get("tumor.description.histological")

        fun descriptionPathological() = get("tumor.description.pathological")

        fun descriptionTumorStages(stagesDisplay: String) = format("tumor.description.tumor.stages", stagesDisplay)

        fun descriptionLocallyAdvancedCancer() = get("tumor.description.locally.advanced.cancer")

        fun descriptionMetastaticCancer() = get("tumor.description.metastatic.cancer")

        fun descriptionOligometastaticCancer() = get("tumor.description.oligometastatic.cancer")

        fun descriptionUnresectableCancer() = get("tumor.description.unresectable.cancer")

        fun descriptionUnresectableStageIiiCancer() = get("tumor.description.unresectable.stage.iii.cancer")

        fun descriptionRecurrentCancer() = get("tumor.description.recurrent.cancer")

        fun descriptionIncurableCancer() = get("tumor.description.incurable.cancer")

        fun descriptionLiver() = get("tumor.description.liver")

        fun descriptionLiverAndOrLymphNodeAndOrLung() = get("tumor.description.liver.and.or.lymph.node.and.or.lung")

        fun descriptionBone() = get("tumor.description.bone")
    }

    inner class VitalFunction {
        fun bloodPressureFunctionsNoData(categoryDisplay: String) =
            format("vitalfunction.blood.pressure.functions.no.data", categoryDisplay)

        fun bloodPressureFunctionsBelowReference(categoryDisplay: String, medianDisplay: Any, reference: Int) =
            format("vitalfunction.blood.pressure.functions.below.reference", categoryDisplay, medianDisplay, reference)

        fun bloodPressureFunctionsEqualToReference(categoryDisplay: String, medianDisplay: Any, reference: Int) =
            format("vitalfunction.blood.pressure.functions.equal.to.reference", categoryDisplay, medianDisplay, reference)

        fun bloodPressureFunctionsAboveReference(categoryDisplay: String, medianDisplay: Any, reference: Int) =
            format("vitalfunction.blood.pressure.functions.above.reference", categoryDisplay, medianDisplay, reference)

        fun bloodPressureFunctionsMarginOfErrorSuffix() = get("vitalfunction.blood.pressure.functions.margin.of.error.suffix")

        fun bodyWeightFunctionsWrongUnit(unitsDisplay: String) =
            format("vitalfunction.body.weight.functions.wrong.unit", unitsDisplay)

        fun bodyWeightFunctionsNoData() = get("vitalfunction.body.weight.functions.no.data")

        fun bodyWeightFunctionsBelowReference(median: Double, reference: Double) =
            format("vitalfunction.body.weight.functions.below.reference", median, reference)

        fun bodyWeightFunctionsEqualToReference(median: Double, reference: Double) =
            format("vitalfunction.body.weight.functions.equal.to.reference", median, reference)

        fun bodyWeightFunctionsAboveReference(median: Double, reference: Double) =
            format("vitalfunction.body.weight.functions.above.reference", median, reference)

        fun hasBmiUpToLimitUndeterminedWrongUnit(unitDisplay: String) =
            format("vitalfunction.has.bmi.up.to.limit.undetermined.wrong.unit", unitDisplay)

        fun hasBmiUpToLimitUndeterminedNoData() = get("vitalfunction.has.bmi.up.to.limit.undetermined.no.data")

        fun hasBmiUpToLimitRecoverablePass(bmiRounded: Int, maximumBMI: Int) =
            format("vitalfunction.has.bmi.up.to.limit.recoverable.pass", bmiRounded, maximumBMI)

        fun hasBmiUpToLimitRecoverableFail(bmiRounded: Int, maximumBMI: Int) =
            format("vitalfunction.has.bmi.up.to.limit.recoverable.fail", bmiRounded, maximumBMI)

        fun hasBmiUpToLimitPassHeightLowerBound(medianDisplay: String, maximumBMI: Int, heightDisplay: String) =
            format("vitalfunction.has.bmi.up.to.limit.pass.height.lower.bound", medianDisplay, maximumBMI, heightDisplay)

        fun hasBmiUpToLimitExceedsLimitForHeight(medianDisplay: String, maximumBMI: Int, heightDisplay: String) =
            format("vitalfunction.has.bmi.up.to.limit.exceeds.limit.for.height", medianDisplay, maximumBMI, heightDisplay)

        fun hasRestingHeartRateWithinBoundsUndeterminedNoData() =
            get("vitalfunction.has.resting.heart.rate.within.bounds.undetermined.no.data")

        fun hasRestingHeartRateWithinBoundsUndeterminedWrongUnit(unitDisplay: String) =
            format("vitalfunction.has.resting.heart.rate.within.bounds.undetermined.wrong.unit", unitDisplay)

        fun hasRestingHeartRateWithinBoundsRecoverablePass(median: Double, minRate: Double, maxRate: Double) =
            format("vitalfunction.has.resting.heart.rate.within.bounds.recoverable.pass", median, minRate, maxRate)

        fun hasRestingHeartRateWithinBoundsRecoverableUndetermined(median: Double, minRate: Double, maxRate: Double) =
            format("vitalfunction.has.resting.heart.rate.within.bounds.recoverable.undetermined", median, minRate, maxRate)

        fun hasRestingHeartRateWithinBoundsRecoverableFail(median: Double, minRate: Double, maxRate: Double) =
            format("vitalfunction.has.resting.heart.rate.within.bounds.recoverable.fail", median, minRate, maxRate)

        fun hasSufficientPulseOximetryUndeterminedNoData() =
            get("vitalfunction.has.sufficient.pulse.oximetry.undetermined.no.data")

        fun hasSufficientPulseOximetryUndeterminedWrongUnit(unitDisplay: String) =
            format("vitalfunction.has.sufficient.pulse.oximetry.undetermined.wrong.unit", unitDisplay)

        fun hasSufficientPulseOximetryRecoverablePass(median: Double, reference: Double) =
            format("vitalfunction.has.sufficient.pulse.oximetry.recoverable.pass", median, reference)

        fun hasSufficientPulseOximetryRecoverableUndetermined(median: Double, reference: Double) =
            format("vitalfunction.has.sufficient.pulse.oximetry.recoverable.undetermined", median, reference)

        fun hasSufficientPulseOximetryRecoverableFail(median: Double, reference: Double) =
            format("vitalfunction.has.sufficient.pulse.oximetry.recoverable.fail", median, reference)
    }

    inner class Washout {
        fun hasRecentlyReceivedCancerTherapyWithDrugPass(namesDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.with.drug.pass", namesDisplay)

        fun hasRecentlyReceivedCancerTherapyWithDrugUndetermined(drugsDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.with.drug.undetermined", drugsDisplay)

        fun hasRecentlyReceivedCancerTherapyWithDrugFail(drugsDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.with.drug.fail", drugsDisplay)

        fun hasRecentlyReceivedRadiotherapyPass(bodyLocationSuffix: String) =
            format("washout.has.recently.received.radiotherapy.pass", bodyLocationSuffix)

        fun hasRecentlyReceivedRadiotherapyUndeterminedUnknownDate(bodyLocationSuffix: String) =
            format("washout.has.recently.received.radiotherapy.undetermined.unknown.date", bodyLocationSuffix)

        fun hasRecentlyReceivedRadiotherapyRecoverableUndeterminedLocation(requestedLocation: String) =
            format("washout.has.recently.received.radiotherapy.recoverable.undetermined.location", requestedLocation)

        fun hasRecentlyReceivedRadiotherapyRecoverableUndeterminedBoth(bodyLocationSuffix: String) =
            format("washout.has.recently.received.radiotherapy.recoverable.undetermined.both", bodyLocationSuffix)

        fun hasRecentlyReceivedRadiotherapyFail(bodyLocationSuffix: String) =
            format("washout.has.recently.received.radiotherapy.fail", bodyLocationSuffix)

        fun hasRecentlyReceivedRadiotherapySuffixBodyLocation(requestedLocation: String) =
            format("washout.has.recently.received.radiotherapy.suffix.body.location", requestedLocation)

        fun hasRecentlyReceivedCancerTherapyOfCategoryPass(categoriesDisplay: String, medicationDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.of.category.pass", categoriesDisplay, medicationDisplay)

        fun hasRecentlyReceivedCancerTherapyOfCategoryUndeterminedInconclusiveDate(categoryNamesDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.of.category.undetermined.inconclusive.date", categoryNamesDisplay)

        fun hasRecentlyReceivedCancerTherapyOfCategoryUndeterminedTrial(categoryNamesDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.of.category.undetermined.trial", categoryNamesDisplay)

        fun hasRecentlyReceivedCancerTherapyOfCategoryFail(categoryNamesDisplay: String) =
            format("washout.has.recently.received.cancer.therapy.of.category.fail", categoryNamesDisplay)

        fun hasRecentlyReceivedTrialMedicationUndeterminedRegistration() =
            get("washout.has.recently.received.trial.medication.undetermined.registration")

        fun hasRecentlyReceivedTrialMedicationPass() = get("washout.has.recently.received.trial.medication.pass")

        fun hasRecentlyReceivedTrialMedicationUndeterminedUnknownDate() =
            get("washout.has.recently.received.trial.medication.undetermined.unknown.date")

        fun hasRecentlyReceivedTrialMedicationFail() = get("washout.has.recently.received.trial.medication.fail")
    }

    private fun get(key: String): String = properties.getProperty(key) ?: error("Missing evaluation message: $key")

    private fun format(key: String, vararg args: Any): String =
        MessageFormat.format(get(key), *args.map { it.toString() }.toTypedArray())

    companion object {
        fun load(intendedUse: ReportIntendedUse): EvaluationLabels {
            val name = "/evaluation/${intendedUse.name.lowercase()}.properties"
            val props = Properties().apply {
                EvaluationLabels::class.java.getResourceAsStream(name)?.bufferedReader(Charsets.UTF_8)?.use(::load)
                    ?: error("Evaluation messages not found: $name")
            }
            return EvaluationLabels(props)
        }
    }
}
