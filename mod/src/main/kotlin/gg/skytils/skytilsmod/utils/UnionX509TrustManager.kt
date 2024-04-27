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

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class UnionX509TrustManager(private vararg val trustManagers: X509TrustManager) : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        val it = trustManagers.iterator()
        while (it.hasNext()) {
            try {
                it.next().checkClientTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                if (!it.hasNext()) {
                    throw e
                }
            }
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        val it = trustManagers.iterator()
        while (it.hasNext()) {
            try {
                it.next().checkServerTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                if (!it.hasNext()) {
                    throw e
                }
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = trustManagers.flatMap { it.acceptedIssuers.asIterable() }.toTypedArray()
}