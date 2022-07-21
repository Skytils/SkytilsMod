# Questionable things presumably present in loader.jar.

## ClassPreloader works like this:

If any of the following classes are present, instead load an empty, private class:

     - com.macromod.macromodmodules.utils.CommandUtils
     - run.hypixel.dupe.hooks.Hooks374
     - assets.load.load
     - assets.load.file
     - com.alphaelite.skyblockextras.SkyblockExtras
     - net.jodah.typetools.Sender
     - net.jodah.typetools.HWIDUtil
     - dev.razebator.bnp.bn.modules.CraftingModule
     - a.b.c.d
     - net.jodah.typetools.Ben
     - net.jodan.typetools.TokenUtil
     - net.mcforge.example.gui.HudEditor
     - com.verify.whitelist.utilities.SBEwhitelist
     - Macro.FailSafe.Discord
     - Macro.FailSafe.Uploader
     - Macro.Plus.ActiveCheck
     - com.alphaelite.skyblockextras.VerifyUser

It also loads gg.skytils.skytilsmod.utils.ContainerCheck which is a new class, regardless of the presence of these classes.

## ContainerCheck works like this:

See a decompilation in (ContainerCheck.decompilation.java).

Requests https://data.skytils.gg/constants/stuff.txt, decodes that from Base64 (see the result of that in uuid.txt).
If the current player (as determined by UPlayer from Essentials universal player) is, it then creates a fake crash.

## There might be more.

This is just what I could find by digging through the commit history of Skytils, however there have been multiple alterations to the obfuscated loader since, so there is no way for us to know what potentially awful things lily has done in absence of any observation or accountability. Again, this was pulled from the git history from **before** lily obfuscated her code.
