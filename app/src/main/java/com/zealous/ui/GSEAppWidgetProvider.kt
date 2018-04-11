package com.zealous.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.zealous.R
import com.zealous.equity.EquityDetailActivity
import com.zealous.utils.Config

/**
 * Created by yaaminu on 4/11/18.
 */

const val ACTION_WATCHED_STOCK_CHANGED = "stock.watch.changed"

class GSEAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val EXTRA_ITEM = "item"
        fun update() {
            val intent = Intent(Config.getApplicationContext(), GSEAppWidgetProvider::class.java)
            intent.action = ACTION_WATCHED_STOCK_CHANGED
            Config.getApplicationContext().sendBroadcast(intent)
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        if (intent.action == ACTION_WATCHED_STOCK_CHANGED) {
            val appWidgetIds = mgr.getAppWidgetIds(ComponentName(context, GSEAppWidgetProvider::class.java))
            Log.e("received", intent.action)
            mgr.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.gse_widget_list)
        }
        super.onReceive(context, intent)
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Config.getApplicationWidePrefs().edit()
                .putInt("foo.widget.count", Config.getApplicationWidePrefs().getInt("foo.widget.count", 0) + 1)
                .apply()
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Config.getApplicationWidePrefs().edit()
                .putInt("foo.widget.count", Config.getApplicationWidePrefs().getInt("foo.widget.count", 0) - 1)
                .apply()
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Get all ids
        val thisWidget = ComponentName(context, GSEAppWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager!!.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {

            val intent2 = Intent(context, StockListService::class.java)

            // Add the app widget ID to the intent extras.
            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent2.data = Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME))

            val remoteViews = RemoteViews(context!!.packageName,
                    R.layout.gse_widget_layout)

            remoteViews.setRemoteAdapter(R.id.gse_widget_list, intent2)


            // Register an list item click listener
            val viewDetailsIntent = Intent(context, EquityDetailActivity::class.java)

            val pendingIntent = PendingIntent.getBroadcast(context,
                    0, viewDetailsIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            remoteViews.setPendingIntentTemplate(R.id.gse_widget_list, pendingIntent)
            remoteViews.setEmptyView(R.id.gse_widget_list, R.id.empty_view)

            appWidgetManager.updateAppWidget(widgetId, remoteViews)
        }
    }
}