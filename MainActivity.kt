package com.jihad.ai.newsassistant

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Typeface
import android.graphics.Color
import android.text.InputType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStream
import java.util.concurrent.Executors

data class NewsItem(val title: String, val link: String, val description: String, val pubDate: String)

class MainActivity : Activity() {
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private val items = mutableListOf<NewsItem>()
    private val prefs by lazy { getSharedPreferences("jihad_news", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        loadSaved()
        refreshNews()
    }

    private fun buildScreen() {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 12)
            setBackgroundColor(Color.rgb(246, 248, 252))
        }
        val header = TextView(this).apply {
            text = "🤖 Jihad AI News Assistant"
            textSize = 23f
            setTextColor(Color.rgb(20, 40, 75))
            typeface = Typeface.DEFAULT_BOLD
        }
        outer.addView(header)
        val subtitle = TextView(this).apply {
            text = "বাংলা সংবাদ • অনলাইন ও অফলাইন লাইব্রেরি"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 5, 0, 12)
        }
        outer.addView(subtitle)

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(makeButton("সংবাদ আপডেট") { refreshNews() }, LinearLayout.LayoutParams(0, -2, 1f))
        actions.addView(makeButton("সেভ করা খবর") { showSaved() }, LinearLayout.LayoutParams(0, -2, 1f))
        outer.addView(actions)

        search = EditText(this).apply {
            hint = "খবর খুঁজুন..."
            singleLine = true
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(12, 8, 12, 8)
        }
        outer.addView(search)
        search.setOnEditorActionListener { _, _, _ -> filterItems(search.text.toString()); true }

        status = TextView(this).apply {
            text = "সংবাদ লোড হচ্ছে..."
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(2, 8, 2, 8)
        }
        outer.addView(status)

        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(outer)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply { text = label; setOnClickListener { action() } }

    private fun refreshNews() {
        status.text = "BBC বাংলা থেকে খবর আনা হচ্ছে..."
        executor.execute {
            try {
                val fresh = fetchRss("https://feeds.bbci.co.uk/bengali/rss.xml")
                runOnUiThread {
                    items.clear()
                    items.addAll(fresh)
                    saveCache(fresh)
                    status.text = "সর্বশেষ ${fresh.size}টি খবর • অনলাইন"
                    render(fresh)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val cached = readCache()
                    items.clear()
                    items.addAll(cached)
                    status.text = if (cached.isNotEmpty())
                        "ইন্টারনেট পাওয়া যায়নি • সংরক্ষিত ${cached.size}টি খবর দেখানো হচ্ছে"
                    else "খবর আনতে সমস্যা হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।"
                    render(cached)
                }
            }
        }
    }

    private fun fetchRss(address: String): List<NewsItem> {
        val connection = (URL(address).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12000
            readTimeout = 12000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "JihadAINewsAssistant/1.0")
        }
        try {
            connection.inputStream.use { return parseRss(it) }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRss(stream: InputStream): List<NewsItem> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(stream, "UTF-8")
        val result = mutableListOf<NewsItem>()
        var event = parser.eventType
        var inItem = false
        var title = ""; var link = ""; var desc = ""; var date = ""
        var current = ""
        while (event != XmlPullParser.END_DOCUMENT && result.size < 40) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("item", true)) {
                        inItem = true; title = ""; link = ""; desc = ""; date = ""
                    } else if (inItem) current = parser.name.lowercase()
                }
                XmlPullParser.TEXT -> if (inItem) {
                    val value = parser.text ?: ""
                    when (current) {
                        "title" -> title += value
                        "link" -> link += value
                        "description" -> desc += value
                        "pubdate" -> date += value
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", true)) {
                        if (title.isNotBlank()) result.add(NewsItem(clean(title), link.trim(), clean(desc), date.trim()))
                        inItem = false
                    }
                    current = ""
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun clean(text: String) = android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY)
        .toString().replace(Regex("\\s+"), " ").trim()

    private fun saveCache(news: List<NewsItem>) {
        val encoded = news.joinToString("\n") {
            listOf(it.title, it.link, it.description, it.pubDate).joinToString("\u001f")
        }
        prefs.edit().putString("cache", encoded).apply()
    }

    private fun readCache(): List<NewsItem> {
        val raw = prefs.getString("cache", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull {
            val p = it.split("\u001f")
            if (p.size == 4) NewsItem(p[0], p[1], p[2], p[3]) else null
        }
    }

    private fun loadSaved() {
        val cached = readCache()
        if (cached.isNotEmpty()) {
            items.clear(); items.addAll(cached)
            status.text = "সংরক্ষিত ${cached.size}টি খবর • অফলাইন"
            render(cached)
        }
    }

    private fun showSaved() {
        val saved = prefs.getStringSet("saved_links", emptySet()) ?: emptySet()
        val savedItems = readCache().filter { saved.contains(it.link) }
        status.text = "অফলাইনে সেভ করা খবর: ${savedItems.size}টি"
        render(savedItems)
    }

    private fun filterItems(query: String) {
        val filtered = if (query.isBlank()) items else items.filter {
            it.title.contains(query, true) || it.description.contains(query, true)
        }
        status.text = "খুঁজে পাওয়া খবর: ${filtered.size}টি"
        render(filtered)
    }

    private fun render(news: List<NewsItem>) {
        list.removeAllViews()
        if (news.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "এখনো কোনো খবর নেই। ইন্টারনেট চালু করে ‘সংবাদ আপডেট’ চাপুন।"
                textSize = 15f; setPadding(12, 24, 12, 24)
            })
            return
        }
        news.forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(14, 12, 14, 12)
                setBackgroundColor(Color.WHITE)
            }
            val title = TextView(this).apply {
                text = item.title; textSize = 17f
                setTextColor(Color.rgb(25, 45, 78)); typeface = Typeface.DEFAULT_BOLD
            }
            card.addView(title)
            if (item.pubDate.isNotBlank()) card.addView(TextView(this).apply {
                text = item.pubDate; textSize = 11f; setTextColor(Color.GRAY); setPadding(0, 4, 0, 4)
            })
            card.addView(TextView(this).apply {
                text = item.description.ifBlank { "বিস্তারিত জানতে মূল সংবাদটি খুলুন।" }
                textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, 6, 0, 8)
            })
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            row.addView(makeButton("সেভ") {
                val saved = (prefs.getStringSet("saved_links", emptySet()) ?: emptySet()).toMutableSet()
                saved.add(item.link)
                prefs.edit().putStringSet("saved_links", saved).apply()
                Toast.makeText(this@MainActivity, "খবর অফলাইনে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
            })
            row.addView(makeButton("খবর খুলুন") {
                if (item.link.isNotBlank()) {
                    try { startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.link))) }
                    catch (_: Exception) { Toast.makeText(this@MainActivity, "লিংক খোলা যায়নি", Toast.LENGTH_SHORT).show() }
                }
            })
            card.addView(row)
            val divider = View(this).apply { setBackgroundColor(Color.rgb(220, 225, 233)) }
            list.addView(card)
            list.addView(divider, LinearLayout.LayoutParams(-1, 1).apply { topMargin = 8; bottomMargin = 8 })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
