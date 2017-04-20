package com.sample.vidance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.VideoView;

import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import java.io.File;


/**
 * Created by Michelle on 12/4/2017.
 */

public class Gallery extends AppCompatActivity {
    //set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    //flag for which one is used for images selection
    private GridView _gallery;
    private Cursor _cursor;
    private int _columnIndex;
    private int[] _videosId;
    private Uri _contentUri;
    private String filename, fullView, selectedPath;
    int flag = 0;
    private SQLiteHandler db;
    private SessionManager session;

    protected Context _context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getApplicationContext();
        setContentView(R.layout.activity_gallery);
        //set GridView for gallery
        _gallery = (GridView) findViewById(R.id.gridView);
        //set default as external/sdcard uri
        _contentUri = MEDIA_EXTERNAL_CONTENT_URI;
        fullView = null; // No video is selected on default
        initVideosId();
        buttonPress();

        //set gallery adapter
        setGalleryAdapter();


    }
    private void setGalleryAdapter() {
        _gallery.setAdapter(new VideoGalleryAdapter(_context));
        _gallery.setOnItemClickListener(_itemClickLis);
        flag = 1;
    }
    private AdapterView.OnItemClickListener _itemClickLis = new OnItemClickListener()
    {
        @SuppressWarnings({ "deprecation", "unused", "rawtypes" })
        public void onItemClick(AdapterView parent, View v, int position, long id)
        {
            // Now we want to actually get the data location of the file
            String [] proj={MEDIA_DATA};
            // We request our cursor again
            _cursor = managedQuery(_contentUri, proj, // Which columns to return
                    MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                    new String[] {"%Movies%ViDance%"},       // WHERE clause selection arguments (none) (Selects only videos from ViDance folder)
                    null); // Order-by clause (ascending by name)
            // Column index for the data uri
            int count = _cursor.getCount();
            _cursor.moveToFirst();
            _columnIndex = _cursor.getColumnIndex(MEDIA_DATA);
            // Move to the selected item in the cursor
            _cursor.moveToPosition(position);
            // Retrieve filename
            filename = _cursor.getString(_columnIndex);


            Uri uri = Uri.parse(filename);
            VideoView video = (VideoView)findViewById(R.id.videoView);
            video.setVideoURI(uri); // Load selected Video into VideoView

            fullView = filename;

            MediaController mediaController = new MediaController(Gallery.this); // Create video buttons
            mediaController.setAnchorView(findViewById(R.id.mediaController));
            video.setMediaController(mediaController);

            File f = new File("" + filename);
            String title = f.getName();
            selectedPath = f.getAbsolutePath();

            TextView text = (TextView) findViewById(R.id.videoName);
            text.setText("Preview:" + title);
            String fontPath = "fonts/CatCafe.ttf";
            Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
            text.setTypeface(tf);

            mediaController.show();
            video.start();
        }
    };

    @SuppressWarnings("deprecation")
    private void initVideosId() {
        try
        {
            //Here we set up a string array of the thumbnail ID column we want to get back
            String [] proj={_ID};
            // Now we create the cursor pointing to the external thumbnail store
            _cursor = managedQuery(_contentUri,
                    proj, // Which columns to return
                    MEDIA_DATA + " like ? ",       // WHERE clause; which rows to return (all rows)
                    new String[] {"%Movies%ViDance%"},       // WHERE clause selection arguments (none) [Movies/Vidance folder from SD card]
                    null); // Order-by clause (ascending by name)
            int count= _cursor.getCount();
            // We now get the column index of the thumbnail id
            _columnIndex = _cursor.getColumnIndex(_ID);
            //initialize
            _videosId = new int[count];
            //move position to first element
            _cursor.moveToFirst();
            for(int i=0;i<count;i++)
            {
                int id = _cursor.getInt(_columnIndex);
                //
                _videosId[i]= id;
                //
                _cursor.moveToNext();
                //
            }
        }catch(Exception ex)
        {
            showToast(ex.getMessage().toString());
        }

    }
    protected void showToast(String msg)
    {
        Toast.makeText(_context, msg, Toast.LENGTH_LONG).show();
    }

    //Create video adapter to load all videos into thumbnails
    private class VideoGalleryAdapter extends BaseAdapter
    {
        public VideoGalleryAdapter(Context c)
        {
            _context = c;
        }
        public int getCount()
        {
            return _videosId.length;
        }
        public Object getItem(int position)
        {
            return position;
        }
        public long getItemId(int position)
        {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imgVw= new ImageView(_context);;
            try
            {
                if(convertView!=null)
                {
                    imgVw= (ImageView) convertView;
                }
                imgVw.setImageBitmap(getImage(_videosId[position]));
                imgVw.setLayoutParams(new GridView.LayoutParams(200, 200));
                imgVw.setPadding(8, 8, 8, 8);
            }
            catch(Exception ex)
            {
                System.out.println("Gallery:getView()-135: ex " + ex.getClass() +", "+ ex.getMessage());
            }
            return imgVw;
        }

        // Create Video Thumbnail
        private Bitmap getImage(int id) {
            Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
            return thumb;
        }
    }


    /** Automatically hides all behaviour options other than1, then displays them on button press **/
    public void buttonPress() {
        Button btnSend = (Button) findViewById(R.id.btnSend);
        Button btnFull = (Button) findViewById(R.id.btnFull);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);

        // Send video
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
                alertDialogBuilder.setTitle("Upload video")
                        .setMessage("Do you wish to send the recorded video to the instructor for viewing?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                uploadVideo(); //Send to database
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //Do Nothing
                                Toast.makeText(getApplicationContext(), "Video not sent!", Toast.LENGTH_SHORT).show();
                            }
                        });
                //Create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                //Show it
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#23C8B2"));
            }
        });

        // Open video with default video player
        btnFull.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(fullView!=null) {
                    Intent openVideo = new Intent(Intent.ACTION_VIEW);
                    openVideo.setDataAndType(Uri.parse(fullView), "video/*");
                    startActivity(openVideo);
                }
                else {
                    showToast("No video selected!");
                }
            }
        });

        // Cancel submission and go back
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Gallery.this, Dashboard.class); // Return to Dashboard
                startActivity(intent);
            }
        });
    }

    private void uploadVideo() {
        class UploadVideo extends AsyncTask<Void, Void, String> {

            ProgressDialog uploading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                uploading = ProgressDialog.show(Gallery.this, "Uploading File", "Please wait...", false, false);
            }

            @Override
            protected void onPostExecute(String s) {
                uploading.dismiss();
                //super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), "Video successfully uploaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                return u.uploadVideo(selectedPath);
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent = new Intent(Gallery.this, Dashboard.class); // Return to Dashboard
        startActivity(intent);
    }
}
