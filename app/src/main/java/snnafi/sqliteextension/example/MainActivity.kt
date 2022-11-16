package snnafi.sqliteextension.example

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import snnafi.sqliteextension.example.databinding.ActivityMainBinding
import snnafi.sqliteextension.example.db.HadithDatabase
import snnafi.sqliteextension.example.viewmodel.AppViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var appDB: HadithDatabase
    private val viewmodel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // testExtension();

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            testExtension()
        }
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun testExtension() {
        Log.d("MainActivity", "testExtension")

        val searchText = "الحمد"
        val verses = App.oldSchoolWay.getVerses(searchText)

        verses.apply {
            forEach {
                Log.d("Quran [$searchText] ", it.toString())
            }
        }

//            viewmodel.getFirstVerse().observe(this, Observer<Verse> {
//                Log.d("MainActivity", "getFirstVerse ${it.toString()}")
//            })
//
//            viewmodel.getVerses().observe(this, Observer<List<Verse>> {
//                Log.d("MainActivity", "getVerses -> ${it.count()}")
//                it.forEach {
//                    Log.d("Quran ", it.toString())
//                }
//
//            })

//        val searchText = "الحمد"
//        viewmodel.getVerses(searchText).observe(this, Observer<List<Verse>> {
//            Log.d("MainActivity", "getVerses -> ${it.count()}")
//            it.forEach {
//                Log.d("Quran [$searchText] ", it.toString())
//            }
//        })

        // Github file limit 100 MB. hadith.db size 134.99 MB.
//        App.hadithDatabase.hadithDao().getHadiths("الحمد")
//            .observe(this, Observer<List<Hadith>> {
//                it.forEach {
//                    Log.d("Hadith", it.toString())
//                }
//            })
    }
}