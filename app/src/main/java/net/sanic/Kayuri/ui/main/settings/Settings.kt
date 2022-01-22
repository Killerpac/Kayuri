package net.sanic.Kayuri.ui.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import net.sanic.Kayuri.MainActivity
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentSettingsBinding
import net.sanic.Kayuri.utils.preference.Preference
import net.sanic.Kayuri.utils.preference.PreferenceHelper

class Settings : Fragment(), View.OnClickListener {

    private lateinit var settingsBinding: FragmentSettingsBinding
    private lateinit var rootView: View
    lateinit var sharesPreference: Preference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        settingsBinding = FragmentSettingsBinding.bind(rootView)
        setOnClickListeners()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharesPreference = PreferenceHelper.sharedPreference
        setRadioButtons()
        super.onViewCreated(view, savedInstanceState)
    }
    private fun setOnClickListeners() {
        settingsBinding.back.setOnClickListener(this)
    }
    private fun setRadioButtons() {
        settingsBinding.nightModeRadioButton.isChecked = sharesPreference.getNightMode()
        settingsBinding.nightModeRadioButton.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setNightMode(isChecked)
            (activity as MainActivity).toggleDayNight()
        }

        settingsBinding.pipRadioButton.isChecked = sharesPreference.getPIPMode()
        settingsBinding.pipRadioButton.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setPIPMode(isChecked)
        }
        settingsBinding.googletoogle.isChecked = sharesPreference.getGoogleServer()
        settingsBinding.googletoogle.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setGoogleServer(isChecked)
        }
        settingsBinding.toogleadvance.isChecked = sharesPreference.getadvancecontrols()
        settingsBinding.toogleadvance.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setadvancecontrols(isChecked)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                findNavController().popBackStack()
            }
        }
    }
}