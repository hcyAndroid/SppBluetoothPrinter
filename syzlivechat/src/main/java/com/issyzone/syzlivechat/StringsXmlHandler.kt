package com.issyzone.syzlivechat


import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

class StringsXmlHandler : DefaultHandler() {
    private var currentValue = ""
    private var currentId = ""
    private var currentArrayName: String? = null
    val stringResources = mutableListOf<StringResource>()
    val arrayResources = mutableMapOf<String, MutableList<StringResource>>()

    override fun startElement(
        uri: String, localName: String, qName: String, attributes: Attributes
    ) {
        currentValue = ""
        when (qName) {
            "string" -> {
                currentId = attributes.getValue("name")
            }
            "string-array" -> {
                currentArrayName = attributes.getValue("name")
                arrayResources[currentArrayName!!] = mutableListOf()
            }
            "item" -> {
                currentId = attributes.getValue("name")
            }
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        currentValue += String(ch, start, length)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            "string" -> {
                stringResources.add(StringResource(currentId, currentValue))
            }
            "item" -> {
                arrayResources[currentArrayName!!]?.add(StringResource(currentId, currentValue))
            }
            "string-array" -> {
                currentArrayName = null
            }
        }
    }
}