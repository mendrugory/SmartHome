package com.example.mendrugory.smarthomecontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mendrugory.beans.Device;
import com.example.mendrugory.beans.Response;
import com.example.mendrugory.com.example.mendrugory.utilities.Utilities;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DeviceActivity extends Activity
{
    private TextView title;
    private Button on;
    private Button off;
    private Button status;
    private Handler handler;
    private String deviceTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        deviceTitle = getIntent().getExtras().getString(
                getApplicationContext().getString(R.string.device));

        title = (TextView) findViewById(R.id.device_name);
        title.setText(deviceTitle);

        on = (Button) findViewById(R.id.device_on);
        on.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String action = getApplicationContext().getResources().getString(R.string.on);
                AsyncRequest request = new AsyncRequest();
                request.execute(new String[]{action});
            }
        });

        off = (Button) findViewById(R.id.device_off);
        off.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String action = getApplicationContext().getResources().getString(R.string.off);
                AsyncRequest request = new AsyncRequest();
                request.execute(new String[]{action});
            }
        });

        status = (Button) findViewById(R.id.device_status);
        status.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String action = getApplicationContext().getResources().getString(R.string.status);
                AsyncRequest request = new AsyncRequest();
                request.execute(new String[]{action});
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
                    }
                    else
                    {
                        Response response = (Response) msg.obj;
                        Toast.makeText(getApplicationContext(), buildMessage(response),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
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

    private String buildMessage(Response response)
    {

        StringBuilder builder = new StringBuilder();

        switch (response.getCode())
        {
            case 0:
                builder.append("Code: ");
                builder.append(String.valueOf(response.getCode()));
                builder.append(". Error");
                break;
            case 1:
                builder.append("Code: ");
                builder.append(String.valueOf(response.getCode()));
                builder.append(". Failure");
                break;
            case 2:
                builder.append("Code: ");
                builder.append(String.valueOf(response.getCode()));
                builder.append(". Success");
                break;
            case 4:
                if(response.isStatus())
                {
                    builder.append("ON !!!!");
                }
                else
                {
                    builder.append("OFF !!!!");
                }
                break;
        }
        return builder.toString();
    }

    private class AsyncRequest extends AsyncTask<String, Void, String>
    {
        private final static int STATUS_CODE_SUCCESS = 200;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            progressDialog = ProgressDialog.show(DeviceActivity.this,
                    getApplicationContext().getString(R.string.waiting_response),
                    getApplicationContext()
                            .getString(R.string.waiting), true);
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... variables)
        {
            String message = null;

            String action = variables[0];

            String urlString = Utilities.getUrl(getApplicationContext(), action, deviceTitle);
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
            Response response = new Response();
            try
            {
                if (result != null)
                {
                    JSONObject jsonObject = new JSONObject(result);

                    response.setCode((Integer) jsonObject.get("code"));

                    Object status = jsonObject.get("status");
                    if(status != JSONObject.NULL)
                    {
                        response.setStatus((Boolean) status);
                    }

                    Object message = jsonObject.get("message");
                    if(message != JSONObject.NULL)
                    {
                        response.setMessage((String) message);
                    }

                    msg.obj = response;

                } else
                {
                    msg.obj = null;
                }
            }
            catch (Exception e)
            {
                Log.e(DeviceActivity.class.getName(), e.getMessage());
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
