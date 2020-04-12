package com.log3900.game.match.coop

import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.game.group.Player
import com.log3900.user.UserRepository
import com.log3900.utils.ui.getAvatarID
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TeamPlayerViewHolder : RecyclerView.ViewHolder {
    private lateinit var player: Player

    // UI
    private var rootView: View
    private var playerChip: Chip

    constructor(itemView: View) : super(itemView) {
        rootView = itemView
        playerChip = itemView.findViewById(R.id.list_item_active_match_team_player_chip_player)
    }

    fun bind(player: Player) {
        this.player = player

        playerChip.text = player.username

        UserRepository.getInstance().getUser(player.ID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    playerChip.chipIcon = ResourcesCompat.getDrawable(MainApplication.instance.resources, getAvatarID(it.pictureID), null)
                },
                {
                }
            )
    }
}