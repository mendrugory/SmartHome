package com.example.mendrugory.smarthomecontroller;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mendrugory.beans.Device;
import com.example.mendrugory.com.example.mendrugory.utilities.Utilities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MyActivity extends Activity
{
    private Button getDevices;
    private Handler handler;
    private ListView listOfDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        listOfDevices = (ListView) findViewById(R.id.devices);

        getDevices = (Button) findViewById(R.id.get_devices);
        getDevices.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AsyncRequest request = new AsyncRequest();
                request.execute(new String[]{});
            }
        });

        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                if (msg.obj != null)
                {
                    if ((msg.obj) instanceof String)
                    {
                        String message = (String) msg.obj;
                        Toast.makeText(getApplicationContext(), message,
                                Toast.LENGTH_SHORT).show();
                    } else
                    {
                        List<String> arrayDevices = new ArrayList<String>();
                        for (Device device : (List<Device>) msg.obj)
                        {
                            arrayDevices.add(device.getPlace());
                        }

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                getApplicationContext(),
                                android.R.layout.simple_list_item_1,
                                arrayDevices);

                        listOfDevices.setAdapter(arrayAdapter);
                        listOfDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
                        {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id)
                            {
                                Intent intent = new Intent(getApplicationContext(), DeviceActivity.class);
                                String key = getApplicationContext().getResources().getString(R.string.device);
                                String value = (String) parent.getAdapter().getItem(position);
                                intent.putExtra(key, value);
                                startActivity(intent);
                            }
                        });
                    }
                } else
                {
                    Toast.makeText(
                            getApplicationContext(), "Error",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class AsyncRequest extends AsyncTask<String, Void, String>
    {
        private final static int STATUS_CODE_SUCCESS = 200;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(MyActivity.this,
                    getApplicationContext().getString(R.string.waiting_devices_title),
                    getApplicationContext()
                            .getString(R.string.waiting), true);
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... variables)
        {
            String message = null;

            String urlString = Utilities.getUrl(getApplicationContext(), getResources().getString(R.string.get_devices), null);
            URL url = null;
            HttpURLConnection urlConnection = null;
            try
            {
                url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                message = getResponseBody(urlConnection.getInputStream());
            }
            catch (MalformedURLException e)
            {
                Log.e(MyActivity.class.getName(), "Error: " + e.getMessage());
            }
            catch (IOException e)
            {
                Log.e(MyActivity.class.getName(), "Error: " + e.getMessage());
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
            }

            return message;
        }

        private boolean isSuccesfulConnection(HttpResponse response)
        {
            boolean ok = false;
            if (response != null)
            {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                ok = statusCode == STATUS_CODE_SUCCESS;
            }
            return ok;
        }

        private String getResponseBody(InputStream content) throws IOException
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    content));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result)
        {
            Message msg = new Message();
            List<Device> devices = new ArrayList<Device>();
            try
            {
                if (result != null)
                {
                    JSONObject jsonObject = new JSONObject(result);
                    Iterator<?> keys = jsonObject.keys();

                    while (keys.hasNext())
                    {
                        Device device = new Device();
                        String place = (String) keys.next();
                        String name = (String) jsonObject.get(place);
                        device.setName(name);
                        device.setPlace(place);

                        devices.add(device);
                    }

                    msg.obj = devices;
                } else
                {
                    msg.obj = null;
                }
            }
            catch (Exception e)
            {
                Log.e(MyActivity.class.getName(), e.getMessage());
                msg.obj = e.getMessage();
            }
            finally
            {
                dismissProgressDialog();
                handler.sendMessage(msg);
            }
        }


        private void dismissProgressDialog()
        {
            if (progressDialog != null)
            {
                progressDialog.dismiss();
            }
        }

    }

}
