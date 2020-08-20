package com.example.youtubestartendtime;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ClipboardManager myClipboard;
    private ClipData myClip;
    private Button share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button get_link_btn = findViewById(R.id.get_link_btn);
        final TextView result_link = findViewById(R.id.result_link);
        result_link.setMovementMethod(LinkMovementMethod.getInstance());
        Button copy_link_btn = findViewById(R.id.copy_btn);
        Button paste_link_btn = findViewById(R.id.paste_btn);
        Button open_btn = findViewById(R.id.open_btn);
        share = findViewById(R.id.share_btn);
        myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);


        get_link_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText start_sec = (EditText) findViewById(R.id.start_sec);
                EditText start_min = (EditText) findViewById(R.id.start_min);
                EditText start_hour = (EditText) findViewById(R.id.start_hour);
                EditText end_sec = (EditText) findViewById(R.id.end_sec);
                EditText end_min = (EditText) findViewById(R.id.end_min);
                EditText end_hour = (EditText) findViewById(R.id.end_hour);
                EditText youtube_link = (EditText) findViewById(R.id.youtube_link);
                TextView result_link = (TextView) findViewById(R.id.result_link);

                int starts = 0;
                int startm = 0;
                int starth = 0;
                int ends = 0;
                int endm = 0;
                int endh = 0;
                String links = youtube_link.getText().toString();


                if (TextUtils.isEmpty(start_sec.getText().toString()))
                {
                    starts = 0;
                }
                else
                {
                    starts = Integer.parseInt(start_sec.getText().toString());
                }

                if (TextUtils.isEmpty(start_min.getText().toString()))
                {
                    startm = 0;
                }
                else
                {
                    startm = Integer.parseInt(start_min.getText().toString());
                }


                if (TextUtils.isEmpty(start_hour.getText().toString()))
                {
                    starth = 0;
                }
                else
                {
                    starth = Integer.parseInt(start_hour.getText().toString());
                }


                if (TextUtils.isEmpty(end_sec.getText().toString()))
                {
                    ends = 0;
                }
                else
                {
                    ends = Integer.parseInt(end_sec.getText().toString());
                }

                if (TextUtils.isEmpty(end_min.getText().toString()))
                {
                    endm = 0;
                }
                else
                {
                    endm = Integer.parseInt(end_min.getText().toString());
                }


                if (TextUtils.isEmpty(end_hour.getText().toString()))
                {
                    endh = 0;
                }
                else
                {
                    endh = Integer.parseInt(end_hour.getText().toString());
                }

//
//                if (TextUtils.isEmpty(youtube_link.getText().toString()))
//                {
//                    links = "";
//                }
//                else
//                {
//                    links = youtube_link.getText().toString();
//                }


                String alternate_link = "Invalid Link. Try Again.";

                if (links.substring(0, 8).equals("youtu.be"))
                {
                    links = "https://" + links;
                }

                if (links.substring(0, 11).equals("www.youtube"))
                {
                    links = "https://" + links;
                }

                if (links.substring(0, 7).equals("youtube"))
                {
                    links = "https://www." + links;
                }


                if (links.contains("?reload="))
                {
                    links = links.substring(0, links.indexOf("?reload="))+"?" + links.substring(links.indexOf("v="));
                    //https://www.youtube.com/watch?reload=9&v=2xMzgvPrXIo
                }

                if (links.contains("?version="))
                {
                    links = links.substring(0, links.indexOf("?version="));
                    //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
                }


                if (links.contains("&"))
                {
                    links = links.substring(0, links.indexOf("&"));
                    //https://www.youtube.com/watch?v=cTWA-a_t_M8
                    //https://www.youtube.com/watch?v=cTWA-a_t_M8&list=PLimyCJLq6B4sIh4pMpXTJ5BHcVfeVgvmF&index=2&t=0s
                }

                if (links.contains("/v/"))
                {
                    links = links.replace("/v/", "/watch?v=");
                    //http://www.youtube.com/v/-wtIMTCHWuI?version=3&autohide=1
                }

                if (links.contains("?start"))
                {
                    links = links.replace("/embed/", "/watch?v=");
                    links = links.substring(0, links.indexOf("?start"));
                    //https://www.youtube.com/embed/PldCWWb52zw?start=3
                }
                if (links.contains("?end"))
                {
                    links = links.replace("/embed/", "/watch?v=");
                    links = links.substring(0, links.indexOf("?end"));
                    //https://www.youtube.com/embed/PldCWWb52zw?end=3
                }

                if (links.contains("?t"))
                {
                    links = links.replace("/embed/", "/watch?v=");
                    links = links.substring(0, links.indexOf("?t"));
                    //https://youtu.be/cTWA-a_t_M8?t=1
                }

                if (links.contains("/embed/"))
                {
                    links = links.replace("/embed/", "/watch?v=");
                }

                if (links.contains("attribution_link?a="))
                {
                    links = links.replace("attribution_link?a=", "/watch?v=");
                    //http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare
                }

                if (links.substring(0, 17).equals("https://youtu.be/"))
                {
                    alternate_link = "https://www.youtube.com/watch?v=" + links.substring(17);
                }
                //https://youtu.be/PldCWWb52zw

                else if (links.substring(0, 32).equals("https://www.youtube.com/watch?v="))
                {
                    alternate_link = "https://youtu.be/" + links.substring(32);
                }
                //https://www.youtube.com/watch?v=dFlPARW5IX8

                int end_time = ends + (endm * 60) + (endh * 60 * 60);
                int start_time = starts + (startm * 60) + (starth * 60 * 60);

                if (end_time > 0 && start_time >= end_time)
                {

                    result_link.setText("End time must be greater than start time");
                    return;

                }
                //https:www.youtube.com/watch?v=E3xYPKcNqeY&list=
                // PLAmiG35BNssvLlk36MnYUU_rSrzL7V7Sb&index=2


//                if (starth > 0 && startm > 0 && starts > 0)
//                {
//                    startm = startm + (starth * 60);
//                    starts = starts + (startm * 60);
//                    start_time = starts;
//                }
//                else if (startm > 0 && starts > 0)
//                {
//                    starts = starts + (startm * 60);
//                    start_time = starts;
//                }
//                else if (starts > 0)
//                {
//                    start_time = starts;
//                }
//
//                if (endh > 0 && endm > 0 && ends > 0)
//                {
//                    endm = endm + (endh * 60);
//                    ends = ends + (endm * 60);
//                    end_time = ends;
//                }
//                else if (endm > 0 && ends > 0)
//                {
//                    ends = ends + (endm * 60);
//                    end_time = ends;
//                }
//                else if (ends > 0)
//                {
//                    end_time = ends;
//                }

                String set_time = "";

                if (links.equals(""))
                {
                    alternate_link = "Invalid Link. Try Again.";
                }

                if (start_time == 0 && end_time == 0)
                {
                    result_link.setText(alternate_link);

                }
                else
                {

                    if (start_time == 0 && end_time > 0)
                    {
                        set_time = "?end=" + end_time;
                    }

                    if (start_time >  0 && end_time == 0)
                    {
                        set_time = "?start=" + start_time;
                    }

                    if (start_time > 0 && end_time > 0)
                    {
                        set_time = "?start=" + start_time + "&end=" + end_time;
                    }




                    if (links.substring(0,28).equals("https://www.youtube.com/watc"))
                    {
                        if (links.substring(0, 32).equals("https://www.youtube.com/watch?v="))
                        {
                        links = links.substring(0,24) + "embed/" + links.substring(32) + set_time;
                        }
                    }
                    else if (links.substring(0, 17).equals("https://youtu.be/"))
                    {
                        links = alternate_link;
                        links = links.substring(0,24) + "embed/" + links.substring(32) + set_time;
                    }
                    else
                    {
                        links = "Invalid Link. Try Again";
                    }

                    result_link.setText(links + "");


                }
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
                        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(result_link.getText().toString()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setPackage("com.android.chrome");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            // Chrome browser presumably not installed so allow user to choose instead
                            intent.setPackage(null);
                            intent.setPackage("org.mozilla.firefox");
                            try {
                                startActivity(intent);
                            } catch(ActivityNotFoundException ex2) {
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
                                        } catch(ActivityNotFoundException ex5){
                                            intent.setPackage(null);
                                            intent.setPackage("org.torproject.torbrowser");
                                            try {
                                                startActivity(intent);
                                            } catch(ActivityNotFoundException ex6){
                                                intent.setPackage(null);
                                                intent.setPackage("com.vivaldi.browser");
                                                try{
                                                    startActivity(intent);
                                                } catch(ActivityNotFoundException ex7){
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

    }
}