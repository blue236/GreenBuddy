package com.blue236.greenbuddy.notifications

import android.content.Intent
import com.blue236.greenbuddy.model.ReminderType
import com.blue236.greenbuddy.model.Tab

internal const val EXTRA_REMINDER_DESTINATION_TAB = "extra_reminder_destination_tab"

internal fun ReminderType.destinationTab(): Tab = when (this) {
    ReminderType.LESSON_READY -> Tab.LEARN
    ReminderType.CARE, ReminderType.STREAK_WARNING -> Tab.HOME
}

internal fun Intent.reminderDestinationTabOrNull(): Tab? {
    val rawValue = getStringExtra(EXTRA_REMINDER_DESTINATION_TAB) ?: return null
    return Tab.entries.firstOrNull { it.name == rawValue }
}
