package com.log3900.game.waiting_room

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import com.log3900.user.UserRepository
import com.log3900.user.account.AccountRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.log3900.utils.ui.getAvatarID

class PlayerViewHolder : RecyclerView.ViewHolder {
    private var player: Player? = null
    private var isPlaceholder = true
    private var isHost = false

    // UI
    private var rootView: View
    private var playerChip: Chip
    private var crownRemoveImageView: ImageView
    private var addImageView: ImageView

    constructor(itemView: View, listener: Listener? = null) : super(itemView) {
        rootView = itemView.findViewById(R.id.list_item_match_waiting_room_player_root_view)
        playerChip = itemView.findViewById(R.id.list_item_match_waiting_room_player_chip_player)
        crownRemoveImageView = itemView.findViewById(R.id.list_item_match_waiting_room_player_image_view_host_crown_remove)
        addImageView = itemView.findViewById(R.id.list_item_match_waiting_room_player_image_view_add)

        rootView.setOnClickListener {
            if (isPlaceholder) {

            } else {
                listener?.playerClicked(player!!)
            }
        }

        addImageView.setOnClickListener {
            listener?.addClicked()
        }

        crownRemoveImageView.setOnClickListener {
            if (!isHost) {
                listener?.removedClicked(player!!)
            }
        }
    }

    fun bind(player: Player?, isPlaceholder: Boolean, isHost: Boolean, showKickOptions: Boolean) {
        this.player = player
        this.isPlaceholder = isPlaceholder

        if (isHost) {
            crownRemoveImageView.setImageResource(R.drawable.ic_crown_black)
            crownRemoveImageView.setColorFilter(Color.parseColor("#FFD700"))
        } else {
            crownRemoveImageView.setImageResource(R.drawable.ic_remove_black_24dp)
            crownRemoveImageView.setColorFilter(Color.parseColor("#FF0000"))
        }

        if (!isPlaceholder) {
            if (showKickOptions) {
                crownRemoveImageView.visibility = View.VISIBLE
            } else {
                crownRemoveImageView.visibility = View.INVISIBLE
            }

            addImageView.visibility = View.INVISIBLE

            playerChip.text = player?.username
            if (!player!!.isCPU) {
                UserRepository.getInstance().getUser(player.ID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            playerChip.setChipIconResource(getAvatarID(it.pictureID))
                        },
                        {

                        }
                    )
            } else {
                if (showKickOptions) {
                    addImageView.visibility = View.VISIBLE
                } else {
                    addImageView.visibility = View.INVISIBLE
                }
                addImageView.visibility = View.INVISIBLE
                playerChip.setChipIconResource(R.drawable.ic_person)
            }
        } else {
            playerChip.text = "Empty Slot"
            playerChip.setChipIconResource(R.drawable.ic_person_colored_foreground)
            if (showKickOptions) {
                addImageView.visibility = View.VISIBLE

            } else {
                addImageView.visibility = View.INVISIBLE
            }
            crownRemoveImageView.visibility = View.INVISIBLE
        }


    }

    interface Listener {
        fun playerClicked(player: Player)
        fun addClicked()
        fun removedClicked(player: Player)
    }
}