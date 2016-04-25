package hcmut.cse.bookslover.utils;

/**
 * Created by huy on 4/24/2016.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

public class BlurTransform implements Transformation {

    RenderScript rs;
    private int radius = 10; //default radius
    public BlurTransform(Context context, int radius) {
        super();
        rs = RenderScript.create(context);
        this.radius = radius;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        // result bitmap
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmap);

        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SHARED);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // blur radius
        script.setRadius(radius);
        script.forEach(output);
        output.copyTo(blurredBitmap);

        bitmap.recycle();
        return blurredBitmap;
    }

    @Override
    public String key() {
        return "blur";
    }

}