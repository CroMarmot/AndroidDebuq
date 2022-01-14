package com.example.mltool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

interface Callable<I0, I1, O> {
    public O call(I0 p0, I1 p1);
}

public class MltoolActivityMainActivity extends AppCompatActivity {
    public final String TAG = MltoolActivityMainActivity.class.getName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_SELECT_IMAGE = 2;
    Bitmap oriBitMap = null;
    Bitmap drawBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mltool_activity_main);
    }

    private void drawGraph(ImageView imageView, int alpha, int color, Callable<Canvas, Paint, Void> func) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(30);

        Bitmap workingBitmap = Bitmap.createBitmap(drawBitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        func.call(canvas, paint);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);
        drawBitmap = mutableBitmap;
    }

    private void drawRect(ImageView imageView, Rect r, int color) {
        drawGraph(imageView, 70, color, (canvas, paint) -> {
            canvas.drawRect(r, paint);
            return null;
        });
    }

    private void drawText(ImageView imageView, String text, float x, float y, int alpha, int color) {
        drawGraph(imageView, alpha, color, (canvas, paint) -> {
            canvas.drawText(text, x, y, paint);
            return null;
        });
    }

    private void drawPoint(ImageView imageView, float x, float y, int raduis, int alpha, int color) {
        drawGraph(imageView, alpha, color, (canvas, paint) -> {
            canvas.drawCircle(x, y, raduis, paint);
            return null;
        });
    }

    private Pair<Integer, Integer> calcBound(ArrayList<Integer> values) {
        int di = 6; // may support config ?
        boolean doubleMax = true; // 双峰
        int lpos = 0;
        int rpos = 0;
        while (doubleMax || lpos == rpos) {
            doubleMax = false;
            Integer maxi = 0;
            // TODO another way min variance
            // simple Convolution
            for (Integer value : values) {
                maxi = Math.max(maxi, value);
            }
            ArrayList<Integer> counts = new ArrayList<Integer>(Collections.nCopies(maxi + 1, 0));
            for (Integer value : values) {
                for (int i = -di; i <= di; i++) {
                    int v = i + value;
                    if (v < 0 || v >= counts.size()) continue;
                    counts.set(v, counts.get(v) + 1);
                }
            }
            lpos = rpos = 0;
            String s = "";
            for (int i = 0; i < counts.size(); i++) {
                s = s + "[" + i + "," + counts.get(i) + "]";
                if (counts.get(i) > counts.get(lpos)) {
                    doubleMax = false;
                    lpos = i;
                    rpos = i;
                } else if (counts.get(i).equals(counts.get(lpos))) {
                    if (rpos + 1 == i) { // when equal
                        rpos = i;
                    } else {
                        doubleMax = true;
                    }
                }
            }
            Log.d(TAG, "calcBoundRes[" + di + "]: " + s);
            di = di + (int) (di * 0.2 + 1);
        }
        return new Pair<>((lpos + rpos) / 2 - di, (lpos + rpos) / 2 + di);
    }

    public void btnClick3(View view) throws IOException {
        parse(3);
    }

    public void btnClick4(View view) throws IOException {
        parse(4);
    }

    private void parse(int N) {
        ImageView iv = findViewById(R.id.imageView);
        // Specify the recognition model for a language
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        if (oriBitMap == null)
            return;
        InputImage image = InputImage.fromBitmap(oriBitMap, Surface.ROTATION_0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                                Log.d(TAG, "onSuccess: " + visionText.toString());
                                ArrayList<Point> whs = new ArrayList<>(); // width height

                                String resultText = visionText.getText();
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    // String blockText = block.getText();
                                    // Log.d(TAG, "BLOCK Text:" + blockText);
                                    // Point[] blockCornerPoints = block.getCornerPoints();
                                    // Rect blockFrame = block.getBoundingBox();
                                    for (Text.Line line : block.getLines()) {
                                        // String lineText = line.getText();
                                        // Log.d(TAG, "line Text:" + lineText);
                                        // Point[] lineCornerPoints = line.getCornerPoints();
                                        // for (Point lineCornerPoint : lineCornerPoints) {
                                        //     Log.d(TAG, "Line point: " + lineCornerPoint.toString());
                                        // }
                                        // Rect lineFrame = line.getBoundingBox();
                                        for (Text.Element element : line.getElements()) {
                                            String elementText = element.getText();
//                                             if (elementText.length() != 1) continue;
                                            String s = "" + elementText + ":";
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            for (Point elementCornerPoint : elementCornerPoints) {
                                                s = s + "[" + elementCornerPoint.x + "," + elementCornerPoint.y + "]";
                                                drawPoint(iv, elementCornerPoint.x, elementCornerPoint.y, 5, 80, Color.BLACK);
                                            }
                                            Log.d(TAG, "element :" + s);
                                            whs.add(new Point(elementCornerPoints[2].x - elementCornerPoints[0].x, elementCornerPoints[2].y - elementCornerPoints[0].y));
                                            Rect elementFrame = element.getBoundingBox();
                                            if (elementText.length() == 1) {
                                                drawRect(iv, element.getBoundingBox(), Color.GREEN);
                                            } else {
                                                drawRect(iv, element.getBoundingBox(), Color.DKGRAY);
                                            }
                                            drawText(iv, elementText, elementCornerPoints[0].x, elementCornerPoints[0].y, 100, Color.BLACK);
                                        }
                                    }
                                }
                                Collections.sort(whs, new Comparator<Point>() {
                                    @Override
                                    public int compare(Point p0, Point p1) {
                                        return (p0.x != p1.x) ? p0.x - p1.x : p0.y - p1.y;
                                    }
                                });
                                String s = "";
                                for (Point point : whs) {
                                    s = s + "[" + point.x + "," + point.y + "]";
                                }
                                Log.d(TAG, "WH P: " + s);
                                Pair<Integer, Integer> xrange = calcBound(whs.stream()
                                        .map(wh -> wh.x)
                                        .collect(Collectors.toCollection(ArrayList::new)));

                                Pair<Integer, Integer> yrange = calcBound(whs.stream()
                                        .map(wh -> wh.y)
                                        .collect(Collectors.toCollection(ArrayList::new)));
                                Log.d(TAG, "Boundx: " + xrange.toString());
                                Log.d(TAG, "Boundy: " + yrange.toString());

                                // TODO guess range
                                int minx = 31;
                                int maxx = 40;
                                int miny = 54;
                                int maxy = 61;

                                minx = xrange.first;
                                maxx = xrange.second;
                                miny = yrange.first;
                                maxy = yrange.second;

                                int minbx = 0x3f3f;
                                int maxbx = 0;
                                int minby = 0x3f3f;
                                int maxby = 0;

                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    for (Text.Line line : block.getLines()) {
                                        for (Text.Element element : line.getElements()) {
                                            String elementText = element.getText();
                                            if (elementText.length() != 1) continue;
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            int dx = elementCornerPoints[2].x - elementCornerPoints[0].x;
                                            int dy = elementCornerPoints[2].y - elementCornerPoints[0].y;
                                            assert (dx > 0 && dy > 0);
                                            if (dx < minx || dx > maxx || dy < miny || dy > maxy)
                                                continue; // ignore
                                            // left - top corner
                                            minbx = Math.min(minbx, elementCornerPoints[0].x);
                                            maxbx = Math.max(maxbx, elementCornerPoints[0].x);
                                            minby = Math.min(minby, elementCornerPoints[0].y);
                                            maxby = Math.max(maxby, elementCornerPoints[0].y);
                                        }
                                    }
                                }
                                Log.d(TAG, "B x:[" + minbx + "," + maxbx + "] => " + (maxbx - minbx));
                                Log.d(TAG, "B y:[" + minby + "," + maxby + "] => " + (maxby - minby));
                                drawRect(iv, new Rect(minbx, minby, maxbx, maxby), Color.BLUE);

//  x:[59,988] => 929
//  y:[520,1453] => 933
//  0: 7 0 2 0 8 6 0 0 0
//  1: 0 0 0 7 0 2 6 3 0
//  2: 9 6 0 0 0 0 7 0 0
//  3: 0 0 0 0 0 8 4 0 0
//  4: 4 0 0 0 3 7 0 9 0
//  5: 0 7 6 1 4 0 5 0 0
//  6: 3 2 7 9 5 4 0 0 0
//  7: 0 0 4 0 0 0 0 0 0
//  8: 0 0 9 0 0 0 0 4 5

                                final int N2 = N * N;
                                int puzz[][] = new int[N2][N2];
                                for (int i = 0; i < N2; i++) {
                                    for (int j = 0; j < N2; j++) {
                                        puzz[i][j] = 0;
                                    }
                                }

                                for (int i = 0; i < N2; i++) {
                                    for (int j = 0; j < N2; j++) {
                                        // center pos
                                        int px = minbx + i * (maxbx - minbx) / (N2 - 1) + (minx + maxx) / 4;
                                        int py = minby + j * (maxby - minby) / (N2 - 1) + (miny + maxy) / 4;
                                        drawPoint(iv, px, py, 5, 100, Color.RED);
                                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                                            for (Text.Line line : block.getLines()) {
                                                for (Text.Element element : line.getElements()) {
                                                    String elementText = element.getText();
                                                    if (elementText.length() != 1) continue;
                                                    Point[] elementCornerPoints = element.getCornerPoints();
                                                    if (px < elementCornerPoints[0].x || px > elementCornerPoints[2].x || py < elementCornerPoints[0].y || py > elementCornerPoints[2].y)
                                                        continue; // ignore
                                                    try {
                                                        puzz[j][i] = Integer.parseInt(elementText);
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "parseInt error: " + e.toString());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                // print
                                String res = "";
                                for (int i = 0; i < N2; i++) {
                                    String rows = "";
                                    for (int j = 0; j < N2; j++) {
                                        rows += puzz[i][j] + " ";
                                    }
                                    Log.d(TAG, "R " + i + ": " + rows);
                                    res += rows + "\r\n";
                                }
                                AlertDialog alertDialog = new AlertDialog.Builder(MltoolActivityMainActivity.this).create();
                                alertDialog.setTitle("Alert");
                                alertDialog.setMessage(res);
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.e(TAG, "onFailure: " + e.toString());
                                    }
                                });

    }


    public void onLaunchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iv = findViewById(R.id.imageView);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                oriBitMap = drawBitmap = (Bitmap) extras.get("data");
                iv.setImageBitmap(drawBitmap);
            } else if (requestCode == REQUEST_SELECT_IMAGE) {
                Uri uri = data.getData();
                iv.setImageURI(uri);
                ContentResolver cr = this.getContentResolver();
                try (InputStream input = cr.openInputStream(uri)) {
                    oriBitMap = drawBitmap = BitmapFactory.decodeStream(input);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void onGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }
}