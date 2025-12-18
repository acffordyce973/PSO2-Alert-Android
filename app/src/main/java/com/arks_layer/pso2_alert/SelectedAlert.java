package com.arks_layer.pso2_alert;

import android.content.Context;

public class SelectedAlert {
    Context _context = null;
    Preferences sharedPref = null;

    private String _Title;
    public String _Key;

    public Boolean isEnabled() {
        return sharedPref.getBoolean(_Key, true);
    }

    public void setEnabled(Boolean newEnabled)
    {
        sharedPref.putBoolean(_Key, newEnabled);
    }

    public String getTitle() {
        return _Title;
    }

    SelectedAlert(Context context, String Title)
    {
        super();
        this._context = context;

        sharedPref = new Preferences(_context);

        this._Title = Title;
        this._Key = String.valueOf(Title.hashCode());
    }
}
