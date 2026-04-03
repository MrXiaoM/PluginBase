package top.mrxiaom.pluginbase.utils.adventure.serializer;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.*;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.adventure.serializer.converter.*;

public class BungeeComponentSerializer {
    protected static final boolean isModernHover = Util.isPresent("net.md_5.bungee.api.chat.hover.content.Text");

    public static BaseComponent serialize(Component input) {
        BaseComponent component = convert(input);
        for (Styles style : Styles.values()) {
            style.apply(component, input);
        }

        for (Component child : input.children()) {
            component.addExtra(serialize(child));
        }
        return component;
    }

    private static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.TextComponent) {
            return new TextComponent(((net.kyori.adventure.text.TextComponent) input).content());
        }
        try {
            BaseComponent result = ConvertTranslatable.convert(input);
            if (result != null) return result;
        } catch (LinkageError ignored) {}
        try {
            BaseComponent result = ConvertKeybind.convert(input);
            if (result != null) return result;
        } catch (LinkageError ignored) {}
        try {
            BaseComponent result = ConvertScore.convert(input);
            if (result != null) return result;
        } catch (LinkageError ignored) {}
        try {
            BaseComponent result = ConvertSelector.convert(input);
            if (result != null) return result;
        } catch (LinkageError ignored) {}
        try {
            BaseComponent result = ConvertObject.convert(input);
            if (result != null) return result;
        } catch (LinkageError ignored) {}
        return new TextComponent("");
    }
}
