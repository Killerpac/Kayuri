package net.sanic.Kayuri.ui.main.settings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputRadioButtons
import net.sanic.Kayuri.MainActivity
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentSettingsBinding
import net.sanic.Kayuri.utils.preference.Preference
import net.sanic.Kayuri.utils.preference.PreferenceHelper

class Settings : Fragment(), View.OnClickListener {

    private lateinit var settingsBinding: FragmentSettingsBinding
    private val servers = arrayOf("Vidcdn","GogoPlay","SbStream","XStreamCdn")
    private lateinit var rootView: View
    private lateinit var sharesPreference: Preference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        settingsBinding = FragmentSettingsBinding.bind(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTransitions(view)
        sharesPreference = PreferenceHelper.sharedPreference
        settingsBinding.servertoogle.text = servers[sharesPreference.getserver()]
        setRadioButtons()
        setOnClickListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hidenavbar()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        hidenavbar()
        super.onViewStateRestored(savedInstanceState)
    }


    private fun setOnClickListeners() {
        settingsBinding.back.setOnClickListener(this)
        settingsBinding.servertoogle.setOnClickListener {
            serversheet()
        }
    }
    private fun setRadioButtons() {
        settingsBinding.nightModeRadioButton.isChecked = sharesPreference.getNightMode()
        settingsBinding.nightModeRadioButton.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setNightMode(isChecked)
            (requireActivity() as MainActivity).toggleDayNight()
        }

        settingsBinding.pipRadioButton.isChecked = sharesPreference.getPIPMode()
        settingsBinding.pipRadioButton.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setPIPMode(isChecked)
        }

        settingsBinding.toogleadvance.isChecked = sharesPreference.getadvancecontrols()
        settingsBinding.toogleadvance.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setadvancecontrols(isChecked)
        }
        settingsBinding.toogledns.isChecked = sharesPreference.getdns()
        settingsBinding.toogledns.setOnCheckedChangeListener { _, isChecked ->
            sharesPreference.setdns(isChecked)
        }
    }

    private fun setupTransitions(view: View) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navHostFragmentContainer
            duration = 300
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_THROUGH
            startContainerColor = ContextCompat.getColor(view.context, android.R.color.transparent)
            endContainerColor = ContextCompat.getColor(view.context, android.R.color.transparent)
        }
    }

    private fun hidenavbar(){
        (requireActivity() as MainActivity).barvisibility(View.GONE)
    }

    private fun serversheet(){
        val sheet = InputSheet().build(requireContext()){
            title("Choose Server")
            with(InputRadioButtons() {
                required()
                selected(sharesPreference.getserver())
                label("Available Servers")
                options(servers.toMutableList())
            })
            onPositive { result ->
                sharesPreference.setserver(result.getInt("0"))
                settingsBinding.servertoogle.text = servers[sharesPreference.getserver()]
            }
        }
        sheet.show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                findNavController().popBackStack()
            }
        }
    }
}