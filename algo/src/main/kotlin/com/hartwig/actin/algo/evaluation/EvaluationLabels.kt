package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.configuration.ReportIntendedUse
import java.text.MessageFormat
import java.util.Properties

class EvaluationLabels(private val properties: Properties) {

    val general = General()
    val molecular = Molecular()

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
