package app.tok.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF101410.toInt())
        }
        root.addView(TextView(this).apply {
            text = "TOK  /  COURIER"
            textSize = 24f
            setTextColor(0xFFC6F36A.toInt())
        })
        root.addView(TextView(this).apply {
            text = "Виджет уже готов к добавлению на главный экран.\n\nЗажмите пустое место на рабочем столе → Виджеты → TOK."
            textSize = 16f
            setTextColor(0xFFE9EDE5.toInt())
            setPadding(0, 24, 0, 24)
        })
        root.addView(Button(this).apply {
            text = "Обновить виджет"
            setOnClickListener { TokWidgetProvider.refresh(this@MainActivity) }
        })
        root.addView(Button(this).apply {
            text = "Сбросить показатели за день"
            setOnClickListener {
                getSharedPreferences(TokWidgetProvider.PREFS, MODE_PRIVATE).edit().clear().apply()
                TokWidgetProvider.refresh(this@MainActivity)
            }
        })
        setContentView(root)
    }
}
