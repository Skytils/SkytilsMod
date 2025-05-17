/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos

enum class F7Terminals(val pos: BlockPos, val type: Type) {
    P1_TERM_1(BlockPos(111, 113, 83), Type.TERMINAL),
    P1_TERM_2(BlockPos(111, 119, 79), Type.TERMINAL),
    P1_DEVICE(BlockPos(110, 121, 91), Type.DEVICE),
    P1_TERM_3(BlockPos(89, 112, 92), Type.TERMINAL),
    P1_TERM_4(BlockPos(89, 122, 101), Type.TERMINAL),
    P1_LEVER_1(BlockPos(106, 124, 113), Type.LEVER),
    P1_LEVER_2(BlockPos(94, 124, 113), Type.LEVER),

    P2_TERM_1(BlockPos(68, 109, 121), Type.TERMINAL),
    P2_DEVICE(BlockPos(63, 134, 143), Type.DEVICE),
    P2_TERM_2(BlockPos(59, 120, 122), Type.TERMINAL),
    P2_TERM_3(BlockPos(47, 109, 121), Type.TERMINAL),
    P2_TERM_4(BlockPos(40, 124, 122), Type.TERMINAL),
    P2_TERM_5(BlockPos(39, 108, 143), Type.TERMINAL),
    P2_LEVER_1(BlockPos(27, 124, 127), Type.LEVER),
    P2_LEVER_2(BlockPos(23, 132, 138), Type.LEVER),

    P3_TERM_1(BlockPos(-3, 109, 112), Type.TERMINAL),
    P3_TERM_2(BlockPos(-3, 119, 93), Type.TERMINAL),
    P3_DEVICE(BlockPos(-3, 121, 80), Type.DEVICE),
    P3_TERM_3(BlockPos(19, 123, 93), Type.TERMINAL),
    P3_TERM_4(BlockPos(-3, 109, 77), Type.TERMINAL),
    P3_LEVER_1(BlockPos(2, 122, 55), Type.LEVER),
    P3_LEVER_2(BlockPos(14, 122, 55), Type.LEVER),

    P4_TERM_1(BlockPos(41, 109, 29), Type.TERMINAL),
    P4_TERM_2(BlockPos(44, 121, 29), Type.TERMINAL),
    P4_DEVICE(BlockPos(63, 127, 35), Type.DEVICE),
    P4_TERM_3(BlockPos(67, 109, 29), Type.TERMINAL),
    P4_TERM_4(BlockPos(72, 115, 48), Type.TERMINAL),
    P4_LEVER_1(BlockPos(84, 121, 34), Type.LEVER),
    P4_LEVER_2(BlockPos(86, 128, 46), Type.LEVER);



    enum class Type(val item: ItemStack) {
        TERMINAL(ItemStack(Blocks.command_block)),
        DEVICE(ItemStack(Blocks.glowstone)),
        LEVER(ItemStack(Blocks.lever))
    }
}