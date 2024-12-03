package com.hartwig.actin.database.dao

import com.hartwig.actin.database.Tables
import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.VariantEffect
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.suspectResistant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.datamodel.molecular.orange.immunology.MolecularImmunology
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import org.jooq.DSLContext
import org.jooq.Record

internal class MolecularDAO(private val context: DSLContext) {

    fun clear(record: MolecularRecord) {
        val sampleId = record.sampleId
        val molecularResults =
            context.select(Tables.MOLECULAR.ID).from(Tables.MOLECULAR).where(Tables.MOLECULAR.SAMPLEID.eq(sampleId)).fetch()

        for (molecularResult in molecularResults) {
            val molecularId = molecularResult.getValue(Tables.MOLECULAR.ID)
            context.delete(Tables.MICROSATELLITEEVIDENCE).where(Tables.MICROSATELLITEEVIDENCE.MOLECULARID.eq(molecularId)).execute()
            context.delete(Tables.HOMOLOGOUSREPAIREVIDENCE).where(Tables.HOMOLOGOUSREPAIREVIDENCE.MOLECULARID.eq(molecularId)).execute()
            context.delete(Tables.TUMORMUTATIONALBURDENEVIDENCE).where(Tables.TUMORMUTATIONALBURDENEVIDENCE.MOLECULARID.eq(molecularId))
                .execute()
            context.delete(Tables.TUMORMUTATIONALLOADEVIDENCE).where(Tables.TUMORMUTATIONALLOADEVIDENCE.MOLECULARID.eq(molecularId))
                .execute()
        }
        val variantResults = context.select(Tables.VARIANT.ID).from(Tables.VARIANT).where(Tables.VARIANT.SAMPLEID.eq(sampleId)).fetch()
        for (variantResult in variantResults) {
            val variantId = variantResult.getValue(Tables.VARIANT.ID)
            context.delete(Tables.VARIANTOTHERIMPACT).where(Tables.VARIANTOTHERIMPACT.VARIANTID.eq(variantId)).execute()
            context.delete(Tables.VARIANTEVIDENCE).where(Tables.VARIANTEVIDENCE.VARIANTID.eq(variantId)).execute()
        }
        context.delete(Tables.VARIANT).where(Tables.VARIANT.SAMPLEID.eq(sampleId)).execute()
        val copyNumberResults =
            context.select(Tables.COPYNUMBER.ID).from(Tables.COPYNUMBER).where(Tables.COPYNUMBER.SAMPLEID.eq(sampleId)).fetch()
        for (copyNumberResult in copyNumberResults) {
            val copyNumberId = copyNumberResult.getValue(Tables.COPYNUMBER.ID)
            context.delete(Tables.COPYNUMBEREVIDENCE).where(Tables.COPYNUMBEREVIDENCE.COPYNUMBERID.eq(copyNumberId)).execute()
        }
        context.delete(Tables.COPYNUMBER).where(Tables.COPYNUMBER.SAMPLEID.eq(sampleId)).execute()

        val homozygousDisruptionResults = context.select(Tables.HOMOZYGOUSDISRUPTION.ID)
            .from(Tables.HOMOZYGOUSDISRUPTION)
            .where(Tables.HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId))
            .fetch()

        for (homozygousDisruptionResult in homozygousDisruptionResults) {
            val homozygousDisruptionId = homozygousDisruptionResult.getValue(Tables.HOMOZYGOUSDISRUPTION.ID)
            context.delete(Tables.HOMOZYGOUSDISRUPTIONEVIDENCE)
                .where(Tables.HOMOZYGOUSDISRUPTIONEVIDENCE.HOMOZYGOUSDISRUPTIONID.eq(homozygousDisruptionId))
                .execute()
        }
        context.delete(Tables.HOMOZYGOUSDISRUPTION).where(Tables.HOMOZYGOUSDISRUPTION.SAMPLEID.eq(sampleId)).execute()

        val disruptionResults =
            context.select(Tables.DISRUPTION.ID).from(Tables.DISRUPTION).where(Tables.DISRUPTION.SAMPLEID.eq(sampleId)).fetch()
        for (disruptionResult in disruptionResults) {
            val disruptionId = disruptionResult.getValue(Tables.DISRUPTION.ID)
            context.delete(Tables.DISRUPTIONEVIDENCE).where(Tables.DISRUPTIONEVIDENCE.DISRUPTIONID.eq(disruptionId)).execute()
        }
        context.delete(Tables.DISRUPTION).where(Tables.DISRUPTION.SAMPLEID.eq(sampleId)).execute()

        val fusionResults = context.select(Tables.FUSION.ID).from(Tables.FUSION).where(Tables.FUSION.SAMPLEID.eq(sampleId)).fetch()
        for (fusionResult in fusionResults) {
            val fusionId = fusionResult.getValue(Tables.FUSION.ID)
            context.delete(Tables.FUSIONEVIDENCE).where(Tables.FUSIONEVIDENCE.FUSIONID.eq(fusionId)).execute()
        }
        context.delete(Tables.FUSION).where(Tables.FUSION.SAMPLEID.eq(sampleId)).execute()

        val virusResults = context.select(Tables.VIRUS.ID).from(Tables.VIRUS).where(Tables.VIRUS.SAMPLEID.eq(sampleId)).fetch()
        for (virusResult in virusResults) {
            val virusId = virusResult.getValue(Tables.VIRUS.ID)
            context.delete(Tables.VIRUSEVIDENCE).where(Tables.VIRUSEVIDENCE.VIRUSID.eq(virusId)).execute()
        }
        context.delete(Tables.VIRUS).where(Tables.VIRUS.SAMPLEID.eq(sampleId)).execute()

        context.delete(Tables.HLAALLELE).where(Tables.HLAALLELE.SAMPLEID.eq(sampleId)).execute()
        context.delete(Tables.PHARMACO).where(Tables.PHARMACO.SAMPLEID.eq(sampleId)).execute()
        context.delete(Tables.MOLECULAR).where(Tables.MOLECULAR.SAMPLEID.eq(sampleId)).execute()
    }

    fun writeMolecularRecord(record: MolecularRecord) {
        writeMolecularDetails(record)
        val sampleId = record.sampleId
        val drivers = record.drivers
        writeVariants(sampleId, drivers.variants)
        writeCopyNumbers(sampleId, drivers.copyNumbers)
        writeHomozygousDisruptions(sampleId, drivers.homozygousDisruptions)
        writeDisruptions(sampleId, drivers.disruptions)
        writeFusions(sampleId, drivers.fusions)
        writeViruses(sampleId, drivers.viruses)
        writeImmunology(sampleId, record.immunology)
        writePharmaco(sampleId, record.pharmaco)
    }

    private fun writeMolecularDetails(record: MolecularRecord) {
        val sampleId = record.sampleId
        val predictedTumorOrigin = record.characteristics.predictedTumorOrigin
        val molecularId = context.insertInto(
            Tables.MOLECULAR,
            Tables.MOLECULAR.PATIENTID,
            Tables.MOLECULAR.SAMPLEID,
            Tables.MOLECULAR.EXPERIMENTTYPE,
            Tables.MOLECULAR.REFGENOMEVERSION,
            Tables.MOLECULAR.EXPERIMENTDATE,
            Tables.MOLECULAR.EVIDENCESOURCE,
            Tables.MOLECULAR.EXTERNALTRIALSOURCE,
            Tables.MOLECULAR.CONTAINSTUMORCELLS,
            Tables.MOLECULAR.HASSUFFICIENTQUALITY,
            Tables.MOLECULAR.PURITY,
            Tables.MOLECULAR.PLOIDY,
            Tables.MOLECULAR.PREDICTEDTUMORTYPE,
            Tables.MOLECULAR.PREDICTEDTUMORLIKELIHOOD,
            Tables.MOLECULAR.ISMICROSATELLITEUNSTABLE,
            Tables.MOLECULAR.HOMOLOGOUSREPAIRSCORE,
            Tables.MOLECULAR.ISHOMOLOGOUSREPAIRDEFICIENT,
            Tables.MOLECULAR.TUMORMUTATIONALBURDEN,
            Tables.MOLECULAR.HASHIGHTUMORMUTATIONALBURDEN,
            Tables.MOLECULAR.TUMORMUTATIONALLOAD,
            Tables.MOLECULAR.HASHIGHTUMORMUTATIONALLOAD
        )
            .values(
                record.patientId,
                sampleId,
                record.experimentType.toString(),
                record.refGenomeVersion.toString(),
                record.date,
                record.evidenceSource,
                record.externalTrialSource,
                record.containsTumorCells,
                record.hasSufficientQuality,
                record.characteristics.purity,
                record.characteristics.ploidy,
                predictedTumorOrigin?.cancerType(),
                predictedTumorOrigin?.likelihood(),
                record.characteristics.isMicrosatelliteUnstable,
                record.characteristics.homologousRepairScore,
                record.characteristics.isHomologousRepairDeficient,
                record.characteristics.tumorMutationalBurden,
                record.characteristics.hasHighTumorMutationalBurden,
                record.characteristics.tumorMutationalLoad,
                record.characteristics.hasHighTumorMutationalLoad
            )
            .returning(Tables.MOLECULAR.ID)
            .fetchOne()!!
            .getValue(Tables.MOLECULAR.ID)
        writeMicrosatelliteEvidence(molecularId, record.characteristics.microsatelliteEvidence)
        writeHomologousRepairEvidence(molecularId, record.characteristics.homologousRepairEvidence)
        writeTumorMutationalBurdenEvidence(molecularId, record.characteristics.tumorMutationalBurdenEvidence)
        writeTumorMutationalLoadEvidence(molecularId, record.characteristics.tumorMutationalLoadEvidence)
    }

    private fun writeMicrosatelliteEvidence(molecularId: Int, evidence: ClinicalEvidence?) {
        if (evidence == null) {
            return
        }
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.MICROSATELLITEEVIDENCE,
                Tables.MICROSATELLITEEVIDENCE.MOLECULARID,
                Tables.MICROSATELLITEEVIDENCE.TREATMENT,
                Tables.MICROSATELLITEEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, molecularId, evidence)
        inserter.execute()
    }

    private fun writeHomologousRepairEvidence(molecularId: Int, evidence: ClinicalEvidence?) {
        if (evidence == null) {
            return
        }
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.HOMOLOGOUSREPAIREVIDENCE,
                Tables.HOMOLOGOUSREPAIREVIDENCE.MOLECULARID,
                Tables.HOMOLOGOUSREPAIREVIDENCE.TREATMENT,
                Tables.HOMOLOGOUSREPAIREVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, molecularId, evidence)
        inserter.execute()
    }

    private fun writeTumorMutationalBurdenEvidence(molecularId: Int, evidence: ClinicalEvidence?) {
        if (evidence == null) {
            return
        }
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.TUMORMUTATIONALBURDENEVIDENCE,
                Tables.TUMORMUTATIONALBURDENEVIDENCE.MOLECULARID,
                Tables.TUMORMUTATIONALBURDENEVIDENCE.TREATMENT,
                Tables.TUMORMUTATIONALBURDENEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, molecularId, evidence)
        inserter.execute()
    }

    private fun writeTumorMutationalLoadEvidence(molecularId: Int, evidence: ClinicalEvidence?) {
        if (evidence == null) {
            return
        }
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.TUMORMUTATIONALLOADEVIDENCE,
                Tables.TUMORMUTATIONALLOADEVIDENCE.MOLECULARID,
                Tables.TUMORMUTATIONALLOADEVIDENCE.TREATMENT,
                Tables.TUMORMUTATIONALLOADEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, molecularId, evidence)
        inserter.execute()
    }

    private fun writeVariants(sampleId: String, variants: Collection<Variant>) {
        for (variant in variants) {
            val variantId = context.insertInto(
                Tables.VARIANT,
                Tables.VARIANT.SAMPLEID,
                Tables.VARIANT.ISREPORTABLE,
                Tables.VARIANT.EVENT,
                Tables.VARIANT.DRIVERLIKELIHOOD,
                Tables.VARIANT.GENE,
                Tables.VARIANT.GENEROLE,
                Tables.VARIANT.PROTEINEFFECT,
                Tables.VARIANT.ISASSOCIATEDWITHDRUGRESISTANCE,
                Tables.VARIANT.TYPE,
                Tables.VARIANT.VARIANTCOPYNUMBER,
                Tables.VARIANT.TOTALCOPYNUMBER,
                Tables.VARIANT.ISBIALLELIC,
                Tables.VARIANT.ISHOTSPOT,
                Tables.VARIANT.CLONALLIKELIHOOD,
                Tables.VARIANT.PHASEGROUPS,
                Tables.VARIANT.CANONICALTRANSCRIPTID,
                Tables.VARIANT.CANONICALHGVSCODINGIMPACT,
                Tables.VARIANT.CANONICALHGVSPROTEINIMPACT,
                Tables.VARIANT.CANONICALAFFECTEDCODON,
                Tables.VARIANT.CANONICALAFFECTEDEXON,
                Tables.VARIANT.CANONICALISSPLICEREGION,
                Tables.VARIANT.CANONICALEFFECTS,
                Tables.VARIANT.CANONICALCODINGEFFECT
            )
                .values(
                    sampleId,
                    variant.isReportable,
                    variant.event,
                    driverLikelihood(variant),
                    variant.gene,
                    variant.geneRole.toString(),
                    variant.proteinEffect.toString(),
                    variant.isAssociatedWithDrugResistance,
                    variant.type.toString(),
                    variant.extendedVariantDetails?.variantCopyNumber,
                    variant.extendedVariantDetails?.totalCopyNumber,
                    variant.extendedVariantDetails?.isBiallelic,
                    variant.isHotspot,
                    variant.extendedVariantDetails?.clonalLikelihood,
                    DataUtil.concat(integersToStrings(variant.extendedVariantDetails?.phaseGroups)),
                    variant.canonicalImpact.transcriptId,
                    variant.canonicalImpact.hgvsCodingImpact,
                    variant.canonicalImpact.hgvsProteinImpact,
                    variant.canonicalImpact.affectedCodon,
                    variant.canonicalImpact.affectedExon,
                    variant.canonicalImpact.isSpliceRegion,
                    DataUtil.concat(effectsToStrings(variant.canonicalImpact.effects)),
                    DataUtil.nullableToString(variant.canonicalImpact.codingEffect)
                )
                .returning(Tables.VARIANT.ID)
                .fetchOne()!!
                .getValue(Tables.VARIANT.ID)
            writeVariantEvidence(variantId, variant.evidence)
        }
    }

    private fun writeVariantEvidence(variantId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.VARIANTEVIDENCE,
                Tables.VARIANTEVIDENCE.VARIANTID,
                Tables.VARIANTEVIDENCE.TREATMENT,
                Tables.VARIANTEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, variantId, evidence)
        inserter.execute()
    }

    private fun writeCopyNumbers(sampleId: String, copyNumbers: Collection<CopyNumber>) {
        for (copyNumber in copyNumbers) {
            val copyNumberId = context.insertInto(
                Tables.COPYNUMBER,
                Tables.COPYNUMBER.SAMPLEID,
                Tables.COPYNUMBER.ISREPORTABLE,
                Tables.COPYNUMBER.EVENT,
                Tables.COPYNUMBER.DRIVERLIKELIHOOD,
                Tables.COPYNUMBER.GENE,
                Tables.COPYNUMBER.GENEROLE,
                Tables.COPYNUMBER.PROTEINEFFECT,
                Tables.COPYNUMBER.ISASSOCIATEDWITHDRUGRESISTANCE,
                Tables.COPYNUMBER.TYPE,
                Tables.COPYNUMBER.MINCOPIES,
                Tables.COPYNUMBER.MAXCOPIES
            )
                .values(
                    sampleId,
                    copyNumber.isReportable,
                    copyNumber.event,
                    driverLikelihood(copyNumber),
                    copyNumber.gene,
                    copyNumber.geneRole.toString(),
                    copyNumber.proteinEffect.toString(),
                    copyNumber.isAssociatedWithDrugResistance,
                    copyNumber.type.toString(),
                    copyNumber.minCopies,
                    copyNumber.maxCopies
                )
                .returning(Tables.COPYNUMBER.ID)
                .fetchOne()!!
                .getValue(Tables.COPYNUMBER.ID)
            writeCopyNumberEvidence(copyNumberId, copyNumber.evidence)
        }
    }

    private fun writeCopyNumberEvidence(copyNumberId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.COPYNUMBEREVIDENCE,
                Tables.COPYNUMBEREVIDENCE.COPYNUMBERID,
                Tables.COPYNUMBEREVIDENCE.TREATMENT,
                Tables.COPYNUMBEREVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, copyNumberId, evidence)
        inserter.execute()
    }

    private fun writeHomozygousDisruptions(sampleId: String, homozygousDisruptions: Collection<HomozygousDisruption>) {
        for (homozygousDisruption in homozygousDisruptions) {
            val homozygousDisruptionId = context.insertInto(
                Tables.HOMOZYGOUSDISRUPTION,
                Tables.HOMOZYGOUSDISRUPTION.SAMPLEID,
                Tables.HOMOZYGOUSDISRUPTION.ISREPORTABLE,
                Tables.HOMOZYGOUSDISRUPTION.EVENT,
                Tables.HOMOZYGOUSDISRUPTION.DRIVERLIKELIHOOD,
                Tables.HOMOZYGOUSDISRUPTION.GENE,
                Tables.HOMOZYGOUSDISRUPTION.GENEROLE,
                Tables.HOMOZYGOUSDISRUPTION.PROTEINEFFECT,
                Tables.HOMOZYGOUSDISRUPTION.ISASSOCIATEDWITHDRUGRESISTANCE
            )
                .values(
                    sampleId,
                    homozygousDisruption.isReportable,
                    homozygousDisruption.event,
                    driverLikelihood(homozygousDisruption),
                    homozygousDisruption.gene,
                    homozygousDisruption.geneRole.toString(),
                    homozygousDisruption.proteinEffect.toString(),
                    homozygousDisruption.isAssociatedWithDrugResistance
                )
                .returning(Tables.HOMOZYGOUSDISRUPTION.ID)
                .fetchOne()!!
                .getValue(Tables.HOMOZYGOUSDISRUPTION.ID)
            writeHomozygousDisruptionEvidence(homozygousDisruptionId, homozygousDisruption.evidence)
        }
    }

    private fun writeHomozygousDisruptionEvidence(homozygousDisruptionId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.HOMOZYGOUSDISRUPTIONEVIDENCE,
                Tables.HOMOZYGOUSDISRUPTIONEVIDENCE.HOMOZYGOUSDISRUPTIONID,
                Tables.HOMOZYGOUSDISRUPTIONEVIDENCE.TREATMENT,
                Tables.HOMOZYGOUSDISRUPTIONEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, homozygousDisruptionId, evidence)
        inserter.execute()
    }

    private fun writeDisruptions(sampleId: String, disruptions: Collection<Disruption>) {
        for (disruption in disruptions) {
            val disruptionId = context.insertInto(
                Tables.DISRUPTION,
                Tables.DISRUPTION.SAMPLEID,
                Tables.DISRUPTION.ISREPORTABLE,
                Tables.DISRUPTION.EVENT,
                Tables.DISRUPTION.DRIVERLIKELIHOOD,
                Tables.DISRUPTION.GENE,
                Tables.DISRUPTION.GENEROLE,
                Tables.DISRUPTION.PROTEINEFFECT,
                Tables.DISRUPTION.ISASSOCIATEDWITHDRUGRESISTANCE,
                Tables.DISRUPTION.TYPE,
                Tables.DISRUPTION.JUNCTIONCOPYNUMBER,
                Tables.DISRUPTION.UNDISRUPTEDCOPYNUMBER,
                Tables.DISRUPTION.REGIONTYPE,
                Tables.DISRUPTION.CODINGCONTEXT,
                Tables.DISRUPTION.CLUSTERGROUP
            )
                .values(
                    sampleId,
                    disruption.isReportable,
                    disruption.event,
                    driverLikelihood(disruption),
                    disruption.gene,
                    disruption.geneRole.toString(),
                    disruption.proteinEffect.toString(),
                    disruption.isAssociatedWithDrugResistance,
                    disruption.type.toString(),
                    disruption.junctionCopyNumber,
                    disruption.undisruptedCopyNumber,
                    disruption.regionType.toString(),
                    disruption.codingContext.toString(),
                    disruption.clusterGroup
                )
                .returning(Tables.DISRUPTION.ID)
                .fetchOne()!!
                .getValue(Tables.DISRUPTION.ID)
            writeDisruptionEvidence(disruptionId, disruption.evidence)
        }
    }

    private fun writeDisruptionEvidence(disruptionId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.DISRUPTIONEVIDENCE,
                Tables.DISRUPTIONEVIDENCE.DISRUPTIONID,
                Tables.DISRUPTIONEVIDENCE.TREATMENT,
                Tables.DISRUPTIONEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, disruptionId, evidence)
        inserter.execute()
    }

    private fun writeFusions(sampleId: String, fusions: Collection<Fusion>) {
        for (fusion in fusions) {
            val fusionId = context.insertInto(
                Tables.FUSION,
                Tables.FUSION.SAMPLEID,
                Tables.FUSION.ISREPORTABLE,
                Tables.FUSION.EVENT,
                Tables.FUSION.DRIVERLIKELIHOOD,
                Tables.FUSION.GENESTART,
                Tables.FUSION.GENETRANSCRIPTSTART,
                Tables.FUSION.FUSEDEXONUP,
                Tables.FUSION.GENEEND,
                Tables.FUSION.GENETRANSCRIPTEND,
                Tables.FUSION.FUSEDEXONDOWN,
                Tables.FUSION.DRIVERTYPE,
                Tables.FUSION.PROTEINEFFECT,
                Tables.FUSION.ISASSOCIATEDWITHDRUGRESISTANCE
            )
                .values(
                    sampleId,
                    fusion.isReportable,
                    fusion.event,
                    driverLikelihood(fusion),
                    fusion.geneStart,
                    fusion.geneTranscriptStart,
                    fusion.fusedExonUp,
                    fusion.geneEnd,
                    fusion.geneTranscriptEnd,
                    fusion.fusedExonDown,
                    fusion.driverType.toString(),
                    fusion.proteinEffect.toString(),
                    fusion.isAssociatedWithDrugResistance
                )
                .returning(Tables.FUSION.ID)
                .fetchOne()!!
                .getValue(Tables.FUSION.ID)
            writeFusionEvidence(fusionId, fusion.evidence)
        }
    }

    private fun writeFusionEvidence(fusionId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.FUSIONEVIDENCE,
                Tables.FUSIONEVIDENCE.FUSIONID,
                Tables.FUSIONEVIDENCE.TREATMENT,
                Tables.FUSIONEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, fusionId, evidence)
        inserter.execute()
    }

    private fun writeViruses(sampleId: String, viruses: Collection<Virus>) {
        for (virus in viruses) {
            val virusId = context.insertInto(
                Tables.VIRUS,
                Tables.VIRUS.SAMPLEID,
                Tables.VIRUS.ISREPORTABLE,
                Tables.VIRUS.EVENT,
                Tables.VIRUS.DRIVERLIKELIHOOD,
                Tables.VIRUS.NAME,
                Tables.VIRUS.TYPE,
                Tables.VIRUS.ISRELIABLE,
                Tables.VIRUS.INTEGRATIONS
            )
                .values(
                    sampleId,
                    virus.isReportable,
                    virus.event,
                    driverLikelihood(virus),
                    virus.name,
                    virus.type.toString(),
                    virus.isReliable,
                    virus.integrations
                )
                .returning(Tables.VIRUS.ID)
                .fetchOne()!!
                .getValue(Tables.VIRUS.ID)
            writeVirusEvidence(virusId, virus.evidence)
        }
    }

    private fun writeVirusEvidence(virusId: Int, evidence: ClinicalEvidence) {
        val inserter = EvidenceInserter(
            context.insertInto(
                Tables.VIRUSEVIDENCE,
                Tables.VIRUSEVIDENCE.VIRUSID,
                Tables.VIRUSEVIDENCE.TREATMENT,
                Tables.VIRUSEVIDENCE.TYPE
            )
        )
        writeEvidence(inserter, virusId, evidence)
        inserter.execute()
    }

    private fun writeImmunology(sampleId: String, immunology: MolecularImmunology) {
        for (hlaAllele in immunology.hlaAlleles) {
            context.insertInto(
                Tables.HLAALLELE,
                Tables.HLAALLELE.SAMPLEID,
                Tables.HLAALLELE.ISRELIABLE,
                Tables.HLAALLELE.NAME,
                Tables.HLAALLELE.TUMORCOPYNUMBER,
                Tables.HLAALLELE.HASSOMATICMUTATIONS
            )
                .values(
                    sampleId,
                    immunology.isReliable,
                    hlaAllele.name,
                    hlaAllele.tumorCopyNumber,
                    hlaAllele.hasSomaticMutations
                )
                .execute()
        }
    }

    private fun writePharmaco(sampleId: String, pharmaco: Set<PharmacoEntry>) {
        for (entry in pharmaco) {
            for (haplotype in entry.haplotypes) {
                context.insertInto(
                    Tables.PHARMACO,
                    Tables.PHARMACO.SAMPLEID,
                    Tables.PHARMACO.GENE,
                    Tables.PHARMACO.ALLELE,
                    Tables.PHARMACO.ALLELECOUNT,
                    Tables.PHARMACO.FUNCTION
                )
                    .values(sampleId, entry.gene.toString(), haplotype.allele, haplotype.alleleCount, haplotype.function.display())
                    .execute()
            }
        }
    }

    private fun effectsToStrings(effects: Set<VariantEffect>): Set<String> {
        return effects.map(VariantEffect::toString).toSet()
    }

    private fun driverLikelihood(driver: Driver): String? {
        return driver.driverLikelihood?.toString()
    }

    private fun integersToStrings(integers: Set<Int>?): Set<String>? {
        return integers?.map(Int::toString)?.toSet()
    }

    private fun <T : Record?> writeEvidence(inserter: EvidenceInserter<T>, topicId: Int, evidence: ClinicalEvidence) {
        writeTreatments(inserter, topicId, treatments(approved(evidence.treatmentEvidence)), "Approved")
        writeTrials(inserter, topicId, evidence.eligibleTrials)
        writeTreatments(inserter, topicId, treatments(experimental(evidence.treatmentEvidence, true)), "On-label experimental")
        writeTreatments(inserter, topicId, treatments(experimental(evidence.treatmentEvidence, false)), "Off-label experimental")
        writeTreatments(inserter, topicId, treatments(preclinical(evidence.treatmentEvidence)), "Pre-clinical")
        writeTreatments(inserter, topicId, treatments(knownResistant(evidence.treatmentEvidence)), "Known resistant")
        writeTreatments(inserter, topicId, treatments(suspectResistant(evidence.treatmentEvidence)), "Suspect resistant")
    }

    private fun treatments(treatmentEvidence: List<TreatmentEvidence>) =
        treatmentEvidence.map { it.treatment }.toSet()

    private fun <T : Record?> writeTreatments(inserter: EvidenceInserter<T>, topicId: Int, treatments: Set<String>, type: String) {
        for (treatment in treatments) {
            inserter.write(topicId, treatment, type)
        }
    }

    private fun <T : Record?> writeTrials(inserter: EvidenceInserter<T>, topicId: Int, externalTrials: Set<ExternalTrial>) {
        for (externalTrial in externalTrials) {
            inserter.write(topicId, externalTrial.title, "Trial")
        }
    }
}