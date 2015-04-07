package com.ythogh.howdoyou;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CreateTutorial extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private static final String[] set_of_tags = new String[]
            {"Art", "Books", "Cars", "Design", "Engineering", "Food", "Garden",
                    "Home", "Ingenuity", "Jests", "Kids", "Love", "Makeup", "Notes",
                    "Outdoors", "Photography", "Quarry", "Racing", "Sleep", "Teaching",
                    "Undo", "Viral", "Workshop", "Xylophone", "You", "Zoology"};
    private static final int INITIAL_SIZE = set_of_tags.length;
    private static final int NUM_PAGES = 3;
    private static final int REQUEST_VIDEO_CAPTURE = 1;

    TextView tvTitle, tvMyTags, tvPage, tvCount;
    EditText etTitle, etTags;
    ImageView ivAdd;
    GridView gridview;
    Button btStartStop;
    VideoView vvFrame;

    HashMap<Integer, View> mSelected;
    HashMap<Integer, SetupViewHolder> nSelected;
    HashMap<Integer, Boolean> selected;
    HashMap<Integer, Boolean> firstTimeNew;

    String mTitle;
    ArrayList<String> mTags, common_tags;
    ArrayList<Bitmap> mBitmapArray = new ArrayList<Bitmap>();

    ViewPager vfPager;
    PagePagerAdapter mPagerAdapter;
    ArrayAdapter<String> adapter;

    MediaMetadataRetriever retriever;
    SlidingProgression<Integer> slidingProgression;

    String path = "";
    int frames = 0;
    long st = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tutorial);
        System.out.println("Remade");
        mTitle = "";

        tvPage = (TextView) findViewById(R.id.CREATETUTORIAL_TEXTVIEW_CREATETUTORIAL);
        vfPager = (ViewPager) findViewById(R.id.viewPagerCreateTutorial);
        mPagerAdapter = new PagePagerAdapter(getSupportFragmentManager());
        vfPager.setAdapter(mPagerAdapter);
        vfPager.setOnPageChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float v, int i2) {
        switch (position) {
            case 0:
                break;
            case 1:

                break;
            case 2:
                if (mTitle.trim().length() == 0) {
                    mTitle = "Untitled";
                }
                tvTitle.setText(mTitle);
                String ms = "";
                if (mTags != null && mTags.size() > 0) {
                    ms = mTags.get(0);
                    for (int i = 1; i < mTags.size(); i++) {
                        ms = ms + ", " + mTags.get(i);
                    }
                } else {
                    ms = "No tags selected";
                }
                tvMyTags.setText(ms);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                if (mTitle.trim().length() == 0) {
                    mTitle = "Untitled";
                }
                tvTitle.setText(mTitle);
                String ms = "";
                if (mTags != null && mTags.size() > 0) {
                    ms = mTags.get(0);
                    for (int i = 1; i < mTags.size(); i++) {
                        ms = ms + ", " + mTags.get(i);
                    }
                } else {
                    ms = "No tags selected";
                }
                tvMyTags.setText(ms);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class PagePagerAdapter extends FragmentStatePagerAdapter {

        public PagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle extras = new Bundle();
            extras.putInt("position", position);
            switch (position) {
                case 0:
                    CreateTutorialSetupFragment ctsf = new CreateTutorialSetupFragment();
                    ctsf.setArguments(extras);
                    return ctsf;
                case 1:
                    CreateTutorialPageOneFragment ctpof = new CreateTutorialPageOneFragment();
                    ctpof.setArguments(extras);
                    return ctpof;
                case 2:
                    CreateTutorialFinalizeFragment ctff = new CreateTutorialFinalizeFragment();
                    ctff.setArguments(extras);
                    return ctff;
                default:
                    break;
            }
          return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private class CreateTutorialPageOneFragment extends Fragment implements View.OnClickListener {

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_setup_tutorial_page_one, container, false);
            tvCount = (TextView) rootView.findViewById(R.id.SETUPTUTORIAL_TEXTVIEW_COUNT);
            btStartStop = (Button) rootView.findViewById(R.id.SETUPTUTORIAL_BUTTON_STARTSTOP);
            vvFrame = (VideoView) rootView.findViewById(R.id.SETUPTUTORIAL_VIDEOVIEW_FRAME);
            btStartStop.setOnClickListener(this);
            return rootView;
        }

        private void dispatchTakeVideoIntent() {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                Uri videoUri = data.getData();
                MediaPlayer mp = MediaPlayer.create(getActivity(), videoUri);
                System.out.println("Duration: " + mp.getDuration());
                mp.release();
                vvFrame.setVideoURI(videoUri);
                vvFrame.pause();
                vvFrame.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.start();
                    }
                });

                vvFrame.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (vvFrame.isPlaying()) {
                            vvFrame.pause();
                        } else {
                            vvFrame.start();
                        }
                        return false;
                    }
                });

                Cursor cursor = null;
                try {

                    String[] proj = { MediaStore.Images.Media.DATA };
                    cursor = getActivity().getContentResolver().query(videoUri,  proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    System.out.println("COLUMN INDEX: " + cursor.getString(column_index));
                    path =  cursor.getString(column_index);
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                new UploadVideoTask(path).execute();
                System.out.println(path);
                getVideoFrame(getActivity(), videoUri);
            }
        }

        public Bitmap getVideoFrame(Context context, Uri uri) {
            retriever = new MediaMetadataRetriever();
            Cursor cursor = null;
            try {
                String[] proj = { MediaStore.Images.Media.DATA };
                cursor = context.getContentResolver().query(uri,  proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                System.out.println("COLUMN INDEX: " + cursor.getString(column_index));
                cursor.close();
                retriever.setDataSource(context, uri);
                MediaPlayer mp = MediaPlayer.create(getActivity(), uri);
                frames = mp.getDuration() / 1000 * 24;
                mp.release();
                //path =  Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera";
                st = Calendar.getInstance().getTimeInMillis() / 1000;
                new GetFramesFromVideo(uri, retriever).execute();
            } catch (Exception ex) {
                ex.printStackTrace();
                retriever.release();
            }
            return null;
        }

        @Override
        public void onClick(View v) {
            dispatchTakeVideoIntent();
        }

        private void onFrameGotten(Bitmap bmp) {
            mBitmapArray.add(bmp);
            slidingProgression.addThumb(bmp);
            System.out.println(mBitmapArray.size());
        }

        private void onUploadVideoReturn() {

        }

        private class UploadVideoTask extends AsyncTask<String, Void, Boolean> {
            File video;
            HttpURLConnection conn;

            public UploadVideoTask(File file) {
                this.video = file;
                path = file.getAbsolutePath();
            }

            public UploadVideoTask(String filePath) {
                this.video = new File(filePath);
                System.out.println(filePath);
            }

            @Override
            protected Boolean doInBackground(String... itemname) {
                //String iname = itemname[0];
                return uploadVideo("file");
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    onUploadVideoReturn();
                }
            }

            private boolean uploadVideo(String itemname) {
                String urlString = "http://ythogh.com/shopwf/videos/upload_video.php";
                String Tag = "UPLOAD";
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                String uploadFileName = video.getName().split("[.]")[0];
                System.out.println(uploadFileName);

                try {
                    FileInputStream fileInputStream = new FileInputStream(video);
                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data;boundary=" + boundary);
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: post-data; name=uploaded_file;filename="
                            + uploadFileName + "" + lineEnd);
                    System.out.println(uploadFileName);
                    dos.writeBytes(lineEnd);
                    Log.e(Tag, "Headers are written");

                    int bytesAvailable = fileInputStream.available();
                    int maxBufferSize = 1000;
                    byte[] buffer = new byte[bytesAvailable];
                    int bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bytesAvailable);
                        bytesAvailable = fileInputStream.available();
                        bytesAvailable = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bytesAvailable);
                    }
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    Log.e(Tag, "File is written");
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (Exception ex) {
                    Log.e(Tag, "error: " + ex.getMessage(), ex);
                }
                try {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                            .getInputStream()));
                    String line;
                    boolean b = false;
                    while ((line = rd.readLine()) != null) {
                        Log.e("Dialoge Box", "Message: " + line);
                        b = line.equals("uploaded");
                    }
                    System.out.println(video.length() + " bytes");
                    System.out.println((video.length()/1024.00) + " KB");
                    System.out.println((video.length()/1024.00/1024.00) + " MB");
                    rd.close();
                    return b;
                } catch (IOException ioex) {
                    Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
                    return false;
                }
            }
        }

        private class GetFramesFromVideo extends AsyncTask<Void, Void, Bitmap> {

            MediaMetadataRetriever mmRetriever;
            Uri uri;

            public GetFramesFromVideo(Uri uri, MediaMetadataRetriever mmRetriever) {
                this.mmRetriever = mmRetriever;
                this.uri = uri;
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                return compressImage(mmRetriever.getFrameAtTime(0)); // obtaining the Bitmap
            }

            private Bitmap compressImage(Bitmap bmp) {
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                int newWidth = 96;
                int newHeight = 96;

                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                matrix.postRotate(0);

                bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
                File mFile = new File(path.split("[.]")[0] + "_compressed.jpg");
                System.out.println("*******************************Path:" + path);
                System.out.println("*******************************File:" + mFile);
                try {
                    FileOutputStream out = new FileOutputStream(mFile);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                onFrameGotten(result);
            }
        }
    }

    private class CreateTutorialFinalizeFragment extends Fragment {

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_setup_tutorial_finalize, container, false);
            return initialize(rootView);
        }

        private View initialize(ViewGroup rootView) {
            tvTitle = (TextView) rootView.findViewById(R.id.SETUPTUTORIAL_TEXTVIEW_TITLE);
            tvMyTags = (TextView) rootView.findViewById(R.id.SETUPTUTORIAL_TEXTVIEW_MYTAGS);
            if (mTitle.trim().length() == 0) {
                tvTitle.setText("Unnamed");
            } else {
                tvTitle.setText(mTitle);
            }
            String mString;
            System.out.println("Size: " + mTags.size());
            if (mTags != null && mTags.size() > 0) {
                mString = mTags.get(0);
                for (int i = 1; i < mTags.size(); i++) {
                    mString = ", " + mTags.get(i);
                }
            } else {
                mString = "No tags selected";
            }
            tvMyTags.setText(mString);

            slidingProgression = new SlidingProgression<>(mBitmapArray, getActivity());
            Log.d("Size", "" + mBitmapArray.size());
            ViewGroup layout = (LinearLayout) rootView.findViewById(R.id.SETUPTUTORIAL_LINEARLAYOUT_MYBOARD);
            layout.addView(slidingProgression);
            return rootView;
        }
    }

    private class CreateTutorialSetupFragment extends Fragment implements View.OnClickListener{

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if (common_tags == null) {
                common_tags = new ArrayList<String>();
                for (String s : set_of_tags) {
                    common_tags.add(s);
                }
            }
            Bundle extras = this.getArguments();
            int position = extras.getInt("position");
            ViewGroup rootView;
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_setup_tutorial_creation, container, false);
            etTitle = (EditText) rootView.findViewById(R.id.SETUPTUTORIAL_EDITTEXT_TITLE);
            etTags = (EditText) rootView.findViewById(R.id.SETUPTUTORIAL_EDITTEXT_TAG);
            ivAdd = (ImageView) rootView.findViewById(R.id.SETUPTUTORIAL_IMAGEVIEW_ADDTAG);
            gridview = (GridView) rootView.findViewById(R.id.SETUPTUTORIAL_GRIDVIEW_COMMONTAGS);
            adapter = new CustomListAdapter(getActivity(), common_tags);
            gridview.setAdapter(adapter);
            gridview.requestLayout();
            etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mTitle = etTitle.getText().toString();
                    System.out.println("Title: " + mTitle);
                    return true;
                }
            });
            etTags.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mTitle = etTitle.getText().toString();
                    return true;
                }
            });

            ivAdd.setOnClickListener(this);
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(etTitle.getWindowToken(), 0);
            return rootView;
        }

        @Override
        public void onClick(View v) {
            if ((etTags.getText().toString().trim().length() > 0) && !common_tags.contains(etTags.getText().toString().trim())) {
                firstTimeNew.put(adapter.getCount(), true);
                common_tags.add(etTags.getText().toString());
                adapter.add(etTags.getText().toString());
                adapter.notifyDataSetChanged();
                gridview.requestLayout();
            }
        }

        private class CustomListAdapter extends ArrayAdapter<String> {
            private final Context context;
            private final ArrayList<String> common_tags;

            public CustomListAdapter(Context context, ArrayList<String> str) {
                super(context, R.layout.gridview_item, new ArrayList<String>(str.size()));
                this.context = context;
                this.common_tags = str;
                selected = new HashMap<Integer, Boolean>();
                mSelected = new HashMap<Integer, View>();
                nSelected = new HashMap<Integer, SetupViewHolder>();
                firstTimeNew = new HashMap<Integer, Boolean>();
                mTags = new ArrayList<String>();
            }



            @Override
            public View getView(final int position, View rowView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final SetupViewHolder holder;
                if (rowView == null) {
                    rowView = inflater.inflate(R.layout.gridview_item, parent, false);
                    holder = new SetupViewHolder();
                    holder.tv = (TextView) rowView.findViewById(R.id.GRIDVIEWITEM_TEXTVIEW_TAG);
                    holder.selected = false;
                    if ((position >= INITIAL_SIZE) && (firstTimeNew.containsKey(position))) {
                        mTags.add(common_tags.get(position));
                        selected.put(position, true);
                        holder.selected = true;
                        firstTimeNew.remove(position);
                        holder.tv.setTextColor(Color.WHITE);
                        rowView.setBackgroundColor(Color.GRAY);
                        mSelected.put(position, rowView);
                        nSelected.put(position, holder);
                        System.out.println("New one added: " + position);
                    }
                    rowView.setTag(holder);
                } else {
                    holder = (SetupViewHolder) rowView.getTag();
                    if ((position >= INITIAL_SIZE) && (firstTimeNew.containsKey(position))) {
                        mTags.add(common_tags.get(position));
                        selected.put(position, true);
                        holder.selected = true;
                        firstTimeNew.remove(position);
                        holder.tv.setTextColor(Color.WHITE);
                        rowView.setBackgroundColor(Color.GRAY);
                        mSelected.put(position, rowView);
                        nSelected.put(position, holder);
                        System.out.println("New one added: " + position);
                    }
                    System.out.println(INITIAL_SIZE + " : " + position);
                }
                holder.tv.setText(common_tags.get(position));
                if (mSelected.containsKey(position)) {
                    rowView.setBackgroundColor(Color.GRAY);
                    holder.tv.setTextColor(Color.WHITE);
                } else {
                    rowView.setBackgroundColor(Color.WHITE);
                    holder.tv.setTextColor(Color.GRAY);
                }

                final View rView = rowView;
                rView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.selected) { // being deselected
                            holder.selected = false;
                            selected.put(position, false);
                            mSelected.remove(position);
                            nSelected.remove(position);
                            holder.tv.setTextColor(Color.GRAY);
                            rView.setBackgroundColor(Color.WHITE);
                            System.out.println("Removed " + position);
                            mTags.remove(common_tags.get(position));
                        } else { // being selected
                            mTags.add(common_tags.get(position));
                            selected.put(position, true);
                            holder.selected = true;
                            holder.tv.setTextColor(Color.WHITE);
                            rView.setBackgroundColor(Color.GRAY);
                            mSelected.put(position, rView);
                            nSelected.put(position, holder);
                            System.out.println("Added " + position);
                        }
                    /* System.out.println("**SET**");
                    for (int i = 0; i < selected.length; i++) {
                        System.out.println("position: " + i + ", " + selected[i]);
                    }
                    System.out.println("\n" + position); */
                    }
                });

                return rowView;
            }

            @Override
            public int getCount(){
                return common_tags.size();
            }
        }
    }

    static class SetupViewHolder {
        boolean selected;
        TextView tv;
    }

}