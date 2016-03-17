package io.github.fvasco.pinpoi.importer

import io.github.fvasco.pinpoi.BuildConfig
import io.github.fvasco.pinpoi.model.Placemark
import io.github.fvasco.pinpoi.util.Util
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Base XML impoter

 * @author Francesco Vasco
 */
abstract class AbstractXmlImporter : AbstractImporter() {
    protected val DOCUMENT_TAG = "<XML>"
    protected val parser: XmlPullParser
    protected var placemark: Placemark? = null
    protected var text: String = ""
    protected var tag: String = DOCUMENT_TAG
    private val tagStack = ArrayDeque<String>()

    init {
        parser = Util.XML_PULL_PARSER_FACTORY.newPullParser()
    }

    @Throws(IOException::class)
    override fun importImpl(inputStream: InputStream) {
        try {
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            tag = DOCUMENT_TAG
            handleStartTag()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        tagStack.addLast(tag)
                        tag = parser.name
                        text = ""
                        handleStartTag()
                    }
                    XmlPullParser.TEXT -> text = parser.text
                    XmlPullParser.END_TAG -> {
                        handleEndTag()
                        tag = tagStack.removeLast()
                        text = ""
                    }
                }
                eventType = parser.next()
            }
            if (BuildConfig.DEBUG && tag !== DOCUMENT_TAG) throw AssertionError(tag)
            if (BuildConfig.DEBUG && placemark != null) throw AssertionError(placemark)
            if (BuildConfig.DEBUG && text != "") throw AssertionError(text)
        } catch (e: XmlPullParserException) {
            throw IOException("Error reading XML file", e)
        }

    }

    /**
     * Create a new placemark and saves old one
     */
    protected fun newPlacemark() {
        if (BuildConfig.DEBUG && placemark != null) throw AssertionError(placemark)
        placemark = Placemark()
    }

    protected fun importPlacemark() {
        importPlacemark(placemark!!)
        placemark = null
    }

    /**
     * Check if current path match given tags, except current tag in [.tag]
     */
    protected fun checkCurrentPath(vararg tags: String): Boolean {
        if (tags.size != tagStack.size - 1) return false
        val iterator = tagStack.descendingIterator()
        for (i in tags.indices.reversed()) {
            if (tags[i] != iterator.next()) return false
        }
        return true
    }

    /**
     * Handle a start tag
     */
    @Throws(IOException::class)
    protected abstract fun handleStartTag()

    /**
     * Handle a end tag, text is in [.text] attribute
     */
    @Throws(IOException::class)
    protected abstract fun handleEndTag()

}
