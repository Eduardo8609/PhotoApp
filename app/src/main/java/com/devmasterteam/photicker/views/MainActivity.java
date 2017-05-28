package com.devmasterteam.photicker.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.devmasterteam.photicker.R;
import com.devmasterteam.photicker.utils.ImageUtil;
import com.devmasterteam.photicker.utils.PermissionUtil;
import com.devmasterteam.photicker.utils.SocialUtil;
import com.facebook.FacebookSdk;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, View.OnLongClickListener {

    static final int REQUEST_TAKE_PHOTO = 2;
    private final ViewHolder mViewHolder = new ViewHolder();
    private Handler mRepeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private LongEventType mLongEventType;
    private ImageView mImageSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Faz a criação da lista de imagens
        List<Integer> mListmages = ImageUtil.getImagesList();

        final RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.relative_photo_content_draw);
        final LinearLayout content = (LinearLayout) this.findViewById(R.id.linear_horizontal_scroll_content);

        // Itera o número de imagens
        for (int i = 0; i < mListmages.size(); i++) {

            // Obtém elemento de imagem
            ImageView image = new ImageView(this);

            // Substitui a imagem com a imagem sendo iterada
            image.setImageBitmap(ImageUtil.decodeSampledBitmapFromResource(getResources(), mListmages.get(i), 70, 70));
            image.setPadding(20,10,20,10);
            // image.setLayoutParams(new LinearLayout.MarginLayoutParams(150, 150));

            final int position = mListmages.get(i);
            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true;

            BitmapFactory.decodeResource(getResources(), mListmages.get(i), dimensions);

            final int width = dimensions.outWidth;
            final int height = dimensions.outHeight;

            image.setOnClickListener(onClickImageOption(relativeLayout, position, width, height));

            // Adiciona nova imagem
            content.addView(image);
        }

        // Adiciona os elementos
        this.mViewHolder.mImageTakePhoto = (ImageView) this.findViewById(R.id.image_take_photo);
        this.mViewHolder.mImagePhoto = (ImageView) this.findViewById(R.id.image_photo);
        this.mViewHolder.mImageInstagram = (ImageView) this.findViewById(R.id.image_instagram);
        this.mViewHolder.mImageTwitter = (ImageView) this.findViewById(R.id.image_twitter);
        this.mViewHolder.mImageFacebook = (ImageView) this.findViewById(R.id.image_facebook);
        this.mViewHolder.mImageWahtsApp = (ImageView) this.findViewById(R.id.image_whatsapp);

        this.mViewHolder.mButtonZoomIn = (ImageView) this.findViewById(R.id.image_zoom_in);
        this.mViewHolder.mButtonZoomOut = (ImageView) this.findViewById(R.id.image_zoom_out);
        this.mViewHolder.mButtonRotateLeft = (ImageView) this.findViewById(R.id.image_rotate_left);
        this.mViewHolder.mButtonRotateRight = (ImageView) this.findViewById(R.id.image_rotate_right);
        this.mViewHolder.mImageFinish = (ImageView) this.findViewById(R.id.image_finish);
        this.mViewHolder.mImageRemove = (ImageView) this.findViewById(R.id.image_remove);

        this.mViewHolder.mLinearSharePanel = (LinearLayout) this.findViewById(R.id.linear_share_panel);
        this.mViewHolder.mLinearControlPanel = (LinearLayout) this.findViewById(R.id.linear_control_panel);
        this.mViewHolder.mRelativePhotoContent = (RelativeLayout) this.findViewById(R.id.relative_photo_content_draw);

        // Adicona evento aos elementos
        this.setListeners();

        // Incializa o SDK do facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    /**
     * Habilita o painel para compartilhamento de imagem ou manipulação de imagem
     */
    private void toogleControlPanel(boolean value) {
        if (value) {
            this.mViewHolder.mLinearControlPanel.setVisibility(View.VISIBLE);
            this.mViewHolder.mLinearSharePanel.setVisibility(View.GONE);
        } else {
            this.mViewHolder.mLinearControlPanel.setVisibility(View.GONE);
            this.mViewHolder.mLinearSharePanel.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener onClickImageOption(final RelativeLayout relativeLayout, final int position, final int width, final int height) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Atualizar a imagem e colocar imagem para sobrepor
                final ImageView image = new ImageView(MainActivity.this);
                image.setBackgroundResource(position);
                relativeLayout.addView(image);

                // Centraliza imagem
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) image.getLayoutParams();
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

                // Adiciona os parametros a imagem
                image.setLayoutParams(layoutParams);
                image.setAdjustViewBounds(true);

                // DEixa selecionado último item
                mImageSelected = image;

                // Painel de controle de imagem ativo
                toogleControlPanel(true);

                ViewGroup.LayoutParams params = image.getLayoutParams();
                params.width = (int) (width * 0.5);
                params.height = (int) (height * 0.5);
                image.setLayoutParams(params);

                // Evento de Drag
                image.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        float x, y;
                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mImageSelected = image;
                                toogleControlPanel(true);
                                break;
                            case MotionEvent.ACTION_MOVE:

                                int coords[] = {0, 0};
                                relativeLayout.getLocationOnScreen(coords);

                                x = (motionEvent.getRawX() - (image.getWidth() / 2));
                                y = motionEvent.getRawY() - (coords[1] + (image.getHeight() / 2));
                                image.setX(x);
                                image.setY(y);

                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }
                        return true;
                    }
                });
            }
        };
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_take_photo:
                if (!PermissionUtil.hasCameraPermission(this)) {
                    PermissionUtil.asksCameraPermission(this);
                } else {
                    dispatchTakePictureIntent();
                }
                break;

            case R.id.image_zoom_in:
                ImageUtil.handleZoomIn(this.mImageSelected);
                break;

            case R.id.image_zoom_out:
                ImageUtil.handleZoomOut(this.mImageSelected);
                break;

            case R.id.image_rotate_left:
                ImageUtil.handleRotateLeft(this.mImageSelected);
                break;

            case R.id.image_rotate_right:
                ImageUtil.handleRotateRight(this.mImageSelected);
                break;

            case R.id.image_finish:
                toogleControlPanel(false);
                break;

            case R.id.image_remove:
                this.mViewHolder.mRelativePhotoContent.removeView(this.mImageSelected);
                toogleControlPanel(false);
                break;

            case R.id.image_whatsapp:
                SocialUtil.shareImageOnWhats(this, this.mViewHolder.mRelativePhotoContent, v);
                break;

            case R.id.image_facebook:
                Toast.makeText(this, R.string.openning_share, Toast.LENGTH_LONG).show();
                SocialUtil.shareImageOnFace(this, this.mViewHolder.mRelativePhotoContent, v);
                break;

            case R.id.image_twitter:
                SocialUtil.shareImageOnTwitter(this, this.mViewHolder.mRelativePhotoContent, v);
                break;

            case R.id.image_instagram:
                SocialUtil.shareImageOnInsta(this, this.mViewHolder.mRelativePhotoContent, v);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.without_permission_camera_explanation))
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            this.setPhotoAsBackground();
        }
    }

    @Override
    public boolean onLongClick(View view) {

        // Verifica qual a ação do click
        if (view.getId() == R.id.image_zoom_in) {
            mAutoIncrement = true;
            this.mLongEventType = LongEventType.ZoomIn;
            new RptUpdater().run();
        } else if (view.getId() == R.id.image_zoom_out) {
            mAutoIncrement = true;
            this.mLongEventType = LongEventType.ZoomOut;
            new RptUpdater().run();
        } else if (view.getId() == R.id.image_rotate_left) {
            mAutoIncrement = true;
            this.mLongEventType = LongEventType.RotateLeft;
            new RptUpdater().run();
        } else if (view.getId() == R.id.image_rotate_right) {
            mAutoIncrement = true;
            this.mLongEventType = LongEventType.RotateRight;
            new RptUpdater().run();
        }

        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int id = view.getId();
        if (id == R.id.image_zoom_in || id == R.id.image_zoom_out || id == R.id.image_rotate_left || id == R.id.image_rotate_right) {

            // Verifica se usuário está liberando o botão
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && mAutoIncrement) {
                mAutoIncrement = false;
                this.mLongEventType = null;
            }
        }
        return false;
    }

    /**
     * Adicona evento aos elementos
     */
    private void setListeners() {
        this.findViewById(R.id.image_take_photo).setOnClickListener(this);
        this.findViewById(R.id.image_instagram).setOnClickListener(this);
        this.findViewById(R.id.image_twitter).setOnClickListener(this);
        this.findViewById(R.id.image_facebook).setOnClickListener(this);
        this.findViewById(R.id.image_whatsapp).setOnClickListener(this);

        this.findViewById(R.id.image_zoom_in).setOnClickListener(this);
        this.findViewById(R.id.image_zoom_out).setOnClickListener(this);
        this.findViewById(R.id.image_rotate_left).setOnClickListener(this);
        this.findViewById(R.id.image_rotate_right).setOnClickListener(this);
        this.findViewById(R.id.image_finish).setOnClickListener(this);
        this.findViewById(R.id.image_remove).setOnClickListener(this);

        this.findViewById(R.id.image_zoom_in).setOnTouchListener(this);
        this.findViewById(R.id.image_zoom_out).setOnTouchListener(this);
        this.findViewById(R.id.image_rotate_left).setOnTouchListener(this);
        this.findViewById(R.id.image_rotate_right).setOnTouchListener(this);

        this.findViewById(R.id.image_zoom_in).setOnLongClickListener(this);
        this.findViewById(R.id.image_zoom_out).setOnLongClickListener(this);
        this.findViewById(R.id.image_rotate_left).setOnLongClickListener(this);
        this.findViewById(R.id.image_rotate_right).setOnLongClickListener(this);
    }

    /**
     * Evento disparado para tirar foto
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Certifica que a Activity da camera existe e consegue responder
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Cria o arquivo onde a foto será salva
            File photoFile = null;
            try {
                photoFile = ImageUtil.createImageFile(this);
                // Save a file: path for use with ACTION_VIEW intents
                this.mViewHolder.mUriPhotoPath = Uri.fromFile(photoFile);
            } catch (IOException ex) {
            }

            // Continua somente se teve sucesso na criação do arquivo
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Usa a imagem recém criada como plano de fundo
     */
    private void setPhotoAsBackground() {

        // Obtém as dimensões da View onde a imagem será colocada
        int targetW = this.mViewHolder.mImagePhoto.getWidth();
        int targetH = this.mViewHolder.mImagePhoto.getHeight();

        // Obtém as dimensões do bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(this.mViewHolder.mUriPhotoPath.getPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determina o quanto dimensionar a imagem
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decodifica a imagem em um arquivo de imagem para preencher a View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(this.mViewHolder.mUriPhotoPath.getPath(), bmOptions);
        Bitmap bitmapRotated = ImageUtil.rotateImageIfRequired(bitmap, this.mViewHolder.mUriPhotoPath);

        this.mViewHolder.mImagePhoto.setImageBitmap(bitmapRotated);
    }

    /**
     * Enumeration para tipos de eventos
     */
    private enum LongEventType {
        ZoomIn, ZoomOut, RotateLeft, RotateRight
    }

    /**
     * ViewHolder
     */
    private static class ViewHolder {
        ImageView mImageTakePhoto;
        ImageView mImagePhoto;
        ImageView mImageInstagram;
        ImageView mImageTwitter;
        ImageView mImageFacebook;
        ImageView mImageWahtsApp;
        Uri mUriPhotoPath;

        ImageView mButtonZoomIn;
        ImageView mButtonZoomOut;
        ImageView mButtonRotateLeft;
        ImageView mButtonRotateRight;
        ImageView mImageFinish;
        ImageView mImageRemove;

        LinearLayout mLinearSharePanel;
        LinearLayout mLinearControlPanel;
        RelativeLayout mRelativePhotoContent;
    }

    /**
     * Thread responsável por acionar um botão diversas vezes
     */
    private class RptUpdater implements Runnable {
        public void run() {

            // Se o usuário ainda estiver pressionando o botão
            if (mAutoIncrement)
                mRepeatUpdateHandler.postDelayed(new RptUpdater(), 50);

            // Verifica o tipo de evento e toma a ação
            if (mLongEventType != null) {
                switch (mLongEventType) {
                    case ZoomIn:
                        ImageUtil.handleZoomIn(mImageSelected);
                        break;
                    case ZoomOut:
                        ImageUtil.handleZoomOut(mImageSelected);
                        break;
                    case RotateLeft:
                        ImageUtil.handleRotateLeft(mImageSelected);
                        break;
                    case RotateRight:
                        ImageUtil.handleRotateRight(mImageSelected);
                        break;
                }
            }
        }
    }

}