package com.log3900.chat.Channel

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.log3900.R
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ChannelSection : Section {
    private var channels: ArrayList<Channel>
    constructor(channels: ArrayList<Channel>) : super(SectionParameters.builder()
                    .itemResourceId(R.layout.list_item_channel)
                    .headerResourceId(R.layout.list_item_channel_group)
                    .build())
    {
        this.channels = channels
    }

    override fun getContentItemsTotal(): Int {
        return channels.size
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
        super.onBindHeaderViewHolder(holder)
    }
}