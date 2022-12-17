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

package gg.skytils.skytilsmod.utils;

import gg.skytils.skytilsmod.Reference;
import gg.skytils.skytilsmod.mixins.interfaces.IMCrashReport;
import io.sentry.Sentry;
import io.sentry.log4j2.SentryAppender;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryStackFrame;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.File;
import java.io.IOException;

public class SentryHandler {
    public static File analyicsCheckFile = new File("./config/skytils/nosentry");
    private static final String dsn = "https://7b587cd3360e4a1ca7bdaec4b0af216c@o1300094.ingest.sentry.io/4504288758464512";

    public static void init() {
        try {
            if (analyicsCheckFile.exists() || SentryHandler.class.getResourceAsStream("yesThisIsASkytilsRelease") == null || Launch.classLoader.getClassBytes("net.minecraft.world.World") != null)
                return;
        } catch (IOException ignored) {
        }

        Sentry.init(options -> {
            options.setDsn(dsn);
            options.setRelease(Reference.VERSION);
            options.setBeforeSend((event, hint) -> {
                event.getContexts().put("mods", Loader.instance().getModList().stream().map(modContainer -> modContainer.getModId() + "=" + modContainer.getVersion()).toArray());
                if (event.getExceptions() == null) return event;

                for (SentryException e : event.getExceptions()) {
                    if (e.getStacktrace() == null || e.getStacktrace().getFrames() == null) continue;
                    for (SentryStackFrame f : e.getStacktrace().getFrames()) {
                        if (f.getPackage() != null && f.getPackage().startsWith("gg.skytils")) return event;
                    }
                }
                return null;
            });
        });

        SentryAppender appender = SentryAppender.createAppender("Sentry", null, null, dsn, false, null, null);
        LoggerContext context = ((LoggerContext) LogManager.getContext(false));
        for (LoggerConfig lc : context.getConfiguration().getLoggers().values()) {
            lc.addAppender(appender, Level.ALL, null);
        }
    }

    public static void handleCrashReport(CrashReport cr) {
        try {
            if (!((IMCrashReport) cr).getSkytilsHook().isSkytilsCrash()) return;
        } catch (Throwable t) {
            Sentry.captureException(t);
        }
        Sentry.captureException(cr.getCrashCause());
    }
}
