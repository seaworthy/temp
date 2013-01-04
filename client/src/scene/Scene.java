package scene;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;


public class Scene {
	public GL2 gl;
	public GLUT glut;
	public FloatBuffer whiteMaterial;
	public FloatBuffer blackMaterial;

	public ArrayList<Integer> selected = new ArrayList<Integer>();
	public SceneObjectManager manager = new SceneObjectManager();

	public Scene(GL2 gl, GLUT glut) {
		this.gl = gl;
		this.glut = glut;
	}

	public void make(FloatBuffer whiteMaterial, FloatBuffer blackMaterial) {
		this.whiteMaterial = whiteMaterial;
		this.blackMaterial = blackMaterial;
		
		drawSheet();
		for (int i = -2; i <= 2; i++) {
			gl.glPushMatrix();
			//gl.glLoadIdentity();
			gl.glTranslatef(2.f * i, 2.f * i, 0.f);
			drawCube();
			setText("Object");
			gl.glPopMatrix();
		}
	}

	private void drawCube() {
		float[] vertices = new float[] { 0.500000f, 0.500000f, 0.500000f,
				-0.500000f, 0.500000f, 0.500000f, 0.500000f, -0.500000f,
				0.500000f, -0.500000f, -0.500000f, 0.500000f, 0.500000f,
				0.500000f, -0.500000f, -0.500000f, 0.500000f, -0.500000f,
				0.500000f, -0.500000f, -0.500000f, -0.500000f, -0.500000f,
				-0.500000f };

		int[] indices = new int[] { 0, 1, 2, 3, 2, 1, 0, 2, 4, 6, 4, 2, 0, 4,
				1, 5, 1, 4, 7, 5, 6, 4, 6, 5, 7, 6, 3, 2, 3, 6, 7, 3, 5, 1, 5,
				3 };

		float[] normals = new float[] { 0.577350f, 0.577350f, 0.577350f,
				-0.333333f, 0.666667f, 0.666667f, 0.666667f, -0.333333f,
				0.666667f, -0.666667f, -0.666667f, 0.333333f, 0.666667f,
				0.666667f, -0.333333f, -0.666667f, 0.333333f, -0.666667f,
				0.333333f, -0.666667f, -0.666667f, -0.577350f, -0.577350f,
				-0.577350f };

		float[] colors = new float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f };

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

		FloatBuffer pointsFloatBuffer = Buffers.newDirectFloatBuffer(vertices);
		IntBuffer indicesIntBuffer = Buffers.newDirectIntBuffer(indices);
		FloatBuffer colorsFloatBuffer = Buffers.newDirectFloatBuffer(colors);
		FloatBuffer normalsFloatBuffer = Buffers.newDirectFloatBuffer(normals);

		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, whiteMaterial);

		gl.glVertexPointer(3, GL.GL_FLOAT, 0, pointsFloatBuffer);
		gl.glNormalPointer(GL.GL_FLOAT, normals.length, normalsFloatBuffer);
		gl.glColorPointer(3, GL.GL_FLOAT, 0, colorsFloatBuffer);
//		//gl.glPolygonMode (GL2.GL_FRONT, GL2.GL_FILL);
//		gl.glPolygonMode (GL2.GL_BACK, GL2.GL_LINE);
		gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT,
				indicesIntBuffer);

		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}
	private void drawSheet() {
		boolean flag = true;
		float[] n = { 0.0f, 1.0f, 0.0f };
		for (int i = -10; i < 11; i++) {
			for (int j = -10; j < 11; j++) {
				if (flag) {
					gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE,
							whiteMaterial);
				} else {
					gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE,
							blackMaterial);
				}
				flag = !flag;
				gl.glBegin(GL2.GL_QUADS);

				gl.glNormal3fv(n, 0);
				gl.glVertex3d(i, 0, j);
				gl.glNormal3fv(n, 0);
				gl.glVertex3d(i, 0, j - 1);
				gl.glNormal3fv(n, 0);
				gl.glVertex3d(i - 1, 0, j - 1);
				gl.glNormal3fv(n, 0);
				gl.glVertex3d(i - 1, 0, j);
				gl.glEnd();
			}
		}
	}
	private void setText(String string) {
		gl.glColor3f(1.f, 0, 0);
		gl.glRasterPos3f(1.f, 1.f, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, string);
	}
}