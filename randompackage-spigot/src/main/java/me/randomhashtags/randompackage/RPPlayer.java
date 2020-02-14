package me.randomhashtags.randompackage;

import me.randomhashtags.randompackage.data.*;

import java.util.UUID;

public interface RPPlayer {

    boolean isLoaded();
    RPPlayer load();
    void unload();
    void backup();

    UUID getUUID();

    CoinFlipData getCoinFlipData();
    DisguiseData getDisguiseData();
    DuelData getDuelData();
    GlobalChallengeData getGlobalChallengeData();
    HomeData getHomeData();
    ItemFilterData getItemFilterData();
    JackpotData getJackpotData();
    MonthlyCrateData getMonthlyCrateData();
    PlayerQuestData getPlayerQuestData();
    RarityGemData getRarityGemData();
    ReputationData getReputationData();
    ShowcaseData getShowcaseData();
    SlotBotData getSlotBotData();
    TitleData getTitleData();
}
