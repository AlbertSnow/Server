package com.albertsnow.demo.utils

import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
import java.io.IOException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


fun jsonToPojo(targetPojo: Any?): String {

    if (targetPojo == null)
        return ""
    val mapper = ObjectMapper()

    try {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        return mapper.writeValueAsString(targetPojo)

    } catch (e: IOException) {
        e.printStackTrace()
    }

    return ""
}