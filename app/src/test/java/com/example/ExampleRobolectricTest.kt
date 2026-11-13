package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.Chronometer
import android.widget.TextClock
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.widget.AlFajrCompactWidgetProvider
import com.example.widget.AlFajrWidgetProvider
import com.example.widget.WidgetUpdateScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Al-Fajr Watch", appName)
  }

  @Test
  fun `update full and compact widgets runs cleanly and binds live countdown`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = AppWidgetManager.getInstance(context)
    val shadowManager = shadowOf(manager)

    val fullWidgetId = shadowManager.createWidget(AlFajrWidgetProvider::class.java, R.layout.widget_al_fajr_full)
    val compactWidgetId = shadowManager.createWidget(AlFajrCompactWidgetProvider::class.java, R.layout.widget_al_fajr_compact)

    AlFajrWidgetProvider.updateAppWidget(context, manager, fullWidgetId)
    AlFajrCompactWidgetProvider.updateAppWidget(context, manager, compactWidgetId)

    val fullView = shadowManager.getViewFor(fullWidgetId)
    assertNotNull(fullView.findViewById<TextClock>(R.id.tc_widget_gregorian))
    assertNotNull(fullView.findViewById<TextClock>(R.id.tc_widget_seconds))
    val fullChrono = fullView.findViewById<Chronometer>(R.id.chrono_widget_countdown)
    assertNotNull(fullChrono)
    assertTrue(fullChrono.isCountDown)
    val flagsText = fullView.findViewById<TextView>(R.id.tv_widget_flags).text.toString()
    assertTrue(flagsText.contains("ALM"))

    val compactView = shadowManager.getViewFor(compactWidgetId)
    val compactChrono = compactView.findViewById<Chronometer>(R.id.chrono_compact_countdown)
    assertNotNull(compactChrono)
    assertTrue(compactChrono.isCountDown)

    WidgetUpdateScheduler.scheduleNextUpdate(context, System.currentTimeMillis() + 60_000L)
  }
}
