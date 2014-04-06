package com.fii.arproject;

import java.io.FileInputStream;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.MetaioSDK;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.tools.io.AssetsManager;

public class ARActivity extends ARViewActivity {

    private IGeometry modelTiger;
    private MetaioSDKCallbackHandler metaioCallbackHandler;
    private boolean closeToTiger;
    private MediaPlayer mediaPlayer;
    private boolean rendered = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	metaioSDK = null;
	mSurfaceView = null;
	mRendererInitialized = false;

	try {
	    modelTiger = null;
	    metaioCallbackHandler = new MetaioSDKCallbackHandler();
	    closeToTiger = false;
	    mediaPlayer = new MediaPlayer();
	    FileInputStream fis = new FileInputStream(AssetsManager.getAssetPath("meow.mp3"));
	    mediaPlayer.setDataSource(fis.getFD());
	    mediaPlayer.prepare();
	    fis.close();

	    mSensors = new SensorsComponentAndroid(getApplicationContext());
	    final String signature = "RstYxD5Qhw1RSmSFVn9xPCW4y6iwod/twolXzvEy/kw=";
	    metaioSDK = MetaioSDK.CreateMetaioSDKAndroid(this, signature);
	    metaioSDK.registerSensorsComponent(mSensors);

	} catch (Exception e) {
	    mediaPlayer = null;
	    MetaioDebug.log(Log.ERROR, "Error creating metaio SDK");
	    finish();
	}
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	metaioCallbackHandler.delete();
	metaioCallbackHandler = null;
	try {
	    mediaPlayer.release();
	} catch (Exception e) {
	}
    }

    /**
     * This method is regularly called in the rendering loop. It calculates the
     * distance between device and the target and performs actions based on the
     * proximity
     */
    private void checkDistanceToTarget() {

	TrackingValues tv = metaioSDK.getTrackingValues(1);

	if (tv.isTrackingState()) {

	    final float distance = tv.getTranslation().norm();
	    final float threshold = 200;
	    if (distance < threshold) {
		if (!closeToTiger) {
		    MetaioDebug.log("Moved close to the tiger");
		    closeToTiger = true;
		    playSound();
		    modelTiger.startAnimation("tap");
		}
	    } else {
		if (closeToTiger) {
		    MetaioDebug.log("Moved away from the tiger");
		    closeToTiger = false;
		}
	    }
	}
    }

    private void playSound() {
	try {
	    MetaioDebug.log("Playing sound");
	    mediaPlayer.start();
	} catch (Exception e) {
	    MetaioDebug.log("Error playing sound: " + e.getMessage());
	}
    }

    @Override
    protected int getGUILayout() {
	return R.layout.ar_view;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
	return metaioCallbackHandler;
    }

    @Override
    public void onDrawFrame() {
	super.onDrawFrame();

	checkDistanceToTarget();
	// while (true) {
	// metaioSDK.requestCameraImage();
	// }
    }

    public void onButtonClick(View v) {
	finish();
    }

    public void onInstantGetFriendButtonClick(View v) {
	modelTiger.setVisible(false);
	metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY", "", rendered);
	rendered = !rendered;
	Button button = (Button) findViewById(R.id.friendButton);
	if (rendered) {
	    button.setText(R.string.friend_place);
	} else {
	    button.setText(R.string.friend_create);
	}
    }

    @Override
    protected void loadContents() {
	try {
	    String tigerModelPath = AssetsManager.getAssetPath("tiger.md2");
	    modelTiger = metaioSDK.createGeometry(tigerModelPath);

	    modelTiger.setScale(8f);
	    modelTiger.setRotation(new Rotation(0f, 0f, (float) Math.PI));
	    modelTiger.setVisible(false);
	    modelTiger.setAnimationSpeed(60f);
	    modelTiger.startAnimation("meow");
	    MetaioDebug.log("Loaded geometry " + tigerModelPath);
	} catch (Exception e) {
	    MetaioDebug.log(Log.ERROR, "Error loading geometry: " + e.getMessage());
	}
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {
	playSound();
	geometry.startAnimation("tap");
    }

//    public void onCreateNewContact(View v) {
//
//    }
//
//    public void onEditContact(View v) {
//
//    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
	@Override
	public void onSDKReady() {
	    runOnUiThread(new Runnable() {
		@Override
		public void run() {
		    mGUIView.setVisibility(View.VISIBLE);
		}
	    });
	}

	@Override
	public void onInstantTrackingEvent(boolean success, String file) {
	    if (success) {
		MetaioDebug.log("MetaioSDKCallbackHandler.onInstantTrackingEvent: " + file);
		metaioSDK.setTrackingConfiguration(file);
		modelTiger.setVisible(true);
	    } else {
		MetaioDebug.log(Log.ERROR, "Failed to create instant tracking configuration!");
	    }
	}

	@Override
	public void onAnimationEnd(IGeometry geometry, String animationName) {
	    // Play a random animation from the list
	    final String[] animations = { "meow", "scratch", "look", "shake", "clean" };
	    final int random = (int) (Math.random() * animations.length);
	    geometry.startAnimation(animations[random]);
	}

	// @Override
	// public void onNewCameraFrame(ImageStruct cameraFrame) {
	// super.onNewCameraFrame(cameraFrame);
	//
	// BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
	// bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
	//
	// Bitmap bitmap =
	// BitmapFactory.decodeByteArray(cameraFrame.getBuffer(), 0, (int)
	// cameraFrame.getBufferSize(), bitmap_options);
	//
	// FaceDetector face_detector = new FaceDetector(bitmap.getWidth(),
	// bitmap.getHeight(), com.fii.arproject.util.Constants.MAX_FACES);
	// FaceDetector.Face[] faces = new
	// FaceDetector.Face[com.fii.arproject.util.Constants.MAX_FACES];
	// PointF tmp_point = new PointF();
	// faces[0].getMidPoint(tmp_point);
	//
	// LinearLayout faceMenuLayout = (LinearLayout)
	// findViewById(R.id.face_menu_layout);
	// RelativeLayout currentLayout = (RelativeLayout)
	// findViewById(R.id.ar_view);
	//
	// RelativeLayout.LayoutParams params = new
	// RelativeLayout.LayoutParams((int) tmp_point.x, (int) (tmp_point.y +
	// 10));
	// currentLayout.addView(getLayoutInflater().inflate(R.id.face_menu_layout,
	// faceMenuLayout), params);
	//
	// }
    }

}
