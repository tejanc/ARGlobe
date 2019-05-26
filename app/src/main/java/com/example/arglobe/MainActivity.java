package com.example.arglobe;

import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private TextView txt;
    private Mode mode;
    private long mLastClickTime = 0;

    enum Mode {
        ADD_FRAGMENT,
        DELETE_FRAGMENT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = findViewById(R.id.mode_text);

        this.mode = Mode.ADD_FRAGMENT;

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        //Add toggle mode feature and button listener
        Button btn = findViewById(R.id.button_toggle);
        btn.setOnClickListener(view -> toggleMode(view));

        //Add ARPlane listener
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            // prevent multi-clicks, using threshold of 0.5 seconds
            if ((SystemClock.elapsedRealtime() - mLastClickTime) < 500) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            Anchor anchor = hitResult.createAnchor();

            switch (mode) {
                case ADD_FRAGMENT:
                    addItem(anchor);
                    break;
                case DELETE_FRAGMENT:
                    deleteModelFromScene(anchor);
                    break;
                default:
                    addItem(anchor);
            }

        });


    }

    public void toggleMode(View view) {
        this.mode = mode == Mode.ADD_FRAGMENT ? Mode.DELETE_FRAGMENT : Mode.ADD_FRAGMENT;
        txt.setText(mode.toString());
    }

    private void addItem(Anchor anchor) {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("NOVELO_EARTH.sfb"))
                .build()
                .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .show();
                    return null;
                });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    private void deleteModelFromScene(Anchor anchor) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        if (node.getParent() != null) {
            node.getScene().onRemoveChild(node.getParent());
            node.setRenderable(null);
            anchor.detach();
        }
        else {
            Log.println(1, "Fatal","Cannot delete from scene. Child node does not have a parent.");
        }
    }

}
