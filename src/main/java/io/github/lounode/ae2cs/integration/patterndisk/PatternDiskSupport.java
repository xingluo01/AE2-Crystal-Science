package io.github.lounode.ae2cs.integration.patterndisk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import io.github.lounode.ae2cs.common.me.logic.MeteoritePatternProviderHost;

import net.minecraft.core.BlockPos;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.parts.AEBasePart;

import io.github.lounode.ae2pattern.api.IPatternDiskHost;
import io.github.lounode.ae2pattern.api.PatternDiskApi;
import io.github.lounode.ae2pattern.common.pattern.PatternDiskTerminalView;

/**
 * The entry point of this package; its classes are the only ones in this mod that name AE2 Pattern Disk's
 * types.
 *
 * <p>Every caller reaches it behind a loaded-mod check, so a game without that mod never loads this class
 * at all - its types are resolved on first use, and a missing mod would otherwise show up as a
 * {@code NoClassDefFoundError} at the worst possible moment.</p>
 *
 * <p>Two things live here: the view AE2's pattern access terminal reads, and the disk-host registration
 * that lets that mod's own disk encoding terminal list the same machines.</p>
 */
public final class PatternDiskSupport {

    /**
     * One terminal view per provider, kept until its disks change. The view freezes its row layout for the
     * length of a terminal session, so rebuilding it per read would shift rows under an open terminal.
     *
     * <p>An entry holds its machine and, through it, that machine's level, so entries live as long as the
     * server does: one whose machine is broken mid-session stays here until {@link #clearCaches()} runs at
     * shutdown.</p>
     */
    private static final Map<MeteoritePatternProviderHost, TerminalAccess> VIEWS = new WeakHashMap<>();

    private PatternDiskSupport() {
    }

    /**
     * What AE2's pattern access terminal reads: the plain patterns still sitting in the provider's slots,
     * plus the recipes on its disks. Both, because the terminal can only read one inventory - see
     * {@link DiskAwareTerminalInventory}.
     */
    public static InternalInventory terminalView(MeteoritePatternProviderHost host) {
        synchronized (VIEWS) {
            return VIEWS.computeIfAbsent(host, PatternDiskSupport::createAccess).rows();
        }
    }

    /** Drops the view cached for {@code host}, so the next read re-scans its disks. */
    public static void invalidate(MeteoritePatternProviderHost host) {
        synchronized (VIEWS) {
            var access = VIEWS.remove(host);
            if (access != null) {
                access.invalidate();
            }
        }
    }

    /**
     * Drops every cached view, for a server shutting down: the machines and the levels they captured go
     * with it, and a view holds on to both.
     */
    public static void clearCaches() {
        synchronized (VIEWS) {
            VIEWS.clear();
        }
    }

    private static TerminalAccess createAccess(MeteoritePatternProviderHost host) {
        var slots = host.getLogic().getPatternInv();
        var disks = PatternDiskApi.terminalView(
                slots,
                host::getGrid,
                (IActionHost) host,
                host.getLogic()::saveChanges,
                () -> host.getBlockEntity() == null ? null : host.getBlockEntity().getLevel());
        return new TerminalAccess(disks, new DiskAwareTerminalInventory(slots, disks.view()));
    }

    /** Hands AE2 Pattern Disk the machines of this mod whose pattern slots may hold disks. */
    public static void registerDiskHosts() {
        PatternDiskApi.registerDiskHost(PatternDiskSupport::hostsOn);
    }

    /**
     * What one provider's terminal reads: the disk mod's own expanded view, and the composite that puts
     * the provider's plain patterns back in front of it. The disk view is kept because it has to be
     * invalidated when the disks change, even though callers only ever see the composite.
     */
    private record TerminalAccess(PatternDiskTerminalView disks, InternalInventory rows) {

        void invalidate() {
            disks.invalidate();
        }
    }

    /** Every provider of this mod on {@code grid}, as a disk host for the encoding terminal. */
    private static List<IPatternDiskHost> hostsOn(IGrid grid) {
        var hosts = new ArrayList<IPatternDiskHost>();
        for (var machineClass : grid.getMachineClasses()) {
            if (machineClass == null || !MeteoritePatternProviderHost.class.isAssignableFrom(machineClass)) {
                continue;
            }
            for (var machine : grid.getActiveMachines(machineClass)) {
                if (machine instanceof MeteoritePatternProviderHost host) {
                    hosts.add(new MeteoriteDiskHost(host));
                }
            }
        }
        return hosts;
    }

    /** A provider of this mod, presented to the disk encoding terminal as a host holding disks. */
    private record MeteoriteDiskHost(MeteoritePatternProviderHost host) implements IPatternDiskHost {

        @Override
        public InternalInventory getDiskInventory() {
            return host.getLogic().getPatternInv();
        }

        @Override
        public BlockPos getBlockPos() {
            var blockEntity = host.getBlockEntity();
            return blockEntity == null ? BlockPos.ZERO : blockEntity.getBlockPos();
        }

        @Override
        public int getIdentitySalt() {
            // Two panels on one cable share a block position, so the face has to tell them apart; whole
            // blocks keep the default of no salt.
            return host instanceof AEBasePart part ? part.getSide().ordinal() + 1 : 0;
        }
    }
}
