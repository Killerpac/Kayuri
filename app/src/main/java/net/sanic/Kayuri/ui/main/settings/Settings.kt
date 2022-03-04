package net.sanic.Kayuri.ui.main.settings

import android.app.AlertDialog
import android.app.Dialog
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
import com.google.android.material.transition.MaterialFadeThrough
import net.sanic.Kayuri.MainActivity
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentSettingsBinding
import net.sanic.Kayuri.utils.constants.C
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
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupTransitions(view)
        sharesPreference = PreferenceHelper.sharedPreference
        settingsBinding.button.text = sharesPreference.getpreferredquality()
        setRadioButtons()
        setOnClickListeners()
        super.onViewCreated(view, savedInstanceState)
    }
    private fun setOnClickListeners() {
        settingsBinding.back.setOnClickListener(this)
        settingsBinding.button.setOnClickListener(this)
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
    private fun setupTransitions(view: View) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        exitTransition = MaterialFadeThrough().apply {
            duration = 300
        }
        reenterTransition = MaterialFadeThrough().apply {
            duration = 300
        }
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.navHostFragmentContainer
            duration = 300
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_THROUGH
            startContainerColor = ContextCompat.getColor(view.context, android.R.color.transparent)
            endContainerColor = ContextCompat.getColor(view.context, android.R.color.transparent)
        }
    }
    private fun qualitydialog(){
        val qualities = arrayOf("360p","480p","720p","1080p")
        var checkeditem = 0
        val alertDialog = AlertDialog.Builder(requireContext(),R.style.RoundedCornersDialog)
        alertDialog.apply { 
            setTitle("Preferred Quality")
            setSingleChoiceItems(qualities,checkeditem) {_,which ->
                    checkeditem = which
            }
            setPositiveButton("OK") {dialog, _ ->
                sharesPreference.setpreferredquality(qualities[checkeditem])
                settingsBinding.button.text = qualities[checkeditem]
                dialog.dismiss()
            }
            setNegativeButton("Cancel") {dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create().show()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                findNavController().popBackStack()
            }
            R.id.button -> {
                qualitydialog()
            }
        }
    }
}