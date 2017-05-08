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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.VideoView;

import com.sample.vidance.app.AppController;
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
    public static final String STORIES_URL = "http://thevidance.com/test/showStories.php";
    public static final String USERVID_URL = "http://thevidance.com/test/retrieve.php";
    private String TAG = Gallery.class.getSimpleName();
    private SessionManager session;
    private SQLiteHandler db;
    private String userID;
    private ProgressDialog pDialog;
    // URL to get contacts JSON
    private ArrayList<LinkedHashMap<String, String>> videoMap = new ArrayList<>();
    private ArrayList<Bitmap> videoThumbnails = new ArrayList<Bitmap>();
    private ArrayList<String> videoLocation = new ArrayList<String>();
    private ArrayList<LinkedHashMap<String, String>> storyMap = new ArrayList<>();
    private ArrayList<String> storyList = new ArrayList<String>();
    // Flag for which one is used for images selection
    private GridView videoList;
    private Cursor _cursor;
    private int _columnIndex;
    private int[] _videosId;
    private Uri _contentUri;
    private String filename, fullView, selectedPath, choiceURL;
    int flag = 0;
    private View choice, preview, buttons;
    private ListView sList;
    private Button btnCancel, btnDwnl, btnSend, btnFull, instructor, user, gallery;
    private String fontPath;
    private Typeface tf;

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
        sList = (ListView) findViewById(R.id.storyList);
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
        sList.setVisibility(View.GONE);

        // Initialize font styles
        fontPath = "fonts/CatCafe.ttf";
        tf = Typeface.createFromAsset(getAssets(), fontPath);
        fontPath = "fonts/James_Fajardo.ttf";
        Typeface jf = Typeface.createFromAsset(getAssets(), fontPath);
        btnDwnl.setTypeface(jf);
        btnCancel.setTypeface(jf);
        btnSend.setTypeface(jf);
        btnFull.setTypeface(jf);
        instructor.setTypeface(jf);
        gallery.setTypeface(jf);
        user.setTypeface(jf);
        // Session manager
        session = new SessionManager(getApplicationContext());
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // Button onclick events
        choiceSelect();

        userID = db.getUserID();
    }

    public AlertDialog alertStyler(String title, String msg, final String selection) { // Function to create a pop up
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
        alertDialogBuilder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform different functions for different popups on this page
                        if(selection.equals("instructor")) {
                            choiceURL = STORIES_URL;
                            changeView();
                            new getStories().execute();
                            buttonPress();
                        } else if (selection.equals("user")) {
                            choiceURL = USERVID_URL;
                            changeView();
                            new getVideos().execute();
                            buttonPress();
                        } else if (selection.equals("upload")) {
                            if (fullView == null) {
                                showToast("No video selected!");
                            } else {
                                uploadVideo();
                            }
                        } else if (selection.equals("download")) {
                            if (fullView == null) {
                                showToast("No video selected!");
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedPath));
                                startActivity(browserIntent);
                            }
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); //Do Nothing
                        if (selection.equals("upload")) {
                            showToast("Video not sent!");
                        }
                    }
                });
        //Create alert dialog
        AlertDialog ad = alertDialogBuilder.create();
         ad.show(); //Show it
        ad.getButton(ad.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E77F7E"));
        ad.getButton(ad.BUTTON_POSITIVE).setTextColor(Color.parseColor("#23C8B2"));
        return ad;
    }

    //Load videos from selected locations
    public void choiceSelect() {
        instructor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("View Social Stories", "This page requires INTERNET usage, proceed?\n(Wi-Fi is recommended to prevent additional charges)", "instructor");
            }
        });

        user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("View Uploaded Videos", "This page requires INTERNET usage, proceed?\n(Wi-Fi is recommended to prevent additional charges)", "user");
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                choiceURL = "";
                changeView();
                initVideosId();
                buttonPress();
                setGalleryAdapter(); // Set gallery adapter
            }
        });
    }

    public void changeView() {
        choice.setVisibility(View.GONE);
        preview.setVisibility(View.VISIBLE);
        buttons.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
        if (choiceURL.equals(USERVID_URL)) {
            sList.setVisibility(View.GONE);
            btnSend.setVisibility(View.GONE);
            btnDwnl.setVisibility(View.VISIBLE);
        }
        else if (choiceURL.equals(STORIES_URL)) {
            sList.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.GONE);
            btnDwnl.setVisibility(View.GONE);
            btnFull.setVisibility(View.GONE);
        }
        else {
            sList.setVisibility(View.GONE);
            btnDwnl.setVisibility(View.GONE);
            btnSend.setVisibility(View.VISIBLE);
            btnFull.setVisibility(View.VISIBLE);
        }
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

    /**Load video thumbnails into gridview**/
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

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr); // Retrieve the JSON object
                    JSONArray videos = jsonObj.getJSONArray("Videos"); // Values from the Videos Array
                    // Get all values within the array
                    for (int i = 0; i < videos.length(); ++i) {
                        String video = videos.getString(i);

                        LinkedHashMap<String, String> videoURL = new LinkedHashMap<>(); // Assign to a LinkedHashMap
                        videoURL.put("videos", video);
                        videoMap.add(videoURL);
                    }
                    int x = 0;
                    for(HashMap<String, String> map: videoMap) {
                        for(Map.Entry<String, String> mapEntry: map.entrySet()) {
                            Bitmap thumb = generateThumbnail(mapEntry.getValue());
                            videoThumbnails.add(thumb); // Generate thumbnail from video
                            videoLocation.add(mapEntry.getValue());
                            x++;
                        }
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Json parsing error: " + e.getMessage());
                        }
                    });

                }
            }
            else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Couldn't get json from server. Check LogCat for possible errors!");
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

    /** Fill Story List **/
    private class getStories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute(); // Show progress dialog
            pDialog = new ProgressDialog(Gallery.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(choiceURL); // Making a request to url and getting response
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray stories = jsonObj.getJSONArray("Stories"); // Get JSON Array
                    // Get all values within the array
                    for (int i = 0; i < stories.length(); ++i) {
                        storyList.add(stories.getString(i)); //Add into array
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Json parsing error: " + e.getMessage());
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Couldn't get json from server. Check LogCat for possible errors!");
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result); // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Gallery.this, android.R.layout.simple_list_item_1, storyList);
            sList.setAdapter(arrayAdapter); // Populates List view with data retrieved
            sList.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                    showToast(sList.getItemAtPosition(position).toString());
                }
            });
        }
    }

    /** Automatically hides all behaviour options other than1, then displays them on button press **/
    public void buttonPress() {
        // Send video
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("Upload video", "Do you wish to send the selected video to the instructor for viewing?", "upload");
            }
        });
        // Download File
        btnDwnl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertStyler("Download video", "Open video file in browser for viewing / download?", "download");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Gallery.this);
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
                changeActivity(Dashboard.class);
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
                Toast.makeText(getApplicationContext(), "Video successfully uploaded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected String doInBackground(Void... params) {
                Upload u = new Upload();
                return u.uploadVideo(selectedPath, userID);
            }
        }
        UploadVideo uv = new UploadVideo();
        uv.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Gallery.this, MenuItems.class);
        switch(item.getItemId()) {
            case R.id.action_notifications:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_contact:
                finish();
                intent.putExtra("SELECTED_ACTIVITY", "Contact");
                startActivity(intent);
                break;
            case R.id.action_about:
                finish();
                intent.putExtra("SELECTED_ACTIVITY", "About");
                startActivity(intent);
                break;
            case R.id.action_help:
                Toast.makeText(getApplicationContext(), "Currently unavailable!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_logout:
                logoutUser();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void logoutUser() {
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        // session manager
        session = new SessionManager(getApplicationContext());
        session.setLogin(false);
        db.deleteUsers();
        AppController.getInstance().setUser(null);
        changeActivity(Login.class);
    }

    public void changeActivity(Class activity) {
        finish();
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        changeActivity(Dashboard.class);
    }
}
