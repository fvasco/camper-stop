package io.github.fvasco.pinpoi.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author Francesco Vasco
 */
@RunWith(AndroidJUnit4::class)
class UtilTest {

    @Test
    fun testIsHtml() {
        assertTrue(!"".isHtml())
        assertTrue(!"plain text".isHtml())
        assertTrue(!"strange <text>".isHtml())
        assertTrue(!"very strange <text".isHtml())
        assertTrue(!"<bad>".isHtml())
        assertTrue(!"so </bad>".isHtml())
        assertTrue(!"<bad></bad2>".isHtml())
        assertTrue("<good>text</good>".isHtml())
        assertTrue("<good>text\ntext2</good>".isHtml())
    }

    @Test
    fun testIsUri() {
        assertTrue(!"".isUri())
        assertTrue(!"/path/file.ext".isUri())
        assertTrue(!"name.ext".isUri())
        assertTrue("file:///path/file".isUri())
        assertTrue("http://server.domain/resource.txt".isUri())
    }
}