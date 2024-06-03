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

package gg.skytils.skytilsmod.localapi

import gg.skytils.event.EventSubscriber
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.localapi.routes.registerMiscRoutes
import gg.skytils.skytilsmod.localapi.routes.registerSkytilsDataRoutes
import gg.skytils.skytilsmod.localapi.routes.registerWaypointRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch

object LocalAPI : EventSubscriber {
    const val port = 56969
    const val version = 1
    private var server: ApplicationEngine? = null

    override fun setup() {

    }

    fun startServer() {
        if (server != null) error("Server is already running!")
        Skytils.IO.launch {
            server = embeddedServer(CIO, port = port, host = "127.0.0.1", module = {
                install(ContentNegotiation) {
                    json(Skytils.json)
                }
                install(DefaultHeaders) {
                    header(HttpHeaders.Server, "Skytils/${Skytils.VERSION}")
                }
                install(CORS) {
                    allowMethod(HttpMethod.Put)
                    allowMethod(HttpMethod.Options)
                    allowHeader(HttpHeaders.Authorization)
                    allowHeader(HttpHeaders.ContentType)
                    allowCredentials = true
                    allowNonSimpleContentTypes = true
                    anyHost()
                }
                install(ConditionalHeaders)
                install(Compression) {
                    gzip {
                        priority = 1.0
                        minimumSize(1024)
                    }
                    deflate {
                        minimumSize(1024)
                    }
                }
                install(AutoHeadResponse)

                authentication {
                    basic(name = "auth") {
                        realm = "Skytils API"
                        validate { credentials ->
                            if (credentials.password == Skytils.config.localAPIPassword && Skytils.config.localAPIPassword.isNotEmpty()) {
                                UserIdPrincipal(credentials.name)
                            } else {
                                null
                            }
                        }
                    }
                }
                routing {
                    get {
                        context.respond("Your Skytils Local API is running correctly! (API v${LocalAPI.version}, Skytils v${Skytils.VERSION})")
                    }
                    route("/api") {
                        authenticate("auth", strategy = AuthenticationStrategy.Required) {
                            route("/skytils") {
                                registerSkytilsDataRoutes()
                                registerWaypointRoutes()
                            }
                        }
                        registerMiscRoutes()
                    }
                }
            }).start()
        }
    }

    fun stopServer() {
        Skytils.IO.launch {
            server?.stop() ?: error("Server wasn't started!")
            server = null
        }
    }

    init {
        if (Skytils.config.localAPIAutoStart) {
            startServer()
        }
    }
}