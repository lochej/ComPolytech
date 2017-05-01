package com.polytech.communicationpolytech;

import android.content.Context;
import android.view.View;
import android.widget.MediaController;

public class ConstantAnchorMediaController extends MediaController {

    public ConstantAnchorMediaController(Context context, View anchor) {
        super(context);
        super.setAnchorView(anchor);
    }

    @Override
    public void setAnchorView(View view) {
        // Do nothing
    }
}
