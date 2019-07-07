package nl.chimpgamer.networkmanager.extensions.discordbot.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.google.gson.Gson;

import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.entities.EmbedType;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Checks;
import net.dv8tion.jda.core.utils.Helpers;

import java.awt.*;
import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonEmbedBuilder {

    public final static String ZERO_WIDTH_SPACE = "\u200E";
    public final static Pattern URL_PATTERN = Pattern.compile("\\s*(https?|attachment)://.+\\..{2,}\\s*", Pattern.CASE_INSENSITIVE);

    private List<MessageEmbed.Field> fields = new LinkedList<>();
    private final StringBuilder description = new StringBuilder();
    private int color = Role.DEFAULT_COLOR_RAW;
    private String url, title;
    private OffsetDateTime timestamp;
    private Boolean showTimestamp;
    private MessageEmbed.Thumbnail thumbnail;
    private MessageEmbed.AuthorInfo author;
    private MessageEmbed.Footer footer;
    private MessageEmbed.ImageInfo image;

    public JsonEmbedBuilder() {
        //
    }

    public static JsonEmbedBuilder fromJson(String json) {
        return new Gson().fromJson(json, JsonEmbedBuilder.class);
    }

    public boolean isEmpty() {
        return title == null
                && description.length() == 0
                && timestamp == null
                //&& color == null color alone is not enough to send
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && fields.isEmpty();
    }

    public JsonEmbedBuilder setTitle(String title) {
        setTitle(title, null);
        return this;
    }

    public JsonEmbedBuilder setTitle(String title, String url) {
        if (title == null) {
            this.title = null;
            this.url = null;
        } else {
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty!");
            }
            if (title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
                throw new IllegalArgumentException("Title cannot be longer than " + MessageEmbed.TITLE_MAX_LENGTH + " characters.");
            }
            if (Helpers.isBlank(url)) {
                url = null;
            }
            urlCheck(url);

            this.title = title;
            this.url = url;
        }
        return this;
    }

    public final JsonEmbedBuilder setDescription(CharSequence description) {
        this.description.setLength(0);
        if (description != null && description.length() >= 1) {
            appendDescription(description);
        }
        return this;
    }

    public JsonEmbedBuilder appendDescription(CharSequence description) {
        Checks.notNull(description, "description");
        Checks.check(this.description.length() + description.length() <= MessageEmbed.TEXT_MAX_LENGTH,
                "Description cannot be longer than %d characters.", MessageEmbed.TEXT_MAX_LENGTH);
        this.description.append(description);
        return this;
    }

    public JsonEmbedBuilder setTimestamp(TemporalAccessor temporal) {
        if (temporal == null) {
            this.timestamp = null;
        } else if (temporal instanceof OffsetDateTime) {
            this.timestamp = (OffsetDateTime) temporal;
        } else {
            ZoneOffset offset;
            try {
                offset = ZoneOffset.from(temporal);
            } catch (DateTimeException ignore) {
                offset = ZoneOffset.UTC;
            }
            try {
                LocalDateTime ldt = LocalDateTime.from(temporal);
                this.timestamp = OffsetDateTime.of(ldt, offset);
            } catch (DateTimeException ignore) {
                try {
                    Instant instant = Instant.from(temporal);
                    this.timestamp = OffsetDateTime.ofInstant(instant, offset);
                } catch (DateTimeException ex) {
                    throw new DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " +
                            temporal + " of type " + temporal.getClass().getName(), ex);
                }
            }
        }
        return this;
    }

    public String getTitle() {
        return title;
    }

    public JsonEmbedBuilder setColor(Color color) {
        this.color = color == null ? Role.DEFAULT_COLOR_RAW : color.getRGB();
        return this;
    }

    public JsonEmbedBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    public JsonEmbedBuilder setThumbnail(String url) {
        if (url == null) {
            this.thumbnail = null;
        } else {
            urlCheck(url);
            this.thumbnail = new MessageEmbed.Thumbnail(url, null, 0, 0);
        }
        return this;
    }

    public JsonEmbedBuilder setImage(String url) {
        if (url == null) {
            this.image = null;
        } else {
            urlCheck(url);
            this.image = new MessageEmbed.ImageInfo(url, null, 0, 0);
        }
        return this;
    }

    public JsonEmbedBuilder setFooter(String text, String iconUrl) {
        //We only check if the text is null because its presence is what determines if the
        // footer will appear in the embed.
        if (text == null) {
            this.footer = null;
        } else {
            if (text.length() > MessageEmbed.TEXT_MAX_LENGTH) {
                throw new IllegalArgumentException("Text cannot be longer than " + MessageEmbed.TEXT_MAX_LENGTH + " characters.");
            }
            urlCheck(iconUrl);
            this.footer = new MessageEmbed.Footer(text, iconUrl, null);
        }
        return this;
    }

    public JsonEmbedBuilder addField(MessageEmbed.Field field) {
        return field == null ? this : addField(field.getName(), field.getValue(), field.isInline());
    }

    public JsonEmbedBuilder addField(String name, String value, boolean inline) {
        if (name == null && value == null) {
            return this;
        }
        this.fields.add(new MessageEmbed.Field(name, value, inline));
        return this;
    }

    public JsonEmbedBuilder addBlankField(boolean inline) {
        this.fields.add(new MessageEmbed.Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline));
        return this;
    }

    public JsonEmbedBuilder setShowTimestamp(Boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
        return this;
    }

    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    public void setFields(List<MessageEmbed.Field> fields) {
        this.fields = fields;
    }

    private void urlCheck(String url) {
        if (url == null) {
            return;
        }
        if (url.length() > MessageEmbed.URL_MAX_LENGTH) {
            throw new IllegalArgumentException("URL cannot be longer than " + MessageEmbed.URL_MAX_LENGTH + " characters.");
        } else if (!URL_PATTERN.matcher(url).matches()) {
            throw new IllegalArgumentException("URL must be a valid http or https url.");
        }
    }

    public String toJson() {
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(this);
    }

    public MessageEmbed build() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot build an empty embed!");
        }
        if (description.length() > MessageEmbed.TEXT_MAX_LENGTH) {
            throw new IllegalStateException(String.format("Description is longer than %d! Please limit your input!", MessageEmbed.TEXT_MAX_LENGTH));
        }
        final String descrip = this.description.length() < 1 ? null : this.description.toString();

        if (this.timestamp == null && this.showTimestamp != null && this.showTimestamp) {
            this.timestamp = OffsetDateTime.now();
        }

        return EntityBuilder.createMessageEmbed(url, title, descrip, EmbedType.RICH, timestamp,
                color, thumbnail, null, author, null, footer, image, new LinkedList<>(fields));
    }
}