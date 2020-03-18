package com.log3900.game.waiting_room

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.R
import com.log3900.game.group.Group
import com.log3900.game.group.MatchMode
import com.log3900.game.group.Player
import com.log3900.user.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.log3900.utils.ui.getAvatarID

class PlayerViewHolder : RecyclerView.ViewHolder {
    private var player: Player? = null
    private var isPlaceholder = true
    private var isHost = false

    // UI
    private var rootView: View
    private var avatarImageView: ImageView
    private var usernameTextView: TextView
    private var crownImageView: ImageView

    constructor(itemView: View, listener: Listener? = null) : super(itemView) {
        rootView = itemView.findViewById(R.id.list_item_match_waiting_room_player_root_view)
        avatarImageView = itemView.findViewById(R.id.list_item_match_waiting_room_player_image_view_avatar)
        usernameTextView = itemView.findViewById(R.id.list_Item_match_waiting_room_player_text_view_player_name)
        crownImageView = itemView.findViewById(R.id.list_item_match_waiting_room_player_image_view_host_crown)

        rootView.setOnClickListener {
            if (isPlaceholder) {

            } else {
                listener?.playerClicked(player!!)
            }
        }
    }

    fun bind(player: Player?, isPlaceholder: Boolean, isHost: Boolean) {
        this.player = player
        this.isPlaceholder = isPlaceholder

        if (isHost) {
            crownImageView.visibility = View.VISIBLE
        } else {
            crownImageView.visibility = View.GONE
        }

        if (!isPlaceholder) {
            usernameTextView.text = player?.username
            if (!player!!.isCPU) {
                UserRepository.getInstance().getUser(player.ID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            avatarImageView.setImageResource(getAvatarID(it.pictureID))
                            avatarImageView.visibility = View.VISIBLE
                        },
                        {

                        }
                    )
            } else {
                avatarImageView.visibility = View.INVISIBLE
            }
        } else {
            usernameTextView.text = "Empty Slot"
            avatarImageView.visibility = View.INVISIBLE
        }

    }

    interface Listener {
        fun playerClicked(player: Player)
    }
}