/*
 * Copyright 2011 Jon A. Webb
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.image.simplecapture;

import java.io.File;

import root.gast.playground.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SimpleCaptureActivity extends Activity
{
    private final int PICTURE_ACTIVITY_CODE = 1;
    private final String FILENAME = "sdcard/photo.jpg";

    private Button mButtonCapture;
    private File mFile;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_simplecapture);
        mButtonCapture = (Button) findViewById(R.id.btnCapture);
        mButtonCapture.setOnClickListener(mCaptureListener);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICTURE_ACTIVITY_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                Uri inputFileUri = Uri.fromFile(mFile);
                imageView.setImageURI(inputFileUri);
            }
        }
    }

    private void launchTakePhoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mFile = new File(FILENAME);
        Uri outputFileUri = Uri.fromFile(mFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, PICTURE_ACTIVITY_CODE);
    }

    private OnClickListener mCaptureListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            launchTakePhoto();
        }
    };
}