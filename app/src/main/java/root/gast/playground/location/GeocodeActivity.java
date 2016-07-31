/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import root.gast.playground.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Activity that performs geocoding on a user entered location.
 * 
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class GeocodeActivity extends ListActivity
{
    private static final String TAG = "GeocodeActivity";
    private static final int MAX_ADDRESSES = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.geocode);
    }

    public void onLookupLocationClick(View view)
    {
        if (Geocoder.isPresent())
        {
            EditText addressText = (EditText) findViewById(R.id.enterLocationValue);
            
            try
            {
                List<Address> addressList = new Geocoder(this).getFromLocationName(addressText.getText().toString(), MAX_ADDRESSES);
                
                List<AddressWrapper> addressWrapperList = new ArrayList<AddressWrapper>();
                
                for (Address address : addressList)
                {
                    addressWrapperList.add(new AddressWrapper(address));
                }
                
                setListAdapter(new ArrayAdapter<AddressWrapper>(this, android.R.layout.simple_list_item_single_choice, addressWrapperList));
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not geocode address", e);
                
                new AlertDialog.Builder(this)
                    .setMessage(R.string.geocodeErrorMessage)
                    .setTitle(R.string.geocodeErrorTitle)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).show();
            }
        }
    }

    public void onOkClick(View view)
    {
        ListView listView = getListView();
        
        Intent intent = getIntent();
        if (listView.getCheckedItemPosition() != ListView.INVALID_POSITION)
        {
            AddressWrapper addressWrapper = (AddressWrapper)listView.getItemAtPosition(listView.getCheckedItemPosition());
            
            intent.putExtra("name", addressWrapper.toString());
            intent.putExtra("latitude", addressWrapper.getAddress().getLatitude());
            intent.putExtra("longitude", addressWrapper.getAddress().getLongitude());
        }
        
        this.setResult(RESULT_OK, intent);
        finish();
    }

    private static class AddressWrapper
    {
        private Address address;

        public AddressWrapper(Address address)
        {
            this.address = address;
        }

        @Override
        public String toString()
        {
            StringBuilder stringBuilder = new StringBuilder();
            
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++)
            {
                stringBuilder.append(address.getAddressLine(i));
                
                if ((i + 1) < address.getMaxAddressLineIndex())
                {
                    stringBuilder.append(", ");
                }
            }
            
            return stringBuilder.toString();
        }

        public Address getAddress()
        {
            return address;
        }
    }
}
