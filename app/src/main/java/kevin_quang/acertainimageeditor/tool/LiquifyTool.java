package kevin_quang.acertainimageeditor.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Pair;

import java.nio.ByteBuffer;

/**
 * Created by Kevin on 5/6/2018.
 */

public class LiquifyTool extends DrawHelper {
    private static final float ENLARGE_FACTOR = .05f;

    private Pair<Integer, Integer> vertDim;
    private GLHelper.VertexArray verts;
    private int indices[];
    private GLHelper.DrawData data;

    private Pair<Integer, Integer> meshDim
            = new Pair<>(50, 50);

    enum Mode
    {
        ENLARGE,
        SHRINK,
        SMUDGE
    };

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (data != null)
            data.destroy();
    }

    @Override
    public void load(Bitmap bitmap, boolean storeHistory) {
        super.load(bitmap, storeHistory);

        createMesh(
                meshDim,
                new Pair<Integer, Integer>(
                        bitmap.getHeight(), bitmap.getWidth())
        );
    }

    private void renderMesh()
    {
        GLES30.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glViewport(0, 0, super.image.getWidth(), super.image.getHeight());

        GLES30.glEnable(GLES30.GL_TEXTURE_2D);

        GLES30.glUseProgram(program);

        // construct matrix
        Matrix.setIdentityM(world, 0);

        Matrix.rotateM(
                world,
                0,
                90.f,
                0.f,
                0.f,
                1.f
        );
        Matrix.scaleM(
                world,
                0,
                1.f / (float)super.image.getHeight() * 2.f,
                -1.f / (float)super.image.getWidth() * 2.f,
                1.f);
        Matrix.translateM(
                world,
                0,
                -(float)super.image.getHeight() / 2.f,
                -(float)super.image.getWidth() / 2.f,
                0.f);

        GLES30.glBindBuffer (GLES30.GL_ARRAY_BUFFER, data.vertBufferID[0]);

        GLES30.glEnableVertexAttribArray(positionAttr);
        GLES30.glVertexAttribPointer(positionAttr, 3, GLES30.GL_FLOAT, false, 4 * 5, 0);

        GLES30.glEnableVertexAttribArray(texCoordAttr);
        GLES30.glVertexAttribPointer(texCoordAttr, 2, GLES30.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID);
        GLES30.glUniform1ui(textureUnif, textureID);

        GLES30.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, data.indexBufferID[0]);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indices.length, GLES30.GL_UNSIGNED_INT, 0);

        GLES30.glDisableVertexAttribArray(positionAttr);
        GLES30.glDisableVertexAttribArray(texCoordAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void processLine(
            GLHelper.Point<Float> start,
            GLHelper.Point<Float> end)
    {
        if (start.x != END_POINT.x
                && start.y != END_POINT.y
                || end.y != END_POINT.y
                && end.y != END_POINT.y)
            smudge(start, end);
    }

    @Override
    void finishedPointProcess()
    {
        // enlarge / shrink
        // TODO: check MODE
//        if (super.cursor.x != END_POINT.x
//                && super.cursor.y != END_POINT.y)
//            enlargeShrink(super.cursor, true);

        // solidify mesh
        if (data != null)
            data.destroy();

        data =
                GLHelper.createBuffers(
                        verts,
                        indices
                );

        this.load(renderToTex(), true);
    }

    private void createMesh(
            Pair<Integer, Integer> meshDim,
            Pair<Integer, Integer> actualDim
    ) {
        // get quad number
        Pair<Integer, Integer> quadDim =
                new Pair<>(
                        actualDim.first / meshDim.first,
                        actualDim.second / meshDim.second);

        // get overflow
        Pair<Integer, Integer> overflowDim =
                new Pair<>(
                        actualDim.first - quadDim.first * meshDim.first,
                        actualDim.second - quadDim.second * meshDim.second
                );

        // number of vertices
        vertDim =
                new Pair<>(
                        quadDim.first + 1,
                        quadDim.second + 1
                );

        // verts
        GLHelper.Vertex vertex = new GLHelper.Vertex();
        verts = new GLHelper.VertexArray(vertDim.first * vertDim.second);

        for (int y = 0; y < vertDim.second; y++) {
            for (int x = 0; x < vertDim.first; x++) {
                vertex.x =
                        (float)Math.min(
                                meshDim.first * x
                                        + Math.round(
                                        (float) (overflowDim.first * x)
                                                / (float) (quadDim.first)
                                ),
                                actualDim.first
                        );
                vertex.y =
                        (float)Math.min(
                                meshDim.second * y
                                        + Math.round(
                                        (float) (overflowDim.second * y)
                                                / (float) (quadDim.second)
                                ),
                                actualDim.second
                        );
                vertex.z = .1f;

                vertex.u =
                        (float)(vertex.y)
                                / (float)(actualDim.second);
                vertex.v =
                        (float)(vertex.x)
                                / (float)(actualDim.first);

                verts.add(vertex);
            }
        }

        // indices
        indices =
                new int[vertDim.first * vertDim.second * 6];

        for (int y = 0; y < quadDim.second; y++) {
            for (int x = 0; x < quadDim.first; x++) {
                int index = (x + y * vertDim.first) * 6;

                indices[index] = x + y * vertDim.first;
                indices[index + 1] = (x + 1) + y * vertDim.first;
                indices[index + 2] = x + (y + 1) * vertDim.first;

                indices[index + 3] = (x + 1) + y * vertDim.first;
                indices[index + 4] = x + (y + 1) * vertDim.first;
                indices[index + 5] = (x + 1) + (y + 1) * vertDim.first;
            }
        }
    }

    private Bitmap renderToTex()
    {
        int[] frameBuffer = new int[1];

        GLES30.glGenFramebuffers(1, frameBuffer, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer[0]);

        int[] renderTex = new int[1];
        GLES30.glGenTextures(1, renderTex, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, renderTex[0]);

        GLES30.glTexImage2D(
                GLES30.GL_TEXTURE_2D,
                0,
                GLES30.GL_RGBA,
                super.image.getWidth(),
                super.image.getHeight(),
                0,
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                null);

        GLES30.glFramebufferTexture2D(
                GLES30.GL_FRAMEBUFFER,
                GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D,
                renderTex[0],
                0
        );

        GLES30.glDrawBuffers(1, new int[]{ GLES30.GL_COLOR_ATTACHMENT0 }, 0);

        int status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);

        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE)
        {
            // TODO: some error
            Log.d("FB error", "Status: " + status);
        }

        // setup viewport
        GLES30.glClearColor(0, 0, 1, 1);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glViewport(0, 0, super.image.getWidth(), super.image.getHeight());

        renderMesh();

        GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, 1);
        GLES30.glPixelStorei(GLES30.GL_PACK_ROW_LENGTH, super.image.getWidth());
        ByteBuffer buffer =
                ByteBuffer.allocateDirect(
                        super.image.getWidth() * super.image.getHeight() * 4);
        GLES30.glReadPixels(
                0, 0,
                super.image.getWidth(),
                super.image.getHeight(),
                GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE,
                buffer
        );

        Bitmap bitmap =
                Bitmap.createBitmap(
                        super.image.getWidth(),
                        super.image.getHeight(),
                        Bitmap.Config.ARGB_8888
                );
        bitmap.copyPixelsFromBuffer(buffer);

        GLES30.glDeleteTextures(1, renderTex, 0);
        GLES30.glDeleteFramebuffers(1, frameBuffer, 0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        return bitmap;
    }

    private void smudge(
            GLHelper.Point<Float> pStart,
            GLHelper.Point<Float> pEnd
    )
    {
        if (pStart.x < 0.f || pStart.y < 0.f || (float)super.image.getWidth() <= pStart.x || (float)super.image.getHeight() <= pStart.y)
            return;

        if (pEnd.x < 0.f || pEnd.y < 0.f || (float)super.image.getWidth() <= pEnd.x || (float)super.image.getHeight() <= pEnd.y)
            return;
/*
        pStart.x /= super.image.getWidth();
        pStart.y /= super.image.getHeight();

        pEnd.x /= super.image.getWidth();
        pEnd.y /= super.image.getHeight();*/

        GLHelper.Point<Float> start =
                new GLHelper.Point<>(pStart.y, pStart.x);

        GLHelper.Point<Float> end =
                new GLHelper.Point<>(pEnd.y, pEnd.x);

        GLHelper.Point<Float> delta =
                start.sub(end);

        GLHelper.Point<Float> xBound =
                new GLHelper.Point<>(0.f, (float)super.image.getHeight());
        GLHelper.Point<Float> yBound =
                new GLHelper.Point<>(0.f, (float)super.image.getWidth());

        float cross =
                Math.min(
                        Math.max(start.x - xBound.x, xBound.y - start.x),
                        Math.max(start.y - yBound.x, yBound.y - start.y)
                );

        for (int i = 0; i < verts.numEls(); i++)
        {
            GLHelper.Vertex v = verts.get(i);

//            v.x /= super.image.getWidth();
//            v.y /= super.image.getHeight();

            float dist =
                    (float)new GLHelper.Point<Float>(v.x, v.y).distance(start);

            float smudgeDist = dist / cross - 1.f;

            if (smudgeDist < 0.f)
            {
                v.x += delta.x * smudgeDist;
                v.y += delta.y * smudgeDist;

//                v.x *= super.image.getWidth();
//                v.y *= super.image.getHeight();

                verts.put(i, v);
            }
        }
    }

    private void enlargeShrink(
            GLHelper.Point<Float> pt,
            boolean enlarge
    )
    {
        if (pt.x < 0.f || pt.y < 0.f || (float)super.image.getWidth() <= pt.x || (float)super.image.getHeight() <= pt.y)
            return;

//        pt.x /= super.image.getWidth();
//        pt.y /= super.image.getHeight();

        GLHelper.Point<Float> curPt =
                new GLHelper.Point<>(pt.y, pt.x);

        GLHelper.Point<Float> xBound =
                new GLHelper.Point<>(0.f, (float)super.image.getHeight());
        GLHelper.Point<Float> yBound =
                new GLHelper.Point<>(0.f, (float)super.image.getWidth());

        float cross =
                Math.min(
                    Math.min(curPt.x - xBound.x, xBound.y - curPt.x),
                    Math.min(curPt.y - yBound.x, yBound.y - curPt.y)
                );

        for (int i = 0; i < verts.numEls(); i++)
        {
            GLHelper.Vertex v = verts.get(i);

//            v.x /= super.image.getWidth();
//            v.y /= super.image.getHeight();

            float dist =
                    (float)new GLHelper.Point<Float>(v.x, v.y).distance(curPt);

            float rad = dist / cross;

            if (rad < 1.f && (.01f < rad || enlarge))
            {
                rad = (float)Math.pow(rad, ENLARGE_FACTOR);

                if (enlarge) {
                    v.x = curPt.x + (v.x - curPt.x) / rad;
                    v.y = curPt.y + (v.y - curPt.y) / rad;
                }
                else {
                    v.x = curPt.x + (v.x - curPt.x) * rad;
                    v.y = curPt.y + (v.y - curPt.y) * rad;
                }

//                v.x *= super.image.getWidth();
//                v.y *= super.image.getHeight();

                verts.put(i, v);
            }
        }
    }
}
