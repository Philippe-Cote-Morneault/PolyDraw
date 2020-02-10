package com.log3900.chat.Channel

import android.view.ViewGroup
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

import android.view.LayoutInflater.from
import com.log3900.R

class ChannelGroupAdapter : ExpandableRecyclerViewAdapter<ChannelGroupViewHolder, ChannelViewHolder> {
    constructor(groups: List<ExpandableGroup<*>>) : super(groups) {

    }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): ChannelGroupViewHolder {
        val view = from(parent?.context).inflate(R.layout.list_item_channel_group, parent, false)
        return ChannelGroupViewHolder(view)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): ChannelViewHolder {
        val view = from(parent?.context).inflate(R.layout.list_item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindGroupViewHolder(holder: ChannelGroupViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder?.bind(group as ChannelGroup)
    }

    override fun onBindChildViewHolder(holder: ChannelViewHolder?, flatPosition: Int, group: ExpandableGroup<*>?, childIndex: Int) {
        val channel = (group as ChannelGroup).channels.get(childIndex)
        holder?.bind(channel)
    }
}
