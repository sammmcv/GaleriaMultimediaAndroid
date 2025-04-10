package com.escom.practica3_1.utils

import android.net.Uri
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Custom TypeAdapter for Gson to handle Uri objects
 * This adapter handles both string and object representations of URIs
 */
class UriTypeAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter, value: Uri?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.toString())
        }
    }

    override fun read(reader: JsonReader): Uri? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        } else if (reader.peek() == JsonToken.STRING) {
            // Handle URI stored as string
            val uriString = reader.nextString()
            return if (uriString.isNullOrEmpty()) null else Uri.parse(uriString)
        } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            // Handle URI stored as object (legacy format)
            reader.beginObject()
            var scheme: String? = null
            var encodedPath: String? = null
            var encodedAuthority: String? = null
            
            while (reader.hasNext()) {
                val name = reader.nextName()
                when (name) {
                    "scheme" -> scheme = reader.nextString()
                    "encodedPath" -> encodedPath = reader.nextString()
                    "encodedAuthority" -> encodedAuthority = reader.nextString()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            
            // Reconstruct URI from components
            val builder = Uri.Builder()
            if (scheme != null) builder.scheme(scheme)
            if (encodedAuthority != null) builder.encodedAuthority(encodedAuthority)
            if (encodedPath != null) builder.encodedPath(encodedPath)
            return builder.build()
        } else {
            // Skip any other format
            reader.skipValue()
            return null
        }
    }
}