package com.example.app1;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Home on 19-07-2017.
 */
public class BookmarkFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bookmarks_fragment, container, false);
        getActivity().setTitle("Bookmarks");

        FragmentManager fragmentManager = getFragmentManager();
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(fragmentManager, MainActivity.context);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);


        if (ImageFragment.data.size() == 0) {
            try {
                for (File fileEntry : ImageFragment.imageStorageDir.listFiles()) {
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    System.out.println(fileEntry);
                    System.out.println(fileEntry.getPath());
                    if (ImageFragment.checkEntry.contains(fileEntry)) {
                        System.out.println("continued");
                        continue;
                    }

                    String extension = (fileEntry.getName()).split("\\.")[1];
                    ImageFragment.checkEntry.add(fileEntry);
                    if (extension.equals("jpg")) {
                        BookmarkedDatabaseRow bookmarkedDatabaseRow = MainActivity.bookmarkedDatabaseHandler.getRowFromUrl(fileEntry.getPath());

                        if (bookmarkedDatabaseRow == null) {
                            bookmarkedDatabaseRow = new BookmarkedDatabaseRow();
                            bookmarkedDatabaseRow.setUrl(fileEntry.getPath());
                            bookmarkedDatabaseRow.setBkmrk(false);
                            MainActivity.bookmarkedDatabaseHandler.addRow(bookmarkedDatabaseRow);
                            bookmarkedDatabaseRow = MainActivity.bookmarkedDatabaseHandler.getRowFromUrl(fileEntry.getPath());
                        }

                        ImageFragment.data.add(bookmarkedDatabaseRow);

                        if (bookmarkedDatabaseRow.getBkmrk()) {
                            ImageGalleryAdapter.bkmrkImages.add(bookmarkedDatabaseRow);
                        }
                    }
                }
            } catch (NullPointerException n) {
                System.out.println("Picture directory empty");
                Toast.makeText(getContext(), "No Files to Show!", Toast.LENGTH_SHORT).show();
            }
        }

        if (RecordingFragment.data.size() == 0){
            try {
                for (File fileEntry : RecordingFragment.specificVdoDir.listFiles()) {
                    System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    String extension = (fileEntry.getName()).split("\\.")[1];
                    if(extension.equals("mp4")) {
                        BookmarkedDatabaseRow bookmarkedDatabaseRow = MainActivity.bookmarkedDatabaseHandler.getRowFromUrl(fileEntry.getPath());

                        if (bookmarkedDatabaseRow == null) {
                            bookmarkedDatabaseRow = new BookmarkedDatabaseRow();
                            bookmarkedDatabaseRow.setUrl(fileEntry.getPath());
                            bookmarkedDatabaseRow.setBkmrk(false);
                            MainActivity.bookmarkedDatabaseHandler.addRow(bookmarkedDatabaseRow);
                            bookmarkedDatabaseRow = MainActivity.bookmarkedDatabaseHandler.getRowFromUrl(fileEntry.getPath());
                        }

                        RecordingFragment.data.add(bookmarkedDatabaseRow);

                        if (bookmarkedDatabaseRow.getBkmrk() && !ImageGalleryAdapter.bkmrkVideos.contains(bookmarkedDatabaseRow))
                            ImageGalleryAdapter.bkmrkVideos.add(bookmarkedDatabaseRow);
                    }
                }
            }catch (NullPointerException e){
                System.out.println("Video directory empty");
                Toast.makeText(getContext(), "No Files to Show!", Toast.LENGTH_SHORT).show();
            }
        }

        return v;
    }

    class PagerAdapter extends FragmentStatePagerAdapter {

        String tabTitles[] = new String[]{"Images", "Videos"};
        Context context= MainActivity.context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new Bookmarkedimages();
                case 1:
                    return new Bookmarkedvideos();
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

    }
}

