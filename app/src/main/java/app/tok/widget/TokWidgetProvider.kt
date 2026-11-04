package app.tok.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Locale
import kotlin.math.max

class TokWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        when (intent.action) {
            ACTION_TOGGLE -> {
                val now = System.currentTimeMillis()
                val started = prefs.getLong(KEY_STARTED, 0L)
                if (started == 0L) prefs.edit().putLong(KEY_STARTED, now).apply()
                else {
                    val worked = prefs.getLong(KEY_WORKED, 0L) + max(0L, (now - started) / 1000L)
                    prefs.edit().putLong(KEY_WORKED, worked).putLong(KEY_STARTED, 0L).apply()
                }
            }
            ACTION_ORDER -> prefs.edit()
                .putInt(KEY_ORDERS, prefs.getInt(KEY_ORDERS, 0) + 1)
                .putLong(KEY_EARNED, prefs.getLong(KEY_EARNED, 0L) + 100L).apply()
            ACTION_MONEY -> prefs.edit()
                .putLong(KEY_EARNED, prefs.getLong(KEY_EARNED, 0L) + 100L).apply()
            ACTION_RESET -> prefs.edit().clear().apply()
        }
        refresh(context)
    }

    companion object {
        const val PREFS = "tok_widget_data"
        const val KEY_EARNED = "earned"
        const val KEY_ORDERS = "orders"
        const val KEY_STARTED = "started"
        const val KEY_WORKED = "worked"
        const val ACTION_TOGGLE = "app.tok.widget.TOGGLE"
        const val ACTION_ORDER = "app.tok.widget.ORDER"
        const val ACTION_MONEY = "app.tok.widget.MONEY"
        const val ACTION_RESET = "app.tok.widget.RESET"

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TokWidgetProvider::class.java))
            ids.forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val earned = prefs.getLong(KEY_EARNED, 0L)
            val orders = prefs.getInt(KEY_ORDERS, 0)
            val started = prefs.getLong(KEY_STARTED, 0L)
            val seconds = prefs.getLong(KEY_WORKED, 0L) +
                if (started > 0L) max(0L, (System.currentTimeMillis() - started) / 1000L) else 0L
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val rate = if (seconds > 0) (earned * 3600L / seconds) else 0L

            val views = RemoteViews(context.packageName, R.layout.tok_widget)
            views.setTextViewText(R.id.earned, String.format(Locale.getDefault(), "%,d ₽", earned))
            views.setTextViewText(R.id.orders, "Заказы  $orders")
            views.setTextViewText(R.id.hours, String.format(Locale.getDefault(), "%02d:%02d", hours, minutes))
            views.setTextViewText(R.id.rate, String.format(Locale.getDefault(), "%,d ₽/ч", rate))
            views.setTextViewText(R.id.shift, if (started > 0L) "● СМЕНА ИДЁТ" else "○ СМЕНА НЕ НАЧАТА")
            views.setTextViewText(R.id.toggle, if (started > 0L) "ЗАВЕРШИТЬ" else "НАЧАТЬ")

            bind(context, views, R.id.toggle, ACTION_TOGGLE, id)
            bind(context, views, R.id.add_order, ACTION_ORDER, id)
            bind(context, views, R.id.add_money, ACTION_MONEY, id)
            manager.updateAppWidget(id, views)
        }

        private fun bind(context: Context, views: RemoteViews, viewId: Int, action: String, id: Int) {
            val intent = Intent(context, TokWidgetProvider::class.java).setAction(action).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            val pending = PendingIntent.getBroadcast(
                context, action.hashCode() + id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, pending)
        }
    }
}
