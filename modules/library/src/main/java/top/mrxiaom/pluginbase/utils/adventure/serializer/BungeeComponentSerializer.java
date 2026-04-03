package top.mrxiaom.pluginbase.utils.adventure.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.objects.PlayerObject;
import net.md_5.bungee.api.chat.objects.SpriteObject;
import net.md_5.bungee.api.chat.player.Profile;
import net.md_5.bungee.api.chat.player.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeeComponentSerializer {
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

    @SuppressWarnings({"deprecation"})
    private static BaseComponent convert(Component input) {
        if (input instanceof net.kyori.adventure.text.TextComponent) {
            return new TextComponent(((net.kyori.adventure.text.TextComponent) input).content());
        }
        if (input instanceof net.kyori.adventure.text.TranslatableComponent) {
            String key = ((net.kyori.adventure.text.TranslatableComponent) input).key();
            List<TranslationArgument> arguments = ((net.kyori.adventure.text.TranslatableComponent) input).arguments();
            List<Object> args = new ArrayList<>();
            for (TranslationArgument argument : arguments) {
                args.add(argument.value());
            }
            TranslatableComponent component = new TranslatableComponent(key, args.toArray());
            try {
                String fallback = ((net.kyori.adventure.text.TranslatableComponent) input).fallback();
                if (fallback != null) {
                    component.setFallback(fallback);
                }
            } catch (LinkageError ignored) {
            }
            return component;
        }
        try {
            if (input instanceof net.kyori.adventure.text.KeybindComponent) {
                String keybind = ((net.kyori.adventure.text.KeybindComponent) input).keybind();
                return new KeybindComponent(keybind);
            }
        } catch (LinkageError ignored) {}
        try {
            if (input instanceof net.kyori.adventure.text.ScoreComponent) {
                String name = ((net.kyori.adventure.text.ScoreComponent) input).name();
                String objective = ((net.kyori.adventure.text.ScoreComponent) input).objective();
                String value = ((net.kyori.adventure.text.ScoreComponent) input).value();
                if (value != null) {
                    return new ScoreComponent(name, objective, value);
                } else {
                    return new ScoreComponent(name, objective);
                }
            }
        } catch (LinkageError ignored) {}
        try {
            if (input instanceof net.kyori.adventure.text.SelectorComponent) {
                String selector = ((net.kyori.adventure.text.SelectorComponent) input).pattern();
                Component separator = ((net.kyori.adventure.text.SelectorComponent) input).separator();
                if (separator != null) {
                    return new SelectorComponent(selector, serialize(separator));
                } else {
                    return new SelectorComponent(selector);
                }
            }
        } catch (LinkageError ignored) {}
        try {
            if (input instanceof net.kyori.adventure.text.ObjectComponent) {
                ObjectContents contents = ((net.kyori.adventure.text.ObjectComponent) input).contents();
                if (contents instanceof PlayerHeadObjectContents) {
                    PlayerHeadObjectContents head = (PlayerHeadObjectContents) contents;
                    String name = head.name();
                    UUID uuid = head.id();
                    boolean hat = head.hat();
                    List<PlayerHeadObjectContents.ProfileProperty> props = head.profileProperties();
                    Property[] properties = new Property[props.size()];
                    for (int i = 0; i < props.size(); i++) {
                        String propName = props.get(i).name();
                        String propValue = props.get(i).value();
                        String propSign = props.get(i).signature();
                        if (propSign != null) {
                            properties[i] = new Property(propName, propValue, propSign);
                        } else {
                            properties[i] = new Property(propName, propValue);
                        }
                    }
                    Profile profile = new Profile(properties);
                    profile.setName(name);
                    profile.setUuid(uuid);
                    return new ObjectComponent(new PlayerObject(profile, hat));
                }
                if (contents instanceof SpriteObjectContents) {
                    SpriteObjectContents props = (SpriteObjectContents) contents;
                    String atlas = props.atlas().asString();
                    String sprite = props.sprite().asString();
                    return new ObjectComponent(new SpriteObject(atlas, sprite));
                }
            }
        } catch (LinkageError ignored) {}
        return new TextComponent("");
    }
}
