package com.example.youtubestartendtime;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;


@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {


    private ClipboardManager myClipboard;
    private ClipData myClip;
    private Button share;
    String newLink;


    String[] required_permissions = new String[]{
            android.Manifest.permission.READ_MEDIA_IMAGES,
            android.Manifest.permission.READ_MEDIA_VIDEO,
            android.Manifest.permission.READ_MEDIA_AUDIO
    };
//
//    ImageView img_deny_image, img_given_image;
//    ImageView img_deny_video, img_given_vídeo;
//    ImageView img_deny_audio, img_given_audio;


    Button btn_check_all_permissions;

    //    boolean is_storage_image_permitted=false;
    boolean is_storage_video_permitted = false;
//    boolean is_storage_audio_permitted=false;


    String TAG = "Permission";

//    public String recentFile = "";
//
//    public String recentFilename = "";

    String[] recentFileName = new String[1];
    String[] recentFileLoc = new String[1];
    int[] fileDuplicates = new int[]{0};

    boolean[] dupesOK = new boolean[]{false};

    String slash = "/";

    private static final int PICK_VIDEO_REQUEST = 1;

    private static final int GET_FILE_NAME = 2;

    private FFmpeg ffmpeg;
//    ActivityResultLauncher<Intent> sActivityResultLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            {
//
//                @Override
//                public void onActivityResult(Instrumentation.ActivityResult result) {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        Uri uri = data.getData();
//                    }
//                }
//            }
//    );
//
//    public void openFileDialog(View view) {
//        Intent data = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        data.setType("video/*");
////        data.setType("*/*");
//          String mineTypes = {"text/*", "image/*};
//          data.putExtra(Intent.EXTRA_MINE_TYPES, mineTypes);

//        data = Intent.createChooser(data, "Choose a file");
//        sActivityResultLauncher.launch(data);
//    https://www.youtube.com/watch?v=4EKlAvjY74U
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button get_link_btn = findViewById(R.id.get_link_btn);
        Button download_btn = findViewById(R.id.download_btn);
        final TextView result_link = findViewById(R.id.result_link);
        result_link.setMovementMethod(LinkMovementMethod.getInstance());
        Button copy_link_btn = findViewById(R.id.copy_btn);
        Button paste_link_btn = findViewById(R.id.paste_btn);
        Button open_btn = findViewById(R.id.open_btn);
        Button storage = findViewById(R.id.storage);
        Button update_btn = findViewById(R.id.update_btn);
        Button github_btn = findViewById(R.id.github_btn);
        share = findViewById(R.id.share_btn);
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//
//        img_deny_image = (ImageView) findViewById(R.id.img_deny_image);
//        img_given_image = (ImageView) findViewById(R.id.img_given_image);
//        img_deny_video = (ImageView) findViewById(R.id.img_deny_video);
//        img_given_video = (ImageView) findViewById(R.id.img_given_video);
//        img_deny_audio = (ImageView) findViewById(R.id.img_deny_audio);
//        img_given_audio = (ImageView) findViewById(R.id.img_given_audio);
//
//        btn_check_all_permissions = (Button) findViewById(R.id.btn_check_all_permissions);

        ActivityCompat.requestPermissions(this,
                new String[]{READ_MEDIA_VIDEO, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

        github_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView result_link = (TextView) findViewById(R.id.result_link);
                result_link.setText("https://github.com/monzano00/YouTubeStartEndTime");
            }
        });

        get_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView result_link = (TextView) findViewById(R.id.result_link);
                result_link.setText(alter_link(false));
            }
        });


        copy_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextView result_link = (TextView) findViewById(R.id.result_link);
                String copyable_link = result_link.getText().toString();


                ClipData clip = ClipData.newPlainText("TextView", copyable_link);
                myClipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();

            }
        });

        paste_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clipData = myClipboard.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);

                EditText youtube_link = (EditText) findViewById(R.id.youtube_link);

                youtube_link.setText(item.getText().toString());
                Toast.makeText(MainActivity.this, "Pasted", Toast.LENGTH_SHORT).show();


            }
        });


        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                share.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                        sharingIntent.setType("text/plain");
//                        String shareBody = "Your body here";
//                        String shareSub = "Your subject here";
//                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
//                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
//                    }
//                });
//
                String s = result_link.getText().toString();

                Intent sharing_intent = new Intent(android.content.Intent.ACTION_SEND);
                sharing_intent.setType("text/plain");
                sharing_intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharing_intent.putExtra(android.content.Intent.EXTRA_TEXT, s);
                //sharing_intent.putExtra(Intent.EXTRA_TEXT)
                startActivity(Intent.createChooser(sharing_intent, "Share via"));

            }
        });
        result_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(result_link.getText().toString()));
                startActivity(browserIntent);
                //https://stackoverflow.com/questions/43025993/how-do-i-open-a-browser-on-clicking-a-text-link-in-textview
            }
        });

        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result_link.getText().toString()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage("com.android.chrome");

//                https://www.youtube.com/watch?v=E3xYPKcNqeY&list=
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // Chrome browser presumably not installed so allow user to choose instead
                    intent.setPackage(null);
                    intent.setPackage("org.mozilla.firefox");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex2) {
                        // Firefox not installed
                        intent.setPackage(null);
                        intent.setPackage("com.opera.browser");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex3) {
                            //Opera not installed
                            intent.setPackage(null);
                            intent.setPackage("com.samsung.android.app.sbrowseredge");
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex4) {
                                intent.setPackage(null);
                                intent.setPackage("com.microsoft.emmx");
                                try {
                                    startActivity(intent);
                                } catch (ActivityNotFoundException ex5) {
                                    intent.setPackage(null);
                                    intent.setPackage("org.torproject.torbrowser");
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException ex6) {
                                        intent.setPackage(null);
                                        intent.setPackage("com.vivaldi.browser");
                                        try {
                                            startActivity(intent);
                                        } catch (ActivityNotFoundException ex7) {
                                            intent.setPackage(null);
                                            startActivity(intent);
                                        }


                                    }
                                }


                            }
                        }
                    }
                }
            }
        });

//        interface GitHubApi {
//            @GET("repos/monzano00/YouTubeStartEndTime/releases/latest")
//            Call<JsonObject> getLatestRelease();
//        }
//
//        public void fetchLatestVersion() {
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://api.github.com/")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            GitHubApi gitHubApi = retrofit.create(GitHubApi.class);
//            Call<Release> call = gitHubApi.getLatestRelease();
//
//            call.enqueue(new Callback<Release>() {
//                @Override
//                public void onResponse(Call<Release> call, Response<Release> response) {
//                    if (response.isSuccessful()) {
//                        Release release = response.body();
//                        String latestVersion = release.getTagName();
//                        // Handle the latest version number
//                    } else {
//                        // Handle API call error
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<Release> call, Throwable t) {
//                    // Handle network or request failure
//                }
//            });
//        }
//
//
//        update_btn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                checkForUpdates(view);
//
//            }
//
//        });


    }


    public void downloadVideo(View view) throws IllegalStateException {
        TextView result_link = (TextView) findViewById(R.id.result_link);

        String values = alter_link(true);
//            TextView result_link = (TextView) findViewById(R.id.result_link);
//            result_link.setText(alter_link());


        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, 1);


        if (!is_storage_video_permitted) {
            requestPermissionsStorageVideo();

        }
        YouTubeUriExtractor videoget = new YouTubeUriExtractor(MainActivity.this) {

            @Override
            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
                // videoId = JJilnzWpUdI
                // videoTitle = Viper Rambles Is Now Cancelled

//                not working link: https://www.youtube.com/watch?v=JJilnzWpUdI

//                https://www.youtube.com/watch?v=-x97iBDaOKQ
//                https://www.youtube.com/watch?v=Ox4l1LrHiCk


                if (ytFiles != null) {
                    int itag = 22;
                    newLink = ytFiles.get(itag).getUrl();
                    String title = linkToFile(videoTitle, true);
//                  title + ".mp4";
//                    storageVolume.getDirectory().getPath() + downloadFolder
//                    this.cacheDirPath = /data/user/0/com.example.youtubestartendtime/cache
//                    recentFilename = title;

//                    StorageManager storageManager = (StorageManager) getSystemService(STORAGE_SERVICE);
//                    StorageVolume storageVolume = storageManager.getStorageVolumes().get(0); // internal Storage
////                    storage.mInternalPath = /storage/emulated/0
//                    storage.mPath = /storage/emulated/0

//                    File fileInputVideo = new File(storageVolume.getDirectory().getPath() + downloadFolder);
//                    recentFileChange(fileInputVideo.toString());
//                    fileInputVideo = /storage/emulated/0/Download/Viper Rambles Is Now Cancelled_0s_to_end.mp4.mp4
//                    Bitmap bitmapInputVideo = BitmapFactory.decodeFile(fileInputVideo.getPath());

//                    request.mDestinationUri = file:///storage/emulated/0/Android/data/com.example.youtubestartendtime/files/Download/Viper Rambles Is Now Cancelled_0s_to_end.mp4
//                    recentFileChange(request.);


//                    storageVolume.getDirectory().toString() = /storage/emulated/0
//                    title = Viper_Rambles_Is_Now_Cancelled_0s_to_end.mp4
                    String extra = "/" + Environment.DIRECTORY_DOWNLOADS + "/";

                    recentFileName[0] = title;
//                    recentFileLoc[0] = storageVolume.getDirectory().toString() + extra;
//                    recentFile = rfl;


                    recentFileLoc[0] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";


                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(newLink));

//  /storage/emulated/0/Android/data/com.example.youtubestartendtime/files/Download/Viper_Rambles_Is_Now_Cancelled_0s_to_end.mp4


                    request.setTitle("download");
                    request.setDescription("Your file is downloading");
//                  some videos like this one don't work.      https://www.youtube.com/watch?v=GJVrna5Rm5U
//                      throws a attempt to invoke virtual method on a null object reference on .getUrl();
//                        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    @SuppressLint({"StaticFieldLeak", "ServiceCast"}) DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);


                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                    request.setDestinationInExternalFilesDir(MainActivity.this, downloadFolder, title);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

//
//                    PackageManager m = getPackageManager();
//                    String s = getPackageName();
//                    try {
//                        PackageInfo p = m.getPackageInfo(s, 0);
//                        s = p.applicationInfo.dataDir;
//                    } catch (PackageManager.NameNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    File destinationDirectory = new File(s + "/files/ffmpeg");
//                    if (!destinationDirectory.exists()) {
//                        boolean success = destinationDirectory.getParentFile().mkdirs();
//                        if (!success) {
//                            result_link.setText("Failed to create /files/ffmpeg");
//                            // Failed to create the destination directory
//                        }
//                    }
//                      file:///storage/emulated/0/Android/data/com.example.youtubestartendtime/files/Download/Viper Rambles Is Now Cancelled_0s_to_end.mp4
///storage/emulated/0/Android/data/com.example.youtubestartendtime/files/ = MainActivity.this

//                    File test3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    test3 = /storage/emulated/0/Download
//                    https://www.youtube.com/watch?v=JJilnzWpUdI
                    manager.enqueue(request);
//                        test video: https://www.youtube.com/watch?v=TnlakHr-O4w

//                        try {
//                            downloadUrl = ytFiles.get(itag).getUrl();
//                            if (downloadUrl != null) {
//
//                                Toast.makeText(MainActivity.this, "Download started...", Toast.LENGTH_SHORT).show();
//
//                                Log.d("DOWNLOAD URL ", "URL :=" + downloaderUrl);
//
//                            }
//                        } catch (Exception e) {
//                            Toast.makeText(MainActivity.this, "download url could not be fetched", Toast.LENGTH_SHORT).show();
//                        }

                }
            }
        };

//        if (videoget.isCancelled())
        try {
            videoget.execute(values);

        } catch (Exception e) {

            result_link.setText("This video cannot be downloaded. You can still clip " +
                    "it from the download folder if you can download it by other means.");
            throw new IllegalStateException(e);
//            InvocationTargetException e) {
//                throw new IllegalStateException(
//                        "Could not execute method for android:onClick", e);
        }


    }

    public void checkForUpdates(View view) {

        TextView result_link = (TextView) findViewById(R.id.result_link);

//        String latestReleaseTag = ""; // Retrieve latest release tag using getLatestReleaseTag()
//        // and parseReleaseTagName() methods
//        String currentVersion = "1.0"; // Replace with your app's current version
//
//        return latestReleaseTag.compareTo(currentVersion) > 0;
        try {
            String github = "https://github.com/monzano00/YouTubeStartEndTime";
            String latestReleaseJson = getLatestReleaseTag();
            String latestReleaseTag = parseReleaseTagName(latestReleaseJson);
            String currentVersion = getCurrentVersion();
//            open github
            if (latestReleaseTag.compareTo(currentVersion) > 0) {
                result_link.setText("Update available at https://github.com/monzano00/YouTubeStartEndTime");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(github));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("GitHub URL", github);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                    // Handle case where the device doesn't have a web browser installed
                }
            } else {
                result_link.setText("No updates at https://github.com/monzano00/YouTubeStartEndTime");
            }

//            return latestReleaseTag.compareTo(currentVersion) > 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
//        return false;
    }


    private String getLatestReleaseTag() throws IOException {
        URL url = new URL("https://api.github.com/repos/monzano00/YouTubeStartEndTime/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
            return response.toString();
        } else {
            throw new IOException("Failed to fetch release details");
        }
    }

    private String getCurrentVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String parseReleaseTagName(String responseJson) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseJson, JsonObject.class);
        return jsonObject.get("tag_name").getAsString();
    }


    public void storageClip(View view) throws IOException {

        EditText youtube_link = (EditText) findViewById(R.id.youtube_link);
        TextView result_link = (TextView) findViewById(R.id.result_link);
        if (convertStartHMSToSeconds() == 0 && convertEndHMSToSeconds() == 0) {
            result_link.setText("Input a start and/or end time");
            return;
        }
        if (convertStartHMSToSeconds() >= convertEndHMSToSeconds() && convertEndHMSToSeconds() != 0) {
            result_link.setText("Start time must be greater than end time");
            return;
        }


//        if time not included, then don't edit clip
        showFilePicker();

//        they are null if just click storage button without pressing download
//        if ((recentFileLoc[0] != null && recentFileName[0] != null) &&
//                (recentFileLoc[0] != "" && recentFileName[0] != "")) {
//
////            String extra = "/Download/";
////            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
////                  = /storage/emulated/0/DCIM
//
//            String extra = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/YouTubeSEClips/";
//
//
//                String videoFileSource = recentFileLoc[0] + recentFileName[0];
//                String videoFileName = recentFileName[0];
//                String trimmedVideoFileName = linkToFile(videoFileName, false);
////            trimmedVideoFileName = video_6s_to_68s.mp4
//                String trimmedVideoFilePath = recentFileLoc[0] + trimmedVideoFileName;
////                trimVideoFile(videoFileSource, trimmedVideoFilePath, startMs, endMs);
//                showFilePicker();
//
//        } else {
//
//            showFilePicker();
//
//
//        }
    }


    private static final int TRIM_VIDEO = 1001;

    // Open the file picker
    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, TRIM_VIDEO);
    }

    // Handle the file picker result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TRIM_VIDEO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            // Now you can use the uri to access the selected file
            // For example:
            int startTime = convertStartHMSToSeconds() * 1000;
            int endTime = convertEndHMSToSeconds() * 1000;



            File file = new File(uri.getPath());//create path from uri


            File newfile = new File(getRealPathFromUri(uri));
            String filename = newfile.getName();
//            final String[] split = file.getPath().split(":");

//            String temp = String.valueOf(this.recentFileName);




            String videoFileSource = recentFileLoc[0] + recentFileName[0];
            String videoFileName = recentFileName[0];
            String trimmedVideoFileName = linkToFile(videoFileName, false);
//            trimmedVideoFileName = video_6s_to_68s.mp4
            String trimmedVideoFilePath = recentFileLoc[0] + trimmedVideoFileName;

            String extra = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/YouTubeSEClips/";

            recentFileName[0] = trimmedVideoFileName;



            // Trim the video
            try {
                File trimmedVideo = trimVideo(this, uri, startTime, endTime, trimmedVideoFileName, trimmedVideoFilePath, new VideoTrimmingListener() {
                    @Override
                    public void onTrimStarted() {

                    }

                    @Override
                    public void onTrimCompleted(String outputUri) {

                    }

                    @Override
                    public void onTrimError(String message) {

                    }
                });
                moveFile(recentFileLoc[0], recentFileName[0], extra);

                // Do something with the trimmed video
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Do something with the input stream
        }
    }
    public interface VideoTrimmingListener {
        void onTrimStarted();
        void onTrimCompleted(String outputUri);
        void onTrimError(String message);
    }
    public static int selectTrack(MediaExtractor extractor, boolean audio) {
        int trackIndex = -1;
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (audio && mime.startsWith("audio/")) {
                trackIndex = i;
                break;
//                was at video/avc on i = 0

            } else if (!audio && mime.startsWith("video/")) {
                trackIndex = i;
                break;
            }
        }
        if (trackIndex == -1) {
            // no video track found
            return -1;
        }
        return trackIndex;
    }

    @SuppressLint("WrongConstant")
    public static File trimVideo(Context context, Uri uri, long startMs, long endMs, String outputName, String outputPath, VideoTrimmingListener listener) throws IOException {

        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(context, uri, null);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onTrimError(e.getMessage());
            return null;
        }

        int trackCount = extractor.getTrackCount();
        int videoTrackIndex = selectTrack(extractor, false);
        if (videoTrackIndex < 0) {
            listener.onTrimError("No video track found in " + uri);
            return null;
        }
//
        extractor.selectTrack(videoTrackIndex);
        MediaFormat videoFormat = extractor.getTrackFormat(videoTrackIndex);

        int videoTrackIndexOutput = -1;
        long duration = videoFormat.getLong(MediaFormat.KEY_DURATION);

        // Create the output muxer
        MediaMuxer muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        videoTrackIndexOutput = muxer.addTrack(videoFormat);

        int audioTrackIndex = selectTrack(extractor, true);
        extractor.selectTrack(audioTrackIndex);

        long originalDuration = duration / 1000; // Convert duration to seconds

        if (startMs > originalDuration || endMs > originalDuration || startMs >= endMs) {
            listener.onTrimError("Invalid trim range specified");
            return null;
        }


        if (audioTrackIndex != -1) {
            MediaFormat audioFormat = extractor.getTrackFormat(audioTrackIndex);
            int audioTrackIndexOutput = muxer.addTrack(audioFormat);

            muxer.start();

            // Set the sample size
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            while (true) {
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    break;
                }

                long presentationTimeUs = extractor.getSampleTime();
                if (presentationTimeUs > endMs * 1000) {
                    break;
                }

                bufferInfo.offset = 0;
                bufferInfo.size = sampleSize;
                bufferInfo.flags = extractor.getSampleFlags();
                bufferInfo.presentationTimeUs = presentationTimeUs;

                if (extractor.getSampleTrackIndex() == videoTrackIndex) {
                    muxer.writeSampleData(videoTrackIndexOutput, buffer, bufferInfo);
                } else if (extractor.getSampleTrackIndex() == audioTrackIndex) {
                    muxer.writeSampleData(audioTrackIndexOutput, buffer, bufferInfo);
                }

                extractor.advance();
            }

        }

        muxer.stop();
        muxer.release();
        extractor.release();

        if (listener != null) {
            listener.onTrimCompleted(String.valueOf(Uri.parse(outputPath)));
        }
        return null;
    }


    private void moveFile(String inputPath, String inputFile, String outputPath) {
//        https://www.youtube.com/watch?v=JJilnzWpUdI
        TextView result_link = (TextView) findViewById(R.id.result_link);
        File sourceFile = new File(inputPath + inputFile);
        File destinationDirectory = new File(outputPath);
        File destinationFile = new File(destinationDirectory, sourceFile.getName());
        // Check if the app has permission to write to external storage
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, 1);

//        File destinationDirectory = new File("/path/to/destination/directory");
//        File destinationFile = new File(destinationDirectory, sourceFile.getName());
        if (!destinationDirectory.exists()) {
            boolean success = destinationDirectory.mkdirs();
            if (!success) {
                result_link.setText("Failed to create the destination directory");
                // Failed to create the destination directory
            }
        }
// Rename the file if it already exists in the destination directory
        if (destinationFile.exists()) {
            String fileName = sourceFile.getName();
            String baseName = FilenameUtils.getBaseName(fileName);
            String extension = FilenameUtils.getExtension(fileName);
            int i = 1;
            while (destinationFile.exists()) {
                destinationFile = new File(destinationFile.getParent(), baseName + "-" + i++ + "." + extension);
            }
        }

        boolean success = sourceFile.renameTo(destinationFile);

        if (success) {
            recentFileLoc[0] = "";
            recentFileName[0] = "";
            result_link.setText("Video clipped to " + outputPath);
        } else {
            result_link.setText("Video was not moved successfully");
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String filePath = null;
        if (uri != null) {
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                filePath = uri.getPath();
            } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                    String displayName = cursor.getString(columnIndex);
                    cursor.close();

//                    displayName = HeraToss.mp4
//                    getCacheDir() = /data/user/0/com.example.youtubestartendtime/cache/HeraToss.mp4

                    recentFileLoc[0] = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)) + "/";
                    recentFileName[0] = displayName;
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), displayName);
                    filePath = file.getAbsolutePath();
                }
            }
        }
        return filePath;
    }
    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Video.Media.DATA};
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            Log.d("VideoTrimmer", "Cursor count: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                Log.d("VideoTrimmer", "Column index: " + columnIndex);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        } else {
            filePath = uri.getPath(); // fallback to uri.getPath()
            Log.d("VideoTrimmer", "File path: " + filePath);

        }
        return filePath;
    }


//get permission to store video
    public void requestPermissionsStorageVideo() {
//        https://github.com/raimiss8/Android-13-Permissions/blob/a64dd9080eea55c4bd591d111b486b8c1585ebf0/app/src/main/java/com/esolar/readwritepermisionsapp/MainActivity.java


        if (ContextCompat.checkSelfPermission(MainActivity.this, required_permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, required_permissions[1] + " Granted");

            is_storage_video_permitted = true;
            request_permission_launcher_storage_video.launch(required_permissions[1]);

        }
    }

    private ActivityResultLauncher<String> request_permission_launcher_storage_video =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted)  //java8
                        {
                            Log.d(TAG, required_permissions[1] + " Granted");

                            is_storage_video_permitted = true;
                        } else {
                            Log.d(TAG, required_permissions[1] + " Not Granted");
                            is_storage_video_permitted = false;
                        }

                    });

    public void sendToSettingDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Alert for Permission")
                .setMessage("Go to Settings for Permissions")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ///  code to go to settings of application
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }

    private int convertEndHMSToSeconds() {
        EditText end_sec = (EditText) findViewById(R.id.end_sec);
        EditText end_min = (EditText) findViewById(R.id.end_min);
        EditText end_hour = (EditText) findViewById(R.id.end_hour);
        int ends;
        int endm;
        int endh;
        if (TextUtils.isEmpty(end_sec.getText().toString())) {
            ends = 0;
        } else {
            ends = Integer.parseInt(end_sec.getText().toString());
        }
        if (TextUtils.isEmpty(end_min.getText().toString())) {
            endm = 0;
        } else {
            endm = Integer.parseInt(end_min.getText().toString());
        }
        if (TextUtils.isEmpty(end_hour.getText().toString())) {
            endh = 0;
        } else {
            endh = Integer.parseInt(end_hour.getText().toString());
        }
        return ends + (endm * 60) + (endh * 60 * 60);
    }

    private int convertStartHMSToSeconds() {
        EditText start_sec = (EditText) findViewById(R.id.start_sec);
        EditText start_min = (EditText) findViewById(R.id.start_min);
        EditText start_hour = (EditText) findViewById(R.id.start_hour);
        int starts;
        int startm;
        int starth;
        if (TextUtils.isEmpty(start_sec.getText().toString())) {
            starts = 0;
        } else {
            starts = Integer.parseInt(start_sec.getText().toString());
        }
        if (TextUtils.isEmpty(start_min.getText().toString())) {
            startm = 0;
        } else {
            startm = Integer.parseInt(start_min.getText().toString());
        }
        if (TextUtils.isEmpty(start_hour.getText().toString())) {
            starth = 0;
        } else {
            starth = Integer.parseInt(start_hour.getText().toString());
        }
        return starts + (startm * 60) + (starth * 60 * 60);
    }

    private String linkToFile(String title, boolean download) {
//        get video title and add timestamps to it for file name

        int end_time = convertEndHMSToSeconds();
        int start_time = convertStartHMSToSeconds();

        if (title.contains("s_mp4")) {
            title = title.substring(0, title.indexOf("s_mp4") + 5);
        }

        if (title.substring(title.length() - 4, title.length()).toLowerCase().equals(".mp4"))
            title = title.substring(0, title.length() - 4);

        title = title.replaceAll("[^a-zA-Z0-9-_.]", "_");


        if (!download) {
            if (end_time > 0) {
                title = title + "_" + start_time + "s" + "_to_" + end_time + "s.mp4";
            } else {
                title = title + "_" + start_time + "s" + "_to_" + "s.mp4";
            }

        } else {
            title = title + ".mp4";
        }
        return title;

//            bad characters
//        # pound
//        % percent
//        & ampersand
//        { left curly bracket
//        } right curly bracket
//        \ back slash
//        < left angle bracket
//        > right angle bracket
//        * asterisk
//        ? question mark
//        / forward slash
//        blank spaces
//        $ dollar sign
//        ! exclamation point
//        ' single quotes
//        " double quotes
//        : colon
//        @ at sign
//        + plus sign
//        ` backtick
//        | pipe
//        = equal sign

//        switch (url) {
//            case url.contains("?start=") & url.contains("end="):

//                break

        //        default:


//
//
//        }
//
//        if (url.contains("tu.be/")) {
//            url.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
////            String url = "jkl-l_kk.?/|][\';&*+";
////            jkl-l_kk___________
//
//        }

    }

    private String alter_link(Boolean download) {
        EditText youtube_link = (EditText) findViewById(R.id.youtube_link);
        TextView result_link = (TextView) findViewById(R.id.result_link);

        String links = youtube_link.getText().toString();
        String alternate_link = "Invalid Link. Try Again.";

        if (links.substring(0, 8).equals("youtu.be")) {
            links = "https://" + links;
        }

        if (links.substring(0, 11).equals("www.youtube")) {
            links = "https://" + links;
        }

        if (links.substring(0, 7).equals("youtube")) {
            links = "https://www." + links;
        }

        boolean test = links.substring(0, 4).equals("http") && !links.substring(4, 5).equals("s");

        if (test) {
            links = "https" + links.substring(4);
        }

        if (links.contains("?reload=")) {
            links = links.substring(0, links.indexOf("?reload=")) + "?" + links.substring(links.indexOf("v="));
            //https://www.youtube.com/watch?reload=9&v=2xMzgvPrXIo
        }

        if (links.contains("?version=")) {
            links = links.substring(0, links.indexOf("?version="));
            //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
        }


        if (links.contains("&")) {
            links = links.substring(0, links.indexOf("&"));
            //https://www.youtube.com/watch?v=cTWA-a_t_M8
            //https://www.youtube.com/watch?v=cTWA-a_t_M8&list=PLimyCJLq6B4sIh4pMpXTJ5BHcVfeVgvmF&index=2&t=0s
        }

        if (!links.contains("//www") && links.contains("youtube")) {
            links = links.substring(0, links.indexOf("https://") + 8) + "www." +
                    links.substring(links.indexOf("youtube"));
            //https://m.youtube.com/watch?v=fMM-hzKin56l
        }

        if (links.contains("/v/")) {
            links = links.replace("/v/", "/watch?v=");
            //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
        }
        if (links.contains("?start")) {
            links = links.replace("/embed/", "/watch?v=");
            links = links.substring(0, links.indexOf("?start"));
            //https://www.youtube.com/embed/PldCWWb52zw?start=3
        }
        if (links.contains("?end")) {
            links = links.replace("/embed/", "/watch?v=");
            links = links.substring(0, links.indexOf("?end"));
            //https://www.youtube.com/embed/PldCWWb52zw?end=3
        }

        if (links.contains("?t")) {
            links = links.replace("/embed/", "/watch?v=");
            links = links.substring(0, links.indexOf("?t"));
            //https://youtu.be/cTWA-a_t_M8?t=1
        }

        if (links.contains("/embed/")) {
            links = links.replace("/embed/", "/watch?v=");
        }
        if (links.contains("attribution_link?a=")) {
            links = links.replace("attribution_link?a=", "/watch?v=");
            //http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare
        }
        if (links.substring(0, 17).equals("https://youtu.be/")) {
            alternate_link = "https://www.youtube.com/watch?v=" + links.substring(17);
        }
        //https://youtu.be/PldCWWb52zw

        else if (links.substring(0, 32).equals("https://www.youtube.com/watch?v=")) {
            alternate_link = "https://youtu.be/" + links.substring(32);
        }
        int end_time = convertEndHMSToSeconds();
        int start_time = convertStartHMSToSeconds();
        if (end_time > 0 && start_time >= end_time && !download) {
            return "End time must be greater than start time";
        }
        //https://www.youtube.com/watch?v=E3xYPKcNqeY&list=
        // PLAmiG35BNssvLlk36MnYUU_rSrzL7V7Sb&index=2

        String set_time = "";

        if (links.equals("")) {
            alternate_link = "Invalid Link. Try Again.";
        }

        if (start_time == 0 && end_time == 0) {
            result_link.setText(alternate_link);

        } else {

            if (start_time == 0 && end_time > 0) {
                set_time = "?end=" + end_time;
            }

            if (start_time > 0 && end_time == 0) {
                set_time = "?start=" + start_time;
            }

            if (start_time > 0 && end_time > 0) {
                set_time = "?start=" + start_time + "&end=" + end_time;
            }
            if (links.substring(0, 28).equals("https://www.youtube.com/watc") && !download) {
                if (links.substring(0, 32).equals("https://www.youtube.com/watch?v=")) {
                    links = links.substring(0, 24) + "embed/" + links.substring(32) + set_time;
                }
            } else if (links.substring(0, 17).equals("https://youtu.be/") && !download) {
                links = alternate_link;
                links = links.substring(0, 24) + "embed/" + links.substring(32) + set_time;
            } else if (download) {
                return links + "";
            } else {
                links = "Invalid Link. Try Again";
            }

            return links + "";


        }
        return links;
    }
}


//        YouTubeUrlExtractor videoget = new YouTubeUrlExtractor(this) {
//            @SuppressLint("StaticFieldLeak")
//            @Override
//            public void onUrisAvailable(String videoId, String videoTitle, SparseArray<YtFile> ytFiles) {
//                if (ytFiles != null) {
//                    int itag = 22;
//
//                    try {
//                        downloadUrl = ytFiles.get(itag).getUrl();
//                        if (downloadUrl != null) {
//
//                            Toast.makeText(MainActivity.this, "Download started...", Toast.LENGTH_SHORT).show();
//
//                            Log.d("DOWNLOAD URL ", "URL :=" + downloaderUrl);
//
//                        }
//                    } catch (Exception e) {
//                        Toast.makeText(MainActivity.this, "download url could not be fetched", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }
//        };

//        public void downloadVideo(String url){
//            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//            request.setTitle("download");
//            request.setDescription("Your file is downloading");
//            request.allowScanningByMediaScanner();
//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//            request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, "testing.mp4");
//
//            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//            manager.enqueue(request);
//        }
//
//        download_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //String url = editText.getText().toString().toString();
//                //String url = get_link_btn(view);
//                String url = alter_link();
//
//
//
//                if (url!=null & url.contains("you")) {
//                    videoget.extract(url);
//
//                }
//
//            }
//        });


//@Override
//public void onClick(View v) {
//
//    EditText start_sec;
//    EditText start_min;
//    EditText start_hour;
//    EditText end_sec;
//    EditText end_min;
//    EditText end_hour;
//    EditText youtube_link;
//    TextView result_link;
//
//    int starts;
//    int startm;
//    int starth;
//    int ends;
//    int endm;
//    int endh;
//    String links;
//
//    switch (v.getId()) {
//        case R.id.get_link_btn:
//            start_sec = (EditText) findViewById(R.id.start_sec);
//            start_min = (EditText) findViewById(R.id.start_min);
//            start_hour = (EditText) findViewById(R.id.start_hour);
//            end_sec = (EditText) findViewById(R.id.end_sec);
//            end_min = (EditText) findViewById(R.id.end_min);
//            end_hour = (EditText) findViewById(R.id.end_hour);
//            youtube_link = (EditText) findViewById(R.id.youtube_link);
//            result_link = (TextView) findViewById(R.id.result_link);
//
//            starts = 0;
//            startm = 0;
//            starth = 0;
//            ends = 0;
//            endm = 0;
//            endh = 0;
//            links = youtube_link.getText().toString();
//
//
//            if (TextUtils.isEmpty(start_sec.getText().toString())) {
//                starts = 0;
//            } else {
//                starts = Integer.parseInt(start_sec.getText().toString());
//            }
//
//            if (TextUtils.isEmpty(start_min.getText().toString())) {
//                startm = 0;
//            } else {
//                startm = Integer.parseInt(start_min.getText().toString());
//            }
//
//
//            if (TextUtils.isEmpty(start_hour.getText().toString())) {
//                starth = 0;
//            } else {
//                starth = Integer.parseInt(start_hour.getText().toString());
//            }
//
//
//            if (TextUtils.isEmpty(end_sec.getText().toString())) {
//                ends = 0;
//            } else {
//                ends = Integer.parseInt(end_sec.getText().toString());
//            }
//
//            if (TextUtils.isEmpty(end_min.getText().toString())) {
//                endm = 0;
//            } else {
//                endm = Integer.parseInt(end_min.getText().toString());
//            }
//
//
//            if (TextUtils.isEmpty(end_hour.getText().toString())) {
//                endh = 0;
//            } else {
//                endh = Integer.parseInt(end_hour.getText().toString());
//            }
//
////
////                if (TextUtils.isEmpty(youtube_link.getText().toString()))
////                {
////                    links = "";
////                }
////                else
////                {
////                    links = youtube_link.getText().toString();
////                }
//
//
//            String alternate_link = "Invalid Link. Try Again.";
//
//            if (links.substring(0, 8).equals("youtu.be")) {
//                links = "https://" + links;
//            }
//
//            if (links.substring(0, 11).equals("www.youtube")) {
//                links = "https://" + links;
//            }
//
//            if (links.substring(0, 7).equals("youtube")) {
//                links = "https://www." + links;
//            }
//
//            boolean test = links.substring(0, 4).equals("http") && !links.substring(4, 5).equals("s");
//
//            if (test) {
//                links = "https" + links.substring(4);
//            }
//
//            if (links.contains("?reload=")) {
//                links = links.substring(0, links.indexOf("?reload=")) + "?" + links.substring(links.indexOf("v="));
//                //https://www.youtube.com/watch?reload=9&v=2xMzgvPrXIo
//            }
//
//            if (links.contains("?version=")) {
//                links = links.substring(0, links.indexOf("?version="));
//                //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
//            }
//
//
//            if (links.contains("&")) {
//                links = links.substring(0, links.indexOf("&"));
//                //https://www.youtube.com/watch?v=cTWA-a_t_M8
//                //https://www.youtube.com/watch?v=cTWA-a_t_M8&list=PLimyCJLq6B4sIh4pMpXTJ5BHcVfeVgvmF&index=2&t=0s
//            }
//
//            if (!links.contains("//www") && links.contains("youtube")) {
//                links = links.substring(0, links.indexOf("https://") + 8) + "www." +
//                        links.substring(links.indexOf("youtube"));
//                //https://m.youtube.com/watch?v=fMM-hzKin56l
//            }
//
//            if (links.contains("/v/")) {
//                links = links.replace("/v/", "/watch?v=");
//                //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
//            }
//
//            if (links.contains("?start")) {
//                links = links.replace("/embed/", "/watch?v=");
//                links = links.substring(0, links.indexOf("?start"));
//                //https://www.youtube.com/embed/PldCWWb52zw?start=3
//            }
//            if (links.contains("?end")) {
//                links = links.replace("/embed/", "/watch?v=");
//                links = links.substring(0, links.indexOf("?end"));
//                //https://www.youtube.com/embed/PldCWWb52zw?end=3
//            }
//
//            if (links.contains("?t")) {
//                links = links.replace("/embed/", "/watch?v=");
//                links = links.substring(0, links.indexOf("?t"));
//                //https://youtu.be/cTWA-a_t_M8?t=1
//            }
//
//            if (links.contains("/embed/")) {
//                links = links.replace("/embed/", "/watch?v=");
//            }
//
//            if (links.contains("attribution_link?a=")) {
//                links = links.replace("attribution_link?a=", "/watch?v=");
//                //http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare
//            }
//
//            if (links.substring(0, 17).equals("https://youtu.be/")) {
//                alternate_link = "https://www.youtube.com/watch?v=" + links.substring(17);
//            }
//            //https://youtu.be/PldCWWb52zw
//
//            else if (links.substring(0, 32).equals("https://www.youtube.com/watch?v=")) {
//                alternate_link = "https://youtu.be/" + links.substring(32);
//            }
//            //https://www.youtube.com/watch?v=dFlPARW5IX8
//
//            int end_time = ends + (endm * 60) + (endh * 60 * 60);
//            int start_time = starts + (startm * 60) + (starth * 60 * 60);
//
//            if (end_time > 0 && start_time >= end_time) {
//
//                result_link.setText("End time must be greater than start time");
//                return;
//
//            }
//            //https:www.youtube.com/watch?v=E3xYPKcNqeY&list=
//            // PLAmiG35BNssvLlk36MnYUU_rSrzL7V7Sb&index=2
//
//
////                if (starth > 0 && startm > 0 && starts > 0)
////                {
////                    startm = startm + (starth * 60);
////                    starts = starts + (startm * 60);
////                    start_time = starts;
////                }
////                else if (startm > 0 && starts > 0)
////                {
////                    starts = starts + (startm * 60);
////                    start_time = starts;
////                }
////                else if (starts > 0)
////                {
////                    start_time = starts;
////                }
////
////                if (endh > 0 && endm > 0 && ends > 0)
////                {
////                    endm = endm + (endh * 60);
////                    ends = ends + (endm * 60);
////                    end_time = ends;
////                }
////                else if (endm > 0 && ends > 0)
////                {
////                    ends = ends + (endm * 60);
////                    end_time = ends;
////                }
////                else if (ends > 0)
////                {
////                    end_time = ends;
////                }
//
//            String set_time = "";
//
//            if (links.equals("")) {
//                alternate_link = "Invalid Link. Try Again.";
//            }
//
//            if (start_time == 0 && end_time == 0) {
//                result_link.setText(alternate_link);
//
//            } else {
//
//                if (start_time == 0 && end_time > 0) {
//                    set_time = "?end=" + end_time;
//                }
//
//                if (start_time > 0 && end_time == 0) {
//                    set_time = "?start=" + start_time;
//                }
//
//                if (start_time > 0 && end_time > 0) {
//                    set_time = "?start=" + start_time + "&end=" + end_time;
//                }
//
//
//                if (links.substring(0, 28).equals("https://www.youtube.com/watc")) {
//                    if (links.substring(0, 32).equals("https://www.youtube.com/watch?v=")) {
//                        links = links.substring(0, 24) + "embed/" + links.substring(32) + set_time;
//                    }
//                } else if (links.substring(0, 17).equals("https://youtu.be/")) {
//                    links = alternate_link;
//                    links = links.substring(0, 24) + "embed/" + links.substring(32) + set_time;
//                } else {
//                    links = "Invalid Link. Try Again";
//                }
//
//                result_link.setText(links + "");
//
//
//            }
//            break;
//
//        case R.id.copy_btn:
//            //result_link = (TextView) findViewById(R.id.result_link);
//            result_link = (TextView) findViewById(R.id.result_link);
//
//            String copyable_link = result_link.getText().toString();
//
//
//            ClipData clip = ClipData.newPlainText("TextView", copyable_link);
//            myClipboard.setPrimaryClip(clip);
//
//            Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();
//
//
//            break;
//        case R.id.paste_btn:
//            ClipData clipData = myClipboard.getPrimaryClip();
//            ClipData.Item item = clipData.getItemAt(0);
//
//            youtube_link = (EditText) findViewById(R.id.youtube_link);
//
//            youtube_link.setText(item.getText().toString());
//            Toast.makeText(MainActivity.this, "Pasted", Toast.LENGTH_SHORT).show();
//            break;
//        case R.id.open_btn:
//            result_link = (TextView) findViewById(R.id.result_link);
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(result_link.getText().toString()));
//            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            //intent.setPackage("com.android.chrome");
//            startActivity(intent);
//
//            //Intent browserIntent = new Intent(Intent.ACTION_VIEW);
//            //browserIntent.setData(Uri.parse(result_link.getText().toString()));
//            //startActivity(browserIntent);
//            //https://stackoverflow.com/questions/43025993/how-do-i-open-a-browser-on-clicking-a-text-link-in-textview
//            break;
//        case R.id.share_btn:
//            result_link = (TextView) findViewById(R.id.result_link);
//
//            String s = result_link.getText().toString();
//
//            Intent sharing_intent = new Intent(android.content.Intent.ACTION_SEND);
//            sharing_intent.setType("text/plain");
//            sharing_intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//            sharing_intent.putExtra(android.content.Intent.EXTRA_TEXT, s);
//            //sharing_intent.putExtra(Intent.EXTRA_TEXT)
//            startActivity(Intent.createChooser(sharing_intent, "Share via"));
//
//
//    }
//
//}
//}



