/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsws.shared.structs

enum class CHWaypointType(val boundingBox: BoundingBox) {
    LostPrecursorCity(BoundingBox(312.0..624.0, 0.0..256.0, 312.0..624.0)),
    JungleTemple(BoundingBox(0.0..312.0, 0.0..256.0, 0.0..312.0)),
    GoblinQueensDen(BoundingBox(0.0..312.0, 0.0..256.0, 312.0..624.0)),
    MinesOfDivan(BoundingBox(312.0..624.0, 0.0..256.0, 0.0..312.0)),
    KingYolkar(BoundingBox(0.0..312.0, 0.0..256.0, 312.0..624.0)),
    KhazadDum(BoundingBox(0.0..624.0, 0.0..256.0, 0.0..624.0)),
    FairyGrotto(BoundingBox(0.0..624.0, 0.0..256.0, 0.0..624.0)),
    Corleone(BoundingBox(312.0..624.0, 0.0..256.0, 0.0..312.0))
}