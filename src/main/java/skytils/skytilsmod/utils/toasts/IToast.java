package skytils.skytilsmod.utils.toasts;

/**
 * Taken from Skyblockcatia under MIT License
 * Modified
 * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
 * @author SteveKunG
 */
public interface IToast<T>
{
    Object NO_TOKEN = new Object();
    IToast.Visibility draw(GuiToast toastGui, long delta);

    default Object getType()
    {
        return NO_TOKEN;
    }

    enum Visibility
    {
        SHOW(),
        HIDE();

        Visibility() {}
    }
}