package io.github.lounode.ae2cs.common.me.logic;

import io.github.lounode.ae2cs.api.ids.AECSConstants;
import io.github.lounode.ae2cs.integration.patterndisk.PatternDiskSupport;

import appeng.api.inventories.InternalInventory;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import net.neoforged.fml.ModList;

public interface MeteoritePatternProviderHost extends PatternProviderLogicHost, IUpgradeableObject {

    @Override
    default IUpgradeInventory getUpgrades() {
        if (getLogic() instanceof MeteoritePatternProviderLogic logic)
            return logic.getUpgrades();
        else
            return UpgradeInventories.empty();
    }

    /**
     * What AE2's pattern access terminal reads. With AE2 Pattern Disk installed the disks sitting in the
     * pattern slots are shown as the recipes on them - which is the only way a terminal can present them
     * at all, since a disk item on its own does not decode into a pattern.
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
