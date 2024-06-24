package com.magic.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.commit
import com.magic.ui.fragments.chatBot.ChatBotFragment
import dagger.hilt.android.AndroidEntryPoint
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        supportFragmentManager.commit {
            add(R.id.containerFragment, ChatBotFragment::class.java, Bundle())
        }
    }
}