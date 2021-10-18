/*
 * Copyright (c) 2009 - Jay Lawson <jaylawson39 at yahoo.com>. All Rights Reserved.
 * Copyright (c) 2020-2021 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

import megamek.Version;
import mekhq.campaign.mission.enums.AtBLanceRole;
import mekhq.campaign.enums.PlanetaryAcquisitionFactionLimit;
import mekhq.campaign.market.enums.ContractMarketMethod;
import mekhq.campaign.market.enums.UnitMarketMethod;
import mekhq.campaign.parts.enums.PartRepairType;
import mekhq.campaign.personnel.enums.*;
import mekhq.service.MassRepairOption;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.Utilities;
import mekhq.campaign.market.PersonnelMarket;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.rating.UnitRatingMethod;
import mekhq.campaign.finances.Money;
import mekhq.campaign.finances.enums.FinancialYearDuration;

/**
 * @author natit
 */
public class CampaignOptions implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = 5698008431749303602L;

    //region Magic Numbers and Constants
    public static final int TECH_INTRO = 0;
    public static final int TECH_STANDARD = 1;
    public static final int TECH_ADVANCED = 2;
    public static final int TECH_EXPERIMENTAL = 3;
    public static final int TECH_UNOFFICIAL = 4;
    // This must always be the highest tech level in order to hide parts
    // that haven't been invented yet, or that are completely extinct
    public static final int TECH_UNKNOWN = 5;

    public static final int TRANSIT_UNIT_DAY = 0;
    public static final int TRANSIT_UNIT_WEEK = 1;
    public static final int TRANSIT_UNIT_MONTH = 2;
    public static final int TRANSIT_UNIT_NUM = 3;

    public static final String S_TECH = "Tech";
    public static final String S_AUTO = "Automatic Success";

    public static final double MAXIMUM_COMBAT_EQUIPMENT_PERCENT = 5.0;
    public static final double MAXIMUM_DROPSHIP_EQUIPMENT_PERCENT = 1.0;
    public static final double MAXIMUM_JUMPSHIP_EQUIPMENT_PERCENT = 1.0;
    public static final double MAXIMUM_WARSHIP_EQUIPMENT_PERCENT = 1.0;
    //endregion Magic Numbers and Constants

    //region Unlisted Variables
    //Mass Repair/Salvage Options
    private boolean massRepairUseRepair;
    private boolean massRepairUseSalvage;
    private boolean massRepairUseExtraTime;
    private boolean massRepairUseRushJob;
    private boolean massRepairAllowCarryover;
    private boolean massRepairOptimizeToCompleteToday;
    private boolean massRepairScrapImpossible;
    private boolean massRepairUseAssignedTechsFirst;
    private boolean massRepairReplacePod;
    private List<MassRepairOption> massRepairOptions;
    //endregion Unlisted Variables

    //region General Tab
    private UnitRatingMethod unitRatingMethod;
    private int manualUnitRatingModifier;
    //endregion General Tab

    //region Repair and Maintenance Tab
    // Repair
    private boolean useEraMods;
    private boolean assignedTechFirst;
    private boolean resetToFirstTech;
    private boolean useQuirks;
    private boolean useAeroSystemHits;
    private boolean destroyByMargin;
    private int destroyMargin;
    private int destroyPartTarget;

    // Maintenance
    private boolean checkMaintenance;
    private int maintenanceCycleDays;
    private int maintenanceBonus;
    private boolean useQualityMaintenance;
    private boolean reverseQualityNames;
    private boolean useUnofficialMaintenance;
    private boolean logMaintenance;
    //endregion Repair and Maintenance Tab

    //region Supplies and Acquisition Tab
    // Acquisition
    private int waitingPeriod;
    private String acquisitionSkill;
    private boolean acquisitionSupportStaffOnly;
    private int clanAcquisitionPenalty;
    private int isAcquisitionPenalty;
    private int maxAcquisitions;

    // Delivery
    private int nDiceTransitTime;
    private int constantTransitTime;
    private int unitTransitTime;
    private int acquireMinimumTime;
    private int acquireMinimumTimeUnit;
    private int acquireMosBonus;
    private int acquireMosUnit;

    // Planetary Acquisition
    private boolean usePlanetaryAcquisition;
    private int maxJumpsPlanetaryAcquisition;
    private PlanetaryAcquisitionFactionLimit planetAcquisitionFactionLimit;
    private boolean planetAcquisitionNoClanCrossover;
    private boolean noClanPartsFromIS;
    private int penaltyClanPartsFromIS;
    private boolean planetAcquisitionVerbose;
    private int[] planetTechAcquisitionBonus;
    private int[] planetIndustryAcquisitionBonus;
    private int[] planetOutputAcquisitionBonus;
    //endregion Supplies and Acquisition Tab

    //region Tech Limits Tab
    private boolean limitByYear;
    private boolean disallowExtinctStuff;
    private boolean allowClanPurchases;
    private boolean allowISPurchases;
    private boolean allowCanonOnly;
    private boolean allowCanonRefitOnly;
    private int techLevel;
    private boolean variableTechLevel;
    private boolean factionIntroDate;
    private boolean useAmmoByType; // Unofficial
    //endregion Tech Limits Tab

    //region Personnel Tab
    // General Personnel
    private boolean useTactics;
    private boolean useInitiativeBonus;
    private boolean useToughness;
    private boolean useArtillery;
    private boolean useAbilities;
    private boolean useEdge;
    private boolean useSupportEdge;
    private boolean useImplants;
    private boolean alternativeQualityAveraging;
    private boolean useTransfers;
    private boolean personnelLogSkillGain;
    private boolean personnelLogAbilityGain;
    private boolean personnelLogEdgeGain;

    // Expanded Personnel Information
    private boolean useTimeInService;
    private TimeInDisplayFormat timeInServiceDisplayFormat;
    private boolean useTimeInRank;
    private TimeInDisplayFormat timeInRankDisplayFormat;
    private boolean useRetirementDateTracking;
    private boolean trackTotalEarnings;
    private boolean trackTotalXPEarnings;
    private boolean showOriginFaction;

    // Medical
    private boolean useAdvancedMedical; // Unofficial
    private int healWaitingPeriod;
    private int naturalHealingWaitingPeriod;
    private int minimumHitsForVehicles;
    private boolean useRandomHitsForVehicles;
    private boolean tougherHealing;

    // Prisoners
    private PrisonerCaptureStyle prisonerCaptureStyle;
    private PrisonerStatus defaultPrisonerStatus;
    private boolean prisonerBabyStatus;
    private boolean useAtBPrisonerDefection;
    private boolean useAtBPrisonerRansom;

    // Personnel Randomization
    private boolean useDylansRandomXP; // Unofficial
    private boolean randomizeOrigin;
    private boolean randomizeDependentOrigin;
    private int originSearchRadius;
    private boolean extraRandomOrigin;
    private double originDistanceScale;

    // Family
    private FamilialRelationshipDisplayLevel displayFamilyLevel;

    // Salary
    private double salaryCommissionMultiplier;
    private double salaryEnlistedMultiplier;
    private double salaryAntiMekMultiplier;
    private double salarySpecialistInfantryMultiplier;
    private double[] salaryXPMultipliers;
    private Money[] roleBaseSalaries;

    // Marriage
    private boolean useManualMarriages;
    private int minimumMarriageAge;
    private int checkMutualAncestorsDepth;
    private boolean logMarriageNameChange;
    private int[] marriageSurnameWeights;
    private boolean useRandomMarriages;
    private double chanceRandomMarriages;
    private int marriageAgeRange;
    private boolean useRandomSameSexMarriages;
    private double chanceRandomSameSexMarriages;

    // Divorce
    private boolean useManualDivorce;
    private boolean useClannerDivorce;
    private boolean usePrisonerDivorce;
    private Map<SplittingSurnameStyle, Integer> divorceSurnameWeights;
    private RandomDivorceMethod randomDivorceMethod;
    private boolean useRandomOppositeSexDivorce;
    private boolean useRandomSameSexDivorce;
    private boolean useRandomClannerDivorce;
    private boolean useRandomPrisonerDivorce;
    private double percentageRandomDivorceOppositeSexChance;
    private double percentageRandomDivorceSameSexChance;

    // Procreation
    private boolean useProcreation;
    private double chanceProcreation;
    private boolean useProcreationNoRelationship;
    private double chanceProcreationNoRelationship;
    private boolean displayTrueDueDate;
    private boolean logConception;
    private BabySurnameStyle babySurnameStyle;
    private boolean determineFatherAtBirth;

    // Death
    private boolean keepMarriedNameUponSpouseDeath;
    //endregion Personnel Tab

    //region Finance tab
    private boolean payForParts;
    private boolean payForRepairs;
    private boolean payForUnits;
    private boolean payForSalaries;
    private boolean payForOverhead;
    private boolean payForMaintain;
    private boolean payForTransport;
    private boolean sellUnits;
    private boolean sellParts;
    private boolean payForRecruitment;
    private boolean useLoanLimits;
    private boolean usePercentageMaint; // Unofficial
    private boolean infantryDontCount; // Unofficial
    private boolean usePeacetimeCost;
    private boolean useExtendedPartsModifier;
    private boolean showPeacetimeCost;
    private FinancialYearDuration financialYearDuration;
    private boolean newFinancialYearFinancesToCSVExport;

    // Price Multipliers
    private double commonPartPriceMultiplier;
    private double innerSphereUnitPriceMultiplier;
    private double innerSpherePartPriceMultiplier;
    private double clanUnitPriceMultiplier;
    private double clanPartPriceMultiplier;
    private double mixedTechUnitPriceMultiplier;
    private double[] usedPartPriceMultipliers;
    private double damagedPartsValueMultiplier;
    private double unrepairablePartsValueMultiplier;
    private double cancelledOrderRefundMultiplier;
    //endregion Finance Tab

    //region Mercenary Tab
    private boolean equipmentContractBase;
    private double equipmentContractPercent;
    private boolean equipmentContractSaleValue;
    private double dropshipContractPercent;
    private double jumpshipContractPercent;
    private double warshipContractPercent;
    private boolean blcSaleValue;
    private boolean overageRepaymentInFinalPayment;
    //endregion Mercenary Tab

    //region Experience Tab
    private int scenarioXP;
    private int killXPAward;
    private int killsForXP;
    private int tasksXP;
    private int nTasksXP;
    private int successXP;
    private int mistakeXP;
    private int idleXP;
    private int monthsIdleXP;
    private int targetIdleXP;
    private int contractNegotiationXP;
    private int adminXP;
    private int adminXPPeriod;
    private int edgeCost;
    //endregion Experience Tab

    //region Skills Tab
    //endregion Skills Tab

    //region Special Abilities Tab
    //endregion Special Abilities Tab

    //region Skill Randomization Tab
    private int[] phenotypeProbabilities;
    //endregion Skill Randomization Tab

    //region Rank System Tab
    //endregion Rank System Tab

    //region Name and Portrait Generation
    private boolean useOriginFactionForNames;
    private boolean[] usePortraitForRole;
    private boolean assignPortraitOnRoleChange;
    //endregion Name and Portrait Generation

    //region Markets Tab
    // Personnel Market
    private String personnelMarketName;
    private boolean personnelMarketReportRefresh;
    private int personnelMarketRandomEliteRemoval;
    private int personnelMarketRandomVeteranRemoval;
    private int personnelMarketRandomRegularRemoval;
    private int personnelMarketRandomGreenRemoval;
    private int personnelMarketRandomUltraGreenRemoval;
    private double personnelMarketDylansWeight;

    // Unit Market
    private UnitMarketMethod unitMarketMethod;
    private boolean unitMarketRegionalMechVariations;
    private boolean instantUnitMarketDelivery;
    private boolean unitMarketReportRefresh;

    // Contract Market
    private ContractMarketMethod contractMarketMethod;
    private boolean contractMarketReportRefresh;
    //endregion Markets Tab

    //region Against the Bot Tab
    private boolean useAtB;
    private boolean useStratCon;
    private int skillLevel;

    // Unit Administration
    private boolean useShareSystem;
    private boolean sharesExcludeLargeCraft;
    private boolean sharesForAll;
    private boolean aeroRecruitsHaveUnits;
    private boolean retirementRolls;
    private boolean customRetirementMods;
    private boolean foundersNeverRetire;
    private boolean atbAddDependents;
    private boolean dependentsNeverLeave;
    private boolean trackUnitFatigue;
    private boolean useLeadership;
    private boolean trackOriginalUnit;
    private boolean useAero;
    private boolean useVehicles;
    private boolean clanVehicles;

    // Contract Operations
    private int searchRadius;
    private boolean variableContractLength;
    private boolean mercSizeLimited;
    private boolean restrictPartsByMission;
    private boolean limitLanceWeight;
    private boolean limitLanceNumUnits;
    private boolean useStrategy;
    private int baseStrategyDeployment;
    private int additionalStrategyDeployment;
    private boolean adjustPaymentForStrategy;
    private int[] atbBattleChance;
    private boolean generateChases;

    // RATs
    private boolean staticRATs;
    private String[] rats;
    private boolean ignoreRatEra;

    // Scenarios
    private boolean doubleVehicles;
    private int opforLanceTypeMechs;
    private int opforLanceTypeMixed;
    private int opforLanceTypeVehicles;
    private boolean opforUsesVTOLs;
    private boolean allowOpforAeros;
    private int opforAeroChance;
    private boolean allowOpforLocalUnits;
    private int opforLocalUnitChance;
    private boolean adjustPlayerVehicles;
    private boolean regionalMechVariations;
    private boolean attachedPlayerCamouflage;
    private boolean playerControlsAttachedUnits;
    private boolean useDropShips;
    private boolean useWeatherConditions;
    private boolean useLightConditions;
    private boolean usePlanetaryConditions;
    private int fixedMapChance;
    //endregion Against the Bot Tab
    //endregion Variable Declarations

    //region Constructors
    public CampaignOptions() {
        // Initialize any reused variables
        final PersonnelRole[] personnelRoles = PersonnelRole.values();

        //region Unlisted Variables
        //Mass Repair/Salvage Options
        massRepairUseRepair = true;
        massRepairUseSalvage = true;
        massRepairUseExtraTime = true;
        massRepairUseRushJob = true;
        massRepairAllowCarryover = true;
        massRepairOptimizeToCompleteToday = false;
        massRepairScrapImpossible = false;
        massRepairUseAssignedTechsFirst = false;
        massRepairReplacePod = true;
        massRepairOptions = new ArrayList<>();

        for (PartRepairType type : PartRepairType.values()) {
            massRepairOptions.add(new MassRepairOption(type));
        }
        //endregion Unlisted Variables

        //region General Tab
        unitRatingMethod = UnitRatingMethod.CAMPAIGN_OPS;
        manualUnitRatingModifier = 0;
        //endregion General Tab

        //region Repair and Maintenance Tab
        // Repair
        useEraMods = false;
        assignedTechFirst = false;
        resetToFirstTech = false;
        useQuirks = false;
        useAeroSystemHits = false;
        destroyByMargin = false;
        destroyMargin = 4;
        destroyPartTarget = 10;

        // Maintenance
        checkMaintenance = true;
        maintenanceCycleDays = 7;
        maintenanceBonus = -1;
        useQualityMaintenance = true;
        reverseQualityNames = false;
        useUnofficialMaintenance = false;
        logMaintenance = false;
        //endregion Repair and Maintenance Tab

        //region Supplies and Acquisitions Tab
        // Acquisition
        waitingPeriod = 7;
        acquisitionSkill = S_TECH;
        acquisitionSupportStaffOnly = true;
        clanAcquisitionPenalty = 0;
        isAcquisitionPenalty = 0;
        maxAcquisitions = 0;

        // Delivery
        nDiceTransitTime = 1;
        constantTransitTime = 0;
        unitTransitTime = TRANSIT_UNIT_MONTH;
        acquireMinimumTime = 1;
        acquireMinimumTimeUnit = TRANSIT_UNIT_MONTH;
        acquireMosBonus = 1;
        acquireMosUnit = TRANSIT_UNIT_MONTH;

        // Planetary Acquisition
        usePlanetaryAcquisition = false;
        maxJumpsPlanetaryAcquisition = 2;
        planetAcquisitionFactionLimit = PlanetaryAcquisitionFactionLimit.NEUTRAL;
        planetAcquisitionNoClanCrossover = true;
        noClanPartsFromIS = true;
        penaltyClanPartsFromIS = 4;
        planetAcquisitionVerbose = false;
        // Planet Socio-Industrial Modifiers
        planetTechAcquisitionBonus = new int[6];
        planetTechAcquisitionBonus[EquipmentType.RATING_A] = -1;
        planetTechAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetTechAcquisitionBonus[EquipmentType.RATING_C] = 1;
        planetTechAcquisitionBonus[EquipmentType.RATING_D] = 2;
        planetTechAcquisitionBonus[EquipmentType.RATING_E] = 4;
        planetTechAcquisitionBonus[EquipmentType.RATING_F] = 8;
        planetIndustryAcquisitionBonus = new int[6];
        planetIndustryAcquisitionBonus[EquipmentType.RATING_A] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_C] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_D] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_E] = 0;
        planetIndustryAcquisitionBonus[EquipmentType.RATING_F] = 0;
        planetOutputAcquisitionBonus = new int[6];
        planetOutputAcquisitionBonus[EquipmentType.RATING_A] = -1;
        planetOutputAcquisitionBonus[EquipmentType.RATING_B] = 0;
        planetOutputAcquisitionBonus[EquipmentType.RATING_C] = 1;
        planetOutputAcquisitionBonus[EquipmentType.RATING_D] = 2;
        planetOutputAcquisitionBonus[EquipmentType.RATING_E] = 4;
        planetOutputAcquisitionBonus[EquipmentType.RATING_F] = 8;
        //endregion Supplies and Acquisitions Tab

        //region Tech Limits Tab
        limitByYear = true;
        disallowExtinctStuff = false;
        allowClanPurchases = true;
        allowISPurchases = true;
        allowCanonOnly = false;
        allowCanonRefitOnly = false;
        techLevel = TECH_EXPERIMENTAL;
        variableTechLevel = false;
        factionIntroDate = false;
        useAmmoByType = false;
        //endregion Tech Limits Tab

        //region Personnel Tab
        // General Personnel
        setUseTactics(false);
        setUseInitiativeBonus(false);
        setUseToughness(false);
        setUseArtillery(false);
        setUseAbilities(false);
        setUseEdge(false);
        setUseSupportEdge(false);
        setUseImplants(false);
        setAlternativeQualityAveraging(false);
        setUseTransfers(true);
        setPersonnelLogSkillGain(false);
        setPersonnelLogAbilityGain(false);
        setPersonnelLogEdgeGain(false);

        // Expanded Personnel Information
        setUseTimeInService(false);
        setTimeInServiceDisplayFormat(TimeInDisplayFormat.YEARS);
        setUseTimeInRank(false);
        setTimeInRankDisplayFormat(TimeInDisplayFormat.MONTHS_YEARS);
        setUseRetirementDateTracking(false);
        setTrackTotalEarnings(false);
        setTrackTotalXPEarnings(false);
        setShowOriginFaction(true);

        // Medical
        setUseAdvancedMedical(false);
        setHealingWaitingPeriod(1);
        setNaturalHealingWaitingPeriod(15);
        setMinimumHitsForVehicles(1);
        setUseRandomHitsForVehicles(false);
        setTougherHealing(false);

        // Prisoners
        setPrisonerCaptureStyle(PrisonerCaptureStyle.TAHARQA);
        setDefaultPrisonerStatus(PrisonerStatus.PRISONER);
        setPrisonerBabyStatus(true);
        setUseAtBPrisonerDefection(false);
        setUseAtBPrisonerRansom(false);

        // Personnel Randomization
        setUseDylansRandomXP(false);
        setRandomizeOrigin(false);
        setRandomizeDependentOrigin(false);
        setOriginSearchRadius(45);
        setExtraRandomOrigin(false);
        setOriginDistanceScale(0.6);

        // Family
        setDisplayFamilyLevel(FamilialRelationshipDisplayLevel.SPOUSE);

        // Salary
        setSalaryCommissionMultiplier(1.2);
        setSalaryEnlistedMultiplier(1.0);
        setSalaryAntiMekMultiplier(1.5);
        setSalarySpecialistInfantryMultiplier(1.0);
        setSalaryXPMultipliers(new double[5]);
        setSalaryXPMultiplier(SkillType.EXP_ULTRA_GREEN, 0.6);
        setSalaryXPMultiplier(SkillType.EXP_GREEN, 0.6);
        setSalaryXPMultiplier(SkillType.EXP_REGULAR, 1.0);
        setSalaryXPMultiplier(SkillType.EXP_VETERAN, 1.6);
        setSalaryXPMultiplier(SkillType.EXP_ELITE, 3.2);
        setRoleBaseSalaries(new Money[personnelRoles.length]);
        setRoleBaseSalary(PersonnelRole.MECHWARRIOR, 1500);
        setRoleBaseSalary(PersonnelRole.LAM_PILOT, 3000);
        setRoleBaseSalary(PersonnelRole.GROUND_VEHICLE_DRIVER, 900);
        setRoleBaseSalary(PersonnelRole.NAVAL_VEHICLE_DRIVER, 900);
        setRoleBaseSalary(PersonnelRole.VTOL_PILOT, 900);
        setRoleBaseSalary(PersonnelRole.VEHICLE_GUNNER, 900);
        setRoleBaseSalary(PersonnelRole.VEHICLE_CREW, 900);
        setRoleBaseSalary(PersonnelRole.AEROSPACE_PILOT, 1500);
        setRoleBaseSalary(PersonnelRole.CONVENTIONAL_AIRCRAFT_PILOT, 900);
        setRoleBaseSalary(PersonnelRole.PROTOMECH_PILOT, 960);
        setRoleBaseSalary(PersonnelRole.BATTLE_ARMOUR, 960);
        setRoleBaseSalary(PersonnelRole.SOLDIER, 750);
        setRoleBaseSalary(PersonnelRole.VESSEL_PILOT, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_GUNNER, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_CREW, 1000);
        setRoleBaseSalary(PersonnelRole.VESSEL_NAVIGATOR, 1000);
        setRoleBaseSalary(PersonnelRole.MECH_TECH, 800);
        setRoleBaseSalary(PersonnelRole.MECHANIC, 800);
        setRoleBaseSalary(PersonnelRole.AERO_TECH, 800);
        setRoleBaseSalary(PersonnelRole.BA_TECH, 800);
        setRoleBaseSalary(PersonnelRole.ASTECH, 400);
        setRoleBaseSalary(PersonnelRole.DOCTOR, 1500);
        setRoleBaseSalary(PersonnelRole.MEDIC, 400);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_COMMAND, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_LOGISTICS, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_TRANSPORT, 500);
        setRoleBaseSalary(PersonnelRole.ADMINISTRATOR_HR, 500);
        setRoleBaseSalary(PersonnelRole.DEPENDENT, 0);
        setRoleBaseSalary(PersonnelRole.NONE, 0);

        // Marriage
        setUseManualMarriages(true);
        setMinimumMarriageAge(16);
        setCheckMutualAncestorsDepth(4);
        setLogMarriageNameChange(false);
        setMarriageSurnameWeights(new int[Marriage.values().length - 1]);
        setMarriageSurnameWeight(Marriage.NO_CHANGE.ordinal(), 100);
        setMarriageSurnameWeight(Marriage.YOURS.ordinal(), 55);
        setMarriageSurnameWeight(Marriage.SPOUSE.ordinal(), 55);
        setMarriageSurnameWeight(Marriage.SPACE_YOURS.ordinal(), 10);
        setMarriageSurnameWeight(Marriage.BOTH_SPACE_YOURS.ordinal(), 5);
        setMarriageSurnameWeight(Marriage.HYP_YOURS.ordinal(), 30);
        setMarriageSurnameWeight(Marriage.BOTH_HYP_YOURS.ordinal(), 20);
        setMarriageSurnameWeight(Marriage.SPACE_SPOUSE.ordinal(), 10);
        setMarriageSurnameWeight(Marriage.BOTH_SPACE_SPOUSE.ordinal(), 5);
        setMarriageSurnameWeight(Marriage.HYP_SPOUSE.ordinal(), 30);
        setMarriageSurnameWeight(Marriage.BOTH_HYP_SPOUSE.ordinal(), 20);
        setMarriageSurnameWeight(Marriage.MALE.ordinal(), 500);
        setMarriageSurnameWeight(Marriage.FEMALE.ordinal(), 160);
        setUseRandomMarriages(false);
        setChanceRandomMarriages(0.00025);
        setMarriageAgeRange(10);
        setUseRandomSameSexMarriages(false);
        setChanceRandomSameSexMarriages(0.00002);

        // Divorce
        setUseManualDivorce(true);
        setUseClannerDivorce(true);
        setUsePrisonerDivorce(false);
        setDivorceSurnameWeights(new HashMap<>());
        getDivorceSurnameWeights().put(SplittingSurnameStyle.ORIGIN_CHANGES_SURNAME, 10);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.SPOUSE_CHANGES_SURNAME, 10);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.BOTH_CHANGE_SURNAME, 30);
        getDivorceSurnameWeights().put(SplittingSurnameStyle.BOTH_KEEP_SURNAME, 50);
        setRandomDivorceMethod(RandomDivorceMethod.NONE);
        setUseRandomOppositeSexDivorce(true);
        setUseRandomSameSexDivorce(true);
        setUseRandomClannerDivorce(true);
        setUseRandomPrisonerDivorce(false);
        setPercentageRandomDivorceOppositeSexChance(0.000001);
        setPercentageRandomDivorceSameSexChance(0.000001);

        // Procreation
        setUseProcreation(false);
        setChanceProcreation(0.0005);
        setUseProcreationNoRelationship(false);
        setChanceProcreationNoRelationship(0.00005);
        setDisplayTrueDueDate(false);
        setLogConception(false);
        setBabySurnameStyle(BabySurnameStyle.MOTHERS);
        setDetermineFatherAtBirth(false);

        // Death
        setKeepMarriedNameUponSpouseDeath(true);
        //endregion Personnel Tab

        //region Finances Tab
        payForParts = false;
        payForRepairs = false;
        payForUnits = false;
        payForSalaries = false;
        payForOverhead = false;
        payForMaintain = false;
        payForTransport = false;
        sellUnits = false;
        sellParts = false;
        payForRecruitment = false;
        useLoanLimits = false;
        usePercentageMaint = false;
        infantryDontCount = false;
        usePeacetimeCost = false;
        useExtendedPartsModifier = false;
        showPeacetimeCost = false;
        setFinancialYearDuration(FinancialYearDuration.ANNUAL);
        newFinancialYearFinancesToCSVExport = false;

        // Price Multipliers
        setCommonPartPriceMultiplier(1.0);
        setInnerSphereUnitPriceMultiplier(1.0);
        setInnerSpherePartPriceMultiplier(1.0);
        setClanUnitPriceMultiplier(1.0);
        setClanPartPriceMultiplier(1.0);
        setMixedTechUnitPriceMultiplier(1.0);
        setUsedPartPriceMultipliers(0.1, 0.2, 0.3, 0.5, 0.7, 0.9);
        setDamagedPartsValueMultiplier(0.33);
        setUnrepairablePartsValueMultiplier(0.1);
        setCancelledOrderRefundMultiplier(0.5);
        //endregion Finances Tab

        //region Mercenary Tab
        equipmentContractBase = false;
        equipmentContractPercent = 5.0;
        equipmentContractSaleValue = false;
        dropshipContractPercent = 1.0;
        jumpshipContractPercent = 0.0;
        warshipContractPercent = 0.0;
        blcSaleValue = false;
        overageRepaymentInFinalPayment = false;
        //endregion Mercenary Tab

        //region Experience Tab
        scenarioXP = 1;
        killXPAward = 0;
        killsForXP = 0;
        tasksXP = 1;
        nTasksXP = 25;
        successXP = 0;
        mistakeXP = 0;
        idleXP = 0;
        monthsIdleXP = 2;
        targetIdleXP = 10;
        contractNegotiationXP = 0;
        adminXP = 0;
        adminXPPeriod = 1;
        edgeCost = 10;
        //endregion Experience Tab

        //region Skills Tab
        //endregion Skills Tab

        //region Special Abilities Tab
        //endregion Special Abilities Tab

        //region Skill Randomization Tab
        phenotypeProbabilities = new int[Phenotype.getExternalPhenotypes().size()];
        phenotypeProbabilities[Phenotype.MECHWARRIOR.getIndex()] = 95;
        phenotypeProbabilities[Phenotype.ELEMENTAL.getIndex()] = 100;
        phenotypeProbabilities[Phenotype.AEROSPACE.getIndex()] = 95;
        phenotypeProbabilities[Phenotype.VEHICLE.getIndex()] = 0;
        phenotypeProbabilities[Phenotype.PROTOMECH.getIndex()] = 95;
        phenotypeProbabilities[Phenotype.NAVAL.getIndex()] = 25;
        //endregion Skill Randomization Tab

        //region Rank System Tab
        //endregion Rank System Tab

        //region Name and Portrait Generation Tab
        useOriginFactionForNames = true;
        usePortraitForRole = new boolean[personnelRoles.length];
        Arrays.fill(usePortraitForRole, false);
        usePortraitForRole[PersonnelRole.MECHWARRIOR.ordinal()] = true;
        assignPortraitOnRoleChange = false;
        //endregion Name and Portrait Generation Tab

        //region Markets Tab
        // Personnel Market
        setPersonnelMarketType(PersonnelMarket.getTypeName(PersonnelMarket.TYPE_STRAT_OPS));
        setPersonnelMarketReportRefresh(true);
        setPersonnelMarketRandomEliteRemoval(10);
        setPersonnelMarketRandomVeteranRemoval(8);
        setPersonnelMarketRandomRegularRemoval(6);
        setPersonnelMarketRandomGreenRemoval(4);
        setPersonnelMarketRandomUltraGreenRemoval(4);
        setPersonnelMarketDylansWeight(0.3);

        // Unit Market
        setUnitMarketMethod(UnitMarketMethod.NONE);
        setUnitMarketRegionalMechVariations(true);
        setInstantUnitMarketDelivery(false);
        setUnitMarketReportRefresh(true);

        // Contract Market
        setContractMarketMethod(ContractMarketMethod.NONE);
        setContractMarketReportRefresh(true);
        //endregion Markets Tab

        //region Against the Bot Tab
        useAtB = false;
        useStratCon = false;
        skillLevel = 2;

        // Unit Administration
        useShareSystem = false;
        sharesExcludeLargeCraft = false;
        sharesForAll = false;
        aeroRecruitsHaveUnits = false;
        retirementRolls = true;
        customRetirementMods = false;
        foundersNeverRetire = false;
        atbAddDependents = true;
        dependentsNeverLeave = false;
        trackUnitFatigue = false;
        useLeadership = true;
        trackOriginalUnit = false;
        useAero = false;
        useVehicles = true;
        clanVehicles = false;

        // Contract Operations
        searchRadius = 800;
        variableContractLength = false;
        mercSizeLimited = false;
        restrictPartsByMission = true;
        limitLanceWeight = true;
        limitLanceNumUnits = true;
        useStrategy = true;
        baseStrategyDeployment = 3;
        additionalStrategyDeployment = 1;
        adjustPaymentForStrategy = false;
        atbBattleChance = new int[AtBLanceRole.values().length - 1];
        atbBattleChance[AtBLanceRole.FIGHTING.ordinal()] = 40;
        atbBattleChance[AtBLanceRole.DEFENCE.ordinal()] = 20;
        atbBattleChance[AtBLanceRole.SCOUTING.ordinal()] = 60;
        atbBattleChance[AtBLanceRole.TRAINING.ordinal()] = 10;
        generateChases = true;

        // RATs
        staticRATs = false;
        rats = new String[]{ "Xotl", "Total Warfare" }; // TODO : Localize me
        ignoreRatEra = false;

        // Scenarios
        doubleVehicles = false;
        opforLanceTypeMechs = 1;
        opforLanceTypeMixed = 2;
        opforLanceTypeVehicles = 3;
        opforUsesVTOLs = true;
        allowOpforAeros = false;
        opforAeroChance = 5;
        allowOpforLocalUnits = false;
        opforLocalUnitChance = 5;
        setFixedMapChance(25);
        adjustPlayerVehicles = false;
        regionalMechVariations = false;
        attachedPlayerCamouflage = true;
        playerControlsAttachedUnits = false;
        useDropShips = false;
        useWeatherConditions = true;
        useLightConditions = true;
        usePlanetaryConditions = false;
        //endregion Against the Bot Tab
    }
    //endregion Constructors

    //region General Tab
    /**
     * @return the method of unit rating to use
     */
    public UnitRatingMethod getUnitRatingMethod() {
        return unitRatingMethod;
    }

    /**
     * @param method the method of unit rating to use
     */
    public void setUnitRatingMethod(UnitRatingMethod method) {
        this.unitRatingMethod = method;
    }

    public int getManualUnitRatingModifier() {
        return manualUnitRatingModifier;
    }

    public void setManualUnitRatingModifier(int manualUnitRatingModifier) {
        this.manualUnitRatingModifier = manualUnitRatingModifier;
    }
    //endregion General Tab

    //region Repair and Maintenance Tab
    //region Repair
    //endregion Repair

    //region Maintenance
    public boolean checkMaintenance() {
        return checkMaintenance;
    }

    public void setCheckMaintenance(boolean b) {
        checkMaintenance = b;
    }

    public int getMaintenanceCycleDays() {
        return maintenanceCycleDays;
    }

    public void setMaintenanceCycleDays(int d) {
        maintenanceCycleDays = d;
    }

    public int getMaintenanceBonus() {
        return maintenanceBonus;
    }

    public void setMaintenanceBonus(int d) {
        maintenanceBonus = d;
    }

    public boolean useQualityMaintenance() {
        return useQualityMaintenance;
    }

    public void setUseQualityMaintenance(boolean b) {
        useQualityMaintenance = b;
    }

    public boolean reverseQualityNames() {
        return reverseQualityNames;
    }

    public void setReverseQualityNames(boolean b) {
        reverseQualityNames = b;
    }

    public boolean useUnofficialMaintenance() {
        return useUnofficialMaintenance;
    }

    public void setUseUnofficialMaintenance(boolean b) {
        useUnofficialMaintenance = b;
    }

    public boolean logMaintenance() {
        return logMaintenance;
    }

    public void setLogMaintenance(boolean b) {
        logMaintenance = b;
    }
    //endregion Maintenance
    //endregion Repair and Maintenance Tab

    //region Supplies and Acquisitions Tab
    //endregion Supplies and Acquisitions Tab

    //region Personnel Tab
    //region General Personnel
    public boolean useTactics() {
        return useTactics;
    }

    public void setUseTactics(final boolean useTactics) {
        this.useTactics = useTactics;
    }

    public boolean useInitiativeBonus() {
        return useInitiativeBonus;
    }

    public void setUseInitiativeBonus(final boolean useInitiativeBonus) {
        this.useInitiativeBonus = useInitiativeBonus;
    }

    public boolean useToughness() {
        return useToughness;
    }

    public void setUseToughness(final boolean useToughness) {
        this.useToughness = useToughness;
    }

    public boolean useArtillery() {
        return useArtillery;
    }

    public void setUseArtillery(final boolean useArtillery) {
        this.useArtillery = useArtillery;
    }

    public boolean useAbilities() {
        return useAbilities;
    }

    public void setUseAbilities(final boolean useAbilities) {
        this.useAbilities = useAbilities;
    }

    public boolean useEdge() {
        return useEdge;
    }

    public void setUseEdge(final boolean useEdge) {
        this.useEdge = useEdge;
    }

    public boolean useSupportEdge() {
        return useSupportEdge;
    }

    public void setUseSupportEdge(final boolean useSupportEdge) {
        this.useSupportEdge = useSupportEdge;
    }

    public boolean useImplants() {
        return useImplants;
    }

    public void setUseImplants(final boolean useImplants) {
        this.useImplants = useImplants;
    }

    public boolean useAlternativeQualityAveraging() {
        return alternativeQualityAveraging;
    }

    public void setAlternativeQualityAveraging(final boolean alternativeQualityAveraging) {
        this.alternativeQualityAveraging = alternativeQualityAveraging;
    }

    public boolean useTransfers() {
        return useTransfers;
    }

    public void setUseTransfers(final boolean useTransfers) {
        this.useTransfers = useTransfers;
    }

    public boolean isPersonnelLogSkillGain() {
        return personnelLogSkillGain;
    }

    public void setPersonnelLogSkillGain(final boolean personnelLogSkillGain) {
        this.personnelLogSkillGain = personnelLogSkillGain;
    }

    public boolean isPersonnelLogAbilityGain() {
        return personnelLogAbilityGain;
    }

    public void setPersonnelLogAbilityGain(final boolean personnelLogAbilityGain) {
        this.personnelLogAbilityGain = personnelLogAbilityGain;
    }

    public boolean isPersonnelLogEdgeGain() {
        return personnelLogEdgeGain;
    }

    public void setPersonnelLogEdgeGain(final boolean personnelLogEdgeGain) {
        this.personnelLogEdgeGain = personnelLogEdgeGain;
    }
    //endregion General Personnel

    //region Expanded Personnel Information
    /**
     * @return whether or not to use time in service
     */
    public boolean getUseTimeInService() {
        return useTimeInService;
    }

    /**
     * @param useTimeInService the new value for whether to use time in service or not
     */
    public void setUseTimeInService(final boolean useTimeInService) {
        this.useTimeInService = useTimeInService;
    }

    /**
     * @return the format to display the Time in Service in
     */
    public TimeInDisplayFormat getTimeInServiceDisplayFormat() {
        return timeInServiceDisplayFormat;
    }

    /**
     * @param timeInServiceDisplayFormat the new display format for Time in Service
     */
    public void setTimeInServiceDisplayFormat(final TimeInDisplayFormat timeInServiceDisplayFormat) {
        this.timeInServiceDisplayFormat = timeInServiceDisplayFormat;
    }

    /**
     * @return whether or not to use time in rank
     */
    public boolean getUseTimeInRank() {
        return useTimeInRank;
    }

    /**
     * @param useTimeInRank the new value for whether or not to use time in rank
     */
    public void setUseTimeInRank(final boolean useTimeInRank) {
        this.useTimeInRank = useTimeInRank;
    }

    /**
     * @return the format to display the Time in Rank in
     */
    public TimeInDisplayFormat getTimeInRankDisplayFormat() {
        return timeInRankDisplayFormat;
    }

    /**
     * @param timeInRankDisplayFormat the new display format for Time in Rank
     */
    public void setTimeInRankDisplayFormat(final TimeInDisplayFormat timeInRankDisplayFormat) {
        this.timeInRankDisplayFormat = timeInRankDisplayFormat;
    }

    /**
     * @return whether or not to track retirement dates
     */
    public boolean useRetirementDateTracking() {
        return useRetirementDateTracking;
    }

    /**
     * @param useRetirementDateTracking the new value for whether or not to track retirement dates
     */
    public void setUseRetirementDateTracking(final boolean useRetirementDateTracking) {
        this.useRetirementDateTracking = useRetirementDateTracking;
    }

    /**
     * @return whether or not to track the total earnings of personnel
     */
    public boolean isTrackTotalEarnings() {
        return trackTotalEarnings;
    }

    /**
     * @param trackTotalEarnings the new value for whether or not to track total earnings for personnel
     */
    public void setTrackTotalEarnings(final boolean trackTotalEarnings) {
        this.trackTotalEarnings = trackTotalEarnings;
    }

    /**
     * @return whether or not to track the total experience earnings of personnel
     */
    public boolean isTrackTotalXPEarnings() {
        return trackTotalXPEarnings;
    }

    /**
     * @param trackTotalXPEarnings the new value for whether or not to track total experience
     *                             earnings for personnel
     */
    public void setTrackTotalXPEarnings(final boolean trackTotalXPEarnings) {
        this.trackTotalXPEarnings = trackTotalXPEarnings;
    }

    /**
     * Gets a value indicating whether or not to show a person's origin faction when displaying
     * their details.
     */
    public boolean showOriginFaction() {
        return showOriginFaction;
    }

    /**
     * Sets a value indicating whether or not to show a person's origin faction when displaying
     * their details.
     */
    public void setShowOriginFaction(final boolean showOriginFaction) {
        this.showOriginFaction = showOriginFaction;
    }
    //endregion Expanded Personnel Information

    //region Medical
    public boolean useAdvancedMedical() {
        return useAdvancedMedical;
    }

    public void setUseAdvancedMedical(final boolean useAdvancedMedical) {
        this.useAdvancedMedical = useAdvancedMedical;
    }

    public int getHealingWaitingPeriod() {
        return healWaitingPeriod;
    }

    public void setHealingWaitingPeriod(final int healWaitingPeriod) {
        this.healWaitingPeriod = healWaitingPeriod;
    }

    public int getNaturalHealingWaitingPeriod() {
        return naturalHealingWaitingPeriod;
    }

    public void setNaturalHealingWaitingPeriod(final int naturalHealingWaitingPeriod) {
        this.naturalHealingWaitingPeriod = naturalHealingWaitingPeriod;
    }

    public int getMinimumHitsForVehicles() {
        return minimumHitsForVehicles;
    }

    public void setMinimumHitsForVehicles(final int minimumHitsForVehicles) {
        this.minimumHitsForVehicles = minimumHitsForVehicles;
    }

    public boolean useRandomHitsForVehicles() {
        return useRandomHitsForVehicles;
    }

    public void setUseRandomHitsForVehicles(final boolean useRandomHitsForVehicles) {
        this.useRandomHitsForVehicles = useRandomHitsForVehicles;
    }

    public boolean useTougherHealing() {
        return tougherHealing;
    }

    public void setTougherHealing(final boolean tougherHealing) {
        this.tougherHealing = tougherHealing;
    }
    //endregion Medical

    //region Prisoners
    public PrisonerCaptureStyle getPrisonerCaptureStyle() {
        return prisonerCaptureStyle;
    }

    public void setPrisonerCaptureStyle(final PrisonerCaptureStyle prisonerCaptureStyle) {
        this.prisonerCaptureStyle = prisonerCaptureStyle;
    }

    public PrisonerStatus getDefaultPrisonerStatus() {
        return defaultPrisonerStatus;
    }

    public void setDefaultPrisonerStatus(final PrisonerStatus defaultPrisonerStatus) {
        this.defaultPrisonerStatus = defaultPrisonerStatus;
    }

    public boolean getPrisonerBabyStatus() {
        return prisonerBabyStatus;
    }

    public void setPrisonerBabyStatus(final boolean prisonerBabyStatus) {
        this.prisonerBabyStatus = prisonerBabyStatus;
    }

    public boolean useAtBPrisonerDefection() {
        return useAtBPrisonerDefection;
    }

    public void setUseAtBPrisonerDefection(final boolean useAtBPrisonerDefection) {
        this.useAtBPrisonerDefection = useAtBPrisonerDefection;
    }

    public boolean useAtBPrisonerRansom() {
        return useAtBPrisonerRansom;
    }

    public void setUseAtBPrisonerRansom(final boolean useAtBPrisonerRansom) {
        this.useAtBPrisonerRansom = useAtBPrisonerRansom;
    }
    //endregion Prisoners

    //region Personnel Randomization
    public boolean useDylansRandomXP() {
        return useDylansRandomXP;
    }

    public void setUseDylansRandomXP(final boolean useDylansRandomXP) {
        this.useDylansRandomXP = useDylansRandomXP;
    }
    /**
     * Gets a value indicating whether or not to randomize the
     * origin of personnel.
     */
    public boolean randomizeOrigin() {
        return randomizeOrigin;
    }

    /**
     * Sets a value indicating whether or not to randomize the origin of personnel.
     * @param randomizeOrigin true for randomize, otherwise false
     */
    public void setRandomizeOrigin(final boolean randomizeOrigin) {
        this.randomizeOrigin = randomizeOrigin;
    }

    /**
     * Gets a value indicating whether or not to randomize the origin of dependents
     */
    public boolean getRandomizeDependentOrigin() {
        return randomizeDependentOrigin;
    }

    /**
     * Sets a value indicating whether or not to randomize the origin of dependents
     * @param randomizeDependentOrigin true for randomize, otherwise false
     */
    public void setRandomizeDependentOrigin(final boolean randomizeDependentOrigin) {
        this.randomizeDependentOrigin = randomizeDependentOrigin;
    }

    /**
     * Gets the search radius to use for randomizing personnel origins.
     */
    public int getOriginSearchRadius() {
        return originSearchRadius;
    }

    /**
     * Sets the search radius to use for randomizing personnel origins.
     * @param originSearchRadius The search radius.
     */
    public void setOriginSearchRadius(final int originSearchRadius) {
        this.originSearchRadius = originSearchRadius;
    }

    /**
     * Gets a value indicating whether or not to randomize origin to the planetary level, rather
     * than just the system level.
     */
    public boolean extraRandomOrigin() {
        return extraRandomOrigin;
    }

    /**
     * Sets a value indicating whether or not to randomize origin to the planetary level, rather
     * than just the system level.
     */
    public void setExtraRandomOrigin(final boolean extraRandomOrigin) {
        this.extraRandomOrigin = extraRandomOrigin;
    }

    /**
     * Gets the distance scale factor to apply when weighting random origin planets.
     */
    public double getOriginDistanceScale() {
        return originDistanceScale;
    }

    /**
     * Sets the distance scale factor to apply when weighting random origin planets
     * (should be between 0.1 and 2).
     */
    public void setOriginDistanceScale(final double originDistanceScale) {
        this.originDistanceScale = originDistanceScale;
    }
    //endregion Personnel Randomization

    //region Family
    /**
     * @return the level of familial relation to display
     */
    public FamilialRelationshipDisplayLevel getDisplayFamilyLevel() {
        return displayFamilyLevel;
    }

    /**
     * @param displayFamilyLevel the level of familial relation to display
     */
    public void setDisplayFamilyLevel(final FamilialRelationshipDisplayLevel displayFamilyLevel) {
        this.displayFamilyLevel = displayFamilyLevel;
    }
    //endregion Family

    //region Salary
    public double getSalaryCommissionMultiplier() {
        return salaryCommissionMultiplier;
    }

    public void setSalaryCommissionMultiplier(final double salaryCommissionMultiplier) {
        this.salaryCommissionMultiplier = salaryCommissionMultiplier;
    }

    public double getSalaryEnlistedMultiplier() {
        return salaryEnlistedMultiplier;
    }

    public void setSalaryEnlistedMultiplier(final double salaryEnlistedMultiplier) {
        this.salaryEnlistedMultiplier = salaryEnlistedMultiplier;
    }

    public double getSalaryAntiMekMultiplier() {
        return salaryAntiMekMultiplier;
    }

    public void setSalaryAntiMekMultiplier(final double salaryAntiMekMultiplier) {
        this.salaryAntiMekMultiplier = salaryAntiMekMultiplier;
    }

    public double getSalarySpecialistInfantryMultiplier() {
        return salarySpecialistInfantryMultiplier;
    }

    public void setSalarySpecialistInfantryMultiplier(final double salarySpecialistInfantryMultiplier) {
        this.salarySpecialistInfantryMultiplier = salarySpecialistInfantryMultiplier;
    }

    public double[] getSalaryXPMultipliers() {
        return salaryXPMultipliers;
    }

    public double getSalaryXPMultiplier(final int index) {
        return ((index < 0) || (index >= getSalaryXPMultipliers().length)) ? 1.0 : getSalaryXPMultipliers()[index];
    }

    public void setSalaryXPMultipliers(final double... salaryXPMultipliers) {
        this.salaryXPMultipliers = salaryXPMultipliers;
    }

    public void setSalaryXPMultiplier(final int index, final double multiplier) {
        if ((index < 0) || (index >= getSalaryXPMultipliers().length)) {
            return;
        }
        getSalaryXPMultipliers()[index] = multiplier;
    }

    public Money[] getRoleBaseSalaries() {
        return roleBaseSalaries;
    }

    public void setRoleBaseSalaries(final Money... roleBaseSalaries) {
        this.roleBaseSalaries = roleBaseSalaries;
    }

    public void setRoleBaseSalary(final PersonnelRole role, final double base) {
        setRoleBaseSalary(role, Money.of(base));
    }

    public void setRoleBaseSalary(final PersonnelRole role, final Money base) {
        getRoleBaseSalaries()[role.ordinal()] = base;
    }
    //endregion Salary

    //region Marriage
    /**
     * @return whether or not to use manual marriages
     */
    public boolean useManualMarriages() {
        return useManualMarriages;
    }

    /**
     * @param useManualMarriages whether or not to use manual marriages
     */
    public void setUseManualMarriages(final boolean useManualMarriages) {
        this.useManualMarriages = useManualMarriages;
    }

    /**
     * @return the minimum age a person can get married at
     */
    public int getMinimumMarriageAge() {
        return minimumMarriageAge;
    }

    /**
     * @param minimumMarriageAge the minimum age a person can get married at
     */
    public void setMinimumMarriageAge(final int minimumMarriageAge) {
        this.minimumMarriageAge = minimumMarriageAge;
    }

    /**
     * This gets the number of recursions to use when checking mutual ancestors between two personnel
     * @return the number of recursions to use
     */
    public int checkMutualAncestorsDepth() {
        return checkMutualAncestorsDepth;
    }

    /**
     * This sets the number of recursions to use when checking mutual ancestors between two personnel
     * @param checkMutualAncestorsDepth the number of recursions
     */
    public void setCheckMutualAncestorsDepth(final int checkMutualAncestorsDepth) {
        this.checkMutualAncestorsDepth = checkMutualAncestorsDepth;
    }

    /**
     * @return whether or not to log a name change in a marriage
     */
    public boolean logMarriageNameChange() {
        return logMarriageNameChange;
    }

    /**
     * @param logMarriageNameChange whether to log marriage name changes or not
     */
    public void setLogMarriageNameChange(final boolean logMarriageNameChange) {
        this.logMarriageNameChange = logMarriageNameChange;
    }

    /**
     * @return the array of weights of potential surname changes for weighted marriage surname generation
     */
    public int[] getMarriageSurnameWeights() {
        return marriageSurnameWeights;
    }

    /**
     * This gets one of the values in the array of weights of potential surname changes for weighted marriage surname generation
     * @param index the array index to get
     * @return the weight at the index
     */
    public int getMarriageSurnameWeight(final int index) {
        return getMarriageSurnameWeights()[index];
    }

    /**
     * @param marriageSurnameWeights the new marriage surname weight array
     */
    public void setMarriageSurnameWeights(final int... marriageSurnameWeights) {
        this.marriageSurnameWeights = marriageSurnameWeights;
    }

    /**
     * This sets one of the values in the array of weights of potential surname changes for weighted marriage surname generation
     * @param index the array index to set
     * @param marriageSurnameWeight the weight to use
     */
    public void setMarriageSurnameWeight(final int index, final int marriageSurnameWeight) {
        marriageSurnameWeights[index] = marriageSurnameWeight;
    }

    /**
     * @return whether or not to use random marriages
     */
    public boolean useRandomMarriages() {
        return useRandomMarriages;
    }

    /**
     * @param useRandomMarriages whether or not to use random marriages
     */
    public void setUseRandomMarriages(final boolean useRandomMarriages) {
        this.useRandomMarriages = useRandomMarriages;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of a random marriage occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getChanceRandomMarriages() {
        return chanceRandomMarriages;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of a random marriage occurring
     * @param chanceRandomMarriages the chance, with a value between 0 and 1
     */
    public void setChanceRandomMarriages(final double chanceRandomMarriages) {
        this.chanceRandomMarriages = chanceRandomMarriages;
    }

    /**
     * A random marriage can only happen between two people whose ages differ (+/-) by the returned value
     * @return the age range ages can differ (+/-)
     */
    public int getMarriageAgeRange() {
        return marriageAgeRange;
    }

    /**
     * A random marriage can only happen between two people whose ages differ (+/-) by this value
     * @param marriageAgeRange the maximum age range
     */
    public void setMarriageAgeRange(final int marriageAgeRange) {
        this.marriageAgeRange = marriageAgeRange;
    }

    /**
     * @return whether or not to use random same sex marriages
     */
    public boolean useRandomSameSexMarriages() {
        return useRandomSameSexMarriages;
    }

    /**
     * @param useRandomSameSexMarriages whether or not to use random same sex marriages
     */
    public void setUseRandomSameSexMarriages(final boolean useRandomSameSexMarriages) {
        this.useRandomSameSexMarriages = useRandomSameSexMarriages;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of a random same sex marriage occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getChanceRandomSameSexMarriages() {
        return chanceRandomSameSexMarriages;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of a random same sex marriage occurring
     * @param chanceRandomSameSexMarriages the chance, with a value between 0 and 1
     */
    public void setChanceRandomSameSexMarriages(final double chanceRandomSameSexMarriages) {
        this.chanceRandomSameSexMarriages = chanceRandomSameSexMarriages;
    }
    //endregion Marriage

    //region Divorce
    public boolean isUseManualDivorce() {
        return useManualDivorce;
    }

    public void setUseManualDivorce(final boolean useManualDivorce) {
        this.useManualDivorce = useManualDivorce;
    }

    public boolean isUseClannerDivorce() {
        return useClannerDivorce;
    }

    public void setUseClannerDivorce(final boolean useClannerDivorce) {
        this.useClannerDivorce = useClannerDivorce;
    }

    public boolean isUsePrisonerDivorce() {
        return usePrisonerDivorce;
    }

    public void setUsePrisonerDivorce(final boolean usePrisonerDivorce) {
        this.usePrisonerDivorce = usePrisonerDivorce;
    }

    public Map<SplittingSurnameStyle, Integer> getDivorceSurnameWeights() {
        return divorceSurnameWeights;
    }

    public void setDivorceSurnameWeights(final Map<SplittingSurnameStyle, Integer> divorceSurnameWeights) {
        this.divorceSurnameWeights = divorceSurnameWeights;
    }

    public RandomDivorceMethod getRandomDivorceMethod() {
        return randomDivorceMethod;
    }

    public void setRandomDivorceMethod(final RandomDivorceMethod randomDivorceMethod) {
        this.randomDivorceMethod = randomDivorceMethod;
    }

    public boolean isUseRandomOppositeSexDivorce() {
        return useRandomOppositeSexDivorce;
    }

    public void setUseRandomOppositeSexDivorce(final boolean useRandomOppositeSexDivorce) {
        this.useRandomOppositeSexDivorce = useRandomOppositeSexDivorce;
    }

    public boolean isUseRandomSameSexDivorce() {
        return useRandomSameSexDivorce;
    }

    public void setUseRandomSameSexDivorce(final boolean useRandomSameSexDivorce) {
        this.useRandomSameSexDivorce = useRandomSameSexDivorce;
    }

    public boolean isUseRandomClannerDivorce() {
        return useRandomClannerDivorce;
    }

    public void setUseRandomClannerDivorce(final boolean useRandomClannerDivorce) {
        this.useRandomClannerDivorce = useRandomClannerDivorce;
    }

    public boolean isUseRandomPrisonerDivorce() {
        return useRandomPrisonerDivorce;
    }

    public void setUseRandomPrisonerDivorce(final boolean useRandomPrisonerDivorce) {
        this.useRandomPrisonerDivorce = useRandomPrisonerDivorce;
    }

    public double getPercentageRandomDivorceOppositeSexChance() {
        return percentageRandomDivorceOppositeSexChance;
    }

    public void setPercentageRandomDivorceOppositeSexChance(final double percentageRandomDivorceOppositeSexChance) {
        this.percentageRandomDivorceOppositeSexChance = percentageRandomDivorceOppositeSexChance;
    }

    public double getPercentageRandomDivorceSameSexChance() {
        return percentageRandomDivorceSameSexChance;
    }

    public void setPercentageRandomDivorceSameSexChance(final double percentageRandomDivorceSameSexChance) {
        this.percentageRandomDivorceSameSexChance = percentageRandomDivorceSameSexChance;
    }
    //endregion Divorce

    //region Procreation
    /**
     * @return whether or not to use unofficial procreation
     */
    public boolean useProcreation() {
        return useProcreation;
    }

    /**
     * @param useProcreation whether or not to use unofficial procreation
     */
    public void setUseProcreation(final boolean useProcreation) {
        this.useProcreation = useProcreation;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of random procreation occurring
     * @return the chance, with a value between 0 and 1
     */
    public double getChanceProcreation() {
        return chanceProcreation;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of random procreation occurring
     * @param chanceProcreation the chance, with a value between 0 and 1
     */
    public void setChanceProcreation(final double chanceProcreation) {
        this.chanceProcreation = chanceProcreation;
    }

    /**
     * @return whether or not to use procreation without a relationship
     */
    public boolean useProcreationNoRelationship() {
        return useProcreationNoRelationship;
    }

    /**
     * @param useProcreationNoRelationship whether or not to use unofficial procreation without a relationship
     */
    public void setUseProcreationNoRelationship(final boolean useProcreationNoRelationship) {
        this.useProcreationNoRelationship = useProcreationNoRelationship;
    }

    /**
     * This gets the decimal chance (between 0 and 1) of random procreation occurring without a relationship
     * @return the chance, with a value between 0 and 1
     */
    public double getChanceProcreationNoRelationship() {
        return chanceProcreationNoRelationship;
    }

    /**
     * This sets the decimal chance (between 0 and 1) of random procreation occurring without a relationship
     * @param chanceProcreationNoRelationship the chance, with a value between 0 and 1
     */
    public void setChanceProcreationNoRelationship(final double chanceProcreationNoRelationship) {
        this.chanceProcreationNoRelationship = chanceProcreationNoRelationship;
    }

    /**
     * @return whether to show the expected or actual due date for personnel
     */
    public boolean getDisplayTrueDueDate() {
        return displayTrueDueDate;
    }

    /**
     * @param displayTrueDueDate whether to show the expected or actual due date for personnel
     */
    public void setDisplayTrueDueDate(final boolean displayTrueDueDate) {
        this.displayTrueDueDate = displayTrueDueDate;
    }

    /**
     * @return whether to log conception
     */
    public boolean logConception() {
        return logConception;
    }

    /**
     * @param logConception whether to log conception
     */
    public void setLogConception(final boolean logConception) {
        this.logConception = logConception;
    }

    /**
     * @return what style of surname to use for a baby
     */
    public BabySurnameStyle getBabySurnameStyle() {
        return babySurnameStyle;
    }

    /**
     * @param babySurnameStyle the style of surname to use for a baby
     */
    public void setBabySurnameStyle(final BabySurnameStyle babySurnameStyle) {
        this.babySurnameStyle = babySurnameStyle;
    }

    /**
     * @return whether or not to determine the father at birth instead of at conception
     */
    public boolean determineFatherAtBirth() {
        return determineFatherAtBirth;
    }

    /**
     * @param determineFatherAtBirth whether or not to determine the father at birth instead of at conception
     */
    public void setDetermineFatherAtBirth(final boolean determineFatherAtBirth) {
        this.determineFatherAtBirth = determineFatherAtBirth;
    }
    //endregion Procreation

    //region Death
    /**
     * @return whether to keep ones married name upon spouse death or not
     */
    public boolean getKeepMarriedNameUponSpouseDeath() {
        return keepMarriedNameUponSpouseDeath;
    }

    /**
     * @param keepMarriedNameUponSpouseDeath whether to keep ones married name upon spouse death or not
     */
    public void setKeepMarriedNameUponSpouseDeath(final boolean keepMarriedNameUponSpouseDeath) {
        this.keepMarriedNameUponSpouseDeath = keepMarriedNameUponSpouseDeath;
    }
    //endregion Death
    //endregion Personnel Tab

    //region Finances Tab
    public boolean payForParts() {
        return payForParts;
    }

    public void setPayForParts(boolean b) {
        this.payForParts = b;
    }

    public boolean payForRepairs() {
        return payForRepairs;
    }

    public void setPayForRepairs(boolean b) {
        this.payForRepairs = b;
    }

    public boolean payForUnits() {
        return payForUnits;
    }

    public void setPayForUnits(boolean b) {
        this.payForUnits = b;
    }

    public boolean payForSalaries() {
        return payForSalaries;
    }

    public void setPayForSalaries(boolean b) {
        this.payForSalaries = b;
    }

    public boolean payForOverhead() {
        return payForOverhead;
    }

    public void setPayForOverhead(boolean b) {
        this.payForOverhead = b;
    }

    public boolean payForMaintain() {
        return payForMaintain;
    }

    public void setPayForMaintain(boolean b) {
        this.payForMaintain = b;
    }

    public boolean payForTransport() {
        return payForTransport;
    }

    public void setPayForTransport(boolean b) {
        this.payForTransport = b;
    }

    public boolean canSellUnits() {
        return sellUnits;
    }

    public void setSellUnits(boolean b) {
        this.sellUnits = b;
    }

    public boolean canSellParts() {
        return sellParts;
    }

    public void setSellParts(boolean b) {
        this.sellParts = b;
    }

    public boolean payForRecruitment() {
        return payForRecruitment;
    }

    public void setPayForRecruitment(boolean b) {
        this.payForRecruitment = b;
    }

    public boolean useLoanLimits() {
        return useLoanLimits;
    }

    public void setLoanLimits(boolean b) {
        this.useLoanLimits = b;
    }

    public boolean usePercentageMaint() {
        return usePercentageMaint;
    }

    public void setUsePercentageMaint(boolean b) {
        usePercentageMaint = b;
    }

    public boolean useInfantryDontCount() {
        return infantryDontCount;
    }

    public void setUseInfantryDontCount(boolean b) {
        infantryDontCount = b;
    }

    public boolean usePeacetimeCost() {
        return usePeacetimeCost;
    }

    public void setUsePeacetimeCost(boolean b) {
        this.usePeacetimeCost = b;
    }

    public boolean useExtendedPartsModifier() {
        return useExtendedPartsModifier;
    }

    public void setUseExtendedPartsModifier(boolean b) {
        this.useExtendedPartsModifier = b;
    }

    public boolean showPeacetimeCost() {
        return showPeacetimeCost;
    }

    public void setShowPeacetimeCost(boolean b) {
        this.showPeacetimeCost = b;
    }

    /**
     * @return the duration of a financial year
     */
    public FinancialYearDuration getFinancialYearDuration() {
        return financialYearDuration;
    }

    /**
     * @param financialYearDuration the financial year duration to set
     */
    public void setFinancialYearDuration(FinancialYearDuration financialYearDuration) {
        this.financialYearDuration = financialYearDuration;
    }

    /**
     * @return whether or not to export finances to CSV at the end of a financial year
     */
    public boolean getNewFinancialYearFinancesToCSVExport() {
        return newFinancialYearFinancesToCSVExport;
    }

    /**
     * @param b whether or not to export finances to CSV at the end of a financial year
     */
    public void setNewFinancialYearFinancesToCSVExport(boolean b) {
        newFinancialYearFinancesToCSVExport = b;
    }

    //region Price Multipliers
    public double getCommonPartPriceMultiplier() {
        return commonPartPriceMultiplier;
    }

    public void setCommonPartPriceMultiplier(final double commonPartPriceMultiplier) {
        this.commonPartPriceMultiplier = commonPartPriceMultiplier;
    }

    public double getInnerSphereUnitPriceMultiplier() {
        return innerSphereUnitPriceMultiplier;
    }

    public void setInnerSphereUnitPriceMultiplier(final double innerSphereUnitPriceMultiplier) {
        this.innerSphereUnitPriceMultiplier = innerSphereUnitPriceMultiplier;
    }

    public double getInnerSpherePartPriceMultiplier() {
        return innerSpherePartPriceMultiplier;
    }

    public void setInnerSpherePartPriceMultiplier(final double innerSpherePartPriceMultiplier) {
        this.innerSpherePartPriceMultiplier = innerSpherePartPriceMultiplier;
    }

    public double getClanUnitPriceMultiplier() {
        return clanUnitPriceMultiplier;
    }

    public void setClanUnitPriceMultiplier(final double clanUnitPriceMultiplier) {
        this.clanUnitPriceMultiplier = clanUnitPriceMultiplier;
    }

    public double getClanPartPriceMultiplier() {
        return clanPartPriceMultiplier;
    }

    public void setClanPartPriceMultiplier(final double clanPartPriceMultiplier) {
        this.clanPartPriceMultiplier = clanPartPriceMultiplier;
    }

    public double getMixedTechUnitPriceMultiplier() {
        return mixedTechUnitPriceMultiplier;
    }

    public void setMixedTechUnitPriceMultiplier(final double mixedTechUnitPriceMultiplier) {
        this.mixedTechUnitPriceMultiplier = mixedTechUnitPriceMultiplier;
    }

    public double[] getUsedPartPriceMultipliers() {
        return usedPartPriceMultipliers;
    }

    public void setUsedPartPriceMultipliers(final double... usedPartPriceMultipliers) {
        this.usedPartPriceMultipliers = usedPartPriceMultipliers;
    }

    public double getDamagedPartsValueMultiplier() {
        return damagedPartsValueMultiplier;
    }

    public void setDamagedPartsValueMultiplier(final double damagedPartsValueMultiplier) {
        this.damagedPartsValueMultiplier = damagedPartsValueMultiplier;
    }

    public double getUnrepairablePartsValueMultiplier() {
        return unrepairablePartsValueMultiplier;
    }

    public void setUnrepairablePartsValueMultiplier(final double unrepairablePartsValueMultiplier) {
        this.unrepairablePartsValueMultiplier = unrepairablePartsValueMultiplier;
    }

    public double getCancelledOrderRefundMultiplier() {
        return cancelledOrderRefundMultiplier;
    }

    public void setCancelledOrderRefundMultiplier(final double cancelledOrderRefundMultiplier) {
        this.cancelledOrderRefundMultiplier = cancelledOrderRefundMultiplier;
    }
    //endregion Price Multipliers
    //endregion Finances Tab

    //region Markets Tab
    //region Personnel Market
    public String getPersonnelMarketType() {
        return personnelMarketName;
    }

    public void setPersonnelMarketType(final String personnelMarketName) {
        this.personnelMarketName = personnelMarketName;
    }

    public boolean getPersonnelMarketReportRefresh() {
        return personnelMarketReportRefresh;
    }

    public void setPersonnelMarketReportRefresh(final boolean personnelMarketReportRefresh) {
        this.personnelMarketReportRefresh = personnelMarketReportRefresh;
    }

    public int getPersonnelMarketRandomEliteRemoval() {
        return personnelMarketRandomEliteRemoval;
    }

    public void setPersonnelMarketRandomEliteRemoval(final int personnelMarketRandomEliteRemoval) {
        this.personnelMarketRandomEliteRemoval = personnelMarketRandomEliteRemoval;
    }

    public int getPersonnelMarketRandomVeteranRemoval() {
        return personnelMarketRandomVeteranRemoval;
    }

    public void setPersonnelMarketRandomVeteranRemoval(final int personnelMarketRandomVeteranRemoval) {
        this.personnelMarketRandomVeteranRemoval = personnelMarketRandomVeteranRemoval;
    }

    public int getPersonnelMarketRandomRegularRemoval() {
        return personnelMarketRandomRegularRemoval;
    }

    public void setPersonnelMarketRandomRegularRemoval(final int personnelMarketRandomRegularRemoval) {
        this.personnelMarketRandomRegularRemoval = personnelMarketRandomRegularRemoval;
    }

    public int getPersonnelMarketRandomGreenRemoval() {
        return personnelMarketRandomGreenRemoval;
    }

    public void setPersonnelMarketRandomGreenRemoval(final int personnelMarketRandomGreenRemoval) {
        this.personnelMarketRandomGreenRemoval = personnelMarketRandomGreenRemoval;
    }

    public int getPersonnelMarketRandomUltraGreenRemoval() {
        return personnelMarketRandomUltraGreenRemoval;
    }

    public void setPersonnelMarketRandomUltraGreenRemoval(final int personnelMarketRandomUltraGreenRemoval) {
        this.personnelMarketRandomUltraGreenRemoval = personnelMarketRandomUltraGreenRemoval;
    }

    public double getPersonnelMarketDylansWeight() {
        return personnelMarketDylansWeight;
    }

    public void setPersonnelMarketDylansWeight(final double personnelMarketDylansWeight) {
        this.personnelMarketDylansWeight = personnelMarketDylansWeight;
    }
    //endregion Personnel Market

    //region Unit Market
    public UnitMarketMethod getUnitMarketMethod() {
        return unitMarketMethod;
    }

    public void setUnitMarketMethod(final UnitMarketMethod unitMarketMethod) {
        this.unitMarketMethod = unitMarketMethod;
    }

    public boolean useUnitMarketRegionalMechVariations() {
        return unitMarketRegionalMechVariations;
    }

    public void setUnitMarketRegionalMechVariations(final boolean unitMarketRegionalMechVariations) {
        this.unitMarketRegionalMechVariations = unitMarketRegionalMechVariations;
    }

    public boolean getInstantUnitMarketDelivery() {
        return instantUnitMarketDelivery;
    }

    public void setInstantUnitMarketDelivery(final boolean instantUnitMarketDelivery) {
        this.instantUnitMarketDelivery = instantUnitMarketDelivery;
    }

    public boolean getUnitMarketReportRefresh() {
        return unitMarketReportRefresh;
    }

    public void setUnitMarketReportRefresh(final boolean unitMarketReportRefresh) {
        this.unitMarketReportRefresh = unitMarketReportRefresh;
    }
    //endregion Unit Market

    //region Contract Market
    public ContractMarketMethod getContractMarketMethod() {
        return contractMarketMethod;
    }

    public void setContractMarketMethod(final ContractMarketMethod contractMarketMethod) {
        this.contractMarketMethod = contractMarketMethod;
    }

    public boolean getContractMarketReportRefresh() {
        return contractMarketReportRefresh;
    }

    public void setContractMarketReportRefresh(final boolean contractMarketReportRefresh) {
        this.contractMarketReportRefresh = contractMarketReportRefresh;
    }
    //endregion Contract Market
    //endregion Markets Tab

    public static String getTechLevelName(int lvl) {
        switch (lvl) {
            case TECH_INTRO:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_INTRO];
            case TECH_STANDARD:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_STANDARD];
            case TECH_ADVANCED:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_ADVANCED];
            case TECH_EXPERIMENTAL:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_EXPERIMENTAL];
            case TECH_UNOFFICIAL:
                return TechConstants.T_SIMPLE_NAMES[TechConstants.T_SIMPLE_UNOFFICIAL];
            default:
                return "Unknown";
        }
    }

    public static String getTransitUnitName(int unit) {
        switch (unit) {
            case TRANSIT_UNIT_DAY:
                return "Days";
            case TRANSIT_UNIT_WEEK:
                return "Weeks";
            case TRANSIT_UNIT_MONTH:
                return "Months";
            default:
                return "Unknown";
        }
    }

    public boolean useEraMods() {
        return useEraMods;
    }

    public void setEraMods(boolean b) {
        this.useEraMods = b;
    }

    public boolean useAssignedTechFirst() {
        return assignedTechFirst;
    }

    public void setAssignedTechFirst(boolean assignedTechFirst) {
        this.assignedTechFirst = assignedTechFirst;
    }

    public boolean useResetToFirstTech() {
        return resetToFirstTech;
    }

    public void setResetToFirstTech(boolean resetToFirstTech) {
        this.resetToFirstTech = resetToFirstTech;
    }

    /**
     * @return true to use the origin faction for personnel names instead of a set faction
     */
    public boolean useOriginFactionForNames() {
        return useOriginFactionForNames;
    }

    /**
     * @param useOriginFactionForNames whether to use personnel names or a set faction
     */
    public void setUseOriginFactionForNames(boolean useOriginFactionForNames) {
        this.useOriginFactionForNames = useOriginFactionForNames;
    }

    public boolean useQuirks() {
        return useQuirks;
    }

    public void setQuirks(boolean b) {
        this.useQuirks = b;
    }

    public int getScenarioXP() {
        return scenarioXP;
    }

    public void setScenarioXP(int xp) {
        scenarioXP = xp;
    }

    public int getKillsForXP() {
        return killsForXP;
    }

    public void setKillsForXP(int k) {
        killsForXP = k;
    }

    public int getKillXPAward() {
        return killXPAward;
    }

    public void setKillXPAward(int xp) {
        killXPAward = xp;
    }

    public int getNTasksXP() {
        return nTasksXP;
    }

    public void setNTasksXP(int xp) {
        nTasksXP = xp;
    }

    public int getTaskXP() {
        return tasksXP;
    }

    public void setTaskXP(int b) {
        tasksXP = b;
    }

    public int getMistakeXP() {
        return mistakeXP;
    }

    public void setMistakeXP(int b) {
        mistakeXP = b;
    }

    public int getSuccessXP() {
        return successXP;
    }

    public void setSuccessXP(int b) {
        successXP = b;
    }

    public boolean limitByYear() {
        return limitByYear;
    }

    public void setLimitByYear(boolean b) {
        limitByYear = b;
    }

    public boolean disallowExtinctStuff() {
        return disallowExtinctStuff;
    }

    public void setDisallowExtinctStuff(boolean b) {
        disallowExtinctStuff = b;
    }

    public boolean allowClanPurchases() {
        return allowClanPurchases;
    }

    public void setAllowClanPurchases(boolean b) {
        allowClanPurchases = b;
    }

    public boolean allowISPurchases() {
        return allowISPurchases;
    }

    public void setAllowISPurchases(boolean b) {
        allowISPurchases = b;
    }

    public boolean allowCanonOnly() {
        return allowCanonOnly;
    }

    public void setAllowCanonOnly(boolean b) {
        allowCanonOnly = b;
    }

    public boolean allowCanonRefitOnly() {
        return allowCanonRefitOnly;
    }

    public void setAllowCanonRefitOnly(boolean b) {
        allowCanonRefitOnly = b;
    }

    public boolean useVariableTechLevel() {
        return variableTechLevel;
    }

    public void setVariableTechLevel(boolean b) {
        variableTechLevel = b;
    }

    public void setFactionIntroDate(boolean b) {
        factionIntroDate = b;
    }

    public boolean useFactionIntroDate() {
        return factionIntroDate;
    }

    public boolean useAmmoByType() {
        return useAmmoByType;
    }

    public void setUseAmmoByType(boolean b) {
        useAmmoByType = b;
    }

    public int getTechLevel() {
        return techLevel;
    }

    public void setTechLevel(int lvl) {
        techLevel = lvl;
    }

    public int[] getPhenotypeProbabilities() {
        return phenotypeProbabilities;
    }

    public int getPhenotypeProbability(Phenotype phenotype) {
        return getPhenotypeProbabilities()[phenotype.getIndex()];
    }

    public void setPhenotypeProbability(int index, int percentage) {
        phenotypeProbabilities[index] = percentage;
    }

    public boolean[] usePortraitForRoles() {
        return usePortraitForRole;
    }

    public boolean usePortraitForRole(final PersonnelRole role) {
        return usePortraitForRoles()[role.ordinal()];
    }

    public void setUsePortraitForRole(int index, boolean b) {
        usePortraitForRole[index] = b;
    }

    public boolean getAssignPortraitOnRoleChange() {
        return assignPortraitOnRoleChange;
    }

    public void setAssignPortraitOnRoleChange(boolean b) {
        assignPortraitOnRoleChange = b;
    }

    public int getIdleXP() {
        return idleXP;
    }

    public void setIdleXP(int xp) {
        idleXP = xp;
    }

    public int getTargetIdleXP() {
        return targetIdleXP;
    }

    public void setTargetIdleXP(int xp) {
        targetIdleXP = xp;
    }

    public int getMonthsIdleXP() {
        return monthsIdleXP;
    }

    public void setMonthsIdleXP(int m) {
        monthsIdleXP = m;
    }

    public int getContractNegotiationXP() {
        return contractNegotiationXP;
    }

    public void setContractNegotiationXP(int m) {
        contractNegotiationXP = m;
    }

    public int getAdminXP() {
        return adminXP;
    }

    public void setAdminXP(int m) {
        adminXP = m;
    }

    public int getAdminXPPeriod() {
        return adminXPPeriod;
    }

    public void setAdminXPPeriod(int m) {
        adminXPPeriod = m;
    }

    public int getEdgeCost() {
        return edgeCost;
    }

    public void setEdgeCost(int b) {
        edgeCost = b;
    }

    public int getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(int d) {
        waitingPeriod = d;
    }

    public String getAcquisitionSkill() {
        return acquisitionSkill;
    }

    public void setAcquisitionSkill(String skill) {
        acquisitionSkill = skill;
    }

    public void setAcquisitionSupportStaffOnly(boolean b) {
        this.acquisitionSupportStaffOnly = b;
    }

    public boolean isAcquisitionSupportStaffOnly() {
        return acquisitionSupportStaffOnly;
    }

    public int getNDiceTransitTime() {
        return nDiceTransitTime;
    }

    public void setNDiceTransitTime(int d) {
        nDiceTransitTime = d;
    }

    public int getConstantTransitTime() {
        return constantTransitTime;
    }

    public void setConstantTransitTime(int d) {
        constantTransitTime = d;
    }

    public int getUnitTransitTime() {
        return unitTransitTime;
    }

    public void setUnitTransitTime(int d) {
        unitTransitTime = d;
    }

    public int getAcquireMosUnit() {
        return acquireMosUnit;
    }

    public void setAcquireMosUnit(int b) {
        acquireMosUnit = b;
    }

    public int getAcquireMosBonus() {
        return acquireMosBonus;
    }

    public void setAcquireMosBonus(int b) {
        acquireMosBonus = b;
    }

    public int getAcquireMinimumTimeUnit() {
        return acquireMinimumTimeUnit;
    }

    public void setAcquireMinimumTimeUnit(int b) {
        acquireMinimumTimeUnit = b;
    }

    public int getAcquireMinimumTime() {
        return acquireMinimumTime;
    }

    public void setAcquireMinimumTime(int b) {
        acquireMinimumTime = b;
    }

    public boolean usesPlanetaryAcquisition() {
        return usePlanetaryAcquisition;
    }

    public void setPlanetaryAcquisition(boolean b) {
        usePlanetaryAcquisition = b;
    }

    public PlanetaryAcquisitionFactionLimit getPlanetAcquisitionFactionLimit() {
        return planetAcquisitionFactionLimit;
    }

    public void setPlanetAcquisitionFactionLimit(final PlanetaryAcquisitionFactionLimit planetAcquisitionFactionLimit) {
        this.planetAcquisitionFactionLimit = planetAcquisitionFactionLimit;
    }

    public boolean disallowPlanetAcquisitionClanCrossover() {
        return planetAcquisitionNoClanCrossover;
    }

    public void setDisallowPlanetAcquisitionClanCrossover(boolean b) {
        planetAcquisitionNoClanCrossover = b;
    }

    public int getMaxJumpsPlanetaryAcquisition() {
        return maxJumpsPlanetaryAcquisition;
    }

    public void setMaxJumpsPlanetaryAcquisition(int m) {
        maxJumpsPlanetaryAcquisition = m;
    }

    public int getPenaltyClanPartsFroIS() {
        return penaltyClanPartsFromIS;
    }

    public void setPenaltyClanPartsFroIS(int i) {
        penaltyClanPartsFromIS = i ;
    }

    public boolean disallowClanPartsFromIS() {
        return noClanPartsFromIS;
    }

    public void setDisallowClanPartsFromIS(boolean b) {
        noClanPartsFromIS = b;
    }

    public boolean usePlanetAcquisitionVerboseReporting() {
        return planetAcquisitionVerbose;
    }

    public void setPlanetAcquisitionVerboseReporting(boolean b) {
        planetAcquisitionVerbose = b;
    }

    public double getEquipmentContractPercent() {
        return equipmentContractPercent;
    }

    public void setEquipmentContractPercent(double b) {
        equipmentContractPercent = Math.min(b, MAXIMUM_COMBAT_EQUIPMENT_PERCENT);
    }

    public boolean useEquipmentContractBase() {
        return equipmentContractBase;
    }

    public void setEquipmentContractBase(boolean b) {
        this.equipmentContractBase = b;
    }

    public boolean useEquipmentContractSaleValue() {
        return equipmentContractSaleValue;
    }

    public void setEquipmentContractSaleValue(boolean b) {
        this.equipmentContractSaleValue = b;
    }

    public double getDropshipContractPercent() {
        return dropshipContractPercent;
    }

    public void setDropshipContractPercent(double b) {
        dropshipContractPercent = Math.min(b, MAXIMUM_DROPSHIP_EQUIPMENT_PERCENT);
    }

    public double getJumpshipContractPercent() {
        return jumpshipContractPercent;
    }

    public void setJumpshipContractPercent(double b) {
        jumpshipContractPercent = Math.min(b, MAXIMUM_JUMPSHIP_EQUIPMENT_PERCENT);
    }

    public double getWarshipContractPercent() {
        return warshipContractPercent;
    }

    public void setWarshipContractPercent(double b) {
        warshipContractPercent = Math.min(b, MAXIMUM_WARSHIP_EQUIPMENT_PERCENT);
    }

    public boolean useBLCSaleValue() {
        return blcSaleValue;
    }

    public void setBLCSaleValue(boolean b) {
        this.blcSaleValue = b;
    }

    public boolean getOverageRepaymentInFinalPayment() {
        return overageRepaymentInFinalPayment;
    }

    public void setOverageRepaymentInFinalPayment(boolean overageRepaymentInFinalPayment) {
        this.overageRepaymentInFinalPayment = overageRepaymentInFinalPayment;
    }

    public int getClanAcquisitionPenalty() {
        return clanAcquisitionPenalty;
    }

    public void setClanAcquisitionPenalty(int b) {
        clanAcquisitionPenalty = b;
    }

    public int getIsAcquisitionPenalty() {
        return isAcquisitionPenalty;
    }

    public void setIsAcquisitionPenalty(int b) {
        isAcquisitionPenalty = b;
    }

    public int getPlanetTechAcquisitionBonus(int type) {
        if (type < 0 || type >= planetTechAcquisitionBonus.length) {
            return 0;
        }
        return planetTechAcquisitionBonus[type];
    }

    public void setPlanetTechAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetTechAcquisitionBonus.length) {
            return;
        }
        this.planetTechAcquisitionBonus[type] = base;
    }

    public int getPlanetIndustryAcquisitionBonus(int type) {
        if (type < 0 || type >= planetIndustryAcquisitionBonus.length) {
            return 0;
        }
        return planetIndustryAcquisitionBonus[type];
    }

    public void setPlanetIndustryAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetIndustryAcquisitionBonus.length) {
            return;
        }
        this.planetIndustryAcquisitionBonus[type] = base;
    }

    public int getPlanetOutputAcquisitionBonus(int type) {
        if (type < 0 || type >= planetOutputAcquisitionBonus.length) {
            return 0;
        }
        return planetOutputAcquisitionBonus[type];
    }

    public void setPlanetOutputAcquisitionBonus(int base, int type) {
        if (type < 0 || type >= planetOutputAcquisitionBonus.length) {
            return;
        }
        this.planetOutputAcquisitionBonus[type] = base;
    }

    public boolean isDestroyByMargin() {
        return destroyByMargin;
    }

    public void setDestroyByMargin(boolean b) {
        destroyByMargin = b;
    }

    public int getDestroyMargin() {
        return destroyMargin;
    }

    public void setDestroyMargin(int d) {
        destroyMargin = d;
    }

    public int getDestroyPartTarget() {
        return destroyPartTarget;
    }

    public void setDestroyPartTarget(int d) {
        destroyPartTarget = d;
    }

    public boolean useAeroSystemHits() {
        return useAeroSystemHits;
    }

    public void setUseAeroSystemHits(boolean b) {
        useAeroSystemHits = b;
    }

    public int getMaxAcquisitions() {
        return maxAcquisitions;
    }

    public void setMaxAcquisitions(int d) {
        maxAcquisitions = d;
    }

    public boolean getUseAtB() {
        return useAtB;
    }

    public void setUseAtB(boolean useAtB) {
        this.useAtB = useAtB;
    }

    public boolean getUseStratCon() {
        return useStratCon;
    }

    public void setUseStratCon(boolean useStratCon) {
        this.useStratCon = useStratCon;
    }

    public boolean getUseAero() {
        return useAero;
    }

    public void setUseAero(boolean useAero) {
        this.useAero = useAero;
    }

    public boolean getUseVehicles() {
        return useVehicles;
    }

    public void setUseVehicles(boolean useVehicles) {
        this.useVehicles = useVehicles;
    }

    public boolean getClanVehicles() {
        return clanVehicles;
    }

    public void setClanVehicles(boolean clanVehicles) {
        this.clanVehicles = clanVehicles;
    }

    public boolean getDoubleVehicles() {
        return doubleVehicles;
    }

    public void setDoubleVehicles(boolean doubleVehicles) {
        this.doubleVehicles = doubleVehicles;
    }

    public boolean getAdjustPlayerVehicles() {
        return adjustPlayerVehicles;
    }

    public int getOpforLanceTypeMechs() {
        return opforLanceTypeMechs;
    }

    public void setOpforLanceTypeMechs(int weight) {
        opforLanceTypeMechs = weight;
    }

    public int getOpforLanceTypeMixed() {
        return opforLanceTypeMixed;
    }

    public void setOpforLanceTypeMixed(int weight) {
        opforLanceTypeMixed = weight;
    }

    public int getOpforLanceTypeVehicles() {
        return opforLanceTypeVehicles;
    }

    public void setOpforLanceTypeVehicles(int weight) {
        opforLanceTypeVehicles = weight;
    }

    public boolean getOpforUsesVTOLs() {
        return opforUsesVTOLs;
    }

    public void setOpforUsesVTOLs(boolean vtol) {
        opforUsesVTOLs = vtol;
    }

    public void setAdjustPlayerVehicles(boolean adjust) {
        adjustPlayerVehicles = adjust;
    }

    public boolean getUseDropShips() {
        return useDropShips;
    }

    public void setUseDropShips(boolean useDropShips) {
        this.useDropShips = useDropShips;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int level) {
        skillLevel = level;
    }

    public boolean getAeroRecruitsHaveUnits() {
        return aeroRecruitsHaveUnits;
    }

    public void setAeroRecruitsHaveUnits(boolean haveUnits) {
        aeroRecruitsHaveUnits = haveUnits;
    }

    public boolean getUseShareSystem() {
        return useShareSystem;
    }

    public boolean getSharesExcludeLargeCraft() {
        return sharesExcludeLargeCraft;
    }

    public void setSharesExcludeLargeCraft(boolean exclude) {
        sharesExcludeLargeCraft = exclude;
    }

    public boolean getSharesForAll() {
        return sharesForAll;
    }

    public void setSharesForAll(boolean set) {
        sharesForAll = set;
    }

    public boolean doRetirementRolls() {
        return retirementRolls;
    }

    public void setRetirementRolls(boolean roll) {
        retirementRolls = roll;
    }

    public boolean getCustomRetirementMods() {
        return customRetirementMods;
    }

    public void setCustomRetirementMods(boolean mods) {
        customRetirementMods = mods;
    }

    public boolean getFoundersNeverRetire() {
        return foundersNeverRetire;
    }

    public void setFoundersNeverRetire(boolean mods) {
        foundersNeverRetire = mods;
    }

    public boolean canAtBAddDependents() {
        return atbAddDependents;
    }

    public void setAtBAddDependents(boolean b) {
        atbAddDependents = b;
    }

    public boolean getDependentsNeverLeave() {
        return dependentsNeverLeave;
    }

    public void setDependentsNeverLeave(boolean b) {
        dependentsNeverLeave = b;
    }

    public boolean getTrackOriginalUnit() {
        return trackOriginalUnit;
    }

    public void setTrackOriginalUnit(boolean track) {
        trackOriginalUnit = track;
    }

    public boolean isMercSizeLimited() {
        return mercSizeLimited;
    }

    public boolean getTrackUnitFatigue() {
        return trackUnitFatigue;
    }

    public void setTrackUnitFatigue(boolean fatigue) {
        trackUnitFatigue = fatigue;
    }

    public void setMercSizeLimited(boolean limit) {
        mercSizeLimited = limit;
    }

    public void setUseShareSystem(boolean shares) {
        useShareSystem = shares;
    }

    public boolean getRegionalMechVariations() {
        return regionalMechVariations;
    }

    public void setRegionalMechVariations(boolean regionalMechVariations) {
        this.regionalMechVariations = regionalMechVariations;
    }

    public boolean getAttachedPlayerCamouflage() {
        return attachedPlayerCamouflage;
    }

    public void setAttachedPlayerCamouflage(boolean attachedPlayerCamouflage) {
        this.attachedPlayerCamouflage = attachedPlayerCamouflage;
    }

    public boolean getPlayerControlsAttachedUnits() {
        return playerControlsAttachedUnits;
    }

    public void setPlayerControlsAttachedUnits(boolean playerControlsAttachedUnits) {
        this.playerControlsAttachedUnits = playerControlsAttachedUnits;
    }

    public String[] getRATs() {
        return rats;
    }

    public void setRATs(String[] rats) {
        this.rats = rats;
    }

    public boolean useStaticRATs() {
        return staticRATs;
    }

    public void setStaticRATs(boolean staticRATs) {
        this.staticRATs = staticRATs;
    }

    public boolean canIgnoreRatEra() {
        return ignoreRatEra;
    }

    public void setIgnoreRatEra(boolean ignore) {
        ignoreRatEra = ignore;
    }

    public int getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(int radius) {
        searchRadius = radius;
    }

    /**
     * @param role the {@link AtBLanceRole} to get the battle chance for
     * @return the chance of having a battle for the specified role
     */
    public int getAtBBattleChance(final AtBLanceRole role) {
        return role.isUnassigned() ? 0 : atbBattleChance[role.ordinal()];
    }

    /**
     * @param role      the {@link AtBLanceRole} ordinal value
     * @param frequency the frequency to set the generation to (percent chance from 0 to 100)
     */
    public void setAtBBattleChance(int role, int frequency) {
        if (frequency < 0) {
            frequency = 0;
        } else if (frequency > 100) {
            frequency = 100;
        }

        this.atbBattleChance[role] = frequency;
    }

    public boolean generateChases() {
        return generateChases;
    }

    public void setGenerateChases(boolean generateChases) {
        this.generateChases = generateChases;
    }

    public boolean getVariableContractLength() {
        return variableContractLength;
    }

    public void setVariableContractLength(boolean variable) {
        variableContractLength = variable;
    }

    public boolean getUseWeatherConditions() {
        return useWeatherConditions;
    }

    public void setUseWeatherConditions(boolean useWeatherConditions) {
        this.useWeatherConditions = useWeatherConditions;
    }

    public boolean getUseLightConditions() {
        return useLightConditions;
    }

    public void setUseLightConditions(boolean useLightConditions) {
        this.useLightConditions = useLightConditions;
    }

    public boolean getUsePlanetaryConditions() {
        return usePlanetaryConditions;
    }

    public void setUsePlanetaryConditions(boolean usePlanetaryConditions) {
        this.usePlanetaryConditions = usePlanetaryConditions;
    }

    public boolean getUseLeadership() {
        return useLeadership;
    }

    public void setUseLeadership(boolean useLeadership) {
        this.useLeadership = useLeadership;
    }

    public boolean getUseStrategy() {
        return useStrategy;
    }

    public void setUseStrategy(boolean useStrategy) {
        this.useStrategy = useStrategy;
    }

    public int getBaseStrategyDeployment() {
        return baseStrategyDeployment;
    }

    public void setBaseStrategyDeployment(int baseStrategyDeployment) {
        this.baseStrategyDeployment = baseStrategyDeployment;
    }

    public int getAdditionalStrategyDeployment() {
        return additionalStrategyDeployment;
    }

    public void setAdditionalStrategyDeployment(int additionalStrategyDeployment) {
        this.additionalStrategyDeployment = additionalStrategyDeployment;
    }

    public boolean getAdjustPaymentForStrategy() {
        return adjustPaymentForStrategy;
    }

    public void setAdjustPaymentForStrategy(boolean adjustPaymentForStrategy) {
        this.adjustPaymentForStrategy = adjustPaymentForStrategy;
    }

    public boolean getRestrictPartsByMission() {
        return restrictPartsByMission;
    }

    public void setRestrictPartsByMission(boolean restrictPartsByMission) {
        this.restrictPartsByMission = restrictPartsByMission;
    }

    public boolean getLimitLanceWeight() {
        return limitLanceWeight;
    }

    public void setLimitLanceWeight(boolean limit) {
        limitLanceWeight = limit;
    }

    public boolean getLimitLanceNumUnits() {
        return limitLanceNumUnits;
    }

    public void setLimitLanceNumUnits(boolean limit) {
        limitLanceNumUnits = limit;
    }

    //region Mass Repair/ Mass Salvage
    public boolean massRepairUseRepair() {
        return massRepairUseRepair;
    }

    public void setMassRepairUseRepair(boolean massRepairUseRepair) {
        this.massRepairUseRepair = massRepairUseRepair;
    }

    public boolean massRepairUseSalvage() {
        return massRepairUseSalvage;
    }

    public void setMassRepairUseSalvage(boolean massRepairUseSalvage) {
        this.massRepairUseSalvage = massRepairUseSalvage;
    }

    public boolean massRepairUseExtraTime() {
        return massRepairUseExtraTime;
    }

    public void setMassRepairUseExtraTime(boolean b) {
        this.massRepairUseExtraTime = b;
    }

    public boolean massRepairUseRushJob() {
        return massRepairUseRushJob;
    }

    public void setMassRepairUseRushJob(boolean b) {
        this.massRepairUseRushJob = b;
    }

    public boolean massRepairAllowCarryover() {
        return massRepairAllowCarryover;
    }

    public void setMassRepairAllowCarryover(boolean b) {
        this.massRepairAllowCarryover = b;
    }

    public boolean massRepairOptimizeToCompleteToday() {
        return massRepairOptimizeToCompleteToday;
    }

    public void setMassRepairOptimizeToCompleteToday(boolean massRepairOptimizeToCompleteToday) {
        this.massRepairOptimizeToCompleteToday = massRepairOptimizeToCompleteToday;
    }

    public boolean massRepairScrapImpossible() {
        return massRepairScrapImpossible;
    }

    public void setMassRepairScrapImpossible(boolean b) {
        this.massRepairScrapImpossible = b;
    }

    public boolean massRepairUseAssignedTechsFirst() {
        return massRepairUseAssignedTechsFirst;
    }

    public void setMassRepairUseAssignedTechsFirst(boolean massRepairUseAssignedTechsFirst) {
        this.massRepairUseAssignedTechsFirst = massRepairUseAssignedTechsFirst;
    }

    public void setMassRepairReplacePod(boolean setMassRepairReplacePod) {
        this.massRepairReplacePod = setMassRepairReplacePod;
    }

    public boolean massRepairReplacePod() {
        return massRepairReplacePod;
    }

    public List<MassRepairOption> getMassRepairOptions() {
        return (massRepairOptions != null) ? massRepairOptions : new ArrayList<>();
    }

    public void setMassRepairOptions(List<MassRepairOption> massRepairOptions) {
        this.massRepairOptions = massRepairOptions;
    }

    public void addMassRepairOption(MassRepairOption mro) {
        if (mro.getType() == PartRepairType.UNKNOWN_LOCATION) {
            return;
        }

        getMassRepairOptions().removeIf(massRepairOption -> massRepairOption.getType() == mro.getType());
        getMassRepairOptions().add(mro);
    }
    //endregion Mass Repair/ Mass Salvage

    public void setAllowOpforAeros(boolean allowOpforAeros) {
        this.allowOpforAeros = allowOpforAeros;
    }

    public boolean getAllowOpforAeros() {
        return allowOpforAeros;
    }

    public void setAllowOpforLocalUnits(boolean allowOpforLocalUnits) {
        this.allowOpforLocalUnits = allowOpforLocalUnits;
    }

    public boolean getAllowOpforLocalUnits() {
        return allowOpforLocalUnits;
    }

    public void setOpforAeroChance(int chance) {
        this.opforAeroChance = chance;
    }

    public int getOpforAeroChance() {
        return opforAeroChance;
    }

    public void setOpforLocalUnitChance(int chance) {
        this.opforLocalUnitChance = chance;
    }

    public int getOpforLocalUnitChance() {
        return opforLocalUnitChance;
    }

    public int getFixedMapChance() {
        return fixedMapChance;
    }

    public void setFixedMapChance(int fixedMapChance) {
        this.fixedMapChance = fixedMapChance;
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "<campaignOptions>");
        //region General Tab
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "manualUnitRatingModifier", getManualUnitRatingModifier());
        //endregion General Tab

        //region Repair and Maintenance Tab
        //region Maintenance
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "logMaintenance", logMaintenance);
        //endregion Maintenance
        //endregion Repair and Maintenance Tab

        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useFactionForNames", useOriginFactionForNames);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "unitRatingMethod", unitRatingMethod.name());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useEraMods", useEraMods);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "assignedTechFirst", assignedTechFirst);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "resetToFirstTech", resetToFirstTech);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useQuirks", useQuirks);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "scenarioXP", scenarioXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "killsForXP", killsForXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "killXPAward", killXPAward);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "nTasksXP", nTasksXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "tasksXP", tasksXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "mistakeXP", mistakeXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "successXP", successXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "idleXP", idleXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "targetIdleXP", targetIdleXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "monthsIdleXP", monthsIdleXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "contractNegotiationXP", contractNegotiationXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "adminWeeklyXP", adminXP);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "adminXPPeriod", adminXPPeriod);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "edgeCost", edgeCost);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "limitByYear", limitByYear);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "disallowExtinctStuff", disallowExtinctStuff);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowClanPurchases", allowClanPurchases);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowISPurchases", allowISPurchases);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowCanonOnly", allowCanonOnly);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowCanonRefitOnly", allowCanonRefitOnly);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "variableTechLevel", variableTechLevel);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "factionIntroDate", factionIntroDate);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useAmmoByType", useAmmoByType);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "waitingPeriod", waitingPeriod);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquisitionSkill", acquisitionSkill);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquisitionSupportStaffOnly", acquisitionSupportStaffOnly);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "techLevel", techLevel);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "nDiceTransitTime", nDiceTransitTime);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "constantTransitTime", constantTransitTime);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "unitTransitTime", unitTransitTime);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquireMosBonus", acquireMosBonus);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquireMosUnit", acquireMosUnit);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquireMinimumTime", acquireMinimumTime);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "acquireMinimumTimeUnit", acquireMinimumTimeUnit);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "usePlanetaryAcquisition", usePlanetaryAcquisition);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionFactionLimit", getPlanetAcquisitionFactionLimit().name());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionNoClanCrossover", planetAcquisitionNoClanCrossover);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "noClanPartsFromIS", noClanPartsFromIS);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "penaltyClanPartsFromIS", penaltyClanPartsFromIS);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "planetAcquisitionVerbose", planetAcquisitionVerbose);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maxJumpsPlanetaryAcquisition", maxJumpsPlanetaryAcquisition);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractPercent", equipmentContractPercent);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "dropshipContractPercent", dropshipContractPercent);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "jumpshipContractPercent", jumpshipContractPercent);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "warshipContractPercent", warshipContractPercent);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractBase", equipmentContractBase);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "equipmentContractSaleValue", equipmentContractSaleValue);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "blcSaleValue", blcSaleValue);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "overageRepaymentInFinalPayment", overageRepaymentInFinalPayment);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "clanAcquisitionPenalty", clanAcquisitionPenalty);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "isAcquisitionPenalty", isAcquisitionPenalty);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "destroyByMargin", destroyByMargin);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "destroyMargin", destroyMargin);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "destroyPartTarget", destroyPartTarget);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useAeroSystemHits", useAeroSystemHits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maintenanceCycleDays", maintenanceCycleDays);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maintenanceBonus", maintenanceBonus);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useQualityMaintenance", useQualityMaintenance);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "reverseQualityNames", reverseQualityNames);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useUnofficalMaintenance", useUnofficialMaintenance);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "checkMaintenance", checkMaintenance);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "maxAcquisitions", maxAcquisitions);

        //region Personnel Tab
        //region General Personnel
        MekHqXmlUtil.writeSimpleXMLTag(pw1, ++indent, "useTactics", useTactics());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useInitiativeBonus", useInitiativeBonus());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useToughness", useToughness());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useArtillery", useArtillery());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useAbilities", useAbilities());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useEdge", useEdge());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useSupportEdge", useSupportEdge());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useImplants", useImplants());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "alternativeQualityAveraging", useAlternativeQualityAveraging());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useTransfers", useTransfers());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelLogSkillGain", isPersonnelLogSkillGain());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelLogAbilityGain", isPersonnelLogAbilityGain());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelLogEdgeGain", isPersonnelLogEdgeGain());
        //endregion General Personnel

        //region Expanded Personnel Information
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useTimeInService", getUseTimeInService());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "timeInServiceDisplayFormat", getTimeInServiceDisplayFormat().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useTimeInRank", getUseTimeInRank());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "timeInRankDisplayFormat", getTimeInRankDisplayFormat().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRetirementDateTracking", useRetirementDateTracking());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "trackTotalEarnings", isTrackTotalEarnings());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "trackTotalXPEarnings", isTrackTotalXPEarnings());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "showOriginFaction", showOriginFaction());
        //endregion Expanded Personnel Information

        //region Medical
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useAdvancedMedical", useAdvancedMedical());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "healWaitingPeriod", getHealingWaitingPeriod());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "naturalHealingWaitingPeriod", getNaturalHealingWaitingPeriod());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "minimumHitsForVehicles", getMinimumHitsForVehicles());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomHitsForVehicles", useRandomHitsForVehicles());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "tougherHealing", useTougherHealing());
        //endregion Medical

        //region Prisoners
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "prisonerCaptureStyle", getPrisonerCaptureStyle().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "defaultPrisonerStatus", getDefaultPrisonerStatus().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "prisonerBabyStatus", getPrisonerBabyStatus());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useAtBPrisonerDefection", useAtBPrisonerDefection());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useAtBPrisonerRansom", useAtBPrisonerRansom());
        //endregion Prisoners

        //region Personnel Randomization
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useDylansRandomXP", useDylansRandomXP());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "randomizeOrigin", randomizeOrigin());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "randomizeDependentOrigin", getRandomizeDependentOrigin());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "originSearchRadius", getOriginSearchRadius());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "extraRandomOrigin", extraRandomOrigin());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "originDistanceScale", getOriginDistanceScale());
        //endregion Personnel Randomization

        //region Family
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "displayFamilyLevel", getDisplayFamilyLevel().name());
        //endregion Family

        //region Salary
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salaryCommissionMultiplier", getSalaryCommissionMultiplier());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salaryEnlistedMultiplier", getSalaryEnlistedMultiplier());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salaryAntiMekMultiplier", getSalaryAntiMekMultiplier());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salarySpecialistInfantryMultiplier", getSalarySpecialistInfantryMultiplier());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salaryXPMultiplier", getSalaryXPMultipliers());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "salaryTypeBase", Utilities.printMoneyArray(getRoleBaseSalaries()));
        //endregion Salary

        //region Marriage
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useManualMarriages", useManualMarriages());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "minimumMarriageAge", getMinimumMarriageAge());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "checkMutualAncestorsDepth", checkMutualAncestorsDepth());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "logMarriageNameChange", logMarriageNameChange());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "randomMarriageSurnameWeights", getMarriageSurnameWeights());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomMarriages", useRandomMarriages());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "chanceRandomMarriages", getChanceRandomMarriages());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "marriageAgeRange", getMarriageAgeRange());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomSameSexMarriages", useRandomSameSexMarriages());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "chanceRandomSameSexMarriages", getChanceRandomSameSexMarriages());
        //endregion Marriage

        //region Divorce
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useManualDivorce", isUseManualDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useClannerDivorce", isUseClannerDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "usePrisonerDivorce", isUsePrisonerDivorce());
        MekHqXmlUtil.writeSimpleXMLOpenTag(pw1, indent++, "divorceSurnameWeights");
        for (final Map.Entry<SplittingSurnameStyle, Integer> entry : getDivorceSurnameWeights().entrySet()) {
            MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, entry.getKey().name(), entry.getValue());
        }
        MekHqXmlUtil.writeSimpleXMLCloseTag(pw1, --indent, "divorceSurnameWeights");
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "randomDivorceMethod", getRandomDivorceMethod().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomOppositeSexDivorce", isUseRandomOppositeSexDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomSameSexDivorce", isUseRandomSameSexDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomClannerDivorce", isUseRandomClannerDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useRandomPrisonerDivorce", isUseRandomPrisonerDivorce());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "percentageRandomDivorceOppositeSexChance", getPercentageRandomDivorceOppositeSexChance());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "percentageRandomDivorceSameSexChance", getPercentageRandomDivorceSameSexChance());
        //endregion Divorce

        //region Procreation
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useProcreation", useProcreation());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "chanceProcreation", getChanceProcreation());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "useProcreationNoRelationship", useProcreationNoRelationship());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "chanceProcreationNoRelationship", getChanceProcreationNoRelationship());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "displayTrueDueDate", getDisplayTrueDueDate());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "logConception", logConception());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "babySurnameStyle", getBabySurnameStyle().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "determineFatherAtBirth", determineFatherAtBirth());
        //endregion Procreation

        //region Death
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "keepMarriedNameUponSpouseDeath", getKeepMarriedNameUponSpouseDeath());
        //endregion Death
        //endregion Personnel Tab

        //region Finances Tab
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForParts", payForParts);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForRepairs", payForRepairs);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForUnits", payForUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForSalaries", payForSalaries);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForOverhead", payForOverhead);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForMaintain", payForMaintain);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForTransport", payForTransport);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "sellUnits", sellUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "sellParts", sellParts);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "payForRecruitment", payForRecruitment);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "useLoanLimits", useLoanLimits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "usePercentageMaint", usePercentageMaint);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "infantryDontCount", infantryDontCount);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "usePeacetimeCost", usePeacetimeCost);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "useExtendedPartsModifier", useExtendedPartsModifier);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "showPeacetimeCost", showPeacetimeCost);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "financialYearDuration", financialYearDuration.name());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "newFinancialYearFinancesToCSVExport", newFinancialYearFinancesToCSVExport);

        //region Price Multipliers
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "commonPartPriceMultiplier", getCommonPartPriceMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "innerSphereUnitPriceMultiplier", getInnerSphereUnitPriceMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "innerSpherePartPriceMultiplier", getInnerSpherePartPriceMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "clanUnitPriceMultiplier", getClanUnitPriceMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "clanPartPriceMultiplier", getClanPartPriceMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "mixedTechUnitPriceMultiplier", getMixedTechUnitPriceMultiplier());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "usedPartPriceMultipliers", getUsedPartPriceMultipliers());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "damagedPartsValueMultiplier", getDamagedPartsValueMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "unrepairablePartsValueMultiplier", getUnrepairablePartsValueMultiplier());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "cancelledOrderRefundMultiplier", getCancelledOrderRefundMultiplier());
        //endregion Price Multipliers
        //endregion Finances Tab

        //region Markets Tab
        //region Personnel Market
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketName", getPersonnelMarketType());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketReportRefresh", getPersonnelMarketReportRefresh());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomEliteRemoval", getPersonnelMarketRandomEliteRemoval());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomVeteranRemoval", getPersonnelMarketRandomVeteranRemoval());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomRegularRemoval", getPersonnelMarketRandomRegularRemoval());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomGreenRemoval", getPersonnelMarketRandomGreenRemoval());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketRandomUltraGreenRemoval", getPersonnelMarketRandomUltraGreenRemoval());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "personnelMarketDylansWeight", getPersonnelMarketDylansWeight());
        //endregion Personnel Market

        //region Unit Market
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "unitMarketMethod", getUnitMarketMethod().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "unitMarketRegionalMechVariations", useUnitMarketRegionalMechVariations());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "instantUnitMarketDelivery", getInstantUnitMarketDelivery());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "unitMarketReportRefresh", getUnitMarketReportRefresh());
        //endregion Unit Market

        //region Contract Market
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent, "contractMarketMethod", getContractMarketMethod().name());
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent--, "contractMarketReportRefresh", getContractMarketReportRefresh());
        //endregion Contract Market
        //endregion Markets Tab

        pw1.println(MekHqXmlUtil.indentStr(indent + 1)
                + "<phenotypeProbabilities>"
                + StringUtils.join(phenotypeProbabilities, ',')
                + "</phenotypeProbabilities>");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useAtB", useAtB);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useStratCon", useStratCon);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useAero", useAero);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useVehicles", useVehicles);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "clanVehicles", clanVehicles);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "doubleVehicles", doubleVehicles);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "adjustPlayerVehicles", adjustPlayerVehicles);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeMechs", opforLanceTypeMechs);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeMixed", opforLanceTypeMixed);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforLanceTypeVehicles", opforLanceTypeVehicles);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforUsesVTOLs", opforUsesVTOLs);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useDropShips", useDropShips);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "skillLevel", skillLevel);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "aeroRecruitsHaveUnits", aeroRecruitsHaveUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useShareSystem", useShareSystem);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "sharesExcludeLargeCraft", sharesExcludeLargeCraft);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "sharesForAll", sharesForAll);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "retirementRolls", retirementRolls);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "customRetirementMods", customRetirementMods);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "foundersNeverRetire", foundersNeverRetire);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "atbAddDependents", atbAddDependents);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "dependentsNeverLeave", dependentsNeverLeave);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "trackUnitFatigue", trackUnitFatigue);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "mercSizeLimited", mercSizeLimited);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "trackOriginalUnit", trackOriginalUnit);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "regionalMechVariations", regionalMechVariations);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "attachedPlayerCamouflage", attachedPlayerCamouflage);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "playerControlsAttachedUnits", playerControlsAttachedUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "searchRadius", searchRadius);
        pw1.println(MekHqXmlUtil.indentStr(indent + 1)
                + "<atbBattleChance>"
                + StringUtils.join(atbBattleChance, ',')
                + "</atbBattleChance>");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "generateChases", generateChases);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "variableContractLength", variableContractLength);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useWeatherConditions", useWeatherConditions);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useLightConditions", useLightConditions);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "usePlanetaryConditions", usePlanetaryConditions);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useLeadership", useLeadership);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "useStrategy", useStrategy);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "baseStrategyDeployment", baseStrategyDeployment);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "additionalStrategyDeployment", additionalStrategyDeployment);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "adjustPaymentForStrategy", adjustPaymentForStrategy);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "restrictPartsByMission", restrictPartsByMission);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "limitLanceWeight", limitLanceWeight);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "limitLanceNumUnits", limitLanceNumUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "assignPortraitOnRoleChange", assignPortraitOnRoleChange);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowOpforAeros", allowOpforAeros);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "allowOpforLocalUnits", allowOpforLocalUnits);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforAeroChance", opforAeroChance);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "opforLocalUnitChance", opforLocalUnitChance);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "fixedMapChance", fixedMapChance);

        //Mass Repair/Salvage Options
        MekHqXmlUtil.writeSimpleXmlTag(pw1, ++indent, "massRepairUseRepair", massRepairUseRepair());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairUseSalvage", massRepairUseSalvage());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairUseExtraTime", massRepairUseExtraTime);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairUseRushJob", massRepairUseRushJob);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairAllowCarryover", massRepairAllowCarryover);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairOptimizeToCompleteToday", massRepairOptimizeToCompleteToday);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairScrapImpossible", massRepairScrapImpossible);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairUseAssignedTechsFirst", massRepairUseAssignedTechsFirst);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "massRepairReplacePod", massRepairReplacePod);

        MekHqXmlUtil.writeSimpleXMLOpenIndentedLine(pw1, indent++, "massRepairOptions");
        for (MassRepairOption massRepairOption : massRepairOptions) {
            massRepairOption.writeToXML(pw1, indent);
        }
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, --indent, "massRepairOptions");

        pw1.println(MekHqXmlUtil.indentStr(indent)
                + "<planetTechAcquisitionBonus>"
                + StringUtils.join(planetTechAcquisitionBonus, ',')
                + "</planetTechAcquisitionBonus>");
        pw1.println(MekHqXmlUtil.indentStr(indent)
                + "<planetIndustryAcquisitionBonus>"
                + StringUtils.join(planetIndustryAcquisitionBonus, ',')
                + "</planetIndustryAcquisitionBonus>");
        pw1.println(MekHqXmlUtil.indentStr(indent)
                + "<planetOutputAcquisitionBonus>"
                + StringUtils.join(planetOutputAcquisitionBonus, ',')
                + "</planetOutputAcquisitionBonus>");


        // cannot use StringUtils.join for a boolean array, so we are using this instead
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < usePortraitForRoles().length; i++) {
            csv.append(usePortraitForRoles()[i]);
            if (i < usePortraitForRoles().length - 1) {
                csv.append(",");
            }
        }

        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "usePortraitForType", csv.toString());

        //region AtB Options
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent, "rats", StringUtils.join(rats, ','));
        if (staticRATs) {
            pw1.println(MekHqXmlUtil.indentStr(indent) + "<staticRATs/>");
        }

        if (ignoreRatEra) {
            pw1.println(MekHqXmlUtil.indentStr(indent) + "<ignoreRatEra/>");
        }
        //endregion AtB Options
        MekHqXmlUtil.writeSimpleXMLCloseIndentedLine(pw1, --indent, "campaignOptions");
    }

    public static CampaignOptions generateCampaignOptionsFromXml(Node wn, Version version) {
        MekHQ.getLogger().info("Loading Campaign Options from Version " + version + " XML...");

        wn.normalize();
        CampaignOptions retVal = new CampaignOptions();
        NodeList wList = wn.getChildNodes();

        // Okay, lets iterate through the children, eh?
        for (int x = 0; x < wList.getLength(); x++) {
            Node wn2 = wList.item(x);

            // If it's not an element node, we ignore it.
            if (wn2.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            MekHQ.getLogger().debug(String.format("%s\n\t%s", wn2.getNodeName(), wn2.getTextContent()));
            try {
                //region Repair and Maintenance Tab
                if (wn2.getNodeName().equalsIgnoreCase("checkMaintenance")) {
                    retVal.checkMaintenance = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maintenanceCycleDays")) {
                    retVal.maintenanceCycleDays = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maintenanceBonus")) {
                    retVal.maintenanceBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useQualityMaintenance")) {
                    retVal.useQualityMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("reverseQualityNames")) {
                    retVal.reverseQualityNames = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficalMaintenance")) {
                    retVal.useUnofficialMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("logMaintenance")) {
                    retVal.logMaintenance = Boolean.parseBoolean(wn2.getTextContent());
                //endregion Repair and Maintenance Tab

                } else if (wn2.getNodeName().equalsIgnoreCase("useFactionForNames")) {
                    retVal.setUseOriginFactionForNames(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useEraMods")) {
                    retVal.useEraMods = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("assignedTechFirst")) {
                    retVal.assignedTechFirst = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("resetToFirstTech")) {
                    retVal.resetToFirstTech = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useQuirks")) {
                    retVal.useQuirks = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("scenarioXP")) {
                    retVal.scenarioXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("killsForXP")) {
                    retVal.killsForXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("killXPAward")) {
                    retVal.killXPAward = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("nTasksXP")) {
                    retVal.nTasksXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("tasksXP")) {
                    retVal.tasksXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("successXP")) {
                    retVal.successXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("mistakeXP")) {
                    retVal.mistakeXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("idleXP")) {
                    retVal.idleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("targetIdleXP")) {
                    retVal.targetIdleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("monthsIdleXP")) {
                    retVal.monthsIdleXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("contractNegotiationXP")) {
                    retVal.contractNegotiationXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adminWeeklyXP")) {
                    retVal.adminXP = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adminXPPeriod")) {
                    retVal.adminXPPeriod = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("edgeCost")) {
                    retVal.edgeCost = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("waitingPeriod")) {
                    retVal.waitingPeriod = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquisitionSkill")) {
                    retVal.acquisitionSkill = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("nDiceTransitTime")) {
                    retVal.nDiceTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("constantTransitTime")) {
                    retVal.constantTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("unitTransitTime")) {
                    retVal.unitTransitTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMosBonus")) {
                    retVal.acquireMosBonus = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMosUnit")) {
                    retVal.acquireMosUnit = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMinimumTime")) {
                    retVal.acquireMinimumTime = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("acquireMinimumTimeUnit")) {
                    retVal.acquireMinimumTimeUnit = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("clanAcquisitionPenalty")) {
                    retVal.clanAcquisitionPenalty = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("isAcquisitionPenalty")) {
                    retVal.isAcquisitionPenalty = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePlanetaryAcquisition")) {
                    retVal.usePlanetaryAcquisition = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionFactionLimit")) {
                    retVal.setPlanetAcquisitionFactionLimit(PlanetaryAcquisitionFactionLimit.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionNoClanCrossover")) {
                    retVal.planetAcquisitionNoClanCrossover = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("noClanPartsFromIS")) {
                    retVal.noClanPartsFromIS = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("penaltyClanPartsFromIS")) {
                    retVal.penaltyClanPartsFromIS = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetAcquisitionVerbose")) {
                    retVal.planetAcquisitionVerbose = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maxJumpsPlanetaryAcquisition")) {
                    retVal.maxJumpsPlanetaryAcquisition = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("planetTechAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetTechAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("planetIndustryAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetIndustryAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("planetOutputAcquisitionBonus")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.planetOutputAcquisitionBonus[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractPercent")) {
                    retVal.setEquipmentContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("dropshipContractPercent")) {
                    retVal.setDropshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("jumpshipContractPercent")) {
                    retVal.setJumpshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("warshipContractPercent")) {
                    retVal.setWarshipContractPercent(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractBase")) {
                    retVal.equipmentContractBase = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("equipmentContractSaleValue")) {
                    retVal.equipmentContractSaleValue = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("blcSaleValue")) {
                    retVal.blcSaleValue = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("overageRepaymentInFinalPayment")) {
                    retVal.setOverageRepaymentInFinalPayment(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("acquisitionSupportStaffOnly")) {
                    retVal.acquisitionSupportStaffOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitByYear")) {
                    retVal.limitByYear = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("disallowExtinctStuff")) {
                    retVal.disallowExtinctStuff = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowClanPurchases")) {
                    retVal.allowClanPurchases = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowISPurchases")) {
                    retVal.allowISPurchases = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowCanonOnly")) {
                    retVal.allowCanonOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowCanonRefitOnly")) {
                    retVal.allowCanonRefitOnly = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAmmoByType")) {
                    retVal.useAmmoByType = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("variableTechLevel")) {
                    retVal.variableTechLevel = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("factionIntroDate")) {
                    retVal.factionIntroDate = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("techLevel")) {
                    retVal.techLevel = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("unitRatingMethod")
                        || wn2.getNodeName().equalsIgnoreCase("dragoonsRatingMethod")) {
                    retVal.setUnitRatingMethod(UnitRatingMethod.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("manualUnitRatingModifier")) {
                    retVal.setManualUnitRatingModifier(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePortraitForType")) {
                    String[] values = wn2.getTextContent().split(",");
                    if (version.isLowerThan("0.49.0")) {
                        for (int i = 0; i < values.length; i++) {
                            retVal.setUsePortraitForRole(PersonnelRole.parseFromString(String.valueOf(i)).ordinal(),
                                    Boolean.parseBoolean(values[i].trim()));
                        }
                    } else {
                        for (int i = 0; i < values.length; i++) {
                            retVal.setUsePortraitForRole(i, Boolean.parseBoolean(values[i].trim()));
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("assignPortraitOnRoleChange")) {
                    retVal.assignPortraitOnRoleChange = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyByMargin")) {
                    retVal.destroyByMargin = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyMargin")) {
                    retVal.destroyMargin = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("destroyPartTarget")) {
                    retVal.destroyPartTarget = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAeroSystemHits")) {
                    retVal.useAeroSystemHits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("maxAcquisitions")) {
                    retVal.maxAcquisitions = Integer.parseInt(wn2.getTextContent().trim());

                //region Personnel Tab
                //region General Personnel
                } else if (wn2.getNodeName().equalsIgnoreCase("useTactics")) {
                    retVal.setUseTactics(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useInitBonus") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("useInitiativeBonus")) {
                    retVal.setUseInitiativeBonus(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useToughness")) {
                    retVal.setUseToughness(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useArtillery")) {
                    retVal.setUseArtillery(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAbilities")) {
                    retVal.setUseAbilities(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useEdge")) {
                    retVal.setUseEdge(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useSupportEdge")) {
                    retVal.setUseSupportEdge(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useImplants")) {
                    retVal.setUseImplants(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("altQualityAveraging") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("alternativeQualityAveraging")) {
                    retVal.setAlternativeQualityAveraging(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useTransfers")) {
                    retVal.setUseTransfers(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogSkillGain")) {
                    retVal.setPersonnelLogSkillGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogAbilityGain")) {
                    retVal.setPersonnelLogAbilityGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelLogEdgeGain")) {
                    retVal.setPersonnelLogEdgeGain(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion General Personnel

                //region Expanded Personnel Information
                } else if (wn2.getNodeName().equalsIgnoreCase("useTimeInService")) {
                    retVal.setUseTimeInService(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("timeInServiceDisplayFormat")) {
                    retVal.setTimeInServiceDisplayFormat(TimeInDisplayFormat.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useTimeInRank")) {
                    retVal.setUseTimeInRank(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("timeInRankDisplayFormat")) {
                    retVal.setTimeInRankDisplayFormat(TimeInDisplayFormat.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRetirementDateTracking")) {
                    retVal.setUseRetirementDateTracking(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("trackTotalEarnings")) {
                    retVal.setTrackTotalEarnings(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("trackTotalXPEarnings")) {
                    retVal.setTrackTotalXPEarnings(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("showOriginFaction")) {
                    retVal.setShowOriginFaction(Boolean.parseBoolean(wn2.getTextContent()));
                //endregion Expanded Personnel Information

                //region Medical
                } else if (wn2.getNodeName().equalsIgnoreCase("useAdvancedMedical")) {
                    retVal.setUseAdvancedMedical(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("healWaitingPeriod")) {
                    retVal.setHealingWaitingPeriod(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("naturalHealingWaitingPeriod")) {
                    retVal.setNaturalHealingWaitingPeriod(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("minimumHitsForVees") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("minimumHitsForVehicles")) {
                    retVal.setMinimumHitsForVehicles(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomHitsForVees") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("useRandomHitsForVehicles")) {
                    retVal.setUseRandomHitsForVehicles(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("tougherHealing")) {
                    retVal.setTougherHealing(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Medical

                //region Prisoners
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerCaptureStyle")) {
                    retVal.setPrisonerCaptureStyle(PrisonerCaptureStyle.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("defaultPrisonerStatus")) {
                    // Most of this is legacy - 0.47.X removal
                    String prisonerStatus = wn2.getTextContent().trim();

                    try {
                        prisonerStatus = String.valueOf(Integer.parseInt(prisonerStatus) + 1);
                    } catch (Exception ignored) {

                    }

                    retVal.setDefaultPrisonerStatus(PrisonerStatus.parseFromString(prisonerStatus));
                } else if (wn2.getNodeName().equalsIgnoreCase("prisonerBabyStatus")) {
                    retVal.setPrisonerBabyStatus(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBPrisonerDefection")) {
                    retVal.setUseAtBPrisonerDefection(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBPrisonerRansom")) {
                    retVal.setUseAtBPrisonerRansom(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Prisoners

                //region Personnel Randomization
                } else if (wn2.getNodeName().equalsIgnoreCase("useDylansRandomXP")) {
                    retVal.setUseDylansRandomXP(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomizeOrigin")) {
                    retVal.setRandomizeOrigin(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomizeDependentOrigin")) {
                    retVal.setRandomizeDependentOrigin(Boolean.parseBoolean(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("originSearchRadius")) {
                    retVal.setOriginSearchRadius(Integer.parseInt(wn2.getTextContent()));
                } else if (wn2.getNodeName().equalsIgnoreCase("extraRandomOrigin")) {
                    retVal.setExtraRandomOrigin(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("originDistanceScale")) {
                    retVal.setOriginDistanceScale(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Personnel Randomization

                //region Family
                } else if (wn2.getNodeName().equalsIgnoreCase("displayFamilyLevel")) {
                    retVal.setDisplayFamilyLevel(FamilialRelationshipDisplayLevel.parseFromString(wn2.getTextContent().trim()));
                //endregion Family

                //region Salary
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryCommissionMultiplier")) {
                    retVal.setSalaryCommissionMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryEnlistedMultiplier")) {
                    retVal.setSalaryEnlistedMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryAntiMekMultiplier")) {
                    retVal.setSalaryAntiMekMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salarySpecialistInfantryMultiplier")) {
                    retVal.setSalarySpecialistInfantryMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryXPMultiplier")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.setSalaryXPMultiplier(i, Double.parseDouble(values[i]));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("salaryTypeBase")) {
                    if (version.isLowerThan("0.49.0")) {
                        Money[] roleBaseSalaries = Utilities.readMoneyArray(wn2);
                        for (int i = 0; i < roleBaseSalaries.length; i++) {
                            retVal.setRoleBaseSalary(PersonnelRole.parseFromString(String.valueOf(i)), roleBaseSalaries[i]);
                        }
                    } else {
                        retVal.setRoleBaseSalaries(Utilities.readMoneyArray(wn2, retVal.getRoleBaseSalaries().length));
                    }
                //endregion Salary

                //region Marriage
                } else if (wn2.getNodeName().equalsIgnoreCase("useManualMarriages")) {
                    retVal.setUseManualMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("minimumMarriageAge")) {
                    retVal.setMinimumMarriageAge(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("checkMutualAncestorsDepth")) {
                    retVal.setCheckMutualAncestorsDepth(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("logMarriageNameChange")) {
                    retVal.setLogMarriageNameChange(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("randomMarriageSurnameWeights")) {
                    String[] values = wn2.getTextContent().split(",");
                    if (values.length == 13) {
                        for (int i = 0; i < values.length; i++) {
                            retVal.marriageSurnameWeights[i] = Integer.parseInt(values[i]);
                        }
                    } else if (values.length == 9) {
                        migrateMarriageSurnameWeights(retVal, values);
                    } else {
                        MekHQ.getLogger().error("Unknown length of randomMarriageSurnameWeights");
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomMarriages")) {
                    retVal.setUseRandomMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceRandomMarriages")) {
                    retVal.setChanceRandomMarriages(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("marriageAgeRange")) {
                    retVal.setMarriageAgeRange(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomSameSexMarriages")) {
                    retVal.setUseRandomSameSexMarriages(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceRandomSameSexMarriages")) {
                    retVal.setChanceRandomSameSexMarriages(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Marriage

                //region Divorce
                } else if (wn2.getNodeName().equalsIgnoreCase("useManualDivorce")) {
                    retVal.setUseManualDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useClannerDivorce")) {
                    retVal.setUseClannerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usePrisonerDivorce")) {
                    retVal.setUsePrisonerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("divorceSurnameWeights")) {
                    if (!wn2.hasChildNodes()) {
                        continue;
                    }
                    final NodeList nl2 = wn2.getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        final Node wn3 = nl2.item(j);
                        if (wn3.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        retVal.getDivorceSurnameWeights().put(
                                SplittingSurnameStyle.valueOf(wn3.getNodeName().trim()),
                                Integer.parseInt(wn3.getTextContent().trim()));
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("randomDivorceMethod")) {
                    retVal.setRandomDivorceMethod(RandomDivorceMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomOppositeSexDivorce")) {
                    retVal.setUseRandomOppositeSexDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomSameSexDivorce")) {
                    retVal.setUseRandomSameSexDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomClannerDivorce")) {
                    retVal.setUseRandomClannerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useRandomPrisonerDivorce")) {
                    retVal.setUseRandomPrisonerDivorce(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomDivorceOppositeSexChance")) {
                    retVal.setPercentageRandomDivorceOppositeSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("percentageRandomDivorceSameSexChance")) {
                    retVal.setPercentageRandomDivorceSameSexChance(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Divorce

                //region Procreation
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficialProcreation") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("useProcreation")) {
                    retVal.setUseProcreation(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceProcreation")) {
                    retVal.setChanceProcreation(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnofficialProcreationNoRelationship") // Legacy - 0.49.1 removal
                        || wn2.getNodeName().equalsIgnoreCase("useProcreationNoRelationship")) {
                    retVal.setUseProcreationNoRelationship(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("chanceProcreationNoRelationship")) {
                    retVal.setChanceProcreationNoRelationship(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("displayTrueDueDate")) {
                    retVal.setDisplayTrueDueDate(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("logConception")) {
                    retVal.setLogConception(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("babySurnameStyle")) {
                    retVal.setBabySurnameStyle(BabySurnameStyle.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("determineFatherAtBirth")) {
                    retVal.setDetermineFatherAtBirth(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Procreation

                //region Death
                } else if (wn2.getNodeName().equalsIgnoreCase("keepMarriedNameUponSpouseDeath")) {
                    retVal.setKeepMarriedNameUponSpouseDeath(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Death
                //endregion Personnel Tab

                //region Finances Tab
                } else if (wn2.getNodeName().equalsIgnoreCase("payForParts")) {
                    retVal.payForParts = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForRepairs")) {
                    retVal.payForRepairs = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForUnits")) {
                    retVal.payForUnits = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForSalaries")) {
                    retVal.payForSalaries = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForOverhead")) {
                    retVal.payForOverhead = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForMaintain")) {
                    retVal.payForMaintain = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForTransport")) {
                    retVal.payForTransport = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("sellUnits")) {
                    retVal.sellUnits = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("sellParts")) {
                    retVal.sellParts = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("payForRecruitment")) {
                    retVal.payForRecruitment = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLoanLimits")) {
                    retVal.useLoanLimits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePercentageMaint")) {
                    retVal.usePercentageMaint = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("infantryDontCount")) {
                    retVal.infantryDontCount = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePeacetimeCost")) {
                    retVal.usePeacetimeCost = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("useExtendedPartsModifier")) {
                    retVal.useExtendedPartsModifier = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("showPeacetimeCost")) {
                    retVal.showPeacetimeCost = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("financialYearDuration")) {
                    retVal.setFinancialYearDuration(FinancialYearDuration.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("newFinancialYearFinancesToCSVExport")) {
                    retVal.newFinancialYearFinancesToCSVExport = Boolean.parseBoolean(wn2.getTextContent().trim());

                //region Price Multipliers
                } else if (wn2.getNodeName().equalsIgnoreCase("commonPartPriceMultiplier")) {
                    retVal.setCommonPartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("innerSphereUnitPriceMultiplier")) {
                    retVal.setInnerSphereUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("innerSpherePartPriceMultiplier")) {
                    retVal.setInnerSpherePartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("clanUnitPriceMultiplier")) {
                    retVal.setClanUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("clanPartPriceMultiplier")) {
                    retVal.setClanPartPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("mixedTechUnitPriceMultiplier")) {
                    retVal.setMixedTechUnitPriceMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartPriceMultipliers")) {
                    final String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        try {
                            retVal.getUsedPartPriceMultipliers()[i] = Double.parseDouble(values[i]);
                        } catch (Exception ignored) {

                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("damagedPartsValueMultiplier")) {
                    retVal.setDamagedPartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unrepairablePartsValueMultiplier")) {
                    retVal.setUnrepairablePartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("cancelledOrderRefundMultiplier")) {
                    retVal.setCancelledOrderRefundMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Price Multipliers
                //endregion Finances Tab

                //region Markets
                //region Personnel Market
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketType")) { // Legacy - pre-0.48
                    retVal.setPersonnelMarketType(PersonnelMarket.getTypeName(Integer.parseInt(wn2.getTextContent().trim())));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketName")) {
                    retVal.setPersonnelMarketType(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketReportRefresh")) {
                    retVal.setPersonnelMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomEliteRemoval")) {
                    retVal.setPersonnelMarketRandomEliteRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomVeteranRemoval")) {
                    retVal.setPersonnelMarketRandomVeteranRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomRegularRemoval")) {
                    retVal.setPersonnelMarketRandomRegularRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomGreenRemoval")) {
                    retVal.setPersonnelMarketRandomGreenRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketRandomUltraGreenRemoval")) {
                    retVal.setPersonnelMarketRandomUltraGreenRemoval(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketDylansWeight")) {
                    retVal.setPersonnelMarketDylansWeight(Double.parseDouble(wn2.getTextContent().trim()));
                //endregion Personnel Market

                //region Unit Market
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketMethod")) {
                    retVal.setUnitMarketMethod(UnitMarketMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketRegionalMechVariations")) {
                    retVal.setUnitMarketRegionalMechVariations(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("instantUnitMarketDelivery")) {
                    retVal.setInstantUnitMarketDelivery(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("unitMarketReportRefresh")) {
                    retVal.setUnitMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Unit Market

                //region Contract Market
                } else if (wn2.getNodeName().equalsIgnoreCase("contractMarketMethod")) {
                    retVal.setContractMarketMethod(ContractMarketMethod.valueOf(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("contractMarketReportRefresh")) {
                    retVal.setContractMarketReportRefresh(Boolean.parseBoolean(wn2.getTextContent().trim()));
                //endregion Contract Market
                // endregion Markets

                } else if (wn2.getNodeName().equalsIgnoreCase("phenotypeProbabilities")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        retVal.phenotypeProbabilities[i] = Integer.parseInt(values[i]);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtB")) {
                    retVal.useAtB = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useStratCon")) {
                    retVal.useStratCon = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useAero")) {
                    retVal.useAero = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useVehicles")) {
                    retVal.useVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("clanVehicles")) {
                    retVal.clanVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("doubleVehicles")) {
                    retVal.doubleVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adjustPlayerVehicles")) {
                    retVal.adjustPlayerVehicles = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeMechs")) {
                    retVal.opforLanceTypeMechs = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeMixed")) {
                    retVal.opforLanceTypeMixed = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLanceTypeVehicles")) {
                    retVal.opforLanceTypeVehicles = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforUsesVTOLs")) {
                    retVal.opforUsesVTOLs = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useDropShips")) {
                    retVal.useDropShips = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("skillLevel")) {
                    retVal.skillLevel = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("aeroRecruitsHaveUnits")) {
                    retVal.aeroRecruitsHaveUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useShareSystem")) {
                    retVal.useShareSystem = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("sharesExcludeLargeCraft")) {
                    retVal.sharesExcludeLargeCraft = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("sharesForAll")) {
                    retVal.sharesForAll = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("retirementRolls")) {
                    retVal.retirementRolls = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("customRetirementMods")) {
                    retVal.customRetirementMods = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("foundersNeverRetire")) {
                    retVal.foundersNeverRetire = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("atbAddDependents")) {
                    retVal.atbAddDependents = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("dependentsNeverLeave")) {
                    retVal.dependentsNeverLeave = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("trackUnitFatigue")) {
                    retVal.trackUnitFatigue = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("trackOriginalUnit")) {
                    retVal.trackOriginalUnit = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("mercSizeLimited")) {
                    retVal.mercSizeLimited = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("regionalMechVariations")) {
                    retVal.regionalMechVariations = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("attachedPlayerCamouflage")) {
                    retVal.attachedPlayerCamouflage = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("playerControlsAttachedUnits")) {
                    retVal.setPlayerControlsAttachedUnits(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("searchRadius")) {
                    retVal.searchRadius = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("atbBattleChance")) {
                    String[] values = wn2.getTextContent().split(",");
                    for (int i = 0; i < values.length; i++) {
                        try {
                            retVal.atbBattleChance[i] = Integer.parseInt(values[i]);
                        } catch (Exception ignored) {
                            // Badly coded, but this is to migrate devs and their games as the swap was
                            // done before a release and is thus better to handle this way than through
                            // a more code complex method
                            retVal.atbBattleChance[i] = (int) Math.round(Double.parseDouble(values[i]));
                        }
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("generateChases")) {
                    retVal.setGenerateChases(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("variableContractLength")) {
                    retVal.variableContractLength = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useWeatherConditions")) {
                    retVal.useWeatherConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLightConditions")) {
                    retVal.useLightConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usePlanetaryConditions")) {
                    retVal.usePlanetaryConditions = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useLeadership")) {
                    retVal.useLeadership = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("useStrategy")) {
                    retVal.useStrategy = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("baseStrategyDeployment")) {
                    retVal.baseStrategyDeployment = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("additionalStrategyDeployment")) {
                    retVal.additionalStrategyDeployment = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("adjustPaymentForStrategy")) {
                    retVal.adjustPaymentForStrategy = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("restrictPartsByMission")) {
                    retVal.restrictPartsByMission = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitLanceWeight")) {
                    retVal.limitLanceWeight = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("limitLanceNumUnits")) {
                    retVal.limitLanceNumUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowOpforLocalUnits")) {
                    retVal.allowOpforLocalUnits = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("allowOpforAeros")) {
                    retVal.allowOpforAeros = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforAeroChance")) {
                    retVal.opforAeroChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("opforLocalUnitChance")) {
                    retVal.opforLocalUnitChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("fixedMapChance")) {
                    retVal.fixedMapChance = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("rats")) {
                    retVal.rats = MekHqXmlUtil.unEscape(wn2.getTextContent().trim()).split(",");
                } else if (wn2.getNodeName().equalsIgnoreCase("staticRATs")) {
                    retVal.staticRATs = true;
                } else if (wn2.getNodeName().equalsIgnoreCase("ignoreRatEra")) {
                    retVal.ignoreRatEra = true;
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseRepair")) {
                    retVal.setMassRepairUseRepair(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseSalvage")) {
                    retVal.setMassRepairUseSalvage(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseExtraTime")) {
                    retVal.massRepairUseExtraTime = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseRushJob")) {
                    retVal.massRepairUseRushJob = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairAllowCarryover")) {
                    retVal.massRepairAllowCarryover = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairOptimizeToCompleteToday")) {
                    retVal.massRepairOptimizeToCompleteToday = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairScrapImpossible")) {
                    retVal.massRepairScrapImpossible = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairUseAssignedTechsFirst")) {
                    retVal.massRepairUseAssignedTechsFirst = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairReplacePod")) {
                    retVal.massRepairReplacePod = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("massRepairOptions")) {
                    retVal.setMassRepairOptions(MassRepairOption.parseListFromXML(wn2, version));

                //region Legacy
                // Removed in 0.49.*
                } else if (wn2.getNodeName().equalsIgnoreCase("clanPriceModifier")) { // Legacy - 0.49.3 Removal
                    final double value = Double.parseDouble(wn2.getTextContent());
                    retVal.setClanUnitPriceMultiplier(value);
                    retVal.setClanPartPriceMultiplier(value);
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueA")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[0] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueB")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[1] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueC")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[2] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueD")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[3] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueE")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[4] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("usedPartsValueF")) { // Legacy - 0.49.3 Removal
                    retVal.getUsedPartPriceMultipliers()[5] = Double.parseDouble(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("damagedPartsValue")) { // Legacy - 0.49.3 Removal
                    retVal.setDamagedPartsValueMultiplier(Double.parseDouble(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("canceledOrderReimbursement")) { // Legacy - 0.49.3 Removal
                    retVal.setCancelledOrderRefundMultiplier(Double.parseDouble(wn2.getTextContent().trim()));

                // Removed in 0.47.*
                } else if (wn2.getNodeName().equalsIgnoreCase("useAtBCapture")) { // Legacy
                    if (Boolean.parseBoolean(wn2.getTextContent().trim())) {
                        retVal.setPrisonerCaptureStyle(PrisonerCaptureStyle.ATB);
                        retVal.setUseAtBPrisonerDefection(true);
                        retVal.setUseAtBPrisonerRansom(true);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("intensity")) { // Legacy
                    double intensity = Double.parseDouble(wn2.getTextContent().trim());

                    retVal.atbBattleChance[AtBLanceRole.FIGHTING.ordinal()] = (int) Math.round(((40.0 * intensity) / (40.0 * intensity + 60.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.DEFENCE.ordinal()] = (int) Math.round(((20.0 * intensity) / (20.0 * intensity + 80.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.SCOUTING.ordinal()] = (int) Math.round(((60.0 * intensity) / (60.0 * intensity + 40.0)) * 100.0 + 0.5);
                    retVal.atbBattleChance[AtBLanceRole.TRAINING.ordinal()] = (int) Math.round(((10.0 * intensity) / (10.0 * intensity + 90.0)) * 100.0 + 0.5);
                } else if (wn2.getNodeName().equalsIgnoreCase("personnelMarketType")) { // Legacy
                    retVal.personnelMarketName = PersonnelMarket.getTypeName(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("capturePrisoners")) { // Legacy
                    retVal.setPrisonerCaptureStyle(Boolean.parseBoolean(wn2.getTextContent().trim())
                            ? PrisonerCaptureStyle.TAHARQA : PrisonerCaptureStyle.NONE);
                } else if (wn2.getNodeName().equalsIgnoreCase("startGameDelay")) { // Legacy
                    MekHQ.getMekHQOptions().setStartGameDelay(Integer.parseInt(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("historicalDailyLog")) { // Legacy
                    MekHQ.getMekHQOptions().setHistoricalDailyLog(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("useUnitRating") // Legacy
                        || wn2.getNodeName().equalsIgnoreCase("useDragoonRating")) { // Legacy
                    if (!Boolean.parseBoolean(wn2.getTextContent())) {
                        retVal.setUnitRatingMethod(UnitRatingMethod.NONE);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoMW")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.MECHWARRIOR.getIndex()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoBA")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.ELEMENTAL.getIndex()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoAero")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.AEROSPACE.getIndex()] = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("probPhenoVee")) { // Legacy
                    retVal.phenotypeProbabilities[Phenotype.VEHICLE.getIndex()] = Integer.parseInt(wn2.getTextContent().trim());
                }
                //endregion Legacy
            } catch (Exception e) {
                MekHQ.getLogger().error(e);
            }
        }

        // Fixing Old Data
        if (version.isLowerThan("0.49.3") && retVal.getUseAtB()) {
            retVal.setUnitMarketMethod(UnitMarketMethod.ATB_MONTHLY);
            retVal.setContractMarketMethod(ContractMarketMethod.ATB_MONTHLY);
        }

        MekHQ.getLogger().debug("Load Campaign Options Complete!");

        return retVal;
    }

    //region Migration
    /**
     * This is annoyingly required for the case of anyone having changed the surname weights.
     * The code is not nice, but will nicely handle the cases where anyone has made changes
     * @param retVal the return CampaignOptions
     * @param values the values to migrate
     */
    private static void migrateMarriageSurnameWeights(CampaignOptions retVal, String... values) {
        int[] weights = new int[values.length];

        for (int i = 0; i < weights.length; i++) {
            try {
                weights[i] = Integer.parseInt(values[i]);
            } catch (Exception e) {
                MekHQ.getLogger().error(e);
                weights[i] = 0;
            }
        }

        // Now we need to test to figure out the weights have changed. If not, we will keep the
        // new default values. If they have, we save their changes and add the new surname weights
        if (
                (weights[0] != retVal.marriageSurnameWeights[0])
                || (weights[1] != retVal.marriageSurnameWeights[1] + 5)
                || (weights[2] != retVal.marriageSurnameWeights[2] + 5)
                || (weights[3] != retVal.marriageSurnameWeights[9] + 5)
                || (weights[4] != retVal.marriageSurnameWeights[10] + 5)
                || (weights[5] != retVal.marriageSurnameWeights[5] + 5)
                || (weights[6] != retVal.marriageSurnameWeights[6] + 5)
                || (weights[7] != retVal.marriageSurnameWeights[11])
                || (weights[8] != retVal.marriageSurnameWeights[12])
        ) {
            retVal.marriageSurnameWeights[0] = weights[0];
            retVal.marriageSurnameWeights[1] = weights[1];
            retVal.marriageSurnameWeights[2] = weights[2];
            // 3 is newly added
            // 4 is newly added
            retVal.marriageSurnameWeights[5] = weights[3];
            retVal.marriageSurnameWeights[6] = weights[4];
            // 7 is newly added
            // 8 is newly added
            retVal.marriageSurnameWeights[9] = weights[5];
            retVal.marriageSurnameWeights[10] = weights[6];
            retVal.marriageSurnameWeights[11] = weights[7];
            retVal.marriageSurnameWeights[12] = weights[8];
        }
    }
    //endregion Migration
}
