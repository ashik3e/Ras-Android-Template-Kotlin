package com.rasel.RasFocus.features

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rasel.RasFocus.DataManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            // ফোন চালু হলেই ডাটাবেস রেডি করবে এবং সার্ভিসকে মনে করিয়ে দেবে সে ফোকাস মোডে আছে
            DataManager.init(context)
            // Accessibility Service সিস্টেম নিজে থেকেই রিস্টার্ট করে দেয়, 
            // তাই আমাদের শুধু ডাটা সিঙ্ক ঠিক রাখতে হয়।
        }
    }
}
