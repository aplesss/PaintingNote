package com.nhvproduction.painting;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

public class MainActivity extends AppCompatActivity {
 private PaintView paintView;
    private View popupLayout, popupEraserLayout;
    private int seekBarStrokeProgress;
    private ImageView strokeImageView, eraserImageView;
    private int size;
    private ColorPicker mColorPicker;
    TextRecognizer textRecognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        paintView= (PaintView) findViewById(R.id.paintView);
        DisplayMetrics displayMetrics= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        paintView.init(displayMetrics);
        LayoutInflater inflater =(LayoutInflater) this.getSystemService(AppCompatActivity.LAYOUT_INFLATER_SERVICE);
        popupLayout = inflater.inflate(R.layout.popup_sketch_stroke,null);
        strokeImageView =(ImageView) popupLayout.findViewById(R.id.stroke_circle);
        final Drawable circleDrawble =getResources().getDrawable(R.drawable.circle);
        size = circleDrawble.getIntrinsicWidth();
        mColorPicker = (ColorPicker) popupLayout.findViewById(R.id.stroke_color_picker);
        mColorPicker.addSVBar((SVBar) popupLayout.findViewById(R.id.svbar));
        mColorPicker.addOpacityBar((OpacityBar) popupLayout.findViewById(R.id.opacitybar));
        mColorPicker.setOnColorChangedListener(paintView::setCurrentColor);
        mColorPicker.setColor(paintView.getCurrentColor());
        mColorPicker.setOldCenterColor(paintView.getCurrentColor());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {

            case R.id.normal:
                paintView.normal();
                return true;
            case  R.id.emboss:
                paintView.emboss();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;
            case R.id.color:
                showpopup();
                return true;
            case R.id.recognize:
                textRecognizer= new TextRecognizer.Builder(this).build();
              ToastText();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ToastText() {

        if(!textRecognizer.isOperational())
        {
            Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(paintView.getmBitmap()!=null) {
                Bitmap bitmap = paintView.getmBitmap();
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock text = items.valueAt(i);
                    builder.append(text.getValue());
                }
                Toast.makeText(this, builder.toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "BITMAP NULL", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showpopup() {
        DisplayMetrics metrics = new DisplayMetrics();
       this.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Creating the PopupWindow
        PopupWindow popup = new PopupWindow(this);
        popup.setContentView(popupLayout);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setOnDismissListener(() -> {
            int oldColor= paintView.getCurrentColor();
            if (mColorPicker.getColor() != oldColor)
                mColorPicker.setOldCenterColor(oldColor);
        });

        // Displaying the popup at the specified location, + offsets (transformed
        // dp to pixel to support multiple screen sizes)
        popup.showAsDropDown(getWindow().getDecorView().getRootView());
        SeekBar mSeekBar;
        mSeekBar= popupLayout.findViewById(R.id.stroke_seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSeekbarProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSeekBar.setProgress(seekBarStrokeProgress);
    }

    private void setSeekbarProgress(int progress) {
        int calcProgress= progress>1 ? progress:1;
        int newSize= Math.round((size/100f)*calcProgress);
        int offset=(size-newSize)/2;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(newSize, newSize);
        lp.setMargins(offset, offset, offset, offset);
        strokeImageView.setLayoutParams(lp);
        seekBarStrokeProgress = progress;
        paintView.setSize(newSize);
    }
}
