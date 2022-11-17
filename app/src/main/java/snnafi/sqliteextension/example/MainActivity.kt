package snnafi.sqliteextension.example

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import snnafi.sqliteextension.example.adapter.VerseAdapter
import snnafi.sqliteextension.example.databinding.ActivityMainBinding
import snnafi.sqliteextension.example.viewmodel.AppViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewmodel by viewModels<AppViewModel>()
    private lateinit var verseAdapter: VerseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.content.type.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        binding.content.type.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_SEARCH) {
                    setUpRecyclerView()
                    p0?.let {
                        if (it.text.toString().length >= 3) {
                            testExtension(it.text.toString().trim())
                        }
                        return true
                    }

                }
                return false
            }

        });
        binding.fab.isVisible = false
    }

    private fun setUpRecyclerView() {
        binding.content.items.setHasFixedSize(true)
        binding.content.items.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun clearRecyclerView() {
        binding.content.items.setHasFixedSize(true)
        binding.content.items.layoutManager = LinearLayoutManager(applicationContext)
        binding.content.items.adapter = VerseAdapter(listOf())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun testExtension(text: String? = null) {
        Log.d("MainActivity", "testExtension")

        val searchText = text ?: "الحمد"
        val verses = viewmodel.getVerses(searchText)

        verses.apply {
            forEach {
                Log.d("Quran [$searchText] ", it.toString())
            }
        }

        verseAdapter = VerseAdapter(verses);
        binding.content.items.adapter = verseAdapter;
    }
}