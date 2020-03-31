package com.log3900.game.waiting_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.log3900.MainApplication
import com.log3900.R
import com.log3900.chat.Channel.Channel
import com.log3900.chat.ChatMessage
import com.log3900.game.group.*
import com.log3900.shared.architecture.EventType
import com.log3900.shared.architecture.MessageEvent
import com.log3900.shared.ui.dialogs.SimpleConfirmationDialog
import com.log3900.utils.format.DateFormatter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList

class MatchWaitingRoomFragment : Fragment(), MatchWaitingRoomView {
    private var matchWaitingRoomPresenter: MatchWaitingRoomPresenter? = null

    // UI
    private lateinit var startMatchButton: MaterialButton
    private lateinit var leaveMatchButton: MaterialButton
    private lateinit var matchModeTextView: TextView
    private lateinit var difficultyTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playersAdapter: PlayerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_match_waiting_room, container, false)

        setupUi(rootView)

        matchWaitingRoomPresenter = MatchWaitingRoomPresenter(this)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackButtonPressed()
            }
        })

        return rootView
    }

    private fun setupUi(rootView: View) {
        startMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_start_match)
        leaveMatchButton = rootView.findViewById(R.id.fragment_match_waiting_room_button_leave_match)
        matchModeTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_mode)
        difficultyTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_difficulty)
        durationTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_duration)
        languageTextView = rootView.findViewById(R.id.fragment_match_waiting_room_text_view_language)
        playersRecyclerView = rootView.findViewById(R.id.fragment_match_waiting_room_recycler_view_players)

        setupRecyclerView()

        setupUIListeners()
    }

    private fun setupUIListeners() {
        startMatchButton.setOnClickListener {
            matchWaitingRoomPresenter?.onStartMatchClick()
        }

        leaveMatchButton.setOnClickListener {
            matchWaitingRoomPresenter?.onLeaveMatchClick()
        }
    }

    private fun setupRecyclerView() {
        playersAdapter = PlayerAdapter(object : PlayerViewHolder.Listener {
            override fun playerClicked(player: Player) {
                //TODO: Open player dialog info
            }

            override fun addClicked() {
                matchWaitingRoomPresenter?.onPlayerAddClick()
            }

            override fun removedClicked(player: Player) {
                SimpleConfirmationDialog(context!!,
                    resources.getString(R.string.kick_player_confirmation_title),
                    resources.getString(R.string.kick_player_confirmation_message, player.username),
                    {_, _->
                        matchWaitingRoomPresenter?.onPlayerRemoveClick(player)
                    },
                    null)
                    .show()
            }
        })
        playersRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = playersAdapter
        }
    }

    override fun setGroup(group: Group) {
        matchModeTextView.text = "Mode: " + MainApplication.instance.getContext().resources.getString(MatchMode.stringRes(group.gameType))
        difficultyTextView.text = MainApplication.instance.getContext().resources.getString(R.string.difficulty) + ": " +
                MainApplication.instance.getContext().resources.getString(Difficulty.stringRes(group.difficulty))
        durationTextView.text = MainApplication.instance.getContext().resources.getString(R.string.match_lobby_duration_title) + ": " +
                DateFormatter.formatDateToTime(Date(group.getDuration()))
        languageTextView.text = MainApplication.instance.getContext().resources.getString(R.string.language) + ": " +
                MainApplication.instance.getContext().resources.getString(Language.stringRes(group.language))
        playersAdapter.setGroup(group)
    }

    override fun setPlayers(players: ArrayList<Player>) {
        playersAdapter.setPlayers(players)
    }

    override fun displayStartMatchButton(display: Boolean) {
        if (display) {
            startMatchButton.visibility = View.VISIBLE
        } else {
            startMatchButton.visibility = View.GONE
        }
    }

    override fun enableStartMatchButton(enable: Boolean) {

    }

    override fun notifyGroupUpdated(group: Group) {
        playersAdapter.notifyDataSetChanged()
        durationTextView.text = MainApplication.instance.getContext().resources.getString(R.string.match_lobby_duration_title) + ": " +
                DateFormatter.formatDateToTime(Date(group.getDuration()))
    }

    override fun notifyPlayerJoined(playerID: UUID) {
        playersAdapter.playerAdded(playerID)
    }

    override fun notifyPlayerLeft(playerID: UUID) {
        playersAdapter.playerRemoved(playerID)
    }

    fun onBackButtonPressed() {
        SimpleConfirmationDialog(
            context!!,
            getString(R.string.quit_waiting_room),
            getString(R.string.quit_waiting_room_confirm),
            {_, _ -> findNavController().popBackStack()},
            null
        ).show()
    }

    override fun onDestroy() {
        matchWaitingRoomPresenter?.destroy()
        matchWaitingRoomPresenter = null
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}