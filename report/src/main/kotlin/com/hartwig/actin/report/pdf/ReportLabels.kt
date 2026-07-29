package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportIntendedUse
import java.text.MessageFormat
import java.util.Properties

class ReportLabels(private val properties: Properties) {

    // Header
    fun reportTitle() = get("report.title")

    // Side Panel
    fun sidePanelPatient() = get("side.panel.patient")
    fun sidePanelReportDate() = get("side.panel.report.date")

    // Footer
    fun footerResearchDisclaimer() = get("footer.research.disclaimer")
    fun footerCtgovDisclaimer(date: String) = format("footer.ctgov.disclaimer", date)
    fun footerCkbAttribution() = get("footer.ckb.attribution")

    // Chapters
    fun chapterSummary() = get("chapter.summary")
    fun chapterClinicalDetails() = get("chapter.clinical.details")
    fun chapterMolecularDetails() = get("chapter.molecular.details")
    fun chapterEfficacyEvidence() = get("chapter.efficacy.evidence")
    fun chapterTrialMatchingDetails() = get("chapter.trial.matching.details")

    // Summary
    fun summaryGender() = get("summary.gender")
    fun summaryBirthYear() = get("summary.birth.year")
    fun summaryWho() = get("summary.who")
    fun summaryTumor() = get("summary.tumor")
    fun summaryLesions() = get("summary.lesions")
    fun summaryStage() = get("summary.stage")
    fun summaryDerivedStages() = get("summary.derived.stages")

    // Trial table titles
    fun trialTitleOpenEligible(prefix: String, suffix: String) = format("trial.title.open.eligible", prefix, suffix)
    fun trialTitleOpenMissingMolecular(prefix: String, suffix: String) = format("trial.title.open.missing.molecular", prefix, suffix)
    fun trialTitleClosedEligible(suffix: String) = format("trial.title.closed.eligible", suffix)
    fun trialTitleFilteredEligible(count: String) = format("trial.title.filtered.eligible", count)
    fun trialTitleIneligible(suffix: String) = format("trial.title.ineligible", suffix)
    fun trialTitleNonEvaluable(suffix: String) = format("trial.title.non.evaluable", suffix)
    fun trialTitleEligibleOpen() = get("trial.title.eligible.open")
    fun trialTitleOther() = get("trial.title.other")

    // Trial phase descriptions
    fun trialPhaseLate(suffix: String) = format("trial.phase.late", suffix)
    fun trialPhaseEarly(suffix: String) = format("trial.phase.early", suffix)
    fun trialPhaseNational() = get("trial.phase.national")
    fun trialPhaseInternational() = get("trial.phase.international")

    // Trial table columns
    fun trialColTrial() = get("trial.col.trial")
    fun trialColCohort() = get("trial.col.cohort")
    fun trialColMolecular() = get("trial.col.molecular")
    fun trialColSites() = get("trial.col.sites")
    fun trialColWarnings() = get("trial.col.warnings")
    fun trialColIneligibilityReasons() = get("trial.col.ineligibility.reasons")
    fun trialColConfiguration() = get("trial.col.configuration")
    fun trialColReference() = get("trial.col.reference")
    fun trialColEvaluation() = get("trial.col.evaluation")

    // Trial detail keys
    fun trialDetailPotentiallyEligible() = get("trial.detail.potentially.eligible")
    fun trialDetailAcronym() = get("trial.detail.acronym")
    fun trialDetailTitle() = get("trial.detail.title")
    fun trialDetailCohortId() = get("trial.detail.cohort.id")
    fun trialDetailPotentiallyEligibleQ() = get("trial.detail.potentially.eligible.q")
    fun trialDetailOpenForInclusion() = get("trial.detail.open.for.inclusion")
    fun trialDetailHasSlots() = get("trial.detail.has.slots")
    fun trialDetailIgnored() = get("trial.detail.ignored")

    // Trial footnotes
    fun trialFootnoteFilteredSuffix() = get("trial.footnote.filtered.suffix")
    fun trialFootnoteChildrensHospital(count: String, suffix: String) = format("trial.footnote.childrens.hospital", count, suffix)
    fun trialFootnoteDutchLung(count: String, suffix: String) = format("trial.footnote.dutch.lung", count, suffix)
    fun trialFootnoteExternalMatched() = get("trial.footnote.external.matched")
    fun trialFootnoteExternalExcluded() = get("trial.footnote.external.excluded")
    fun trialFootnoteNationalMolecular(count: String, suffix: String) = format("trial.footnote.national.molecular", count, suffix)

    // SOC tables
    fun socEligibleTitle() = get("soc.eligible.title")
    fun socEfficacyTitle() = get("soc.efficacy.title")
    fun socNoOptions() = get("soc.no.options")
    fun socNoLiterature() = get("soc.no.literature")
    fun socNoResistance() = get("soc.no.resistance")
    fun socTreatmentNote() = get("soc.treatment.note")
    fun socColTreatment() = get("soc.col.treatment")
    fun socColLiteratureEvidence() = get("soc.col.literature.evidence")
    fun socColWarnings() = get("soc.col.warnings")
    fun socColMutation() = get("soc.col.mutation")
    fun socColEvidenceSource() = get("soc.col.evidence.source")
    fun socColEvidenceLevel() = get("soc.col.evidence.level")
    fun socColFoundInMolecular() = get("soc.col.found.in.molecular")
    fun socResistanceTitle() = get("soc.resistance.title")

    // Efficacy evidence
    fun efficacyPatientCharacteristics() = get("efficacy.patient.characteristics")
    fun efficacyPrimaryEndpoints() = get("efficacy.primary.endpoints")
    fun efficacySecondaryEndpoints() = get("efficacy.secondary.endpoints")
    fun efficacyStudy() = get("efficacy.study")
    fun efficacyMolecularRequirements() = get("efficacy.molecular.requirements")
    fun efficacyTherapies() = get("efficacy.therapies")
    fun efficacyNone() = get("efficacy.none")
    fun efficacyCi() = get("efficacy.ci")
    fun efficacyMedianOs() = get("efficacy.median.os")
    fun efficacyMedianPfs() = get("efficacy.median.pfs")
    fun efficacyColWhoEcog() = get("efficacy.col.who.ecog")
    fun efficacyColPrimaryTumorLocation() = get("efficacy.col.primary.tumor.location")
    fun efficacyColMutations() = get("efficacy.col.mutations")
    fun efficacyColMetastaticSites() = get("efficacy.col.metastatic.sites")
    fun efficacyColPreviousSystemicTherapy() = get("efficacy.col.previous.systemic.therapy")
    fun efficacyColPriorTherapies() = get("efficacy.col.prior.therapies")
    fun efficacyColAgeMedianRange() = get("efficacy.col.age.median.range")
    fun efficacyColSex() = get("efficacy.col.sex")
    fun efficacyColRace() = get("efficacy.col.race")
    fun efficacyColRegion() = get("efficacy.col.region")
    fun efficacyColTimeOfMetastases() = get("efficacy.col.time.of.metastases")
    fun efficacyColHrOr() = get("efficacy.col.hr.or")
    fun efficacyColPValue() = get("efficacy.col.p.value")
    fun efficacySexMale() = get("efficacy.sex.male")
    fun efficacySexFemale() = get("efficacy.sex.female")
    fun efficacyMedianFollowUpPfs(months: String) = format("efficacy.median.follow.up.pfs", months)
    fun efficacyMedianPfsLabel() = get("efficacy.median.pfs.label")
    fun efficacyMedianOsLabel() = get("efficacy.median.os.label")

    // Charts
    fun chartSurvivalX() = get("chart.survival.x")
    fun chartSurvivalY() = get("chart.survival.y")
    fun chartShapTitle(treatment: String) = format("chart.shap.title", treatment)
    fun chartTreatmentDistributionTitle() = get("chart.treatment.distribution.title")

    // Clinical tables
    fun clinicalSummaryTitle() = get("clinical.summary.title")
    fun clinicalPatientDetailsTitle(date: String) = format("clinical.patient.details.title", date)
    fun clinicalBloodTransfusionTitle() = get("clinical.blood.transfusion.title")
    fun clinicalMedicationTitle() = get("clinical.medication.title")
    fun clinicalTumorDetailsTitle(date: String) = format("clinical.tumor.details.title", date)

    // Clinical section names
    fun clinicalSectionSystemicHistory() = get("clinical.section.systemic.history")
    fun clinicalSectionOtherOncological() = get("clinical.section.other.oncological")
    fun clinicalSectionPreviousPrimary() = get("clinical.section.previous.primary")
    fun clinicalSectionNonOncological() = get("clinical.section.non.oncological")

    // Clinical detail keys
    fun clinicalKeyToxicities() = get("clinical.key.toxicities")
    fun clinicalKeyInfection() = get("clinical.key.infection")
    fun clinicalKeyEcg() = get("clinical.key.ecg")
    fun clinicalKeyQtcf() = get("clinical.key.qtcf")
    fun clinicalKeyJtc() = get("clinical.key.jtc")
    fun clinicalKeyLvef() = get("clinical.key.lvef")
    fun clinicalKeyAllergies() = get("clinical.key.allergies")
    fun clinicalKeySurgeries() = get("clinical.key.surgeries")
    fun clinicalKeyMeasurableDisease() = get("clinical.key.measurable.disease")
    fun clinicalKeyLesions() = get("clinical.key.lesions")
    fun clinicalKeyNoLesions() = get("clinical.key.no.lesions")
    fun clinicalValueInfectionUnknown() = get("clinical.value.infection.unknown")
    fun clinicalValueEcgUnknown() = get("clinical.value.ecg.unknown")

    // Clinical table columns
    fun clinicalColProduct() = get("clinical.col.product")
    fun clinicalColDate() = get("clinical.col.date")
    fun clinicalColMedication() = get("clinical.col.medication")
    fun clinicalColAdminRoute() = get("clinical.col.admin.route")
    fun clinicalColStartDate() = get("clinical.col.start.date")
    fun clinicalColStopDate() = get("clinical.col.stop.date")
    fun clinicalColDosage() = get("clinical.col.dosage")
    fun clinicalColFrequency() = get("clinical.col.frequency")

    // Molecular tables
    fun molecularSummaryTitle() = get("molecular.summary.title")
    fun molecularHistoryTitle() = get("molecular.history.title")
    fun molecularGeneralTitle() = get("molecular.general.title")
    fun molecularEfficacyDescriptionTitle() = get("molecular.efficacy.description.title")
    fun molecularIhcTitle() = get("molecular.ihc.title")
    fun molecularIhcSummaryTitle() = get("molecular.ihc.summary.title")
    fun molecularImmunologyTitle() = get("molecular.immunology.title")
    fun molecularWgsNoSuccessful() = get("molecular.wgs.no.successful")

    // Molecular columns
    fun molecularColType() = get("molecular.col.type")
    fun molecularColDriver() = get("molecular.col.driver")
    fun molecularColTrialsLocations() = get("molecular.col.trials.locations")
    fun molecularColTrialsSource(source: String) = format("molecular.col.trials.source", source)
    fun molecularColBestEvidence(source: String) = format("molecular.col.best.evidence", source)
    fun molecularColResistance(source: String) = format("molecular.col.resistance", source)
    fun molecularColHlaGene() = get("molecular.col.hla.gene")
    fun molecularColTumorCopyNumber() = get("molecular.col.tumor.copy.number")
    fun molecularColMutatedInTumor() = get("molecular.col.mutated.in.tumor")
    fun molecularColEvent() = get("molecular.col.event")
    fun molecularColDescription() = get("molecular.col.description")
    fun molecularColScore() = get("molecular.col.score")
    fun molecularColTreatment() = get("molecular.col.treatment")
    fun molecularTreatmentRankingTitle() = get("molecular.treatment.ranking.title")

    // Molecular characteristics headers
    fun molecularCharPurity() = get("molecular.char.purity")
    fun molecularCharPloidy() = get("molecular.char.ploidy")
    fun molecularCharTml() = get("molecular.char.tml")
    fun molecularCharTmb() = get("molecular.char.tmb")
    fun molecularCharMs() = get("molecular.char.ms")
    fun molecularCharHr() = get("molecular.char.hr")
    fun molecularCharDpyd() = get("molecular.char.dpyd")
    fun molecularCharUgt1a1() = get("molecular.char.ugt1a1")

    // Molecular clinical evidence titles
    fun molecularEvidenceOnLabel() = get("molecular.evidence.on.label")
    fun molecularEvidenceOffLabel() = get("molecular.evidence.off.label")

    // Molecular WGS summary table rows
    fun molecularWgsTissueOriginTitle() = get("molecular.wgs.tissue.origin.title")
    fun molecularWgsTissueOriginTitleWgts() = get("molecular.wgs.tissue.origin.title.wgts")
    fun molecularWgsTmbLabel() = get("molecular.wgs.tmb.label")
    fun molecularWgsTmlTmbLabel() = get("molecular.wgs.tml.tmb.label")
    fun molecularWgsMsStability() = get("molecular.wgs.ms.stability")
    fun molecularWgsHrStatus() = get("molecular.wgs.hr.status")
    fun molecularWgsDriverMutations() = get("molecular.wgs.driver.mutations")
    fun molecularWgsOtherMutations() = get("molecular.wgs.other.mutations")
    fun molecularWgsAmplifiedGenes() = get("molecular.wgs.amplified.genes")
    fun molecularWgsDeletedGenes() = get("molecular.wgs.deleted.genes")
    fun molecularWgsHomozygouslyDisruptedGenes() = get("molecular.wgs.homozygously.disrupted.genes")
    fun molecularWgsGeneFusions() = get("molecular.wgs.gene.fusions")
    fun molecularWgsDriverVirus() = get("molecular.wgs.driver.virus")
    fun molecularWgsPotentialEventsNoHighDriver() = get("molecular.wgs.potential.events.no.high.driver")
    fun molecularWgsPotentialEventsNoTumorDriver() = get("molecular.wgs.potential.events.no.tumor.driver")
    fun molecularWgsNoRelevantAlterations() = get("molecular.wgs.no.relevant.alterations")
    fun molecularWgsInsufficientQuality() = get("molecular.wgs.insufficient.quality")

    // Molecular tumor origin prediction
    fun molecularOriginTitle() = get("molecular.origin.title")
    fun molecularOriginTitleWgts() = get("molecular.origin.title.wgts")
    fun molecularOriginAllBelow10(likelihood: String, cancerType: String) = format("molecular.origin.all.below.10", likelihood, cancerType)
    fun molecularOriginOtherCohorts(likelihood: String) = format("molecular.origin.other.cohorts", likelihood)
    fun molecularOriginCombinedScore() = get("molecular.origin.combined.score")
    fun molecularOriginScoreNote() = get("molecular.origin.score.note")
    fun molecularOriginSnvTypes() = get("molecular.origin.snv.types")
    fun molecularOriginSnvGenomic() = get("molecular.origin.snv.genomic")
    fun molecularOriginDriverGenes() = get("molecular.origin.driver.genes")
    fun molecularOriginGeneExpression() = get("molecular.origin.gene.expression")
    fun molecularOriginAltSplice() = get("molecular.origin.alt.splice")

    // Molecular record
    fun molecularKeyDrivers() = get("molecular.key.drivers")
    fun molecularOtherDrivers() = get("molecular.other.drivers")
    fun molecularNoWgs() = get("molecular.no.wgs")
    fun molecularLowPurity(purity: String) = format("molecular.low.purity", purity)
    fun molecularOldTestVersion(testDate: String, versionDate: String) = format("molecular.old.test.version", testDate, versionDate)
    fun molecularSubClonalNote(threshold: String) = format("molecular.sub.clonal.note", threshold)

    // Misc
    fun miscNotAvailable() = get("misc.not.available")
    fun miscYes() = get("misc.yes")
    fun miscNo() = get("misc.no")
    fun miscNoHlaAlleles() = get("misc.no.hla.alleles")
    fun miscHlaTypingNotAvailable() = get("misc.hla.typing.not.available")
    fun miscTrial() = get("misc.trial")
    fun miscCohort() = get("misc.cohort")
    fun miscFrom() = get("misc.from")
    fun miscZeroTrials() = get("misc.zero.trials")

    private fun get(key: String): String = properties.getProperty(key) ?: error("Missing label: $key")
    private fun format(key: String, vararg args: Any): String = MessageFormat.format(get(key), *args)

    companion object {
        fun load(intendedUse: ReportIntendedUse): ReportLabels {
            val name = "/labels/${intendedUse.name.lowercase()}.properties"
            val props = Properties().apply {
                ReportLabels::class.java.getResourceAsStream(name)?.bufferedReader(Charsets.UTF_8)?.use(::load)
                    ?: error("Labels not found: $name")
            }
            return ReportLabels(props)
        }
    }
}
