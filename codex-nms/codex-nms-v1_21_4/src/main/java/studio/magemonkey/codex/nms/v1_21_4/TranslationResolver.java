package studio.magemonkey.codex.nms.v1_21_4;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.minecraft.locale.LocaleLanguage;

import java.util.Locale;

public class TranslationResolver {
    public static final ComponentRenderer<Locale> NMS_RENDERER = (component, locale) -> {
        if (component instanceof TranslatableComponent) {
            // Get the translation key from the component
            String key = ((TranslatableComponent) component).key();
            // Use NMS to get the translation (defaults to English).
            // Later, you can extend this to use `locale` if you load additional locale files.
            String translated = LocaleLanguage.a().a(key);
            return Component.text(translated);
        }
        return component;
    };

}
