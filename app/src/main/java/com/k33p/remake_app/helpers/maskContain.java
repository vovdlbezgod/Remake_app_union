package com.k33p.remake_app.helpers;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.photoeditor.PhotoEditorView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class maskContain {
    private Bitmap maskFromTensorflow;
    private Bitmap gotObjectFromMask;
    private boolean deleted;
    private ArrayList boxes;
    private Bitmap maskSum;

    public Bitmap getMaskSum() {
        return maskSum;
    }

    public void setMaskSum(Bitmap maskSum) {
        this.maskSum = maskSum;
    }

    public maskContain(Bitmap maskFromTensorflow, Bitmap gotObjectFromMask, Bitmap masksSum) {
        this.maskFromTensorflow = maskFromTensorflow;
        this.gotObjectFromMask = gotObjectFromMask;
        this.deleted = false;
        this.maskSum = masksSum;
    }

    public Bitmap getMaskFromTensorflow() {
        return maskFromTensorflow;
    }

    public void setMaskFromTensorflow(Bitmap maskFromTensorflow) {
        this.maskFromTensorflow = maskFromTensorflow;
    }

    public Bitmap getGotObjectFromMask() {
        return gotObjectFromMask;
    }

    public void setGotObjectFromMask(Bitmap gotObjectFromMask) {
        this.gotObjectFromMask = gotObjectFromMask;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean containingPoint(float x, float y, @Nullable Bitmap tempSrc) {
        Bitmap tempDst = Bitmap.createScaledBitmap(this.maskFromTensorflow, tempSrc.getWidth(), tempSrc.getHeight(), false);
        int tempX = Math.round(x);
        int tempY = Math.round(y);
        if(tempDst.getPixel(tempX,tempY) == Color.WHITE){
            return true;
        }
        return false;
    }
}
