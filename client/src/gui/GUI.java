package gui;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.gl2.GLUT;

public class GUI {
	public GL2 gl;
	public GLUT glut;
	public float cameraDistance;
	public float cameraAngleInXZ;
	public float cameraAngleInYZ;

	public GUI(GL2 gl, GLUT glut) {
		this.gl = gl;
		this.glut = glut;
	}

	public void showDefault(int objectCount, Integer lastObject,
			ArrayList<Integer> selected) {
		gl.glColor3f(0, 1f, 0);
		setText("# of scene objects: " + Integer.toString(objectCount), 20,
				570);
		setText("Selected objects: " + selected, 20, 550);
		if (lastObject != null) {
			setText("Last selected object: " + Integer.toString(lastObject),
					20, 530);
		}
		
		gl.glColor3f(0, 1.f, 1.f);
		setText("Distance: " + Float.toString(cameraDistance), 20, 60);
		setText("Rotation Angle (Azimuth): " + Float.toString(cameraAngleInXZ),
				20, 40);
		setText("Pan Angle (Altitude): " + Float.toString(cameraAngleInYZ),
				20, 20);
	}

	public void showConsole(String lines[]) {
		gl.glColor3f(0, 1f, 0);
		int start, end;
		start = 0;
		end = lines.length;
		// Input on the last line of console
		if (end > 30)
			start = end - 30;
		int j = 0;
		for (int i = start; i < end; i++) {
			setText(lines[i], -5.f, 3.f - 0.2f * (j));
			j += 1;
		}
	}

	public void showHelp() {
		ArrayList<String> shortcuts = new ArrayList<String>();
		shortcuts.add("ESC - key shorcuts");
		shortcuts.add("~ - console");
		shortcuts.add("X - zoom-in");
		shortcuts.add("Z - zoom-out");
		shortcuts.add("LEFT - rotate scene to the left");
		shortcuts.add("RIGHT - rotate scene to the right");
		shortcuts.add("UP - pan camera up");
		shortcuts.add("DOWN - pan camera down");
		shortcuts.add("0 - reset camera view");
		shortcuts.add("R - reset scene");
		shortcuts.add("M - toggle object motion");
		shortcuts.add("L - randomize scene");
		shortcuts.add("- - toggle view volume");
		shortcuts.add("DEL - delete selected object");

		gl.glColor3f(1.f, 0, 0);

		setText("KEY SHORTCUTS", -5.f, 3.f);
		gl.glColor3f(0, 1.f, 0);

		int i = 0;
		for (String shortcut : shortcuts) {
			setText(shortcut, -5.f, 2.8f - 0.2f * i);
			i += 1;
		}
	}

	public void showFPS(String fps) {
		setText(fps, 700, 570);
	}

	private void setText(String string, float x, float y) {
		//gl.glLoadIdentity();
		gl.glRasterPos3f(x, y, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, string);
	}

	public void enable2D () {
		int[] vPort = new int[4];

		gl.glGetIntegerv(GL2.GL_VIEWPORT, vPort, 0);

		gl.glMatrixMode(GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glOrtho(0, vPort[2], 0, vPort[3], -1, 1);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
	}
	
	public void disable2D() {
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glPopMatrix();
	}
}
