/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.utils

import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectionHelper {
    val classes = hashMapOf<String, Class<*>>()
    val fields = hashMapOf<String, Field>()
    val methods = hashMapOf<String, Method>()

    fun Class<*>.getFieldHelper(fieldName: String) = runCatching {
        ReflectionHelper.fields.getOrPut("$name $fieldName") {
            getDeclaredField(fieldName).apply {
                isAccessible = true
            }
        }
    }.getOrNull()

    fun Class<*>.getMethodHelper(methodName: String) = runCatching {
        ReflectionHelper.methods.getOrPut("$name $methodName") {
            getDeclaredMethod(methodName).apply {
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
    fun getMethodFor(className: String, methodName: String) = getClassHelper(className)?.getMethodHelper(methodName)
}