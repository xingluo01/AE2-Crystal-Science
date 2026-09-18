package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.common.me.crafting.EncodedResonatingPattern;
import io.github.lounode.ae2cs.integration.patterndisk.PatternDiskSupport;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import net.minecraft.world.item.ItemStack;

import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Optional;

public interface ResonatingPatternProviderHost extends PatternProviderLogicHost, IUpgradeableObject {

    ResonatingPatternProviderLogic getResonatingLogic();

    @Override
    default IUpgradeInventory getUpgrades() {
        if (getLogic() instanceof ResonatingPatternProviderLogic logic)
            return logic.getUpgrades();
        else
            return UpgradeInventories.empty();
    }

    boolean isExtended();

    default void readDefaultsFromItem(ItemStack stack) {
        getResonatingLogic().readDefaultsFromItem(stack);
    }

    default void writeDefaultsToStack(ItemStack stack) {
        getResonatingLogic().writeDefaultsToStack(stack);
    }

    default List<Optional<EncodedResonatingPattern.Target>> getDefaultInputTargets() {
        return getResonatingLogic().getDefaultInputTargets();
    }

    default int getDefaultSelectedInput() {
        return getResonatingLogic().getDefaultSelectedInput();
    }

    default void setDefaultInputTarget(int input, Optional<EncodedResonatingPattern.Target> target) {
        getResonatingLogic().setDefaultInputTarget(input, target);
    }

    default void setDefaultSelectedInput(int input) {
        getResonatingLogic().setDefaultSelectedInput(input);
    }

    void markForLogicClientUpdate();

    /**
     * What AE2's pattern access terminal - and an uploader picking a target - reads.
     *
     * <p>This provider's pattern slots take no disks of their own: its menu keeps AE2's plain pattern slot,
     * so in practice this is an identity passthrough. It is overridden all the same - a disk that reached a
     * slot by some other route stays visible and usable, and the terminal then sees the same rows the
     * upload path does.</p>
     *
     * <p>The gate has to come first: the support class names that mod's types, and without it installed
     * this method must never load it.</p>
     */
    @Override
    default InternalInventory getTerminalPatternInventory() {
        if (!ModList.get().isLoaded(AECSConstants.PATTERN_DISK_ID)) {
            return getLogic().getPatternInv();
        }
        return PatternDiskSupport.terminalView(this);
    }
}
