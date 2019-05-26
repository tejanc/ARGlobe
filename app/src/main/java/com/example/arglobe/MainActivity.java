package com.example.arglobe;

import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    Mode mode;

    enum Mode {
        ADD_MODEL, DELETE_MODEL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();

            Mode mode = Mode.ADD_MODEL;

            switch (mode) {
                case ADD_MODEL:
                    ModelRenderable.builder()
                            .setSource(this, Uri.parse("ArcticFox_Posed.sfb"))
                            .build()
                            .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage(throwable.getMessage())
                                        .show();
                                toggleMode();
                                return null;
                            });
                    break;
                case DELETE_MODEL:
                    ModelRenderable.builder()
                            .setSource(this, Uri.parse("ArcticFox_Posed.sfb"))
                            .build()
                            .thenAccept(modelRenderable -> deleteModelToScene(anchor, modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage(throwable.getMessage())
                                        .show();
                                toggleMode();
                                return null;
                            });
                    break;
            }

        });


    }

    private void toggleMode() {
        if (mode == Mode.ADD_MODEL)
            mode = Mode.DELETE_MODEL;
        else
            mode = Mode.ADD_MODEL;
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    private void deleteModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().removeChild(anchorNode);
        transformableNode.select();
    }
}
