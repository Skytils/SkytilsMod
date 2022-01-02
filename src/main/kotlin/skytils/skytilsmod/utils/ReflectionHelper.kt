/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.utils

import java.lang.reflect.Field

object ReflectionHelper {
    val classes = hashMapOf<String, Class<*>>()
    val fields = hashMapOf<String, Field>()

    fun Class<*>.getFieldHelper(fieldName: String) = runCatching {
        ReflectionHelper.fields.getOrPut("$name $fieldName") {
            getDeclaredField(fieldName).apply {
                isAccessible = true
            }
        }
    }.getOrNull()

    fun getClassHelper(className: String) = runCatching {
        classes.getOrPut(className) {
            Class.forName(className)
        }
    }.getOrNull()

    fun getFieldFor(className: String, fieldName: String) = getClassHelper(className)?.getFieldHelper(fieldName)
}