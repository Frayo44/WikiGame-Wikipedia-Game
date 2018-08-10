package com.yoavfranco.wikigame.helpers;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.ViewSwitcher;

import com.heinrichreimersoftware.singleinputform.steps.Step;
import com.yoavfranco.wikigame.fragments.BaseScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoav on 03/03/17.
 */

public abstract class SingleInputFragment extends BaseScreen {


    private static final String KEY_DATA = "key_data";
    private static final String KEY_STEP_INDEX = "key_step_index";

    private static final Property<ProgressBar, Integer> PB_PROGRESS_PROPERTY =
            new Property<ProgressBar, Integer>(Integer.class, "PB_PROGRESS_PROPERTY"){

                @Override
                public void set(ProgressBar pb, Integer value){
                    pb.setProgress(value);
                }

                @Override
                public Integer get(ProgressBar pb){
                    return pb.getProgress();
                }
            };

    private List<Step> steps = new ArrayList<>();
    protected Bundle setupData = new Bundle();
    private int stepIndex = 0;
    private boolean error;

    private FrameLayout container;
    public ScrollView containerScrollView;
    private LinearLayout innerContainer;
    private TextSwitcher titleSwitcher;
    private TextSwitcher errorSwitcher;
    private TextSwitcher detailsSwitcher;
    private CardView textField;
    private ViewAnimator inputSwitcher;
    private ImageButton nextButton;
    private TextView stepText;

    private View.OnClickListener nextButtonClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            nextStep();
        }
    };

    private Drawable buttonNextIcon;
    private Drawable buttonFinishIcon;

    private int textFieldBackgroundColor = -1;
    private int progressBackgroundColor = -1;

    private int titleTextColor = -1;
    private int detailsTextColor = -1;
    private int errorTextColor = -1;

    public void onBackPressed(){
        if(stepIndex == 0){
            //finish();
        }
        else{
            previousStep();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.heinrichreimersoftware.singleinputform.R.layout.activity_single_input_form, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadTheme();

        findViews(view);

        steps = onCreateSteps();

        if(savedInstanceState != null){
            setupData = savedInstanceState.getBundle(KEY_DATA);
            stepIndex = savedInstanceState.getInt(KEY_STEP_INDEX, 0);
        }

        setupTitle();
        setupInput();
        setupError();
        setupDetails();

        nextButton.setOnClickListener(nextButtonClickListener);
        errorSwitcher.setText("");
        updateStep();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            setupData = savedInstanceState.getBundle(KEY_DATA);
            stepIndex = savedInstanceState.getInt(KEY_STEP_INDEX, 0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        setupData = getCurrentStep().save(setupData);
        outState.putBundle(KEY_DATA, setupData);
        outState.putInt(KEY_STEP_INDEX, stepIndex);
    }

    @Override
    public void onPause() {
        hideSoftInput();
        super.onPause();
    }

    protected abstract List<Step>  onCreateSteps();

    private void findViews(View v){
        container = (FrameLayout) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.container);
        containerScrollView = (ScrollView) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.containerScrollView);
        innerContainer = (LinearLayout) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.innerContainer);
        titleSwitcher = (TextSwitcher) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.titleSwitcher);
        errorSwitcher = (TextSwitcher) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.errorSwitcher);
        detailsSwitcher = (TextSwitcher) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.detailsSwitcher);
        textField = (CardView) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.textField);
        inputSwitcher = (ViewAnimator) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.inputSwitcher);
        nextButton = (ImageButton) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.nextButton);
        stepText = (TextView) v.findViewById(com.heinrichreimersoftware.singleinputform.R.id.stepText);
    }

    protected Step getCurrentStep(){
        return getStep(stepIndex);
    }

    protected Step getStep(int position){
        return steps.get(position);
    }

    @SuppressWarnings("ResourceType")
    private void loadTheme() {
        /* Default values */
        buttonNextIcon = ContextCompat.getDrawable(getActivity(), com.heinrichreimersoftware.singleinputform.R.drawable.ic_arrow_forward);
        buttonFinishIcon = ContextCompat.getDrawable(getActivity(), com.heinrichreimersoftware.singleinputform.R.drawable.ic_done);


		/* Custom values */
        int[] attrs = {com.heinrichreimersoftware.singleinputform.R.attr.colorPrimary, com.heinrichreimersoftware.singleinputform.R.attr.colorPrimaryDark, android.R.attr.textColorPrimary, android.R.attr.textColorSecondary, com.heinrichreimersoftware.singleinputform.R.attr.sifNextIcon, com.heinrichreimersoftware.singleinputform.R.attr.sifFinishIcon};
        TypedArray array = getActivity().obtainStyledAttributes(attrs);

        textFieldBackgroundColor = array.getColor(0, 0);
        progressBackgroundColor = array.getColor(1, 0);
        titleTextColor = errorTextColor = array.getColor(2, 0);
        detailsTextColor = array.getColor(3, 0);

        Drawable buttonNextIcon = array.getDrawable(4);
        if(buttonNextIcon != null){
            this.buttonNextIcon = buttonNextIcon;
        }

        Drawable buttonFinishIcon = array.getDrawable(5);
        if(buttonFinishIcon != null){
            this.buttonFinishIcon = buttonFinishIcon;
        }

        array.recycle();
    }

    private Animation getAnimation(int animationResId, boolean isInAnimation){
        final Interpolator interpolator;

        if(isInAnimation){
            interpolator = new DecelerateInterpolator(1.0f);
        }
        else{
            interpolator = new AccelerateInterpolator(1.0f);
        }

        Animation animation = AnimationUtils.loadAnimation(getActivity(), animationResId);
        animation.setInterpolator(interpolator);

        return animation;
    }

    private void setupTitle(){
        titleSwitcher.setInAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.slide_in_to_bottom, true));
        titleSwitcher.setOutAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.slide_out_to_top, false));

        titleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            @Override
            public View makeView() {
                TextView view = (TextView) getActivity().getLayoutInflater().inflate(com.heinrichreimersoftware.singleinputform.R.layout.view_title, titleSwitcher, false);
                if (view != null) {
                    view.setTextColor(titleTextColor);
                }
                return view;
            }
        });

        titleSwitcher.setText("");
    }

    private void setupInput(){
        inputSwitcher.setInAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.alpha_in, true));
        inputSwitcher.setOutAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.alpha_out, false));

        inputSwitcher.removeAllViews();
        for(int i = 0; i < steps.size(); i++){
            inputSwitcher.addView(getStep(i).getView());
        }
    }

    private void setupError(){
        errorSwitcher.setInAnimation(getAnimation(android.R.anim.slide_in_left, true));
        errorSwitcher.setOutAnimation(getAnimation(android.R.anim.slide_out_right, false));

        errorSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            @Override
            public View makeView() {
                TextView view = (TextView) getActivity().getLayoutInflater().inflate(com.heinrichreimersoftware.singleinputform.R.layout.view_error, titleSwitcher, false);
                if (view != null && errorTextColor != -1) {
                    view.setTextColor(errorTextColor);
                }
                return view;
            }
        });

        errorSwitcher.setText("");
    }

    private void setupDetails(){
        detailsSwitcher.setInAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.alpha_in, true));
        detailsSwitcher.setOutAnimation(getAnimation(com.heinrichreimersoftware.singleinputform.R.anim.alpha_out, false));

        detailsSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            @Override
            public View makeView() {
                TextView view = (TextView) getActivity().getLayoutInflater().inflate(com.heinrichreimersoftware.singleinputform.R.layout.view_details, titleSwitcher, false);
                if (view != null && detailsTextColor != -1) {
                    view.setTextColor(detailsTextColor);
                }
                return view;
            }
        });

        detailsSwitcher.setText("");
    }

    private void updateStep(){
        if(stepIndex >= steps.size()){
            hideSoftInput();

            View finishedView = onCreateFinishedView(getActivity().getLayoutInflater(), container);
            if(finishedView != null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
                    finishedView.setAlpha(0);
                    finishedView.setVisibility(View.VISIBLE);
                    container.addView(finishedView);
                    finishedView.animate()
                            .alpha(1)
                            .setDuration(getResources().getInteger(
                                    android.R.integer.config_mediumAnimTime));
                }
                else {
                    finishedView.setVisibility(View.VISIBLE);
                    container.addView(finishedView);
                }
            }

            onFormFinished(setupData);
            return;
        }
        updateViews();
        containerScrollView.smoothScrollTo(0, 0);
    }

    private void hideSoftInput(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        View v = getActivity().getCurrentFocus();
        if(v == null) return;

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    protected View onCreateFinishedView(LayoutInflater inflater, ViewGroup parent){
        return null;
    }

    protected abstract void onFormFinished(Bundle data);

    private void updateViews(){
        Step step = getCurrentStep();

        if(stepIndex + 1 >= steps.size()){
            nextButton.setImageDrawable(buttonFinishIcon);
            nextButton.setContentDescription(getString(com.heinrichreimersoftware.singleinputform.R.string.finish));
            step.updateView(true);
        }
        else{
            nextButton.setImageDrawable(buttonNextIcon);
            nextButton.setContentDescription(getString(com.heinrichreimersoftware.singleinputform.R.string.next_step));
            step.updateView(false);
        }

        step.restore(setupData);

        setTextFieldBackgroundDrawable();

        inputSwitcher.setDisplayedChild(stepIndex);
        errorSwitcher.setText("");
        detailsSwitcher.setText(step.getDetails(getActivity()));
        titleSwitcher.setText(step.getTitle(getActivity()));
        stepText.setText(getString(com.heinrichreimersoftware.singleinputform.R.string.page_number, stepIndex + 1, steps.size()));

        stepText.setTextColor(detailsTextColor);

    }

    private void setTextFieldBackgroundDrawable(){
        if(textFieldBackgroundColor != -1) {
            textField.setCardBackgroundColor(textFieldBackgroundColor);
        }
    }


    protected void previousStep(){
        setupData = getCurrentStep().save(setupData);
        stepIndex--;
        updateStep();
    }

    public void showError(String message)
    {
        error = true;
        errorSwitcher.setText(message);
    }
    protected void nextStep(){
        Step step = getCurrentStep();
        boolean checkStep = checkStep();
        if(!checkStep){
            if(!error){
                error = true;
             //   errorSwitcher.setText(checkStep);
            }
        }
        else{
            error = false;
        }
        if(error){
            return;
        }
        setupData = step.save(setupData);

        stepIndex++;
        updateStep();
    }

    protected void nextStep2(){
        Step step = getCurrentStep();
        boolean checkStep = true;
        if(!checkStep){
            if(!error){
                error = true;
                //   errorSwitcher.setText(checkStep);
            }
        }
        else{
            error = false;
        }
        if(error){
            return;
        }
        setupData = step.save(setupData);

        stepIndex++;
        updateStep();
    }

    private boolean checkStep(){
        return getCurrentStep().validate();
    }

    public void setInputGravity(int gravity) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) innerContainer.getLayoutParams();
        layoutParams.gravity = gravity;
        innerContainer.setLayoutParams(layoutParams);
    }

    public int getInputGravity() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) innerContainer.getLayoutParams();
        return layoutParams.gravity;
    }
}