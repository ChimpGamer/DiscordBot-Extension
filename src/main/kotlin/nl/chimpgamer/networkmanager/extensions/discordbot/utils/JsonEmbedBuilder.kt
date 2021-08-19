package nl.chimpgamer.networkmanager.extensions.discordbot.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.*
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.dv8tion.jda.internal.utils.Helpers
import nl.chimpgamer.networkmanager.api.utils.GsonUtils.prettyGson
import java.awt.Color
import java.time.OffsetDateTime
import java.time.LocalDateTime
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.TemporalAccessor
import java.util.LinkedList
import java.util.regex.Pattern

class JsonEmbedBuilder {
    val description = StringBuilder()
    var fields: MutableList<Field> = LinkedList()
    private var color = Role.DEFAULT_COLOR_RAW
    private var url: String? = null
    var title: String? = null
    private var timestamp: OffsetDateTime? = null
    private var showTimestamp: Boolean? = null
    private var thumbnail: Thumbnail? = null
    private val author: AuthorInfo? = null
    private var footer: Footer? = null
    private var image: ImageInfo? = null

    val isEmpty: Boolean
        get() = title == null && description.isEmpty() && timestamp == null //&& color == null color alone is not enough to send
                && thumbnail == null && author == null && footer == null && image == null && fields.isEmpty()

    fun setTitle(title: String?, url: String?): JsonEmbedBuilder {
        @Suppress("NAME_SHADOWING") var url = url
        if (title == null) {
            this.title = null
            this.url = null
        } else {
            require(title.isNotEmpty()) { "Title cannot be empty!" }
            require(title.length <= TITLE_MAX_LENGTH) { "Title cannot be longer than $TITLE_MAX_LENGTH characters." }
            if (Helpers.isBlank(url)) {
                url = null
            }
            urlCheck(url)
            this.title = title
            this.url = url
        }
        return this
    }

    fun setDescription(description: CharSequence?): JsonEmbedBuilder {
        this.description.setLength(0)
        if (description != null && description.isNotEmpty()) {
            appendDescription(description)
        }
        return this
    }

    fun appendDescription(description: CharSequence): JsonEmbedBuilder {
        check(this.description.length + description.length <= TEXT_MAX_LENGTH)
        { "Description cannot be longer than $TEXT_MAX_LENGTH characters." }
        this.description.append(description)
        return this
    }

    @Throws(DateTimeException::class)
    fun setTimestamp(temporal: TemporalAccessor?): JsonEmbedBuilder {
        when (temporal) {
            null -> {
                timestamp = null
            }
            is OffsetDateTime -> {
                timestamp = temporal
            }
            else -> {
                val offset: ZoneOffset? = try {
                    ZoneOffset.from(temporal)
                } catch (ignore: DateTimeException) {
                    ZoneOffset.UTC
                }
                timestamp = try {
                    val ldt = LocalDateTime.from(temporal)
                    OffsetDateTime.of(ldt, offset)
                } catch (ignore: DateTimeException) {
                    try {
                        val instant = Instant.from(temporal)
                        OffsetDateTime.ofInstant(instant, offset)
                    } catch (ex: DateTimeException) {
                        throw DateTimeException(
                            "Unable to obtain OffsetDateTime from TemporalAccessor: " +
                                    temporal + " of type " + temporal.javaClass.name, ex
                        )
                    }
                }
            }
        }
        return this
    }

    fun setTitle(title: String?): JsonEmbedBuilder {
        setTitle(title, null)
        return this
    }

    fun setColor(color: Color?): JsonEmbedBuilder {
        this.color = color?.rgb ?: Role.DEFAULT_COLOR_RAW
        return this
    }

    fun setColor(color: Int): JsonEmbedBuilder {
        this.color = color
        return this
    }

    fun setThumbnail(url: String?): JsonEmbedBuilder {
        thumbnail = if (url == null) {
            null
        } else {
            urlCheck(url)
            Thumbnail(url, null, 0, 0)
        }
        return this
    }

    fun setImage(url: String?): JsonEmbedBuilder {
        image = if (url == null) {
            null
        } else {
            urlCheck(url)
            ImageInfo(url, null, 0, 0)
        }
        return this
    }

    fun setFooter(
        text: String?,
        iconUrl: String?
    ): JsonEmbedBuilder { //We only check if the text is null because its presence is what determines if the
// footer will appear in the embed.
        footer = if (text == null) {
            null
        } else {
            require(text.length <= TEXT_MAX_LENGTH) { "Text cannot be longer than $TEXT_MAX_LENGTH characters." }
            urlCheck(iconUrl)
            Footer(text, iconUrl, null)
        }
        return this
    }

    fun addField(field: Field?): JsonEmbedBuilder {
        return if (field == null) this else addField(field.name, field.value, field.isInline)
    }

    fun addField(name: String?, value: String?, inline: Boolean): JsonEmbedBuilder {
        if (name == null && value == null) {
            return this
        }
        fields.add(Field(name, value, inline))
        return this
    }

    fun addBlankField(inline: Boolean): JsonEmbedBuilder {
        fields.add(Field(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline))
        return this
    }

    fun setShowTimestamp(showTimestamp: Boolean?): JsonEmbedBuilder {
        this.showTimestamp = showTimestamp
        return this
    }

    private fun urlCheck(url: String?) {
        if (url == null) {
            return
        }
        require(url.length <= URL_MAX_LENGTH) { "URL cannot be longer than $URL_MAX_LENGTH characters." }
        require(URL_PATTERN.matcher(url).matches()) { "URL must be a valid http or https url." }
    }

    fun toJson(): String {
        return prettyGson.toJson(this)
    }

    fun build(): MessageEmbed {
        check(!isEmpty) { "Cannot build an empty embed!" }
        check(description.length <= TEXT_MAX_LENGTH) {
            String.format(
                "Description is longer than %d! Please limit your input!",
                TEXT_MAX_LENGTH
            )
        }
        val descrip = if (description.isEmpty()) null else description.toString()
            .replace("%newline%", "\n")
        if (timestamp == null && showTimestamp == true) {
            timestamp = OffsetDateTime.now()
        }
        return EntityBuilder.createMessageEmbed(
            url, title, descrip, EmbedType.RICH, timestamp,
            color, thumbnail, null, author, null, footer, image, LinkedList(fields)
        )
    }

    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(
                OffsetDateTime::class.java,
                JsonDeserializer { json, _, _ -> OffsetDateTime.parse(json.toString()) })
            .create()

        const val ZERO_WIDTH_SPACE = "\u200E"
        val URL_PATTERN: Pattern = Pattern.compile("\\s*(https?|attachment)://.+\\..{2,}\\s*", Pattern.CASE_INSENSITIVE)

        @JvmStatic
        fun fromJson(json: String?): JsonEmbedBuilder {
            return gson.fromJson(json, JsonEmbedBuilder::class.java)
        }
    }
}