package com.arks_layer.pso2_alert;

import android.content.Context;

public class SelectedServer
{
    Context _context = null;
    Preferences sharedPref = null;

    private String _Title;
    private String _Code;
    private Boolean _Enabled = true;

    public Boolean isEnabled() {
        return _Enabled;
    }

    public void setEnabled(Boolean newEnabled)
    {
            _Enabled = newEnabled;
            sharedPref.putBoolean(_Code, newEnabled);
    }

    public String getTitle() {
        return _Title;
    }

    public String getCode() {
        return _Code;
    }

    public SelectedServer(Context context, String ServerTitle, String ServerCode, Boolean Enabled)
    {
        super();
        this._context = context;
        
        sharedPref = new Preferences(_context);

        this._Title = ServerTitle;
        this._Code = ServerCode;
        this._Enabled = Enabled;

    }
}