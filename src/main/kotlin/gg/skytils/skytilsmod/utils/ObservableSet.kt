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

package gg.skytils.skytilsmod.utils

import java.util.*

/**
 * @see gg.essential.elementa.utils.ObservableList
 */
class ObservableSet<E>(val backingSet: MutableSet<E>) : MutableSet<E> by backingSet, Observable() {
    override fun add(element: E): Boolean {
        return backingSet.add(element).also {
            if (it) {
                setChanged()
                notifyObservers(ObservableAddEvent(element))
            }
        }
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return backingSet.addAll(elements).also {
            if (it) {
                elements.forEach {
                    notifyObservers(ObservableAddEvent(it))
                }
            }
        }
    }

    override fun remove(element: E): Boolean {
        return backingSet.remove(element).also {
            if (it) {
                notifyObservers(ObservableRemoveEvent(element))
            }
        }
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return backingSet.removeAll(elements).also {
            if (it) {
                elements.forEach {
                    notifyObservers(ObservableRemoveEvent(it))
                }
            }
        }
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return backingSet.retainAll(elements).also {
            if (it) {
                backingSet.forEach {
                    notifyObservers(ObservableRemoveEvent(it))
                }
            }
        }
    }

    override fun clear() {
        notifyObservers(ObservableClearEvent(backingSet.toSet()))
        backingSet.clear()
    }
}

sealed class ObservableSetEvent<E>

class ObservableAddEvent<E>(val element: E) : ObservableSetEvent<E>()

class ObservableRemoveEvent<E>(val element: E) : ObservableSetEvent<E>()

class ObservableClearEvent<E>(val oldChildren: Set<E>) : ObservableSetEvent<E>()

fun <E> MutableSet<E>.asObservable() = ObservableSet(this)