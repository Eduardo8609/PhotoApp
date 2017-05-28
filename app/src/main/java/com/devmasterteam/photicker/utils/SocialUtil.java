package com.devmasterteam.photicker.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devmasterteam.photicker.R;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class SocialUtil {

    private static final String HASHTAG = "#photickerapp";

    /**
     * Handle Whatsapp
     */
    public static void shareImageOnWhats(Activity activity, RelativeLayout photoContent, View v) {

        PackageManager pkManager = activity.getPackageManager();
        try {
            // Da erro caso não encontre
            pkManager.getPackageInfo("com.whatsapp", 0);

            // Cria arquivo único
            String fileName = "temp_file" + System.currentTimeMillis() + ".jpg";
            try {
                // Bitmap image = ImageUtil.drawBitmap(photoContent);

                photoContent.setDrawingCacheEnabled(true);
                photoContent.buildDrawingCache(true);
                File imageFile = new File(Environment.getExternalStorageDirectory(), fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                photoContent.getDrawingCache(true).compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.close();
                photoContent.setDrawingCacheEnabled(false);
                photoContent.destroyDrawingCache();

                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/" + fileName));
                    sendIntent.setType("image/jpeg");
                    sendIntent.setPackage("com.whatsapp");
                    v.getContext().startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.share_image)));
                } catch (Exception e) {
                    Toast.makeText(activity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, R.string.unexpected_error, Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, R.string.whatsapp_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle Facebook
     */
    public static void shareImageOnFace(Activity activity, RelativeLayout photoContent, View v) {
        // Cria conteudo para ser publicado no face
        SharePhoto photo = new SharePhoto.Builder().setBitmap(ImageUtil.drawBitmap(photoContent)).build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setShareHashtag(new ShareHashtag.Builder().setHashtag(HASHTAG).build())
                .build();
        new ShareDialog(activity).show(content);
    }

    /**
     * Handle Twitter
     */
    public static void shareImageOnTwitter(Activity activity, RelativeLayout photoContent, View v) {
        PackageManager pkManager = activity.getPackageManager();
        try {
            // Da erro caso não encontre o aplicativo
            pkManager.getPackageInfo("com.twitter.android", 0);

            try {
                Intent tweetIntent = new Intent(Intent.ACTION_SEND);

                Bitmap image = ImageUtil.drawBitmap(photoContent);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp_file.jpg");

                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());

                tweetIntent.putExtra(Intent.EXTRA_TEXT, HASHTAG);
                tweetIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temp_file.jpg"));
                tweetIntent.setType("image/jpeg");
                PackageManager pm = activity.getPackageManager();
                List<ResolveInfo> lract = pm.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean resolved = false;
                for (ResolveInfo ri : lract) {
                    if (ri.activityInfo.name.contains("twitter")) {
                        tweetIntent.setClassName(ri.activityInfo.packageName,
                                ri.activityInfo.name);
                        resolved = true;
                        break;
                    }
                }

                v.getContext().startActivity(resolved ? tweetIntent : Intent.createChooser(tweetIntent, "Choose one"));
            } catch (final ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.twitter_not_installed, Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, R.string.twitter_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle Whatsapp
     */
    public static void shareImageOnInsta(Activity activity, RelativeLayout photoContent, View v) {

        PackageManager pkManager = activity.getPackageManager();
        try {
            // Da erro caso não encontre o aplicativo
            pkManager.getPackageInfo("com.instagram.android", 0);

            try {
                Bitmap image = ImageUtil.drawBitmap(photoContent);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temp_file.jpg");
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temp_file.jpg"));
                    sendIntent.setType("image/*");
                    sendIntent.setPackage("com.instagram.android");
                    v.getContext().startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.share_image)));
                } catch (IOException e) {
                }

            } catch (Exception e) {
            }

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, R.string.instagram_not_installed, Toast.LENGTH_SHORT).show();
        }

    }
}
