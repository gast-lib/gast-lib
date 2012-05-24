package root.gast.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class LogoView extends View {
    private int DRAW_COLOR = Color.GREEN;
    
    private ImageCameraView mImageCameraView;
    private RectF mRect;

    public LogoView(Context context) {
        super(context);
    }
    
    public LogoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LogoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    public void clearRect() {
        mRect = null;
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (mImageCameraView != null && mRect != null) {
            Matrix m = mImageCameraView.getImageMatrix();
            if (m != null) {
                RectF rectDraw = new RectF();
                m.mapRect(rectDraw, mRect);
                Paint p = new Paint();
                p.setColor(DRAW_COLOR);
                p.setStyle(Style.STROKE);
                c.drawRect(rectDraw, p);
            }
        }
    }
    
    public void setImageCameraView(ImageCameraView icv) {
        mImageCameraView = icv;
    }
    
    public void setRect(RectF r) {
        mRect = r;
    }
}
