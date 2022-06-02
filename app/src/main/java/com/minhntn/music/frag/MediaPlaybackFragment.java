package com.minhntn.music.frag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.minhntn.music.R;

public class MediaPlaybackFragment extends Fragment {
    public static final String KEY_COVER = "KEY_COVER";
    private ImageView mIVBackground;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_playback, container, false);
        mIVBackground = rootView.findViewById(R.id.iv_album_cover_large);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        byte[] cover = getArguments().getByteArray(KEY_COVER);
        Bitmap bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
        mIVBackground.setImageBitmap(bitmap);
    }
}
