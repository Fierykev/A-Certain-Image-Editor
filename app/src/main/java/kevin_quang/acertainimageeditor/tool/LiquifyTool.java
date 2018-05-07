package kevin_quang.acertainimageeditor.tool;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES31;
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

    public static final int LIQUIFY = 1;
    private boolean update = false;

    private Pair<Integer, Integer> meshDim
            = new Pair<>(50, 50);

    private Mode mode = null;

    public enum Mode
    {
        ENLARGE,
        SHRINK,
        SMUDGE
    }

    public static class LiquifyArgs {
        Mode mode;

        public LiquifyArgs(Mode mode) {
            this.mode = mode;
        }
    }

    @Override
    public void init(Context context) {
        super.init(context);
    }

    @Override
    public void destroy() {
        super.destroy();

        this.forceTexLoad(renderToTex());

        if (data != null)
            data.destroy();
    }

    public void save(String path)
    {
        save(path, renderToTex());
    }

    @Override
    public void load(Bitmap bitmap, boolean storeHistory) {
        super.load(bitmap, storeHistory);

        createMesh(
                meshDim,
                new Pair<Integer, Integer>(
                        bitmap.getHeight(), bitmap.getWidth())
        );

        data =
                GLHelper.createBuffers(
                        verts,
                        indices
                );
    }

    @Override
    public void onDraw(float aspectRatio, int width, int height)
    {
        processPointList();

        renderMesh(aspectRatio, width, height, false);
    }

    public void setArgs(Args args)
    {
        switch (args.type)
        {
            case LIQUIFY:
                mode = ((LiquifyArgs)args.arg).mode;
                break;
        }
    }

    private void renderMesh(float aspectRatio, int width, int height, boolean renderTex)
    {
        GLES31.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        GLES31.glViewport(0, 0, width, height);

        GLES31.glEnable(GLES31.GL_TEXTURE_2D);

        GLES31.glUseProgram(program);

        // construct matrix
        Matrix.setIdentityM(world, 0);

        if (aspectRatio != 1.0) {
            float currentAspectRatio =
                    (float) image.getWidth() / (float) image.getHeight();
            float hS = Math.min(aspectRatio / currentAspectRatio, 1.f);
            float wS = Math.min(currentAspectRatio / aspectRatio, 1.f);

            Matrix.scaleM(
                    world,
                    0,
                    wS,
                    hS,
                    1.f);
        }

        if (renderTex)
            Matrix.rotateM(
                    world,
                    0,
                    90.f,
                    0.f,
                    0.f,
                    1.f
            );
        else {
            Matrix.rotateM(
                    world,
                    0,
                    -90.f,
                    0.f,
                    0.f,
                    1.f
            );
            Matrix.scaleM(
                    world,
                    0,
                    1.f,
                    -1.f,
                    1.f);
        }

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

        GLES31.glBindBuffer (GLES31.GL_ARRAY_BUFFER, data.vertBufferID[0]);

        GLES31.glEnableVertexAttribArray(positionAttr);
        GLES31.glVertexAttribPointer(positionAttr, 3, GLES31.GL_FLOAT, false, 4 * 5, 0);

        GLES31.glEnableVertexAttribArray(texCoordAttr);
        GLES31.glVertexAttribPointer(texCoordAttr, 2, GLES31.GL_FLOAT, false, 4 * 5, 4 * 3);

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, textureID);
        GLES31.glUniform1ui(textureUnif, textureID);

        GLES31.glUniformMatrix4fv(worldUnif, 1, false, world, 0);

        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, data.indexBufferID[0]);
        GLES31.glDrawElements(GLES31.GL_TRIANGLES, indices.length, GLES31.GL_UNSIGNED_INT, 0);

        GLES31.glDisableVertexAttribArray(positionAttr);
        GLES31.glDisableVertexAttribArray(texCoordAttr);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, 0);
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void processLine(
            GLHelper.Point<Float> start,
            GLHelper.Point<Float> end)
    {
        if (start.x != END_POINT.x
                && start.y != END_POINT.y
                || end.y != END_POINT.y
                && end.y != END_POINT.y) {
            if(mode == Mode.SMUDGE) {
                smudge(start, end);
                update = true;
            }
        }
    }

    @Override
    void finishedPointProcess()
    {
        // enlarge / shrink
        // TODO: check MODE
        if (super.cursor.x != END_POINT.x
                && super.cursor.y != END_POINT.y) {
            if(mode == Mode.ENLARGE || mode == Mode.SHRINK) {
                enlargeShrink(super.cursor, mode == Mode.ENLARGE);
                update = true;
            }
        }

        // solidify mesh
        if (update) {
            if (data != null)
                data.destroy();

            data =
                    GLHelper.createBuffers(
                            verts,
                            indices
                    );

//            this.forceTexLoad(renderToTex());
        }
        update = false;
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

        GLES31.glGenFramebuffers(1, frameBuffer, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBuffer[0]);

        int[] renderTex = new int[1];
        GLES31.glGenTextures(1, renderTex, 0);

        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D_MULTISAMPLE, renderTex[0]);

        GLES31.glTexStorage2DMultisample(
                GLES31.GL_TEXTURE_2D_MULTISAMPLE,
                4,
                GLES31.GL_RGBA8,
                super.image.getWidth(),
                super.image.getHeight(),
                false
        );

        GLES31.glFramebufferTexture2D(
                GLES31.GL_FRAMEBUFFER,
                GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D_MULTISAMPLE,
                renderTex[0],
                0
        );

        GLES31.glDrawBuffers(1, new int[]{ GLES31.GL_COLOR_ATTACHMENT0 }, 0);

        int status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER);

        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE)
        {
            // TODO: some error
            Log.d("FB error", "Status: " + status);
        }

        // setup viewport
        GLES31.glClearColor(0, 0, 1, 1);
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        GLES31.glViewport(0, 0, super.image.getWidth(), super.image.getHeight());

        renderMesh(1.f, super.image.getWidth(), super.image.getHeight(), true);

        // swap to a regular buffer to read from
        int[] frameBufferReg = new int[1];

        GLES31.glGenFramebuffers(1, frameBufferReg, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBufferReg[0]);

        int[] renderTexReg = new int[1];
        GLES31.glGenTextures(1, renderTexReg, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, renderTexReg[0]);

        GLES31.glTexImage2D(
                GLES31.GL_TEXTURE_2D,
                0,
                GLES31.GL_RGBA,
                super.image.getWidth(),
                super.image.getHeight(),
                0,
                GLES31.GL_RGBA,
                GLES31.GL_UNSIGNED_BYTE,
                null);

        GLES31.glFramebufferTexture2D(
                GLES31.GL_FRAMEBUFFER,
                GLES31.GL_COLOR_ATTACHMENT0,
                GLES31.GL_TEXTURE_2D,
                renderTexReg[0],
                0
        );

        GLES31.glDrawBuffers(1, new int[]{ GLES31.GL_COLOR_ATTACHMENT0 }, 0);

        status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER);

        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE)
        {
            // TODO: some error
            Log.d("FB error", "Status: " + status);
        }

        GLES31.glBindFramebuffer(
                GLES31.GL_READ_FRAMEBUFFER,
                frameBuffer[0]
        );

        GLES31.glBlitFramebuffer(
                0, 0,
                super.image.getWidth(),
                super.image.getHeight(),
                0, 0,
                super.image.getWidth(),
                super.image.getHeight(),
                GLES31.GL_COLOR_BUFFER_BIT,
                GLES31.GL_NEAREST);

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBufferReg[0]);

        GLES31.glPixelStorei(GLES31.GL_PACK_ALIGNMENT, 1);
        GLES31.glPixelStorei(GLES31.GL_PACK_ROW_LENGTH, super.image.getWidth());
        ByteBuffer buffer =
                ByteBuffer.allocateDirect(
                        super.image.getWidth() * super.image.getHeight() * 4);

        GLES31.glReadPixels(
                0, 0,
                super.image.getWidth(),
                super.image.getHeight(),
                GLES31.GL_RGBA,
                GLES31.GL_UNSIGNED_BYTE,
                buffer
        );

        Bitmap bitmap =
                Bitmap.createBitmap(
                        super.image.getWidth(),
                        super.image.getHeight(),
                        Bitmap.Config.ARGB_8888
                );
        bitmap.copyPixelsFromBuffer(buffer);

        GLES31.glDeleteTextures(1, renderTex, 0);
        GLES31.glDeleteFramebuffers(1, frameBuffer, 0);

        GLES31.glDeleteTextures(1, renderTexReg, 0);
        GLES31.glDeleteFramebuffers(1, frameBufferReg, 0);

        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D_MULTISAMPLE, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);

        GLES31.glBindFramebuffer(
                GLES31.GL_READ_FRAMEBUFFER,
                0
        );

        GLES31.glBindFramebuffer(
                GLES31.GL_DRAW_FRAMEBUFFER,
                0
        );

        bitmap = GLHelper.standardizeBitamp(bitmap);

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

                v.x = Math.min(Math.max(0.f, v.x), super.image.getHeight());
                v.y = Math.min(Math.max(0.f, v.y), super.image.getWidth());

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

                v.x = Math.min(Math.max(0.f, v.x), super.image.getHeight());
                v.y = Math.min(Math.max(0.f, v.y), super.image.getWidth());

//                v.x *= super.image.getWidth();
//                v.y *= super.image.getHeight();

                verts.put(i, v);
            }
        }
    }
}
