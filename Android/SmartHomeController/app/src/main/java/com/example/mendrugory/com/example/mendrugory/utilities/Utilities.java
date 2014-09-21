package com.example.mendrugory.com.example.mendrugory.utilities;

import android.app.Activity;
import android.content.Context;

import com.example.mendrugory.smarthomecontroller.R;

/**
 * Created by mendrugory on 20/09/14.
 */
public class Utilities
{
    public static String getUrl(Context cntx, String urlPart, String device)
    {
        String url = cntx.getResources().getString(R.string.root) + urlPart;
        url = url.replace(cntx.getResources().getString(R.string.server), cntx.getResources().getString(R.string.server_host));
        url = url.replace(cntx.getResources().getString(R.string.port), cntx.getResources().getString(R.string.server_port));

        if (device != null)
        {
            url = url.replace(cntx.getResources().getString(R.string.device), device);
        }

        return url;
    }
}
