package com.log3900.chat.Channel

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ChannelSection : Section {
    private var channelGroup: ChannelGroup
    private var channels: ArrayList<Channel>

    private var listener: ClickListener

    var expanded: Boolean = true

    constructor(channelGroup: ChannelGroup, channels: ArrayList<Channel>, listener: ClickListener) : super(SectionParameters.builder()
                    .itemResourceId(R.layout.list_item_channel)
                    .headerResourceId(R.layout.list_item_channel_group)
                    .build())
    {
        this.channels = channels
        this.channelGroup = channelGroup
        this.listener = listener
    }

    override fun getContentItemsTotal(): Int {
        if (expanded) {
            return channels.size
        } else {
            return 0
        }
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return ChannelViewHolder(view!!)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val itemHolder = holder as ChannelViewHolder
        itemHolder.bind(channels.get(position))
    }

    override fun getHeaderViewHolder(view: View?): RecyclerView.ViewHolder {
        return ChannelGroupViewHolder(view!!)
    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?) {
        val viewHolder = holder as ChannelGroupViewHolder
        viewHolder.expandableIcon.setOnClickListener {
            listener.onHeaderRootViewClick(viewHolder.channelGroup)
        }
        viewHolder.bind(channelGroup)
    }

    interface ClickListener {
        fun onHeaderRootViewClick(group: ChannelGroup)
    }
}