package com.mobdeve.s20.group7.mco2.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.mobdeve.s20.group7.mco2.MissionsActivity
import com.mobdeve.s20.group7.mco2.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<ImageButton>(R.id.missionsButton).setOnClickListener {
            startActivity(Intent(requireContext(), MissionsActivity::class.java))
        }

        return view
    }
}