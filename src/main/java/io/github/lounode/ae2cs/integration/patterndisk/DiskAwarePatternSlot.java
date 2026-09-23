package io.github.lounode.ae2cs.integration.patterndisk;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.RestrictedInputSlot;

import net.minecraft.world.item.ItemStack;

/**
 * The pattern slot of a provider whose slots serve two purposes: they hold encoded patterns and, in place
 * of one, a whole pattern disk full of them.
 *
 * <p>It lives here rather than beside the menu because only the providers with the disk feature use it -
 * the others keep AE2's plain pattern slot, so a machine that cannot decode a disk never has to know this
 * class exists.</p>
 *
 * <p>It extends AE2's restricted pattern slot rather than plain {@code AppEngSlot}: a slot's icon overlay
 * comes from the placeable item type, which only that class carries.</p>
 */
public final class DiskAwarePatternSlot extends RestrictedInputSlot {

    public DiskAwarePatternSlot(InternalInventory inv, int index) {
        super(RestrictedInputSlot.PlacableItemType.PROVIDER_PATTERN, inv, index);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // AE2's own rule accepts the encoded patterns; a disk is the extra case.
        return super.mayPlace(stack) || PatternDiskSupport.isPatternDisk(stack);
    }
}
