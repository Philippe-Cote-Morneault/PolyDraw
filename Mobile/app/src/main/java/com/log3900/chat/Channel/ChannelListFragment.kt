package com.log3900.chat.Channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import java.util.*
import kotlin.collections.ArrayList

class ChannelListFragment : Fragment(), ChannelListView {
    // Services
    private lateinit var channelListPresenter: ChannelListPresenter
    // UI elements
    private lateinit var channelsRecyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var channelsAdapter: SectionedRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View = inflater.inflate(R.layout.fragment_channel_list, container, false)

        channelsRecyclerView = rootView.findViewById(R.id.fragment_channel_list_recycler_view_channels)
        layoutManager = LinearLayoutManager(this.context)
        channelsRecyclerView.layoutManager = layoutManager
        channelsAdapter = SectionedRecyclerViewAdapter()

        channelsAdapter.addSection(GroupType.JOINED.name, ChannelSection(ChannelGroup(GroupType.JOINED, arrayListOf()), object: ChannelSection.ClickListener {
            override fun onHeaderRootViewClick(group: ChannelGroup) {
                val sectionAdapter = channelsAdapter.getAdapterForSection(GroupType.JOINED.name)
                val section = channelsAdapter.getSection(GroupType.JOINED.name) as ChannelSection
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
        }))

        channelsAdapter.addSection(GroupType.AVAILABLE.name,ChannelSection(ChannelGroup(GroupType.AVAILABLE, arrayListOf()), object: ChannelSection.ClickListener {
            override fun onHeaderRootViewClick(group: ChannelGroup) {
                val sectionAdapter = channelsAdapter.getAdapterForSection(GroupType.AVAILABLE.name)
                val section = channelsAdapter.getSection(GroupType.AVAILABLE.name) as ChannelSection
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
        }))

        channelListPresenter = ChannelListPresenter(this)
        channelsRecyclerView.adapter = channelsAdapter
        return rootView
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
}