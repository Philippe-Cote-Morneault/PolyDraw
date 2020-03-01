package com.log3900.chat.Channel

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import com.log3900.shared.ui.SearchViewUtils
import com.log3900.shared.ui.dialogs.SimpleConfirmationDialog
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.dialog_fragment_progress_dialog.*
import kotlin.collections.ArrayList

class ChannelListFragment : Fragment(), ChannelListView {
    // Services
    private lateinit var channelListPresenter: ChannelListPresenter
    // UI elements
    private lateinit var channelsRecyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var channelsAdapter: SectionedRecyclerViewAdapter
    private lateinit var createChannelButton: ImageView
    private lateinit var channelSearchView: SearchView
    private var channelCreationDialog: Dialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_channel_list, container, false)

        channelsRecyclerView = rootView.findViewById(R.id.fragment_channel_list_recycler_view_channels)
        layoutManager = LinearLayoutManager(this.context)
        channelsRecyclerView.layoutManager = layoutManager
        channelsAdapter = SectionedRecyclerViewAdapter()

        channelSearchView = rootView.findViewById(R.id.fragment_channel_list_search_view_channels)
        channelSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                (channelsAdapter.getSection(GroupType.JOINED.name) as ChannelSection).filter.filter(query)
                (channelsAdapter.getSection(GroupType.AVAILABLE.name) as ChannelSection).filter.filter(query)
                channelsAdapter.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (SearchViewUtils.isFocused(channelSearchView)) {
                    (channelsAdapter.getSection(GroupType.JOINED.name) as ChannelSection).filter.filter(
                        newText
                    )
                    (channelsAdapter.getSection(GroupType.AVAILABLE.name) as ChannelSection).filter.filter(
                        newText
                    )
                    channelsAdapter.notifyDataSetChanged()
                }
                return false
            }
        })

        createChannelButton = rootView.findViewById(R.id.fragment_channel_list_button_create_channel)
        createChannelButton.setOnClickListener {
            channelListPresenter.onCreateChannelButtonClick()
        }

        channelsRecyclerView.adapter = channelsAdapter
        channelListPresenter = ChannelListPresenter(this)
        return rootView
    }

    override fun addChannelSection(channelGroup: ChannelGroup) {
        channelsAdapter.addSection(channelGroup.type.name,ChannelSection(channelGroup, object: ChannelSection.ClickListener {
            override fun onHeaderRootViewClick(group: ChannelGroup) {
                val sectionAdapter = channelsAdapter.getAdapterForSection(channelGroup.type.name)
                val section = channelsAdapter.getSection(channelGroup.type.name) as ChannelSection
                val count = section.contentItemsTotal

                section.expanded = !section.expanded
                sectionAdapter.notifyHeaderChanged()

                if (!section.expanded) {
                    sectionAdapter.notifyItemRangeRemoved(0, count)
                } else {
                    sectionAdapter.notifyAllItemsInserted()
                }
            }

            override fun onChannelClickListener(channel: Channel) {
                channelListPresenter.onChannelClicked(channel)
            }

            override fun onChannelActionButton1Click(channel: Channel, channelState: GroupType) {
                channelListPresenter.onChannelActionButton1Click(channel, channelState)
            }

            override fun onChannelActionButton2Click(channel: Channel, channelState: GroupType) {
                channelListPresenter.onChannelActionButton2Click(channel, channelState)
            }
        }))
        channelsAdapter.notifyDataSetChanged()
        channelsAdapter.getAdapterForSection(channelGroup.type.name).notifyAllItemsChanged()
    }

    override fun setAvailableChannels(channels: ArrayList<Channel>) {
        (channelsAdapter.getSection(GroupType.AVAILABLE.name) as ChannelSection).setChannels(channels)
        val sectionAdapter = channelsAdapter.getAdapterForSection(GroupType.AVAILABLE.name)
        sectionAdapter.notifyAllItemsChanged()
    }

    override fun setJoinedChannels(channels: ArrayList<Channel>) {
        (channelsAdapter.getSection(GroupType.JOINED.name) as ChannelSection).setChannels(channels)
        val sectionAdapter = channelsAdapter.getAdapterForSection(GroupType.JOINED.name)
        sectionAdapter.notifyAllItemsChanged()
    }

    override fun notifyChannelSubscribed(channel: Channel) {
        channelsAdapter.getAdapterForSection(GroupType.AVAILABLE.name).notifyAllItemsChanged()
        channelsAdapter.getAdapterForSection(GroupType.JOINED.name).notifyAllItemsChanged()
    }

    override fun notifyChannelUnsubscried(channel: Channel) {
        channelsAdapter.getAdapterForSection(GroupType.AVAILABLE.name).notifyAllItemsChanged()
        channelsAdapter.getAdapterForSection(GroupType.JOINED.name).notifyAllItemsChanged()
    }

    override fun showChannelCreationDialog(positiveCallback: (channelName: String) -> Unit) {
        val editText = EditText(this.context)
        editText.setHint("Channel Name")
        channelCreationDialog = AlertDialog.Builder(this.context)
            .setTitle("Create Channel")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                positiveCallback(editText.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    override fun hideChannelCreationDialog() {
        if (channelCreationDialog != null) {
            channelCreationDialog?.dismiss()
            channelCreationDialog = null
        }
    }

    override fun showConfirmationDialog(title: String, message: String, positiveButtonListener: (dialog: DialogInterface, which: Int) -> Unit,
                                        negativeButtonListener: (dialog: DialogInterface, which: Int) -> Unit) {

        SimpleConfirmationDialog(this.context!!, title, message, positiveButtonListener, negativeButtonListener).show()
    }

    override fun hideConfirmationDialog() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notifyChannelsChange() {
        (channelsAdapter.getSection(GroupType.JOINED.name) as ChannelSection).filter.filter(channelSearchView.query)
        (channelsAdapter.getSection(GroupType.AVAILABLE.name) as ChannelSection).filter.filter(channelSearchView.query)
        channelsAdapter.notifyDataSetChanged()
    }

    override fun changeActiveChannel(channel: Channel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeChanngelUnreadMessages(channel: Channel, unreadMessages: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}