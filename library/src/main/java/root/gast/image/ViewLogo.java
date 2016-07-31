/*
 * Copyright 2011 Jon A. Webb
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *              http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ViewLogo extends ImageView
{
    private RectF mRect = null;

    public ViewLogo(Context context)
    {
        super(context);
    }

    public ViewLogo(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ViewLogo(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (mRect != null)
        {
            if (this.mRect != null)
            {
                Paint p = new Paint();
                p.setColor(Color.GREEN);
                p.setStyle(Style.STROKE);
                canvas.drawRect(this.mRect, p);
            }
        }
    }

    public void setRect(RectF rect, int width, int height)
    {
        mRect = new RectF(rect.left * getWidth() / width, rect.top
                * getHeight() / height, rect.right * getWidth() / width,
                rect.bottom * getHeight() / height);
    }
}
