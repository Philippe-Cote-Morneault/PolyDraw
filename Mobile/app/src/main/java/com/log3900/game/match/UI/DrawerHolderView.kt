package com.log3900.game.match.UI

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.chip.Chip
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.group.Player
import com.log3900.user.UserRepository
import com.log3900.utils.ui.getAvatarID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DrawerHolderView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {
    private var layout: ConstraintLayout
    private var chip: Chip

    init {
        layout = View.inflate(context, R.layout.view_drawer_holder, this) as ConstraintLayout
        chip = layout.findViewById(R.id.view_drawer_holder_chip)
    }

    fun setDrawer(player: Player) {
        chip.text = player.username
    }
}