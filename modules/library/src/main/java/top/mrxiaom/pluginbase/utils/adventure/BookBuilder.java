package top.mrxiaom.pluginbase.utils.adventure;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BookBuilder {
    private Component title = Component.empty();
    private Component author = Component.empty();
    private final List<Component> pages = new ArrayList<>();
    private BookBuilder() {}

    @Contract("_ -> this")
    @NotNull BookBuilder title(@NotNull Component title) {
        this.title = Objects.requireNonNull(title, "title");
        return this;
    }

    @Contract("_ -> this")
    @NotNull BookBuilder author(@NotNull Component author) {
        this.author = Objects.requireNonNull(author, "author");
        return this;
    }

    @Contract("_ -> this")
    @NotNull BookBuilder addPage(@NotNull Component page) {
        this.pages.add(Objects.requireNonNull(page, "page"));
        return this;
    }

    @Contract("_ -> this")
    @NotNull BookBuilder pages(@NotNull Collection<Component> pages) {
        this.pages.addAll(Objects.requireNonNull(pages, "pages"));
        return this;
    }

    @Contract("_ -> this")
    @NotNull BookBuilder pages(@NotNull Component@NotNull... pages) {
        Collections.addAll(this.pages, pages);
        return this;
    }

    public Book build() {
        return Book.book(title, author, new ArrayList<>(pages));
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }
}
