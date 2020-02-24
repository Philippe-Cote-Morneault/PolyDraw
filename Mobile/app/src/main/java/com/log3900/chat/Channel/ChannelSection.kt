package com.log3900.chat.Channel

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ChannelSection : Section {
    private var channelGroup: ChannelGroup

    private var listener: ClickListener

    var expanded: Boolean = true

    constructor(channelGroup: ChannelGroup, listener: ClickListener) : super(SectionParameters.builder()
                    .itemResourceId(R.layout.list_item_channel)
                    .headerResourceId(R.layout.list_item_channel_group)
                    .build())
    {
        this.channelGroup = channelGroup
        this.listener = listener
    }

    fun setChannels(channels: ArrayList<Channel>) {
        this.channelGroup.channels = channels
    }

    override fun getContentItemsTotal(): Int {
        if (expanded) {
            return this.channelGroup.channels.size
        } else {
            return 0
        }
    }

    override fun getItemViewHolder(view: View?): RecyclerView.ViewHolder {
        return ChannelViewHolder(view!!)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val itemHolder = holder as ChannelViewHolder
        itemHolder.itemView.setOnClickListener {
            listener.onChannelClickListener(this.channelGroup.channels.get(position))
        }
        itemHolder.buttonAction1.setOnClickListener {
            listener.onChannelActionButton1Click(this.channelGroup.channels.get(position), channelGroup.type)
        }
        itemHolder.buttonAction2.setOnClickListener {
            listener.onChannelActionButton2Click(this.channelGroup.channels.get(position), channelGroup.type)
        }
        if (this.channelGroup.channels.get(position).ID.toString() == "00000000-0000-0000-0000-000000000000") {
            itemHolder.buttonAction1.visibility = View.GONE
            itemHolder.buttonAction2.visibility = View.GONE
        } else {
            itemHolder.buttonAction1.visibility = View.VISIBLE
            itemHolder.buttonAction2.visibility = View.VISIBLE
        }

        if (channelGroup.type == GroupType.JOINED) {
            itemHolder.buttonAction1.setImageResource(R.drawable.ic_remove_black_24dp)
        } else {
            itemHolder.buttonAction1.setImageResource(R.drawable.ic_add_black_24dp)
        }
        itemHolder.bind(this.channelGroup.channels.get(position))
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
        if (expanded) {
            viewHolder.expandableIcon.setImageResource(R.drawable.ic_expand_more_black_24dp)
        } else {
            viewHolder.expandableIcon.setImageResource(R.drawable.ic_chevron_right_black)
        }
    }

    interface ClickListener {
        fun onHeaderRootViewClick(group: ChannelGroup)
        fun onChannelClickListener(channel: Channel)
        fun onChannelActionButton1Click(channel: Channel, channelState: GroupType)
        fun onChannelActionButton2Click(channel: Channel, channelState: GroupType)
    }
}