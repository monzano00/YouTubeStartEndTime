//package com.example.youtubestartendtime;
//
//import static junit.framework.TestCase.assertEquals;
//import static junit.framework.TestCase.assertNotNull;
//
//import android.widget.TextView;
//
//import androidx.test.filters.SmallTest;
//import androidx.test.runner.AndroidJUnit4;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import org.robolectric
//
//public class MyActivityTest {
//    @RunWith(AndroidJUnit4.class)
//    @SmallTest
//    public class MainActivityTest {
//        private MainActivity mActivity;
//
//        @Before
//        public void setUp() throws Exception {
//            mActivity = Robolectric.buildActivity(MainActivity.class).create().resume().get();
//        }
//
//        @Test
//        public void testTextView() throws Exception {
//            TextView textView = mActivity.findViewById(R.id.my_text_view);
//            assertNotNull(textView);
//            assertEquals("Hello, World!", textView.getText().toString());
//        }
//
//        @After
//        public void tearDown() throws Exception {
//            mActivity.finish();
//        }
//    }
//
//}
