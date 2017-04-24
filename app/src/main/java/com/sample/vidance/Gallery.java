package com.sample.vidance;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import com.sample.vidance.helper.HttpHandler;
import com.sample.vidance.helper.SQLiteHandler;
import com.sample.vidance.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Michelle on 12/4/2017.
 */

public class Gallery extends AppCompatActivity {
    // Set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    private String TAG = Gallery.class.getSimpleName();
    public static final String TUTORIALVID_URL = "http://thevidance.com/test/upload.php";
    public static final String USERVID_URL = "http://thevidance.com/test/retrieve.php";

    private ProgressDialog pDialog;
    // URL to get contacts JSON
    private ArrayList<LinkedHashMap<String, String>> videoMap = new ArrayList<>();
    private ArrayList<Bitmap> videoThumbnails = new ArrayList<Bitmap>();
    private ArrayList<String> videoLocation = new ArrayList<String>();
    // Flag for which one is used for images selection
    private GridView videoList;
    private Cursor _cursor;
    private int _columnIndex;
    private int[] _videosId;
    private Uri _contentUri;
    private String filename, fullView, selectedPath, choiceURL;
    int flag = 0;
    private SQLiteHandler db;
    private SessionManager session;
    private View choice, preview, buttons;
    private Button btnCancel, btnDwnl, btnSend, btnFull, instructor, user, gallery;

    protected Context _context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getApplicationContext();
        setContentView(R.layout.activity_gallery);
        // Set GridView for gallery
        videoList = (GridView) findViewById(R.id.gridView);
        // Set default as external/sdcard uri
        _contentUri = MEDIA_EXTERNAL_CONTENT_URI;
        fullView = null; // No video is selected on default

        // Initialise view & button values
        choice = findViewById(R.id.galleryChoice);
        preview = findViewById(R.id.galleryPreview);
        buttons = findViewById(R.id.galleryButtons);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnDwnl = (Button) findViewById(R.id.btnDownload);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnFull = (Button) findViewById(R.id.btnFull);
        instructor = (Button) findViewById(R.id.frmInstruct);
        gallery = (Button) findViewById(R.id.frmGallery);
        user = (Button) findViewById(R.id.frmUser);

        // Hide views until choice is made
        preview.setVisibility(View.GONE);
        buttons.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        // Button onclick events
        choiceSelect();
    }

    //Load videos from selected locations
    public void choiceSelect() {
        instructor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                choice.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnDwnl.setVisibility(View.VISIBLE);
            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
                alertDialogBuilder.setTitle("Upload video")
                        .setMessage("This page requires a large amount of INTERNET usage, proceed?\n(Wi-Fi is recommended to prevent additional charges)")
                        .setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                choice.setVisibility(View.GONE);
                                preview.setVisibility(View.VISIBLE);
                                buttons.setVisibility(View.VISIBLE);
                                btnCancel.setVisibility(View.VISIBLE);
                                btnSend.setVisibility(View.GONE);
                                btnDwnl.setVisibility(View.VISIBLE);
                                choiceURL = USERVID_URL;
                                new getVideos().execute();
                                buttonPress();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //Do Nothing
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

        gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                choice.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);
                buttons.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                btnDwnl.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
                choiceURL = null;
                initVideosId();
                buttonPress();
                // Set gallery adapter
                setGalleryAdapter();
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Bitmap> images;

        // Constructor
        public ImageAdapter(Context c, ArrayList<Bitmap> images) {
            mContext = c;
            this.images = images;
        }

        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
                imageView.setId(position);
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        showToast("Video selected! Please wait for the video to load!");
                        preview(imageView.getId());
                    }
                });
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(images.get(position));
            return imageView;
        }
    }



    private void preview(int index){

        Uri uri = Uri.parse(videoLocation.get(index));
        VideoView video = (VideoView)findViewById(R.id.videoView);
        video.setVideoURI(uri); // Load selected Video into VideoView

        fullView = uri.toString();

        MediaController mediaController = new MediaController(Gallery.this); // Create video buttons
        mediaController.setAnchorView(findViewById(R.id.mediaController));
        video.setMediaController(mediaController);


        String title = fullView.replace("http://www.thevidance.com/test/videos/", "");
        selectedPath = fullView;

        TextView text = (TextView) findViewById(R.id.videoName);
        text.setText("Preview:" + title);
        String fontPath = "fonts/CatCafe.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        text.setTypeface(tf);

        mediaController.show();
        video.start();
    }

    private void setGalleryAdapter() {
        videoList.setAdapter(new VideoGalleryAdapter(_context));
        videoList.setOnItemClickListener(_itemClickLis);
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

    public static Bitmap generateThumbnail(String videoPath)
    {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 96, 96);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (mediaMetadataRetriever != null)
            {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    private class getVideos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Gallery.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(choiceURL);

            Log.e(TAG, "Response from url: " + jsonStr);
            //ArrayList<String> videoList = new ArrayList<String>();

            if (jsonStr != null) {
                try {
                    // Retrieve the JSON object
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Values from the Videos Array
                    JSONArray videos = jsonObj.getJSONArray("Videos");

                    // Get all values within the array
                    for (int i = 0; i < videos.length(); ++i) {
                        String video = videos.getString(i);

                        LinkedHashMap<String, String> videoURL = new LinkedHashMap<>();
                        videoURL.put("videos", video);
                        videoMap.add(videoURL);
                    }

                    int x = 0;
                    for(HashMap<String, String> map: videoMap) {
                        for(Map.Entry<String, String> mapEntry: map.entrySet()) {
                            Bitmap thumb = generateThumbnail(mapEntry.getValue());
                            videoThumbnails.add(thumb); //Video URL
                            videoLocation.add(mapEntry.getValue());
                            x++;
                        }
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            videoList.setAdapter(new ImageAdapter(_context,videoThumbnails));
        }
    }

    /** Automatically hides all behaviour options other than1, then displays them on button press **/
    public void buttonPress() {
        // Download video
        btnDwnl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
                alertDialogBuilder.setTitle("Download video")
                        .setMessage("Download the selected video? (Requires INTERNET access)")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //Do Nothing
                                Toast.makeText(getApplicationContext(), "Video not downloaded!", Toast.LENGTH_SHORT).show();
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
        // Download File
        btnDwnl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
                alertDialogBuilder.setTitle("Upload video")
                        .setMessage("Open video file in browser for viewing / download?")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedPath));
                                startActivity(browserIntent);
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel(); //Do Nothing
                                Toast.makeText(getApplicationContext(), "Video not saved!", Toast.LENGTH_SHORT).show();
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
