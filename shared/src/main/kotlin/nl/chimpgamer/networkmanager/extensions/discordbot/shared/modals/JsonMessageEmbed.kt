package nl.chimpgamer.networkmanager.extensions.discordbot.shared.modals

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSyntaxException
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.internal.entities.EntityBuilder
import java.awt.Color
import java.time.OffsetDateTime
import java.time.DateTimeException
import java.time.ZoneOffset
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.TemporalAccessor
import java.util.LinkedList
import java.util.function.Function

data class JsonMessageEmbed(
    val title: String? = null,
    val url: String? = null,
    val description: String? = null,
    val fields: List<MessageEmbed.Field> = LinkedList(),
    val color: Int = Role.DEFAULT_COLOR_RAW,
    val timestamp: OffsetDateTime? = null,
    val thumbnail: MessageEmbed.Thumbnail? = null,
    val footer: MessageEmbed.Footer? = null,
    val image: MessageEmbed.ImageInfo? = null
) {

    fun toBuilder(): Builder = Builder(this)

    fun toMessageEmbed(): MessageEmbed = EntityBuilder.createMessageEmbed(
        url,
        title,
        description,
        EmbedType.RICH,
        timestamp,
        color,
        thumbnail,
        null,
        null,
        null,
        footer,
        image,
        fields
    )

    data class Builder(
        private var title: String? = null,
        private var url: String? = null,
        private var description: String? = null,
        private var fields: MutableList<MessageEmbed.Field> = LinkedList(),
        private var color: Int = Role.DEFAULT_COLOR_RAW,
        private var timestamp: OffsetDateTime? = null,
        private var thumbnail: MessageEmbed.Thumbnail? = null,
        private var footer: MessageEmbed.Footer? = null,
        private var image: MessageEmbed.ImageInfo? = null
    ) {
        constructor(jsonMessageEmbed: JsonMessageEmbed) : this() {
            title = jsonMessageEmbed.title
            url = jsonMessageEmbed.url
            description = jsonMessageEmbed.description
            fields = jsonMessageEmbed.fields.toMutableList()
            color = jsonMessageEmbed.color
            timestamp = jsonMessageEmbed.timestamp
            thumbnail = jsonMessageEmbed.thumbnail
            footer = jsonMessageEmbed.footer
            image = jsonMessageEmbed.image
        }

        fun title(title: String?) = apply {
            this.title = if (title == null) {
                null
            } else {
                require(title.isNotEmpty()) { "Title cannot be empty!" }
                require(title.length <= MessageEmbed.TITLE_MAX_LENGTH) { "Title cannot be longer than ${MessageEmbed.TITLE_MAX_LENGTH} characters." }
                title
            }
        }

        fun url(url: String?) = apply {
            this.url = if (url == null) {
                null
            } else {
                if (url.isEmpty() || url.isBlank()) {
                    null
                } else {
                    urlCheck(url)
                    url
                }
            }
        }

        fun description(description: String?) = apply { this.description = description }
        fun fields(fields: List<MessageEmbed.Field>) = apply { this.fields = fields.toMutableList() }

        fun addField(field: MessageEmbed.Field?) = apply { if (field != null) this.fields.add(field) }
        fun addField(name: String?, value: String?, inline: Boolean) = apply {
            if (name == null && value == null) {
                return@apply
            }
            fields.add(MessageEmbed.Field(name, value, inline))
        }

        fun addBlankField(inline: Boolean) =
            apply { addField(EmbedBuilder.ZERO_WIDTH_SPACE, EmbedBuilder.ZERO_WIDTH_SPACE, inline) }

        fun parsePlaceholdersToFields(placeholders: Map<String, String>) = apply {
            val fields: MutableList<MessageEmbed.Field> = LinkedList()
            for (field in this.fields) {
                var name = field.name
                var value = field.value
                placeholders.forEach { (toReplace, replacement) ->
                    name = name?.replace(toReplace, replacement); value = value?.replace(toReplace, replacement)
                }

                fields.add(MessageEmbed.Field(name, value, field.isInline))
            }
            this.fields = fields
        }

        fun parsePlaceholdersToFields(handler: Function<String, String>) = apply {
            val fields: MutableList<MessageEmbed.Field> = LinkedList()
            for (field in this.fields) {
                var name = field.name
                var value = field.value

                if (name != null) {
                    name = handler.apply(name)
                }

                if (value != null) {
                    value = handler.apply(value)
                }

                fields.add(MessageEmbed.Field(name, value, field.isInline))
            }
            this.fields = fields
        }

        fun color(color: Color?) = apply { this.color = color?.rgb ?: Role.DEFAULT_COLOR_RAW }
        fun color(color: Int) = apply { this.color = color }

        @Throws(DateTimeException::class)
        fun timestamp(temporalAccessor: TemporalAccessor?) = apply {
            when (temporalAccessor) {
                null -> this.timestamp = null
                is OffsetDateTime -> this.timestamp = temporalAccessor
                else -> {
                    val offset: ZoneOffset? = try {
                        ZoneOffset.from(temporalAccessor)
                    } catch (ignore: DateTimeException) {
                        ZoneOffset.UTC
                    }
                    this.timestamp = try {
                        val ldt = LocalDateTime.from(temporalAccessor)
                        OffsetDateTime.of(ldt, offset)
                    } catch (ignore: DateTimeException) {
                        try {
                            val instant = Instant.from(temporalAccessor)
                            OffsetDateTime.ofInstant(instant, offset)
                        } catch (ex: DateTimeException) {
                            throw DateTimeException(
                                "Unable to obtain OffsetDateTime from TemporalAccessor: " +
                                        temporalAccessor + " of type " + temporalAccessor.javaClass.name, ex
                            )
                        }
                    }
                }
            }
        }

        fun thumbnail(url: String?) = apply {
            this.thumbnail = if (url == null) {
                null
            } else {
                urlCheck(url)
                MessageEmbed.Thumbnail(url, null, 0, 0)
            }
        }

        fun footer(text: String?, iconUrl: String?) = apply {
            footer = if (text == null) {
                null
            } else {
                urlCheck(iconUrl)
                MessageEmbed.Footer(text, iconUrl, null)
            }
        }

        fun image(url: String?) = apply {
            image = if (url == null) {
                null
            } else {
                urlCheck(url)
                MessageEmbed.ImageInfo(url, null, 0, 0)
            }
        }

        private fun urlCheck(url: String?) {
            if (url == null) {
                return
            }
            require(url.length <= MessageEmbed.URL_MAX_LENGTH) { "URL cannot be longer than ${MessageEmbed.URL_MAX_LENGTH} characters." }
            require(EmbedBuilder.URL_PATTERN.matcher(url).matches()) { "URL must be a valid http or https url." }
        }

        private fun isEmpty(): Boolean {
            return title == null && timestamp == null && thumbnail == null && footer == null && image == null && color == Role.DEFAULT_COLOR_RAW && description.isNullOrEmpty() && fields.isEmpty()
        }

        fun build(): JsonMessageEmbed {
            check(!isEmpty()) { "Cannot build an empty embed!" }
            val description = this.description?.replace("%newline%", "\n")
            if (description != null) {
                check(description.length <= MessageEmbed.TEXT_MAX_LENGTH) { "Description is longer than ${MessageEmbed.TEXT_MAX_LENGTH}! Please limit the description input!" }
            }

            return JsonMessageEmbed(title, url, description, fields, color, timestamp, thumbnail, footer, image)
        }
    }

    companion object {
        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(
                OffsetDateTime::class.java,
                JsonDeserializer { json, _, _ -> OffsetDateTime.parse(json.toString()) })
            .create()

        fun fromJson(json: String?): JsonMessageEmbed {
            return try {
                gson.fromJson(json, JsonMessageEmbed::class.java)
            } catch (ignored: JsonSyntaxException) {
                throw ignored
            }
        }
    }
}
