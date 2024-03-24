package com.promcteam.risecore.legacy.util.item;

import com.promcteam.risecore.legacy.util.DeserializationWorker;
import com.promcteam.risecore.legacy.util.ItemUtils;
import com.promcteam.risecore.legacy.util.SerializationBuilder;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@NoArgsConstructor
public class BookDataBuilder extends DataBuilder {
    private String       title;
    private String       author;
    private List<String> pages = new ArrayList<>(10);

    @SuppressWarnings("unchecked")
    public BookDataBuilder(final Map<String, Object> map) {
        final DeserializationWorker w = DeserializationWorker.start(map);
        this.title = w.getString("title");
        this.author = w.getString("author");
        this.pages = w.getTypedObject("pages", new ArrayList<>(3));
    }

    public BookDataBuilder title(final String title) {
        this.title = title;
        return this;
    }

    public BookDataBuilder author(final String author) {
        this.author = author;
        return this;
    }

    public BookDataBuilder newPage(final String page) {
        this.pages.add(page);
        return this;
    }

    public BookDataBuilder newPage(final String... pages) {
        Collections.addAll(this.pages, pages);
        return this;
    }

    public BookDataBuilder newPage(final Collection<String> pages) {
        this.pages.addAll(pages);
        return this;
    }

    public BookDataBuilder newPage(final int index, final String page) {
        this.pages.add(index, page);
        return this;
    }

    public BookDataBuilder newPage(final int index, final String... pages) {
        return this.newPage(index, Arrays.asList(pages));
    }

    public BookDataBuilder newPage(final int index, final Collection<String> pages) {
        this.pages.addAll(index, pages);
        return this;
    }

    public BookDataBuilder setPage(final int index, final String page) {
        this.pages.set(index, page);
        return this;
    }

    public BookDataBuilder removePage(final int index) {
        this.pages.remove(index);
        return this;
    }

    public BookDataBuilder removePage(final String page) {
        this.pages.remove(page);
        return this;
    }

    public BookDataBuilder removePage(final String... pages) {
        return this.removePage(Arrays.asList(pages));
    }

    public BookDataBuilder removePage(final Collection<String> pages) {
        this.pages.removeAll(pages);
        return this;
    }

    public BookDataBuilder clearPages() {
        this.pages.clear();
        return this;
    }

    public BookDataBuilder clearTitle() {
        this.title = null;
        return this;
    }

    public BookDataBuilder clearAuthor() {
        this.author = null;
        return this;
    }

    @Override
    public void apply(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof BookMeta)) {
            return;
        }

        BookMeta meta = (BookMeta) itemMeta;
        meta.setPages(ItemUtils.fixColors(this.pages));
        meta.setAuthor(ItemUtils.fixColors(this.author));
        meta.setTitle(ItemUtils.fixColors(this.title));
    }

    @Override
    public BookDataBuilder use(final ItemMeta itemMeta) {
        if (!(itemMeta instanceof BookMeta)) {
            return null;
        }

        BookMeta meta = (BookMeta) itemMeta;
        this.title = ItemUtils.removeColors(meta.getTitle());
        this.author = ItemUtils.removeColors(meta.getAuthor());
        this.pages = ItemUtils.removeColors(new ArrayList<>(meta.getPages()));
        return this;
    }

    @Override
    public String getType() {
        return "book";
    }
//
//    @Override
//    public BookDataBuilder applyFunc(final UnaryOperator<String> func)
//    {
//        if (this.title != null)
//        {
//            this.title = func.apply(this.title);
//        }
//        if (this.author != null)
//        {
//            this.author = func.apply(this.author);
//        }
//        if ((this.pages != null) && ! this.pages.isEmpty())
//        {
//            this.pages = Stream.of(func.apply(StringUtils.join(this.pages, '\n')).split("\n")).filter(s -> ! s.equals("<NO-LINE>")).collect(Collectors.toList());
//        }
//        return this;
//    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("title", this.title)
                .append("author", this.author)
                .append("pages", this.pages)
                .toString();
    }

    @Override
    public Map<String, Object> serialize() {
        final SerializationBuilder b = SerializationBuilder.start(4).append(super.serialize());
        b.append("title", this.title);
        b.append("author", this.author);
        b.append("pages", this.pages);
        return b.build();
    }

    public static BookDataBuilder start() {
        return new BookDataBuilder();
    }
}
