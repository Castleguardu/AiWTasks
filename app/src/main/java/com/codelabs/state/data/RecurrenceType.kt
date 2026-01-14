package com.codelabs.state.data

enum class RecurrenceType(val label: String, val rruleValue: String) {
    NONE("不重复", ""),
    DAILY("每天", "DAILY"),
    WEEKLY("每周", "WEEKLY"),
    MONTHLY("每月", "MONTHLY"),
    YEARLY("每年", "YEARLY")
}