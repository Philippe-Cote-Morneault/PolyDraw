package com.log3900.draw.temp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.log3900.R
import com.log3900.draw.DrawViewFragment

class DoubleDrawViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_double_draw_view, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val receiver = childFragmentManager.findFragmentById(R.id.receiver_draw_view) as DrawViewFragment
        receiver.enableDrawFunctions(false)

        val sender = childFragmentManager.findFragmentById(R.id.sender_draw_view) as DrawViewFragment
        sender.drawView.socketDrawingSender!!.receiver = receiver.drawView.socketDrawingReceiver
    }
}