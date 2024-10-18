package com.gongluck.opengles_java;

import android.content.Context;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private final Context context;
    private static final int POSITION_COMPONENT_COUNT = 2;//每个坐标位置包含2个值
    private static final int COLOR_COMPONENT_COUNT = 3;//每个坐标颜色包含3个值
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = //每个顶点的数据占用
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private FloatBuffer vertexData;//本地堆

    private int program;
    private static final String _POSITION = "_Position";
    private int positionLocation;
    private static final String _COLOR = "_Color";
    private int colorLocation;

    public AirHockeyRenderer(Context context) {
        this.context = context;

        float[] tableVerticesWithTriangles = {//opengl坐标横向范围[-1,1]从左到右,纵向范围[-1,1]从下到上
                //三角形的顶点顺序以逆时针排列，叫做 “卷曲顺序”，原因统一使用卷曲顺序有利于 OpenGL 的优化，可以减少绘制那些无法被看到的图形

                // X, Y, R, G, B

                //三角形扇顶点
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,

                //线
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 0f, 1f, 0f,

                //椎
                0f, -0.25f, 0f, 0f, 1f,
                0f, 0.25f, 1f, 0f, 0f,
        };
        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        String vertexShaderSource = Utils.readTextFileFromRawResource(context, R.raw.vertex_shader);
        String fragmentShaderSource = Utils.readTextFileFromRawResource(context, R.raw.fragment_shader);

        //编译opengl着色器代码
        int vertexShader = Utils.compileVertexShader(vertexShaderSource);
        int fragmentShader = Utils.compileFragmentShader(fragmentShaderSource);

        //链接opengl程序
        program = Utils.linkProgram(vertexShader, fragmentShader);

        //验证opengl程序
        Utils.validateProgram(program);

        //使用opengl程序
        glUseProgram(program);

        //获取opengl程序中的属性索引
        positionLocation = glGetAttribLocation(program, _POSITION);

        //获取opengl程序中的统一变量索引
        //colorLocation = glGetUniformLocation(program, _COLOR);

        //获取opengl程序中的属性索引
        colorLocation = glGetAttribLocation(program, _COLOR);

        vertexData.position(0);
        //传递_Position的数据
        glVertexAttribPointer(positionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        //使能_Position属性
        glEnableVertexAttribArray(positionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        //传递v_Color属性
        glVertexAttribPointer(colorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        //使能v_Color属性
        glEnableVertexAttribArray(colorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置视口范围
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //将缓存清除为预先的设置值
        glClear(GL_COLOR_BUFFER_BIT);

        //更新u_Color
        //glUniform4f(colorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        //绘制
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);//从0-5共6个点4个三角形形成三角形扇

        //画线
        //glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);//从6-7共2个点1条线

        //画点
        //glUniform4f(colorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 8, 1);
        //glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
