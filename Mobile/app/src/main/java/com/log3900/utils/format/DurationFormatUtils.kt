package com.log3900.utils.format

/**
 * Formats given duration to a time format
 * For example: 10h09m53s, 12m34s
 *
 * @param duration the duration in seconds
 * @return the formatted duration
 */
fun formatDuration(duration: Int): String {
    val hours = duration / 3600
    var remDuration = duration % 3600
    val hoursStr =
        if (hours > 0)
            "${hours.toString().padStart(2, '0')}h"
        else
            ""

    val mins = remDuration / 60
    remDuration = remDuration % 60
    val minsStr =
        if (mins > 0 || hoursStr.isNotEmpty())
            "${mins.toString().padStart(2, '0')}m"
        else
            ""

    val secs = remDuration
    val secsStr = "${secs.toString().padStart(2, '0')}s"

    return "$hoursStr$minsStr$secsStr"
}
